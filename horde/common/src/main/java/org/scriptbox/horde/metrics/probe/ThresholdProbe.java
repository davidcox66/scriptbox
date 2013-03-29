package org.scriptbox.horde.metrics.probe;

import groovy.lang.Closure;

import org.scriptbox.box.groovy.Closures;
import org.scriptbox.horde.metrics.AverageMetric;
import org.scriptbox.horde.metrics.AvgTransactionTime;
import org.scriptbox.horde.metrics.CountMetric;

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
			long end = System.currentTimeMillis();
			long time = end - start;
			if( time >= threshold ) {
				average.record(true, time);
				count.increment();
				percent.increment();
				if( handler != null ) {
					ret = Closures.coerceToBoolean( handler.call( new Object[] { time, true } ) );
				}
			}
		}
		catch( VirtualMachineError ex ) {
			throw ex;
		}
		catch( Throwable ex ) {
			long end = System.currentTimeMillis();
			long time = end - start;
			if( time >= threshold ) {
				average.record(false, time );
				count.increment();
				percent.increment();
				if( handler != null ) {
					handler.call( new Object[] { time, false } );
				}
			}
			throw ex;
		}
		finally {
			percent.record(true, 0);
		}
		return ret;
	}
}
