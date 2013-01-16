package org.scriptbox.horde.action;

import groovy.lang.Closure;

import java.util.List;
import java.util.Map;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.BoxScript;
import org.scriptbox.box.container.Lookup;
import org.scriptbox.box.controls.BoxService;
import org.scriptbox.box.events.BoxContextListener;
import org.scriptbox.box.events.BoxInvocationContext;
import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.box.groovy.Closures;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionGroovyInjector implements ActionInjector, BoxContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger( ActionGroovyInjector.class );
	
	private static final String SERVICE_KEY = "action.service";
	private static final String SCRIPT_KEY = "action.script";
	
	@Override
	public String getLanguage() {
		return "groovy";
	}

	@Override
	public void inject(BoxContext context) {
		Lookup vars = context.getScriptVariables();
		vars.put( "addAction", new MethodClosure( this, "addAction")  );
		vars.put( "addGlobalErrorHandler", new MethodClosure( this, "addGlobalErrorHandler")  );
		vars.put( "addDestructor", new MethodClosure( this, "addDestructor")  );
	}
	
	public void addAction( String name, Map<String,Object> params ) {
		ActionScript actionScript = getCurrentActionScript();
		Action action = new Action(actionScript,name);
		action.setRun( toRunnable( params, "run", action) );
		action.setInit(toRunnable( params, "init", action) );
		action.setPre( toRunnable( params, "pre", action) );
		action.setPost( toRunnable( params, "post", action) );
		actionScript.addAction( action );
	}

	public void addGlobalErrorHandler( final Closure closure ) {
		ActionScript actionScript = getCurrentActionScript();
		actionScript.addGlobalErrorHandler( new ActionErrorHandler() {
			public void handle( Throwable ex ) throws Throwable {
				closure.call( ex );
			}
		});
	}
	
	public ParameterizedRunnableWithResult<Boolean,List> toRunnable( Map<String,Object> params, String key, Action action ) {
		Closure closure = (Closure)params.get(key);
		if( closure != null ) {
			closure.setDelegate( action );
			return Closures.toRunnableWithBoolean(closure, List.class);
		}
		return null;
		
	}
	public void addDestructor( Closure closure ) {
		getCurrentActionScript().addDestructor( Closures.toRunnable(closure) );
	}
	
	public void contextCreated( BoxContext context ) throws Exception {
	}
	
	public void contextShutdown( BoxContext context ) throws Exception {
	}
	
	public void executingScript( final BoxInvocationContext invocation ) throws Exception {
		BoxScript script = invocation.getScript();
		final ActionScript actionScript = new ActionScript( script );
		ExecContext.with( actionScript, new ExecRunnable() {
			public void run() throws Exception {
				invocation.next();
				actionScript.initialize();
			}
		});
		
		
		script.getContext().getBeans().put( SERVICE_KEY + "." + script.getName(), new BoxService() {
			public void start( List arguments ) throws Exception {
				if( arguments == null || arguments.size() < 1 ) {
					throw new IllegalArgumentException( "Must specify a number of threads");
				}
				int threadCount = 0;
				try {
					threadCount = Integer.parseInt(String.valueOf(arguments.get(0)) );
				}
				catch( Exception ex ) {
					throw new IllegalArgumentException( "Must specify a number of threads", ex );
				}
				actionScript.start( threadCount );
			}
			public void shutdown() throws Exception {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "shutdown: script=" + actionScript ); }
				actionScript.shutdown();
			}
			public void stop() throws Exception {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "stop: script=" + actionScript ); }
				actionScript.stop();
			}
			public String status() throws Exception {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "status: script=" + actionScript ); }
				return actionScript.toString();
			}
		});
	}
	private ActionScript getCurrentActionScript() {
		return ExecContext.getNearestEnclosing(ActionScript.class);
	}
}
