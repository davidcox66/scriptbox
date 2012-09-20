package org.scriptbox.horde.main;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.BoxScript;
import org.scriptbox.box.container.Lookup;
import org.scriptbox.box.events.BoxContextListener;
import org.scriptbox.box.groovy.Closures;

public class LoadGroovyInjector implements LoadInjector, BoxContextListener {

	private static final String DESTRUCTORS_KEY = "load.destructors";
	
	@Override
	public String getLanguage() {
		return "groovy";
	}

	@Override
	public void inject(BoxContext context) {
		Lookup vars = context.getScriptVariables();
		vars.put( "addTest", new MethodClosure( this, "addTest")  );
		vars.put( "destroy", new MethodClosure( this, "destroy")  );
		context.getBeans().put(DESTRUCTORS_KEY, new ArrayList<Closure>() );
	}
	
	public void addTest( String name, Map<String,Object> params ) {
		BoxScript script = BoxScript.getCurrentScript();
		LoadTest test = new LoadTest(script,name);
		test.run = convertToRunnable( params, "run", test );
		test.init = convertToRunnable( params, "init", test );
		test.pre = convertToRunnable( params, "pre", test );
		test.post = convertToRunnable( params, "post", test );
	}

	public Runnable convertToRunnable( Map<String,Object> params, String key, LoadTest test ) {
		Closure closure = (Closure)params.get(key);
		if( closure != null ) {
			closure.setDelegate( test );
			return Closures.toRunnable(closure);
		}
		return null;
		
	}
	public void destroy( Closure closure ) {
		getDestructors().add( closure );
		
	}
	
	public void contextCreated( BoxContext context ) throws Exception {
	}
	
	public void contextShutdown( BoxContext context ) throws Exception {
		List<Closure> destructors = getDestructors();
		for( Closure destructor : destructors ) {
			destructor.call();
		}
	}
	
	public void executingScript( BoxScript script ) throws Exception {
		
	}
	
	private List<Closure> getDestructors() {
		return BoxContext.getCurrentContext().getBeans().getEx(DESTRUCTORS_KEY,List.class);
	}
}
