package org.scriptbox.gwt;

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction

class GwtCompileTask extends JavaExec {
    def logLevel = 'INFO'
    def style = 'PRETTY'
    def war = null;
    def localWorkers = '2'
    def checkAssertions = false 
    def draftCompile = false
    def validateOnly = false
    def disableClassMetadata = false
    def disableCastChecking = false
    def disableRunAsync = false
    def failOnError = false
    def detailedSoyc = false
    def closureCompiler = false
    def compileReport = false
    def compilerMetrics = false
    def disableAggressiveOptimization = false
    def fragmentCount
    def clusterFunctions = true
    def enforceStrictResources = false
    def inlineLiteralParameters = true
    def optimizeDataflow = true
    def ordinalizeEnums = true
    def removeDuplicateFunctions = true
    def saveSource = false
    def sourceLevel
    def saveSourceOutput
    def optimizationLevel
    
    def moduleName

    def gwtVersion = "2.4.0"


    // @TaskAction
    public void init() {
	    def gwtServletDepend = "com.google.gwt:gwt-servlet:${gwtVersion}" // runtime
	    def gwtUserDepend = "com.google.gwt:gwt-user:${gwtVersion}" // provided
	    def gwtDevDepend = "com.google.gwt:gwt-dev:${gwtVersion}" // test
	    // def gwtLogDepend = "com.google.code.gwt-log:gwt-log:2.6.2"

        def config = getConfigName();
        project.dependencies.add( config, gwtUserDepend )
        project.dependencies.add( config, gwtDevDepend )
        project.dependencies.add( 'runtime', gwtServletDepend )

        inputs.source project.sourceSets.main.java.srcDirs
        inputs.dir project.sourceSets.main.output.resourcesDir
        outputs.dir project.gwt.buildDir

        // Add generated directory to sources

        // Workaround for incremental build (GRADLE-1483)
        // outputs.upToDateSpec = new org.gradle.api.specs.AndSpec()

        setMain( 'com.google.gwt.dev.Compiler' )
        classpath { [
            project.sourceSets.main.java.srcDirs, // Java source
            project.sourceSets.main.resources.srcDirs, 
            project.sourceSets.main.output.resourcesDir, // Generated resources
            project.sourceSets.main.output.classesDir, // Generated classes
            project.sourceSets.main.compileClasspath // Deps
        ] }
    
        maxHeapSize = '768M'

        doFirst {
            args = buildArgs()
            project.file(project.gwt.buildDir).mkdirs()
        }
    }

    def buildArgs() {
        if( !moduleName ) {
            throw new Exception( "moduleName not defined on GWT plugin" );
        }
        def args = []

        args.add( moduleName );

        addArg( args, '-logLevel', logLevel );
        addArg( args, '-style', style );
        addArg( args, '-war', war ? war : project.gwt.buildDir );
        addArg( args, "-localWorkers", localWorkers )

        // optional advanced arguments
        addArg( args, checkAssertions, "-checkAssertions" )
        addArg( args, draftCompile, "-draftCompile" )
        addArg( args, validateOnly, "-validateOnly" )
        addArg( args, disableClassMetadata, "-XnoclassMetadata" )
        addArg( args, disableCastChecking, "-XnocheckCasts" )
        addArg( args, disableRunAsync, "-XnocodeSplitting" )
        addArg( args, failOnError, "-failOnError" )
        addArg( args, detailedSoyc, "-XdetailedSoyc" )
        addArg( args, closureCompiler, "-XclosureCompiler" )
        addArg( args, compileReport, "-compileReport" )
        addArg( args, compilerMetrics, "-XcompilerMetrics" )
        addArg( args, disableAggressiveOptimization, "-XnoaggressiveOptimizations" )
        addArg( args, "-XfragmentCount",  fragmentCount )
        addArg( args, !clusterFunctions, "-XnoclusterFunctions" )
        addArg( args, enforceStrictResources, "-XenforceStrictResources" )
        addArg( args, !inlineLiteralParameters, "-XnoinlineLiteralParameters" )
        addArg( args, !optimizeDataflow, "-XnooptimizeDataflow" )
        addArg( args, !ordinalizeEnums, "-XnoordinalizeEnums" )
        addArg( args, !removeDuplicateFunctions, "-XnoremoveDuplicateFunctions" )
        addArg( args, saveSource, "-saveSource" )
        addArg( args, "-sourceLevel", sourceLevel )
        addArg( args, "-saveSourceOutput", saveSourceOutput?.getAbsolutePath() );
        addArg( args, "-optimize", optimizationLevel );
        addArg( args, "-workDir", project.gwt.workDir );

        /*
        args = [
            moduleName,
            '-war', buildDir,
            '-logLevel', 'INFO',
            '-localWorkers', '2',
            '-compileReport',
            '-extra', extraDir,
            // '-draftCompile' // Speeds up compile with 25%
        ]
        */
        println "GWT compile args: ${args}"
        return args
    }

    void addArg( def args, String arg, String value ) {
        if( value ) {
            args.add( arg );
            args.add( value );
        }
    }
    void addArg( def args, boolean cond, String arg ) {
        if( cond ) {
            args.add( arg );
        }
    }

    String getConfigName() {
        boolean war = false; 
        try {
            project.configurations['providedCompile'] 
            war = true;
        }
        catch( Exception ex ) {
        }

        return war ? 'providedCompile' : 'compile'
    }
}

