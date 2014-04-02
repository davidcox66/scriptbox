package org.scriptbox.gwt;

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.JavaExec

import java.nio.charset.Charset;

import groovy.text.SimpleTemplateEngine

import com.thoughtworks.qdox.JavaDocBuilder
import com.thoughtworks.qdox.model.Annotation
import com.thoughtworks.qdox.model.JavaClass
import com.thoughtworks.qdox.model.JavaMethod
import com.thoughtworks.qdox.model.JavaParameter
import com.thoughtworks.qdox.model.Type

class GwtPlugin implements Plugin<Project> {

    void apply( Project project ) {
	    def gwtVersion = "2.6.0"

	    def gwtServletDepend = "com.google.gwt:gwt-servlet:${gwtVersion}" // runtime
	    def gwtUserDepend = "com.google.gwt:gwt-user:${gwtVersion}" // provided
	    def gwtDevDepend = "com.google.gwt:gwt-dev:${gwtVersion}" // test
	    def gwtLogDepend = "com.google.code.gwt-log:gwt-log:2.6.2"

        project.extensions.create("gwt", GwtPluginExtension)
       
        boolean war = false; 
        try {
            project.configurations['providedCompile'] 
            war = true;
        }
        catch( Exception ex ) {
        }

        def config = war ? 'providedCompile' : 'compile'
        project.dependencies.add( config, gwtUserDepend )
        project.dependencies.add( config, gwtDevDepend )
        project.dependencies.add( 'runtime', gwtServletDepend )

        project.task('gwtCompile',type: GwtCompileTask ) {
            buildDir = "${project.buildDir}/gwt"
            extraDir = "${project.buildDir}/extra"
            
            inputs.source project.sourceSets.main.java.srcDirs
            inputs.dir project.sourceSets.main.output.resourcesDir
            outputs.dir buildDir

            // Workaround for incremental build (GRADLE-1483)
            // outputs.upToDateSpec = new org.gradle.api.specs.AndSpec()

            main = 'com.google.gwt.dev.Compiler'
            classpath { [
                project.sourceSets.main.java.srcDirs, // Java source
                project.sourceSets.main.output.resourcesDir, // Generated resources
                project.sourceSets.main.output.classesDir, // Generated classes
                project.sourceSets.main.compileClasspath, // Deps
            ] }
        
            maxHeapSize = '256M'

            doFirst {
                if( !moduleName ) {
                    throw new Exception( "moduleName not defined on GWT plugin" );
                }
                args = [
                    moduleName,
                    '-war', buildDir,
                    '-logLevel', 'INFO',
                    '-localWorkers', '2',
                    '-compileReport',
                    '-extra', extraDir,
                    // '-draftCompile' // Speeds up compile with 25%
                ]
                project.file(buildDir).mkdirs()
            }

        }

        project.task('gwtGenerateAsync',type: GwtGenerateAsyncTask ) {
            doFirst {
                if( !servicePattern ) {
                    throw new Exception( "serviceName not defined on GWT plugin" );
                }
                def ss = project.sourceSets.main.java

                def services = ss.matching{ include servicePattern }
                def root = ss.srcDirs.iterator().next()
                def path = root.absolutePath
                println "Root: ${root}"

                def sources = [];
                services.each{ 
                    sources.add( getRelativePath(root,it) );
                    // println "Matching service: ${it}"
                }
                JavaDocBuilder builder = createJavaDocBuilder( project )
                sources.each{ 
                    String className = getTopLevelClassName( it ); 
                    JavaClass clazz = builder.getClassByName( className );
                    if( isEligibleForGeneration(clazz) ) {
                        println "Generating class: ${className}"
                        println generateAsync( clazz, null )                        ;
                    }
                    else {
                        println "Not eligible class: ${className}"
                    } 
                }
            }
        }
    }
}

class GwtPluginExtension {
}

class GwtCompileTask extends JavaExec {
    def moduleName;
    def buildDir;
    def extraDir;
}

class GwtGenerateAsyncTask extends DefaultTask {

    private static final String REMOTE_SERVICE_INTERFACE = "com.google.gwt.user.client.rpc.RemoteService";

    def servicePattern;
    def encoding;
    def returnRequest;

    String getRelativePath( File root, File file ) {
        def path = root.absolutePath
        return file.absolutePath.substring(path.length()+1) 
    }

    File getTargetFile( File parent, String source ) {
        String targetFileName = source.substring( 0, source.length() - 5 ) + "Async.java";
        File targetFile = new File( parent, targetFileName );
        return targetFile;
    }

    String getTopLevelClassName( String sourceFile ) {
        String className = sourceFile.substring( 0, sourceFile.length() - 5 ); // strip ".java"
        return className.replace( File.separatorChar, '.' as char );
    }

    JavaDocBuilder createJavaDocBuilder( Project project )
    {
        JavaDocBuilder builder = new JavaDocBuilder();
        builder.setEncoding( encoding ? encoding : Charset.defaultCharset().name() );
        builder.getClassLibrary().addClassLoader( getProjectClassLoader(project) );
        for( String sourceRoot : project.sourceSets.main.java.srcDirs ) {
            File src = new File( sourceRoot );
            println "Adding source: ${src}"
            builder.getClassLibrary().addSourceFolder( src );
        }
        return builder;
    }

    ClassLoader getProjectClassLoader( Project project ) {
        Collection<File> classpath = project.sourceSets.main.compileClasspath.files
        URL[] urls = new URL[classpath.size()];
        int i = 0;
        for ( File classpathFile : classpath ) {
            urls[i] = classpathFile.toURI().toURL();
            println "Adding URL: ${urls[i]}"
            i++;
        }
        return new URLClassLoader( urls, ClassLoader.getSystemClassLoader() );
    }

