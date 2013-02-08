package org.scriptbox.horde.metrics.probe;

import java.util.ArrayList;
import java.util.List;

import org.scriptbox.horde.metrics.mbean.Exposable;

public class BasicProbe extends Probe {

	private List<Exposable> metrics = new ArrayList<Exposable>();
	
	public BasicProbe( String name, String description ) {
		super( name, description );
	}
	
	@Override
	public List<Exposable> getExposables() {
		return metrics;
	}

	public void add( Exposable exposable ) {
		metrics.add( exposable );
	}
}
