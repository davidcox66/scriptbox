package org.scriptbox.horde.metrics.probe;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.List;

import org.scriptbox.horde.metrics.ActionMetric;
import org.scriptbox.horde.metrics.AvgTransactionTime;
import org.scriptbox.horde.metrics.CountMetric;
import org.scriptbox.horde.metrics.mbean.Exposable;

public class TimingProbe extends Probe {

	private AvgTransactionTime average;
	private CountMetric count;
	private CountMetric failures;
	private List<Exposable> metrics;
	
	public TimingProbe( String name, String description ) {
		super( name, description );
		average = new AvgTransactionTime( "avg" ) ;
		count = new CountMetric( "count", description + " count" ) ;
		failures = new CountMetric( "fail", description + " failures" ) ;
		metrics = new ArrayList<Exposable>();
		metrics.add( average );
		metrics.add( count );
		metrics.add( failures );
	}
	
	@Override
	public List<Exposable> getExposables() {
		return metrics;
	}

	@Override
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
