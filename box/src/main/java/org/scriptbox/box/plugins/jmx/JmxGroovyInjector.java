package org.scriptbox.box.plugins.jmx;

import groovy.lang.Closure;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.ObjectName;
import javax.management.QueryExp;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.scriptbox.box.exec.AbstractExecBlock;
import org.scriptbox.box.exec.ExecBlock;
import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.plugins.jmx.MBeanProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.scriptbox.util.common.regex.RegexUtil;
import org.scriptbox.util.common.os.proc.ProcessStatus;
import org.scriptbox.util.common.os.proc.ProcessStatusParser;

public class JmxGroovyInjector implements JmxInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger( JmxGroovyInjector.class );
	
	private JmxPlugin plugin;
	
	public JmxPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(JmxPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public String getLanguage() {
		return "groovy";
	}

	@Override
	public void inject(BoxContext context) {
		Lookup vars = context.getScriptVariables();
		vars.put( "ps", new MethodClosure( this, "ps")  );
		vars.put( "remote", new MethodClosure( this, "remote")  );
		vars.put( "mbeans", new MethodClosure( this, "mbeans")  );
		vars.put( "mbean", new MethodClosure( this, "mbean")  );
	}

	public void ps( final Object filter, final Closure closure ) throws Exception {
		final ParameterizedRunnableWithResult<Boolean,ProcessStatus> finder;
		if( filter instanceof Closure ) {
			final Closure fc = (Closure)filter;
			finder = new ParameterizedRunnableWithResult<Boolean,ProcessStatus>() {
				public Boolean run( ProcessStatus ps ) throws Exception {
					return coerceToBoolean( fc.call(ps) );
				}
			};
		}
		else {
			final List<Pattern> patterns = RegexUtil.toPatternCollection(filter);
			finder = new ParameterizedRunnableWithResult<Boolean,ProcessStatus>() {
				public Boolean run( ProcessStatus status ) throws Exception {
					return RegexUtil.matchesAny( status.command, patterns );
				}
			};
		}
		
		add( closure, new AbstractExecBlock<ExecRunnable>() {
			public void run() throws Exception {
				List<ProcessStatus> procs = ProcessStatusParser.find( finder );
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "ps: matching processes=" + procs ); }
				for( ProcessStatus proc : procs ) {
					JmxConnection connection = plugin.getConnections().getConnection( proc.pid, proc.command );
					with( connection );
				}
			}
		} ); 
		
	}
	
	public void remote( final String host, final int port, Closure closure ) throws Exception {
		add( closure, new AbstractExecBlock<ExecRunnable>() {
			public void run() throws Exception {
				JmxConnection connection = plugin.getConnections().getConnection( host, port );
				with( connection );
			}
		} ); 
	}
	
	@SuppressWarnings("unchecked")
	public void mbeans( final String objectName, final QueryExp expression, final Closure filter, final Closure closure ) throws Exception {
		ExecBlock<ExecRunnable> block = ExecContext.getEnclosing(ExecBlock.class);
		ExecBlock<ExecRunnable> receiver = new AbstractExecBlock<ExecRunnable>() {
			public void run() throws Exception  {
				JmxConnection connection = ExecContext.getEnclosing( JmxConnection.class );
		        Set<ObjectName> objectNames = connection.getServer().queryNames(new ObjectName(objectName), expression );
	            if( filter != null ) {
	                Set<ObjectName> filtered = new HashSet<ObjectName>();
	                for( ObjectName objectName : objectNames ) {
	                    if( coerceToBoolean(filter.call(objectName)) ) {
	                        filtered.add( objectName );
	                    }
	                }
	                objectNames = filtered;
	            }
                if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "mbeans: connection=" + connection + ", objectNames=" + objectNames ); }
                with( objectNames );
			}
		};
		block.add( receiver );
		callClosureWithEnclosing( receiver, closure );
	}
	
	@SuppressWarnings("unchecked")
	public void mbean( final Closure closure ) throws Exception {
		ExecBlock<ExecRunnable> block = ExecContext.getEnclosing(ExecBlock.class);
		ExecRunnable receiver = new ExecRunnable() {
			public void run() throws Exception  {
				JmxConnection connection = ExecContext.getNearestEnclosing( JmxConnection.class );
		        Set<ObjectName> objectNames = ExecContext.getEnclosing( Set.class );
		        for( ObjectName objectName : objectNames ) {
			        MBeanProxy proxy = new MBeanProxy( connection, objectName );
			        closure.call( proxy );
	            }
			}
		};
		block.add( receiver );
	}
	
	@SuppressWarnings("unchecked")
	private void add( final Closure closure, ExecRunnable runnable ) throws Exception {
		ExecBlock<ExecRunnable> container = ExecContext.getEnclosing(ExecBlock.class);
		container.add( runnable ); 
		callClosureWithEnclosing( runnable, closure );
	}
	
	private void callClosureWithEnclosing( Object enclosing, final Closure closure ) throws Exception {
		ExecContext.with(enclosing, new ExecRunnable() {
			public void run() throws Exception {
				closure.call();
			}
		} );
		
	}
	
	private boolean coerceToBoolean( Object obj ) {
		if( obj != null ) {
			if( obj instanceof Boolean ) {
				return ((Boolean)obj).booleanValue();
			}
			else if( obj instanceof Matcher ) {
				return ((Matcher)obj).matches();
			}
			else {
				return Boolean.valueOf( String.valueOf(obj) ).booleanValue();
			}
		}
		return false;
	}
}
