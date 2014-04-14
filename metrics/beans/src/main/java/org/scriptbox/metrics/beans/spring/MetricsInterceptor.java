package org.scriptbox.metrics.beans.spring;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.scriptbox.metrics.beans.AvgTransactionTime;
import org.scriptbox.metrics.beans.DeviationMetric;
import org.scriptbox.metrics.beans.FailureCount;
import org.scriptbox.metrics.beans.MaxTransactionTime;
import org.scriptbox.metrics.beans.MinTransactionTime;
import org.scriptbox.metrics.beans.RecordableMetric;
import org.scriptbox.metrics.beans.TransactionCount;
import org.scriptbox.metrics.beans.TransactionsPerSecond;
import org.scriptbox.metrics.beans.distro.DistroCountMetric;
import org.scriptbox.metrics.beans.distro.DistroPercentMetric;
import org.scriptbox.metrics.beans.mbean.AbstractDynamicExposableMBean;
import org.scriptbox.metrics.beans.mbean.DynamicExposableMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsInterceptor implements MethodInterceptor, MetricsInterceptorMBean {

	private static final Logger logger = LoggerFactory.getLogger( MetricsInterceptor.class );
	
    private static final int[] DISTROS = new int[] { 25, 50, 100, 250, 500, 750, 1000, 2000, 4000, 8000, 16000, 32000, -1 };
	private Map<Object,InvocationContext> contexts = new HashMap<Object,InvocationContext>();
	private boolean enabled;
	
	private static class InvocationContext {
		private List<RecordableMetric> metrics = new ArrayList<RecordableMetric>();
		private String objectName;
		private InvocationContext parent;
		private List<DynamicExposableMBean> mbeans = new ArrayList<DynamicExposableMBean>();
		
		InvocationContext( String objectName, InvocationContext parent ) {
            this.objectName = objectName;
		    this.parent = parent;
		    DynamicExposableMBean mbean = new DynamicExposableMBean( objectName );
		    mbeans.add( mbean );
		    
	        addMetric( mbean, new TransactionsPerSecond() );
	        addMetric( mbean, new TransactionCount() );
	        addMetric( mbean, new MinTransactionTime() );
	        addMetric( mbean, new MaxTransactionTime() );
	        addMetric( mbean, new AvgTransactionTime() );
	        addMetric( mbean, new FailureCount() );
	        addMetric( mbean, new DeviationMetric() );
	        
	        addDistroCountMetrics();
	        addDistroPercentMetrics();
	        
	    	for( AbstractDynamicExposableMBean bean : mbeans ) {
	    		try {
		    		bean.register();
		    	}
		        catch( Exception ex ) {
		        	logger.error( "Error registering mbean: '" + bean.getObjectName() + "'", ex );
		        }
	        }
		}
		
		public void record( boolean success, long millis ) {
			for( RecordableMetric metric : metrics ) {
				metric.record(success, millis);
			}
			if( parent != null ) {
				parent.record( success, millis );
			}
		}
		
	    private void addDistroCountMetrics() {
	    	addDistroCountMetrics( DISTROS );
	    }
	    
	    private void addDistroCountMetrics( int... distros ) {
	    	DynamicExposableMBean distroCount = new DynamicExposableMBean( objectName, "distro=count" );
	        long prev = 0;
	        for( long dist : distros ) {
	        	addMetric( distroCount, new DistroCountMetric("distro", prev, dist ) );
	        	prev = dist + 1;
	        }
	    	mbeans.add( distroCount );
	    }
	    
	    private void addDistroPercentMetrics() {
	    	addDistroPercentMetrics( DISTROS );
	    }
	    
	    private void addDistroPercentMetrics( int... distros ) {
	    	DynamicExposableMBean distroPercent = new DynamicExposableMBean( objectName, "distro=percent" );
	        long prev = 0;
	        for( long dist : distros ) {
	        	addMetric( distroPercent, new DistroPercentMetric("distro", prev, dist ) );
	        	prev = dist + 1;
	        }
	        mbeans.add( distroPercent );
	    }
	    
	    private void addMetric( AbstractDynamicExposableMBean bean, RecordableMetric metric ) {
	        bean.addExposable( metric );
	        metrics.add( metric );
	    }
	}
	
	public MetricsInterceptor() {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = new ObjectName("SpringMetricsInterceptor:type=MethodInterceptor");		
			mbs.registerMBean(this, name);		
		}
		catch( Exception ex ) {
			logger.error( "Error registering MetricsInterceptor", ex );
		}
	}
	
	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
	}
	
	public boolean getEnabled() {
		return enabled;
	}
	
	public  Object invoke(MethodInvocation invocation) throws Throwable {
		if( enabled ) {
			long start = System.currentTimeMillis();
			
			try {
				Object rval = invocation.proceed();
				long end = System.currentTimeMillis();
				record( invocation, true, end - start );
				return rval;
			}
			catch (Throwable ex) {
				long end = System.currentTimeMillis();
				record( invocation, false, end - start );
				throw ex;
			}
		}
		else {
			return invocation.proceed();
		}
	}

	public void record( MethodInvocation invocation, boolean success, long millis ) {
		InvocationContext ctx = getInvocationContext( invocation.getMethod() );
		ctx.record( success, millis );
	}
	
	synchronized private InvocationContext getInvocationContext( Method method ) {
		InvocationContext methodContext = contexts.get( method );
		if( methodContext == null ) {
			String classShortName = getClassShortName(method.getDeclaringClass());
			String methodName = getMethodName(method);
			InvocationContext classContext = contexts.get( method.getDeclaringClass() );
			if( classContext == null ) {
				classContext = new InvocationContext( "SpringMetrics:class=" + classShortName + ",method=all", null );
				contexts.put( method.getDeclaringClass(), classContext );
			}
			methodContext = new InvocationContext( "SpringMetrics:class=" + classShortName +  ",method=" + methodName, classContext );
			contexts.put( method, methodContext );
		}
		return methodContext;
	}
	
	protected String getMethodName(Method method) {
		StringBuilder builder = new StringBuilder( getMethodShortName(method) );
		Class[] clss = method.getParameterTypes();
		for( int i=0 ; i < clss.length ; i++ ) {
			Class cls = clss[i];
            builder.append( "_" );
			builder.append( getClassShortName(cls) );
		}
		return builder.toString();
	}

    
	private String getMethodShortName( Method method ) {
		// return getClassShortName(method.getDeclaringClass()) + "." + method.getName();
		return method.getName();
	}
	
    @SuppressWarnings("rawtypes")
	private String getClassShortName( Class cls ) {
		String clsName = cls.getName();
		int pos = clsName.lastIndexOf(".");
		if( pos != -1 ) {
    		clsName = clsName.substring(pos+1);
		}
		return clsName;
	}
}
