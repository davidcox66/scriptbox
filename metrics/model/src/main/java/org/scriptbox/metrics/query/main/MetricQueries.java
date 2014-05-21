package org.scriptbox.metrics.query.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.model.MetricStore;
import org.scriptbox.metrics.model.MetricTree;
import org.scriptbox.metrics.query.exp.AverageQueryExp;
import org.scriptbox.metrics.query.exp.BinaryQueryExp;
import org.scriptbox.metrics.query.exp.BottomAverageQueryExp;
import org.scriptbox.metrics.query.exp.BottomQueryExp;
import org.scriptbox.metrics.query.exp.DeltaQueryExp;
import org.scriptbox.metrics.query.exp.DiffQueryExp;
import org.scriptbox.metrics.query.exp.DivideQueryExp;
import org.scriptbox.metrics.query.exp.DivisorQueryExp;
import org.scriptbox.metrics.query.exp.InvertQueryExp;
import org.scriptbox.metrics.query.exp.MaxQueryExp;
import org.scriptbox.metrics.query.exp.MetricQueryExp;
import org.scriptbox.metrics.query.exp.MinQueryExp;
import org.scriptbox.metrics.query.exp.MultiplierQueryExp;
import org.scriptbox.metrics.query.exp.MultiplyQueryExp;
import org.scriptbox.metrics.query.exp.NaryQueryExp;
import org.scriptbox.metrics.query.exp.PerSecondQueryExp;
import org.scriptbox.metrics.query.exp.TopAverageQueryExp;
import org.scriptbox.metrics.query.exp.TopQueryExp;
import org.scriptbox.metrics.query.exp.TotalPerSecondQueryExp;
import org.scriptbox.metrics.query.exp.TotalQueryExp;
import org.scriptbox.metrics.query.exp.TreePathQueryExp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import groovy.lang.Closure;

public class MetricQueries {
	static final Logger LOGGER = LoggerFactory.getLogger(MetricQueries.class);

	public static Map<? extends MetricProvider, ? extends MetricRange> providers(
		MetricQueryContext ctx, 
		MetricQueryExp expr) 
			throws Exception 
	{
		Object obj = expr.evaluate(ctx);
		if( LOGGER.isDebugEnabled() ) {
			LOGGER.debug( "providers: evaluated expr=" + expr + ", result=" + obj );
		}
		if( obj == null ) { 
			throw new MetricException( "Query did not match any metrics: " + expr);
		}
		Collection<MetricProvider> providers = MetricQueries.toCollection(obj, MetricProvider.class);
		return ctx.getStore().getAllMetrics(providers, ctx);
	}

	public static <X extends MetricProvider> X provider(
		MetricQueryContext ctx, 
		MetricQueryExp expr) 
			throws Exception 
	{
		Object obj = expr.evaluate(ctx);
		if( LOGGER.isDebugEnabled() ) {
			LOGGER.debug( "providers: evaluated expr=" + expr + ", result=" + obj );
		}
		if (obj instanceof Collection) {
			Collection<X> providers = (Collection<X>) obj;
			if (providers.size() != 1) {
				LOGGER.error( "provider: found multiple providers for expression: " + expr + ", providers: " + providers ); 
				throw new RuntimeException( "Expected only a single metric provider - expression:" + expr + ", size:" + providers.size());
			}
			return providers.iterator().next();
		} 
		else {
			return (X) obj;
		}
	}

	public static <X> Collection<X> toCollection(Object obj, Class<X> cls) {
		if (obj != null) {
			if (obj instanceof Collection) {
				return (Collection<X>) obj;
			} else {
				List<X> ret = new ArrayList<X>(1);
				ret.add((X) obj);
				return ret;
			}
		}
		return null;
	}

	public static Map<? extends MetricProvider, ? extends MetricRange> query( MetricStore store, MetricTree tree, MetricQueryExp exp, Date start, Date end, int chunk ) 
		throws Exception
	{
	    if( LOGGER.isDebugEnabled() ) {
		    LOGGER.debug( "query: tree=" + tree + ", expression=" + exp + ", start=" + start + ", end=" + end + ", chunk=" + chunk );
	    }
	    long begin = LOGGER.isInfoEnabled() ? System.currentTimeMillis() : 0;
	    
	    MetricQueryContext ctx = new MetricQueryContext( store, tree, start.getTime(), end.getTime(), chunk );
	    try {
		    return providers( ctx, exp );
	    }
	    finally {
	    	if( begin != 0 ) {
	    		long millis = System.currentTimeMillis() - begin;
	    		LOGGER.info( "query: time=" + millis + ", tree=" + tree + ", expression=" + exp + ", start=" + start + ", end=" + end + ", chunk=" + chunk ); 
	    	}
	    }
	}

	public static MetricQueryExp source(String pattern) {
		return source( Pattern.compile(pattern) );
	}

	public static MetricQueryExp source( Pattern pattern ) {
	    return new TreePathQueryExp( "source", pattern );
	}

	public static MetricQueryExp instance(String pattern) {
		return instance( Pattern.compile(pattern) );
	}

	public static MetricQueryExp instance( Pattern pattern ) {
	    return new TreePathQueryExp( "instance", pattern );
	  }

