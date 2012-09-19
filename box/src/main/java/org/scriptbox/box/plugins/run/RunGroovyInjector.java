package org.scriptbox.box.plugins.run;

import groovy.lang.Closure;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.scriptbox.box.exec.ExecBlock;
import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.exec.ExecRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RunGroovyInjector implements RunInjector {

	private static Logger LOGGER = LoggerFactory.getLogger( RunGroovyInjector.class );
	
	private RunPlugin plugin;
	
	public RunPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(RunPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public String getLanguage() {
		return "groovy";
	}

	@Override
	public void inject(BoxContext context) {
		Lookup vars = context.getScriptVariables();
		vars.put( "run", new MethodClosure( this, "run")  );
	}

	@SuppressWarnings("unchecked")
	public void run( final Closure closure ) throws Exception {
		ExecBlock<ExecRunnable> container = ExecContext.getEnclosing(ExecBlock.class);
		ExecRunnable runner = new ExecRunnable() {
			public void run() throws Exception {
				closure.call();
			}
		}; 
		container.add( runner );
	}
}
