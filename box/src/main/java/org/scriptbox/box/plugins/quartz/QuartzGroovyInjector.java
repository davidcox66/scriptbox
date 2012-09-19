package org.scriptbox.box.plugins.quartz;

import groovy.lang.Closure;

import org.codehaus.groovy.runtime.MethodClosure;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.exec.ExecRunnable;


public class QuartzGroovyInjector implements QuartzInjector {

	private QuartzPlugin plugin;
	
	public QuartzPlugin getPlugin() {
		return plugin;
	}
	public void setPlugin(QuartzPlugin plugin) {
		this.plugin = plugin;
	}
	public String getLanguage() {
		return "groovy";
	}
	public void inject( BoxContext context ) {
		Lookup vars = context.getScriptVariables();
		vars.put( "at", new MethodClosure( this, "at")  );
		vars.put( "every", new MethodClosure( this, "every")  );
		vars.put( "once", new MethodClosure( this, "once")  );
		vars.put( "enclosing", new MethodClosure( this, "enclosing")  );
	}
	
	public void at( String when, final Closure closure ) throws Exception {
		int cnt = plugin.getNextId();
		CronTrigger trigger = new CronTrigger("at " + cnt );
	    trigger.setCronExpression( new CronExpression(when) );
		plugin.configureJob( BoxContext.getCurrentContext(), cnt, trigger, createBlock(closure) );
	}
	
	public void every( int when, Closure closure ) throws Exception {
		int cnt = plugin.getNextId();
		Trigger trigger = new SimpleTrigger("every " + cnt, SimpleTrigger.REPEAT_INDEFINITELY, when );
		plugin.configureJob( BoxContext.getCurrentContext(), cnt, trigger, createBlock(closure) );
	}
	
	public void once( Closure closure ) throws Exception {
		int cnt = plugin.getNextId();
		Trigger trigger = new SimpleTrigger("once " + cnt );
		plugin.configureJob( BoxContext.getCurrentContext(), cnt, trigger, createBlock(closure) );
	}
	
	public Object enclosing() throws Exception {
		return ExecContext.getEnclosing();
	}

	private QuartzExecBlock createBlock( final Closure closure ) throws Exception {
		QuartzExecBlock block = new QuartzExecBlock( BoxContext.getCurrentContext() );
		ExecContext.with(block, new ExecRunnable() {
			public void run() throws Exception {
				closure.call();
			}
		} );
		return block;
	}
}
