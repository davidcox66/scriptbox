package org.scriptbox.panopticon.jmx;

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
import org.scriptbox.box.jmx.conn.JmxConnections;
import org.scriptbox.box.jmx.proc.JmxPsProcessProvider;
import org.scriptbox.box.jmx.proc.JmxRemoteProcessProvider;
import org.scriptbox.box.jmx.proc.JmxVmProcessProvider;
import org.scriptbox.box.jmx.vm.VmJmxConnectionBuilder;
import org.scriptbox.panopticon.capture.CaptureContext;
import org.scriptbox.panopticon.capture.CaptureResult;
import org.scriptbox.plugins.jmx.MBeanProxy;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.scriptbox.util.common.os.proc.ProcessStatus;
import org.scriptbox.util.common.regex.RegexUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxGroovyInjector implements JmxInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger( JmxGroovyInjector.class );
	
	@Override
	public String getLanguage() {
		return "groovy";
	}

	@Override
	public void inject(BoxContext context) {
		Lookup vars = context.getScriptVariables();
		vars.put( "ps", new MethodClosure( this, "ps")  );
		vars.put( "vms", new MethodClosure( this, "vms")  );
		vars.put( "remote", new MethodClosure( this, "remote")  );
		
		vars.put( "mbeans", new MethodClosure( this, "mbeans")  );
		vars.put( "mbean", new MethodClosure( this, "mbean")  );
		vars.put( "capture", new MethodClosure( this, "capture")  );
	}

	public void ps( String name, Object filter, Closure closure ) throws Exception {
		final ParameterizedRunnableWithResult<Boolean,ProcessStatus> finder = createProcessFinder( filter );
		add( closure, new JmxPsProcessProvider(getConnections(), name, finder) );
	}
	
	public void vms( String name, Object filter, Closure closure ) throws Exception {
		final ParameterizedRunnableWithResult<Boolean,ProcessStatus> finder = createProcessFinder( filter );
		add( closure, new JmxVmProcessProvider(getConnections(), name, finder) );
	}

	private ParameterizedRunnableWithResult<Boolean,ProcessStatus> createProcessFinder( Object filter ) {
		if( filter instanceof Closure ) {
			final Closure fc = (Closure)filter;
			return Closures.toRunnableWithBoolean( fc, ProcessStatus.class );
		}
		else {
			final List<Pattern> patterns = RegexUtil.toPatternCollection(filter);
			return new ParameterizedRunnableWithResult<Boolean,ProcessStatus>() {
				public Boolean run( ProcessStatus status ) throws Exception {
					return RegexUtil.matchesAny( status.command, patterns );
				}
			};
		}
		
	}
	public void remote( String name, String url, Closure closure ) throws Exception {
		add( closure, new JmxRemoteProcessProvider(getConnections(), name, url) );
	}
	public void remote( String name, String host, int port, Closure closure ) throws Exception {
		String url = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
		remote( name, url, closure );
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

	private synchronized JmxConnections getConnections() {
		BoxContext context = BoxContext.getCurrentContext();
		JmxConnections connections = context.getBeans().get( "jmx.connections", JmxConnections.class );
		if( connections == null ) {
			connections = new JmxConnections();
			connections.setBuilder( new VmJmxConnectionBuilder() );
			context.getBeans().put( "jmx.connections", connections );
		}
		return connections;
	}
}
