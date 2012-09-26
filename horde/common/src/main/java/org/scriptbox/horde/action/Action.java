package org.scriptbox.horde.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

import org.scriptbox.horde.metrics.AbstractMetric;
import org.scriptbox.horde.metrics.ActionMetric;
import org.scriptbox.horde.metrics.AvgTransactionTime;
import org.scriptbox.horde.metrics.FailureCount;
import org.scriptbox.horde.metrics.MaxTransactionTime;
import org.scriptbox.horde.metrics.MinTransactionTime;
import org.scriptbox.horde.metrics.TransactionCount;
import org.scriptbox.horde.metrics.TransactionsPerSecond;
import org.scriptbox.util.common.collection.CollectionUtil;
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
    private ParameterizedRunnableWithResult<Boolean,List> init;
    private ParameterizedRunnableWithResult<Boolean,List> run;
    private ParameterizedRunnableWithResult<Boolean,List> pre;
    private ParameterizedRunnableWithResult<Boolean,List> post;
    
    private Map<ObjectName,ActionMetric> preMetrics = new HashMap<ObjectName,ActionMetric>();
    private Map<ObjectName,ActionMetric> metrics = new HashMap<ObjectName,ActionMetric>();
    private Map<ObjectName,ActionMetric> postMetrics = new HashMap<ObjectName,ActionMetric>();

    public Action( ActionScript script, String name ) {
        this.script = script;
        this.name = name;
    }
    
    public ActionScript getActionScript() {
    	return script;
    }
    public String getName() {
    	return name;
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
        if( CollectionUtil.isEmpty(metrics) && 
        	CollectionUtil.isEmpty(preMetrics) && 
        	CollectionUtil.isEmpty(postMetrics) )  {
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
    private void addActionMetric( Map<ObjectName,ActionMetric> mets, ActionMetric metric ) {
        metric.init( this );
        AbstractMetric.addMetric( mets, metric );
    }
   
    public Object getTestAttribute( String name ) {
       return attributes.get().get( name ); 
    } 
    public void setTestAttribute( String name, Object value ) {
        attributes.get().put( name, value );
    }

    public void callAllAndCollectMetrics() throws Throwable {
       callAndCollectMetrics( preMetrics, pre );
       callAndCollectMetrics( metrics, run );
       callAndCollectMetrics( postMetrics, post );
    }
    public void callAndCollectMetrics( Map<ObjectName,ActionMetric> met, ParameterizedRunnableWithResult<Boolean,List> closure ) 
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
               throw ex;
            }
        }
    }
    
    void collectMetrics( boolean success, long millis ) {
        ActionMetric.collectAll( metrics, success, millis );
    }
    public void registerMBeans() throws Exception {
    	AbstractMetric.registerAll(preMetrics);
    	AbstractMetric.registerAll(metrics);
    	AbstractMetric.registerAll(postMetrics);
    }    
    
    public void unregisterMBeans() {
    	AbstractMetric.unregisterAll(preMetrics);
    	AbstractMetric.unregisterAll(metrics);
    	AbstractMetric.unregisterAll(postMetrics);
    }
    
    public String toString() {
    	return "Action{ name=" + name + " }";
    }
}
