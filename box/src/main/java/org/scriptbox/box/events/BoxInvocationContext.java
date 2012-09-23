package org.scriptbox.box.events;

import java.util.Iterator;
import java.util.List;

import javax.script.ScriptEngine;

import org.scriptbox.box.container.BoxScript;

public class BoxInvocationContext {

	private BoxScript script;
	private Iterator<BoxContextListener> iter;
	private ScriptEngine engine;
	private boolean evaluated;
	
	public BoxInvocationContext( ScriptEngine engine, BoxScript script, List<BoxContextListener> listeners ) {
		this.engine = engine;
		this.script = script;
		this.iter = listeners.iterator();
	}
	
	public BoxScript getScript() {
		return script;
	}

	public void next() throws Exception {
		if( iter.hasNext() ) {
			BoxContextListener listener = iter.next();
			listener.executingScript( this );
		}
		else {
			if( evaluated ) {
				throw new RuntimeException( "Cannot evaluted script multiple times");
			}
			evaluated = true;
			engine.put( "args", script.getArguments() );
	    	engine.eval( script.getScriptText() );
			
		}
	}
}
