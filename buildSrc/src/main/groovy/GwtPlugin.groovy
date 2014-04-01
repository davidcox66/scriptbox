import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.JavaExec

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
                    mod,
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
                sources.each{ 
                    println "Matching source: ${it}"
                    String clsName = getTopLevelClassName( it ); 
                    println "Matching class: ${clsName}"
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
    def servicePattern;

    String getRelativePath( File root, File file ) {
        def path = root.absolutePath
        return file.absolutePath.substring(path.length()+1) 
    }

    String getTopLevelClassName( String sourceFile ) {
        String className = sourceFile.substring( 0, sourceFile.length() - 5 ); // strip ".java"
        return className.replace( File.separatorChar, '.' as char );
    }
}


