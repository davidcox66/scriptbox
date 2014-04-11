package org.scriptbox.horde.metrics.probe;

import groovy.lang.Closure;
import org.scriptbox.box.groovy.Closures;
import org.scriptbox.metrics.beans.AverageMetric;
import org.scriptbox.metrics.beans.AvgTransactionTime;
import org.scriptbox.metrics.beans.CountMetric;

public class ThresholdProbe extends BasicProbe {

	private AvgTransactionTime average;
	private CountMetric count;
	private int threshold;
	private AverageMetric percent;
	
	public ThresholdProbe( String name, String description, int threshold ) {
		super( name, description );
		average = new AvgTransactionTime( "probe-avg" ) ;
		count = new CountMetric( "probe-count", description + " count" ) ;
		percent = new AverageMetric( "probe-percent", description + " percent" );
		add( average );
		add( count );
		add( percent );
		this.threshold = threshold;
	}
	
	public boolean watch( Closure closure, Closure handler ) throws Throwable {
		boolean ret = true;
		long start = System.currentTimeMillis();
		try {
			ret = Closures.coerceToBoolean( closure.call() );
			ret = check( ret, start, handler );
		}
		catch( VirtualMachineError ex ) {
			throw ex;
		}
		catch( Throwable ex ) {
			check( false, start, handler );
			throw ex;
		}
		finally {
			percent.record(true, 0);
		}
		return ret;
	}
	
	private boolean check( boolean ret, long start, Closure handler ) {
		long end = System.currentTimeMillis();
		long time = end - start;
		if( time >= threshold ) {
			average.record(ret, time);
			count.increment();
			percent.increment();
			if( handler != null ) {
				ret = Closures.coerceToBoolean( handler.call( new Object[] { time, ret } ) );
			}
		}
		return ret;
	}
}
