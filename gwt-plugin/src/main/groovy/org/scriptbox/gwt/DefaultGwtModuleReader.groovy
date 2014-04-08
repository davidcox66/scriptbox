package org.scriptbox.gwt;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gradle.api.Project;
import org.gradle.api.file.FileTree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;

class DefaultGwtModuleReader implements GwtModuleReader
{
    public static final String GWT_MODULE_EXTENSION = ".gwt.xml";

    private Project project;
    private static final Logger log = LoggerFactory.getLogger( DefaultGwtModuleReader );

    public DefaultGwtModuleReader( Project project  ) {
        this.project = project;
    }

    public List<String> getGwtModules() {
        FileTree mods = project.sourceSets.main.allSource.matching{ include "**/*${GWT_MODULE_EXTENSION}" }
        if ( mods.isEmpty() ) {
            log.warn( "GWT plugin is configured to detect modules, but none were found." );
        }

        List<String> modules = new ArrayList<String>( mods.size() );
        mods.visit{ details ->
            String rel = details.relativePath;
            modules.add( rel[0..-GWT_MODULE_EXENSION.length()].replaceAll( File.separatorChar, '.' as char ) );
        }
        if ( modules.size() > 0 ) {
            log.info( "auto discovered modules " + modules );
        }
        return modules;
    }

    public GwtModule readModule( String name ) {
        String modulePath = name.replace( '.' as char, '/' as char) + GWT_MODULE_EXTENSION;
        Collection<String> sourceRoots = mavenProject.getCompileSourceRoots();
        for( File root : project.sourceSets.main.allSource.srcDirs ) {
            File xml = new File( root, modulePath ); 
            if ( xml.exists() ) {
                log.debug( "GWT module " + name + " found in " + root );
                return readModuleFile( name, xml );
            }
        }

        Set<File> classpath = project.sourceSets.main.classpath.files;
        URL[] urls = new URL[classpath.size()];
        int i = 0;
        for ( File file : classpath ) {
            urls[i++] = file.toURI().toURL();
        }
        InputStream stream = new URLClassLoader( urls ).getResourceAsStream( modulePath );
        if ( stream != null ) {
            return readModuleStream( name, stream );
        }

        throw new Exception( "GWT Module " + name + " not found in project sources or resources." );
    }

    private GwtModule readModuleFile( String name, File file ) {
        try {
            return readModuleStream( name, new FileInputStream( file ) );
        }
        catch ( FileNotFoundException e ) {
            throw new Exception( "Failed to read module file " + file );
        }
    }

    private GwtModule readModuleStream( String name, InputStream xml ) {
        try {
            Xpp3Dom dom = Xpp3DomBuilder.build( ReaderFactory.newXmlReader( xml ) );
            return new GwtModule( name, dom, this );
        }
        catch ( Exception e ) {
            String error = "Failed to read module XML file " + xml;
            log.error( error );
            throw new Exception( error, e );
        }
    }
}
