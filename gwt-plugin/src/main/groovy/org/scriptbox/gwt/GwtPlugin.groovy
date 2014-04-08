package org.scriptbox.gwt;

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskAction

class GwtPlugin implements Plugin<Project> {

    void apply( Project project ) {
        project.extensions.create("gwt", GwtPluginExtension)
    
        project.gwt.buildDir = "${project.buildDir}/gwt"
        project.gwt.extraDir = "${project.buildDir}/extra"

        def dir = new File(project.gwt.buildDir)

        project.sourceSets.main.java.srcDirs = project.sourceSets.main.java.srcDirs << dir
        project.sourceSets.main.resources.srcDirs = project.sourceSets.main.resources.srcDirs << dir
        println "Java directories: ${project.sourceSets.main.java.srcDirs}"
        println "Source directories: ${project.sourceSets.main.resources.srcDirs}"

        project.task('gwtCompile',type: GwtCompileTask ) { it.init(); }
        project.task('gwtGenerateAsync',type: GwtGenerateAsyncTask ) { it.init(); }
        /*
        project.task('gwtResources',type: Copy ) { 
            // DefaultGwtModuleReader reader = new DefaultGwtModuleReader();
            // GwtModule module = reader.readModule( project.gwt.moduleName );
            include project.sourceSets.main.allJava
            into project.gwt.buildDir 
        }
        */
    }
}

class GwtPluginExtension {
    def buildDir
    def extraDir
    def workDir
}