	public static MetricQueryExp attr(String pattern) {
		return attr( Pattern.compile(pattern) );
	}

	public static MetricQueryExp attr( Pattern pattern ) {
	    return new TreePathQueryExp( "attribute", pattern );
	}

	public static MetricQueryExp metric(String pattern) {
		return metric( Pattern.compile(pattern) );
	}

	public static MetricQueryExp metric( Pattern pattern ) {
	    return new TreePathQueryExp( "metric", pattern );
	}
	
	public static MetricQueryExp list( String name, MetricQueryExp... expressions ) {
	    return new NaryQueryExp( name, "list", expressions ) {
	    	public Object process(List<Object> results, MetricQueryContext ctx) {
	    		return results;
	    	}
	    }; 
	}

	public static MetricQueryExp filter( String name, final Closure closure, MetricQueryExp... expressions ) {
	    return new NaryQueryExp( name, "filter", expressions ) {
	    	public Object process(List<Object> results, MetricQueryContext ctx) {
	    		Set ret = new HashSet(); 
	    		for( Object result : results ) {
	    			if( coerceToBoolean(closure.call(result)) ) {
	    				ret.add( result );
	    			}
	    		}
	    		return ret;
	    	}
	    }; 
	}
	public static MetricQueryExp and( String name, MetricQueryExp... expressions ) {
	    return new NaryQueryExp( name, "and", expressions ) {
	    	public Object process(List<Object> results, MetricQueryContext ctx) {
	    		Set ret = null; 
	    		for( Object result : results ) {
	    			if( ret == null ) {
	    				ret = new HashSet((Collection)result);
	    			}
	    			else {
	    				ret.retainAll((Collection)result);
	    			}
	    		}
	    		return ret != null ? ret : new HashSet();
	    	}
	    }; 
	}

	public static MetricQueryExp or( String name, MetricQueryExp... expressions ) {
	    return new NaryQueryExp( name, "or", expressions ) {
	    	public Object process(List<Object> results, MetricQueryContext ctx) {
	    		Set ret = new HashSet(); 
	    		for( Object result : results ) {
    				ret.addAll((Collection)result);
	    		}
	    		return ret;
	    	}
	    }; 
	}

	public static MetricQueryExp not( String name, MetricQueryExp lhs, MetricQueryExp rhs ) {
	    return new BinaryQueryExp( name, "not", lhs, rhs ) {
	    	public Object process(Object lresult, Object rresult, MetricQueryContext ctx) {
	    		Set metrics = new HashSet((Collection)lresult); 
	    		metrics.removeAll( (Collection)rresult );
	    		return metrics;
	    	}
	    }; 
	}

	public static MetricQueryExp total( String name, MetricQueryExp child ) {
		return new TotalQueryExp(name, child);
	}

	public static MetricQueryExp average( String name, MetricQueryExp child) {
		return new AverageQueryExp(name, child);
	}

	public static MetricQueryExp min( String name, MetricQueryExp child) {
		return new MinQueryExp(name,child);
	}

	public static MetricQueryExp max( String name, MetricQueryExp child) {
		return new MaxQueryExp(name, child);
	}

	public static MetricQueryExp diff(String name, int chunk, MetricQueryExp lhs, MetricQueryExp rhs) {
		return new DiffQueryExp(name, chunk, lhs, rhs);
	}
	
	public static MetricQueryExp divisor(String name, int chunk, MetricQueryExp totals, MetricQueryExp divisors) {
		return new DivisorQueryExp(name, chunk, totals, divisors);
	}
	
	public static MetricQueryExp multiplier(String name, int chunk, MetricQueryExp totals, MetricQueryExp multiples) {
		return new MultiplierQueryExp(name, chunk, totals, multiples);
	}

	public static MetricQueryExp delta(String name, MetricQueryExp exp) {
		return new DeltaQueryExp(name, exp);
	}

	public static MetricQueryExp persecond(String name, MetricQueryExp exp) {
		return new PerSecondQueryExp(name, exp);
	}

	public static MetricQueryExp tps(String name, MetricQueryExp exp) {
		return new TotalPerSecondQueryExp(name, exp);
	}

	public static MetricQueryExp divide(String name, float divisor, MetricQueryExp exp) {
		return new DivideQueryExp(name, divisor, exp);
	}

	public static MetricQueryExp multiply(String name, float multiplier, MetricQueryExp exp) {
		return new MultiplyQueryExp(name, multiplier, exp);
	}

	public static MetricQueryExp invert(String name, float from, MetricQueryExp exp) {
		return new InvertQueryExp(name, from, exp);
	}

	public static MetricQueryExp top(String name, int count, MetricQueryExp exp) {
		return new TopQueryExp(name, count, exp);
	}

	public static MetricQueryExp bottom(String name, int count, MetricQueryExp exp) {
		return new BottomQueryExp(name, count, exp);
	}

	public static MetricQueryExp topavg(String name, int count, MetricQueryExp exp) {
		return new TopAverageQueryExp(name, count, exp);
	}

	public static MetricQueryExp bottomavg(String name, int count, MetricQueryExp exp) {
		return new BottomAverageQueryExp(name, count, exp);
	}
	
   public static boolean coerceToBoolean( Object obj ) {
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
