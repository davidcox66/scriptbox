package org.scriptbox.horde.main;

import java.util.List

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.ihg.atp.crs.loadgen.metrics.TestMetric

class LoadContext extends Scriptable {

    private static final Logger LOGGER = LoggerFactory.getLogger( LoadContext );
  
    GroovyShell shell; 
    Binding binding = new Binding();
    GroovyClassLoader loader = new GroovyClassLoader();

    Map<String,LoadScript> scripts = new HashMap<String,LoadScript>();     
    Map<String,Object> variables = new HashMap<String,Object>();     
     
    def init;
    def destroy;
    
    LoadContext( String contextName, String scriptText, List args ) {
        super( contextName, scriptText, args );
    }

    
    void initialize() {
        buildBinding();
        shell = new GroovyShell( loader, binding );
        callInClassLoader(loader) {
	        shell.run( scriptText, scriptName, args );
	        init = getVariable( 'initContext');
	        destroy = getVariable( 'destroyContext' );
            try {
		        init?.call(); 
            }
            catch( Exception ex ) {
                LOGGER.error( "Errur during context initialization, attempting cleanup...", ex );
                try {
                    destroy?.call();    
                }
                catch( Exception ex2 ) {
                    LOGGER.error( "Error during attempted cleanup", ex2 );
                }
                throw ex;
            }
        }
    }

    void buildBinding() {
       String loggerName = scriptName.replaceAll('/', '.').replaceAll( '.groovy$', '' );
       Logger lgr = LoggerFactory.getLogger(loggerName);
       binding.setVariable( 'logger', lgr );
       binding.setVariable( 'LOGGER', lgr );

       binding.setVariable( 'export', { String name, Object value ->
            variables.put( name, value );
        } );
    }
    
    void runScript( LoadScript script ) {
        callInClassLoader(loader) {
            variables.each{ String name, Object value ->
	            binding.setVariable(name, value );
            }
	        binding.setVariable( 'addTest', { String name, def params ->
                script.addTest( name, params );
	        } );
	        binding.setVariable( 'destroy', { Closure destroy ->
                script.setDestroyCallback( destroy );
	        } );
	  
            shell.run( script.scriptText, script.scriptName, script.args );
        }
    }

    synchronized void loadScript( String name, String script, List args ) {
        try {
	        if( scripts.get(name) ) {
	            LOGGER.info( "loadScript: alreadying running ${name}, will unload first" );
	            shutdownScript( name );
	        }
	        LOGGER.info( "loadScript: loading script: ${name}" );
	        LoadScript config = new LoadScript(this, name, script, args ) ;
            config.initialize();
	        scripts.put( name, config );
            LOGGER.info( "loadScript: finished loading script: ${name}");
        }
        catch( Throwable ex ) {
            LOGGER.error( "loadScript: failed starting: ${name}", ex );
            throw ex;
        }
    }

    synchronized void startScript( String name, int threadCount, List args ) {
        LoadScript config = scripts.get( name );
        if( config ) { 
            config.start( threadCount, args );
        }
        else {
            throw new Exception( "Script '${name}' not found" );
        }
    }    
    
    synchronized void stopScript( String name ) {
        LoadScript config = scripts.get( name );
        if( config ) {
	        try { 
		        LOGGER.info( "stopScript: stopping: ${name}");
		        config.stop();
		        LOGGER.info( "stopScript: complete: ${name}");
	        }
	        catch( Throwable ex ) {
	            LOGGER.error( "stopScript: failed stopping: ${name}", ex );
	            throw ex;
	        }
        }
        else {
            LOGGER.info( "stopScript: not loaded: ${name}");
        }
    }
    
    synchronized void shutdownScript( String name ) {
        LoadScript config = scripts.remove( name );
        if( config ) {
	        try { 
		        LOGGER.info( "shutdownScript: stopping: ${name}");
		        config.shutdown();
		        LOGGER.info( "shutdownScript: complete: ${name}");
	        }
	        catch( Throwable ex ) {
	            LOGGER.error( "shutdownScript: failed stopping: ${name}", ex );
	            throw ex;
	        }
        }
        else {
            LOGGER.info( "shutdownScript: not loaded: ${name}");
        }
    }
   
    private void stopImpl( String name, LoadScript config ) {
    } 
    
    synchronized void shutdown() {
        LOGGER.debug( "shutdown: stopping ${scripts.size()} scripts")
        try {
            scripts.values().each{ LoadScript script ->
                script.shutdown();
            }
        }
        finally {
            try {
                callInClassLoader( loader, destroy );
            }
            catch( Exception ex ) {
                LOGGER.error( "Error invoking destroy", ex );
            }
	        scripts.clear();
        }
    }

    Object getVariable( String name ) {
        return binding.getVariables().get(name); 
    }    
    Object getVariableEx( String name ) {
        return binding.getVariable(name); 
    }    

    synchronized public int getTotalNumRunners() {
        int total = 0;
        scripts.each{ LoadScript script -> total += script.numRunners }
        return total;
    }
    
    public String toString() {
       StringBuilder builder = new StringBuilder( "[name=${name}, scripts=[" );
       int i=0;
       scripts.values().each{ LoadScript script ->
           if( i++ > 0 ) {
               builder.append( ',' );
           }    
           builder.append( script.toString() );
       } 
       builder.append( "]]");
       return builder.toString();
    } 
}
