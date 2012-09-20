package org.scriptbox.box.plugins.jmx;

import groovy.lang.Closure;

import java.util.List;
import java.util.regex.Pattern;

import javax.management.ObjectName;
import javax.management.QueryExp;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.scriptbox.box.exec.ExecBlock;
import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.box.groovy.Closures;
import org.scriptbox.box.plugins.jmx.capture.CaptureContext;
import org.scriptbox.box.plugins.jmx.capture.CaptureResult;
import org.scriptbox.box.plugins.jmx.capture.JmxCapture;
import org.scriptbox.box.plugins.jmx.proc.JmxLocalProcessProvider;
import org.scriptbox.box.plugins.jmx.proc.JmxRemoteProcessProvider;
import org.scriptbox.plugins.jmx.MBeanProxy;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.scriptbox.util.common.os.proc.ProcessStatus;
import org.scriptbox.util.common.regex.RegexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		vars.put( "capture", new MethodClosure( this, "capture")  );
	}

	public void ps( String name, Object filter, Closure closure ) throws Exception {
		final ParameterizedRunnableWithResult<Boolean,ProcessStatus> finder;
		if( filter instanceof Closure ) {
			final Closure fc = (Closure)filter;
			finder = Closures.toRunnableWithBoolean( fc, ProcessStatus.class );
		}
		else {
			final List<Pattern> patterns = RegexUtil.toPatternCollection(filter);
			finder = new ParameterizedRunnableWithResult<Boolean,ProcessStatus>() {
				public Boolean run( ProcessStatus status ) throws Exception {
					return RegexUtil.matchesAny( status.command, patterns );
				}
			};
		}
		
		add( closure, new JmxLocalProcessProvider(plugin, name, finder) );
		
	}
	
	public void remote( String name, String host, int port, Closure closure ) throws Exception {
		add( closure, new JmxRemoteProcessProvider(plugin, name, host, port) );
	}
	
	public void mbeans( String objectName, Closure closure ) throws Exception {
		mbeans( objectName, null, false, null, closure );
	}
	
	@SuppressWarnings("unchecked")
	public void mbeans( String objectName, QueryExp expression, boolean caching, final Closure filter, Closure closure ) throws Exception {
		ExecBlock<ExecRunnable> block = ExecContext.getEnclosing(ExecBlock.class);
		ParameterizedRunnableWithResult<Boolean,ObjectName> runner = Closures.toRunnableWithBoolean( filter, ObjectName.class );
		JmxMBeans mbeans = new JmxMBeans( objectName, expression, caching, runner );
		block.add( mbeans );
		callClosureWithEnclosing( mbeans, closure );
	}
	
	@SuppressWarnings("unchecked")
	public void mbean( final Closure closure ) throws Exception {
		ExecBlock<ExecRunnable> block = ExecContext.getEnclosing(ExecBlock.class);
		ExecRunnable receiver = new JmxMBean( Closures.toRunnable(closure,MBeanProxy.class) );
		block.add( receiver );
	}

    public void capture( final Object oneOrMoreAttributes )  {
    	capture( oneOrMoreAttributes, null );
    }
    public void capture( final Object oneOrMoreAttributes, final Closure closure )  {
    	capture( oneOrMoreAttributes, null, false, closure );
    }
    public void capture( final Object oneOrMoreAttributes, final Object oneOrMoreExclusions, final boolean caching )  {
    	capture( oneOrMoreAttributes, oneOrMoreExclusions, caching, null );
    }
	@SuppressWarnings("unchecked")
    public void capture( final Object oneOrMoreAttributes, final Object oneOrMoreExclusions, final boolean caching, final Closure closure )  {
		ExecBlock<ExecRunnable> block = ExecContext.getEnclosing(ExecBlock.class);
		ParameterizedRunnableWithResult<CaptureResult,CaptureContext> runner = null;
		if( closure != null ) {
			runner = new ParameterizedRunnableWithResult<CaptureResult,CaptureContext>() {
				public CaptureResult run( CaptureContext ctx ) {
					return (CaptureResult)closure.call( ctx );
				}
			};
		}
		JmxCapture receiver = new JmxCapture( oneOrMoreAttributes, oneOrMoreExclusions, caching, runner );
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
	
}
