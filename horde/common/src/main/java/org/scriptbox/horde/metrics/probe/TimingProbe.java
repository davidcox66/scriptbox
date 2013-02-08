package org.scriptbox.horde.metrics.probe;

import groovy.lang.Closure;

import org.scriptbox.horde.metrics.AvgTransactionTime;
import org.scriptbox.horde.metrics.CountMetric;

public class TimingProbe extends BasicProbe {

	private AvgTransactionTime average;
	private CountMetric count;
	private CountMetric failures;
	
	public TimingProbe( String name, String description ) {
		super( name, description );
		average = new AvgTransactionTime( "probe-avg" ) ;
		count = new CountMetric( "probe-count", description + " count" ) ;
		failures = new CountMetric( "probe-fail", description + " failures" ) ;
		add( average );
		add( count );
		add( failures );
	}
	
	public void measure( Closure closure ) throws Throwable {
		long start = System.currentTimeMillis();
		try {
			closure.run();
			long end = System.currentTimeMillis();
			average.record(true, end - start);
			count.increment();
		}
		catch( VirtualMachineError ex ) {
			throw ex;
		}
		catch( Throwable ex ) {
			long end = System.currentTimeMillis();
			average.record(false, end - start);
			count.increment();
			failures.increment();
			throw ex;
		}
	}
}
