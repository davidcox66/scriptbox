package org.scriptbox.horde.threads;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.scriptbox.box.events.BoxContextListener;
import org.scriptbox.box.events.BoxInvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadsGroovyInjector implements ThreadsInjector, BoxContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger( ThreadsGroovyInjector.class );

	private static final String SERVICE_KEY = "threads.service";
	
	private static class ClosureCallable implements Callable<Object>
	{
		private Closure closure;
	
		public ClosureCallable( Closure closure ) {
			this.closure = closure;
		}
	
		public Object call() {
			return closure.call();
		}
	}
	
	@Override
	public String getLanguage() {
		return "groovy";
	}

	@Override
	public void inject(BoxContext context) {
		Lookup vars = context.getScriptVariables();
		vars.put( "fork", new MethodClosure( this, "fork")  );
	}
	
	public void fork( List<Closure> operations ) throws InterruptedException, ExecutionException {
		ExecutorService service = BoxContext.getCurrentContext().getBeans().getEx( SERVICE_KEY, ExecutorService.class );
		
		List<Callable<Object>> callables = new ArrayList<Callable<Object>>();
		for( Closure closure : operations ) {
			callables.add( new ClosureCallable(closure) );
		}
		List<Future<Object>> results = service.invokeAll( callables );
		for( Future<Object> result : results ) {
			result.get();
		}
	}

	public void contextCreated( BoxContext context ) throws Exception {
		ExecutorService service = Executors.newCachedThreadPool();
		context.getBeans().put( SERVICE_KEY, service );
	}

	public void contextShutdown( BoxContext context ) throws Exception {
		context.getBeans().getEx( SERVICE_KEY, ExecutorService.class ).shutdownNow();
	}
	
	public void executingScript( final BoxInvocationContext invocation ) throws Exception {
		invocation.next();
	}
}