    boolean isEligibleForGeneration( JavaClass javaClass ) {
        println "Class: ${javaClass} interface=${javaClass.isInterface()}, public=${javaClass.isPublic()}, remote=${isRemote(javaClass)}"
        return javaClass.isInterface() && javaClass.isPublic() && isRemote(javaClass);
    }

    boolean isRemote( JavaClass javaClass ) {
        return javaClass.isA( REMOTE_SERVICE_INTERFACE );
    }

    boolean isDeprecated( JavaMethod method ) {
        if ( method == null ) {
            return false;
        }
        for ( Annotation annotation : method.getAnnotations() ) {
            if ( "java.lang.Deprecated".equals( annotation.getType().getFullyQualifiedName() ) ) {
                return true;
            }
        }
        return method.getTagByName( "deprecated" ) != null;
    }

    boolean hasRemoteServiceRelativePath(JavaClass clazz) {
        if ( clazz != null && clazz.getAnnotations() != null ) {
            for ( Annotation annotation : clazz.getAnnotations() ) {
                println "annotation found on service interface " + annotation;
                if ( annotation.getType().getValue().equals( "com.google.gwt.user.client.rpc.RemoteServiceRelativePath" ) ) {
                    println "@RemoteServiceRelativePath annotation found on service interface";
                    return true;
                }
            }
        }
        return false;
    }

    String getParameterExpression( JavaMethod method, JavaParameter param, int pi ) {
        return method.getParameterTypes(true)[pi].genericValue + " " +  
            getParameterDimensionsExpression(method,param,pi) + " " + param.name;
    }

    String getParameterDimensionsExpression( JavaMethod method, JavaParameter param, int pi ) {
        StringBuilder ret = new StringBuilder();
        if ( param.getType().getDimensions() != method.getParameterTypes( true )[pi].getDimensions() ) {
            for ( int dimensions = 0; dimensions < param.getType().getDimensions(); dimensions++ ) {
                ret.append( "[]" );
            }
        }
        return ret.toString();
    }

    String getCallbackExpression( JavaMethod method ) {
        if ( method.getReturnType().isVoid() ) {
            return "AsyncCallback<Void> callback );";
        }
        else if ( method.getReturnType().isPrimitive() ) {
            String primitive = method.getReturnType().getGenericValue();
            return "AsyncCallback<" + WRAPPERS.get( primitive ) + "> callback );";
        }
        else {
            Type returnType = method.getReturnType( true );
            String type = returnType.getGenericValue();

            if ( method.getReturnType().getDimensions() != method.getReturnType( true ).getDimensions() ) {
                for ( int dimensions = 0; dimensions < method.getReturnType().getDimensions(); dimensions++ ) {
                    type += "[]";
                }
            }
            return "AsyncCallback<" + type + "> callback );\n";
        }
    }

    String generateAsync( JavaClass clazz, File targetFile ) {
        /*
        PrintWriter writer = new PrintWriter( new BufferedWriter(
            new OutputStreamWriter( buildContext.newFileOutputStream( targetFile ), encoding ) ) );
        */

        String text = ''' 
<%= clazz.package ?  "package ${clazz.packageName};\n" :"" %>
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

<%= !hasRemoteServiceRelativePath ? "import com.google.gwt.user.client.rpc.ServiceDefTarget;\n" :"" %> 
public interface ${className}Async
{
    <% 
    methods.each{ method -> 
        boolean deprecated = isDeprecated( method );
    %>

        /**
          * GWT-RPC service  asynchronous (client-side) interface
          * @see ${clazz.fullyQualifiedName} <%=  deprecated ? "\n     * @deprecated" : "" %> 
          */
        <%=  deprecated ? "     * @Deprecated" : "" %> 
        <% 
        print ((returnRequest ? "    com.google.gwt.http.client.Request" : "void") + " ${method.name}("); 
        def params = method.getParameters();
        int pi=0;
        params.each{ param -> 
            print ((pi > 0 ? ", " : "") + getParameterExpression(method,param,pi))
            pi++
        } 
        print ((params.length > 0 ? ", " :"") + getCallbackExpression(method))
    }
    %>
    /**
      * Utility class to get the RPC Async interface from client-side code
      */
    public static final class Util 
    { 
        private static ${className}Async instance;\n" );
        public static final ${className}Async getInstance() {
            if ( instance == null ) {;
                instance = (${className}Async)GWT.create( ${className}.class;
                <% if ( !hasRemoteServiceRelativePath ) { 
                    String uri = MessageFormat.format( rpcPattern, className );
                %>
                    ServiceDefTarget target = (ServiceDefTarget) instance;
                    target.setServiceEntryPoint( GWT.getModuleBaseURL() + "${uri}" );
                <% } %>
            }
            return instance;
        };
        private Util() {
            // Utility class should not be instanciated
        }
    }

}
''';
        boolean hasRemoteServiceRelativePath = hasRemoteServiceRelativePath(clazz);
        String className = clazz.getName();
        JavaMethod[] methods = clazz.getMethods( true );

        def binding = [
            className: className,
            clazz: clazz,
            methods: methods,
            returnRequest: returnRequest,
            hasRemoteServiceRelativePath: hasRemoteServiceRelativePath,
            isDeprecated: { isDeprecated(it) },
            getParameterExpression: { method, param, pi -> getParameterExpression(method,param,pi) }, 
            getCallbackExpression: { getCallbackExpression(it) } 
        ] 

        def engine = new SimpleTemplateEngine()
        def tmpl = engine.createTemplate(text).make(binding)
        return tmpl.toString();
    }
}
