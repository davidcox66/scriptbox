package org.scriptbox.horde.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scriptbox.horde.metrics.ActionAware;
import org.scriptbox.horde.metrics.ActionMetric;
import org.scriptbox.horde.metrics.AvgTransactionTime;
import org.scriptbox.horde.metrics.FailureCount;
import org.scriptbox.horde.metrics.MaxTransactionTime;
import org.scriptbox.horde.metrics.MinTransactionTime;
import org.scriptbox.horde.metrics.TransactionCount;
import org.scriptbox.horde.metrics.TransactionsPerSecond;
import org.scriptbox.horde.metrics.mbean.AbstractDynamicExposableMBean;
import org.scriptbox.horde.metrics.mbean.ActionDynamicMetricMBean;
import org.scriptbox.horde.metrics.mbean.Exposable;
import org.scriptbox.horde.metrics.probe.Probe;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Action {

	private static final Logger LOGGER = LoggerFactory.getLogger( Action.class );
	
	private static ThreadLocal<Map<String,Object>> attributes = new ThreadLocal<Map<String,Object>>() {
        public Map<String,Object> initialValue() {
            return new HashMap<String,Object>();
        }
    };

    private String name;
	private ActionScript script;
    private ParameterizedRunnableWithResult<Integer,List> iterations;
    private ParameterizedRunnableWithResult<Boolean,List> init;
    private ParameterizedRunnableWithResult<Boolean,List> run;
    private ParameterizedRunnableWithResult<Boolean,List> pre;
    private ParameterizedRunnableWithResult<Boolean,List> post;
    
    private Map<String,ActionMetric> preMetrics = new HashMap<String,ActionMetric>();
    private Map<String,ActionMetric> metrics = new HashMap<String,ActionMetric>();
    private Map<String,ActionMetric> postMetrics = new HashMap<String,ActionMetric>();

    private AbstractDynamicExposableMBean mbean;
    private Map<String,AbstractDynamicExposableMBean> probes = new HashMap<String,AbstractDynamicExposableMBean>();
    
    public Action( ActionScript script, String name ) {
        this.script = script;
        this.name = name;
        this.mbean = new ActionDynamicMetricMBean( this );
    }
    
    public ActionScript getActionScript() {
    	return script;
    }
    public String getName() {
    	return name;
    }
    
    public ParameterizedRunnableWithResult<Integer,List>  getIterations() {
		return iterations;
	}

	public void setIterations(ParameterizedRunnableWithResult<Integer,List>  iterations) {
		this.iterations = iterations;
	}

    public ParameterizedRunnableWithResult<Boolean,List>  getInit() {
		return init;
	}

	public void setInit(ParameterizedRunnableWithResult<Boolean,List>  init) {
		this.init = init;
	}

	public ParameterizedRunnableWithResult<Boolean,List>  getRun() {
		return run;
	}

	public void setRun(ParameterizedRunnableWithResult<Boolean,List>  run) {
		this.run = run;
	}

	public ParameterizedRunnableWithResult<Boolean,List>  getPre() {
		return pre;
	}

	public void setPre(ParameterizedRunnableWithResult<Boolean,List>  pre) {
		this.pre = pre;
	}

	public ParameterizedRunnableWithResult<Boolean,List>  getPost() {
		return post;
	}

	public void setPost(ParameterizedRunnableWithResult<Boolean,List>  post) {
		this.post = post;
	}

	public void initialize() throws Exception {
    	
        if( run == null ) {  
            throw new RuntimeException( "No run block defined for action");
        }
        if( init != null ) {
        	init.run( script.getBoxScript().getArguments() );
        }
        
        if( pre != null ) {
            addPreMetric( new AvgTransactionTime("pre") );
        }
        
        addRunMetric( new TransactionsPerSecond() );
        addRunMetric( new TransactionCount() );
        addRunMetric( new MinTransactionTime() );
        addRunMetric( new MaxTransactionTime() );
        addRunMetric( new AvgTransactionTime() );
        addRunMetric( new FailureCount() );
        
        if( post != null ) {
            addPostMetric( new AvgTransactionTime("post") );
        }
    }
	
    void addRunProbe( Probe probe ) {
    	List<Exposable> exposables = probe.getExposables();
    	ActionDynamicMetricMBean bean = new ActionDynamicMetricMBean(this, "probe=" + probe.getName() );
    	for( Exposable exposable : exposables ) {
    		if( exposable instanceof ActionAware ) {
    			((ActionAware)exposable).init( this );
    		}
	        bean.addExposable( exposable );
    	}
    	probes.put( probe.getName(), bean );
    }
    
    void addRunMetric( ActionMetric metric ) {
        addActionMetric( metrics, metric );
    }
    void addPreMetric( ActionMetric metric ) {
        addActionMetric( preMetrics, metric );
    }
    void addPostMetric( ActionMetric metric ) {
        addActionMetric( postMetrics, metric );
    }
    private void addActionMetric( Map<String,ActionMetric> mets, ActionMetric metric ) {
        metric.init( this );
        mbean.addExposable( metric );
        mets.put( metric.getName(), metric );
    }
   
    public Object getTestAttribute( String name ) {
       return getVariable( name );
    } 
    public void setTestAttribute( String name, Object value ) {
    	setVariable( name, value );
    }
    
    public Object getVariable( String name ) {
       return attributes.get().get( name ); 
    } 
    public void setVariable( String name, Object value ) {
        attributes.get().put( name, value );
    }

    public void callAllAndCollectMetrics() throws Throwable {
       int count = 1;
       if( iterations != null ) {
    	   List<String> arguments = script.getBoxScript().getArguments();
    	   count = iterations.run( arguments );
       }
       for( int i=0 ; i < count ; i++ ) { 
	       callAndCollectMetrics( preMetrics, pre );
	       callAndCollectMetrics( metrics, run );
	       callAndCollectMetrics( postMetrics, post );
       }
    }
    
    public void callAndCollectMetrics( Map<String,ActionMetric> met, ParameterizedRunnableWithResult<Boolean,List> closure ) 
    	throws Throwable
    {
        List<String> arguments = script.getBoxScript().getArguments();
        if( closure != null ) {
            long before = System.currentTimeMillis(); 
            try {
               boolean ret = closure.run( arguments );
               ActionMetric.collectAll( met, ret, System.currentTimeMillis() - before );
            }
            catch( VirtualMachineError ex ) {
                throw ex;
            }
            catch( Throwable ex ) {
               ActionMetric.collectAll( met, false, System.currentTimeMillis() - before );
               script.callGlobalErrorHandlers( ex );
            }
        }
    }
    
    void collectMetrics( boolean success, long millis ) {
        ActionMetric.collectAll( metrics, success, millis );
    }
    public void registerMBeans() throws Exception {
    	mbean.register();
    	for( AbstractDynamicExposableMBean bean : probes.values() ) {
    		bean.register();
    	}
    }    
    
    public void unregisterMBeans() throws Exception {
    	mbean.unregister();
    	for( AbstractDynamicExposableMBean bean : probes.values() ) {
    		bean.unregister();
    	}
    }
    
    public String toString() {
    	return "Action{ name=" + name + " }";
    }
}
