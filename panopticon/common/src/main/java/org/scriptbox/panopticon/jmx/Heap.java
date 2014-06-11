package org.scriptbox.panopticon.jmx;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.jmx.proc.JmxProcess;
import org.scriptbox.panopticon.capture.CaptureResult;

public class Heap implements Storable {

	private long init;
	private long committed;
	private long max;
	private long used;
	
	public Heap( long init, long committed, long max, long used ) {
		this.init = init;
		this.committed = committed;
		this.max = max;
		this.used = used;
	}
	
	public Collection<CaptureResult> getResults() {
		JmxProcess proc = ExecContext.getNearestEnclosing(JmxProcess.class);
		List<CaptureResult> ret = new ArrayList<CaptureResult>(4);
		long ts = System.currentTimeMillis();
		ret.add( new CaptureResult(proc, "Memory,type=heap", "init", init, ts) );
		ret.add( new CaptureResult(proc, "Memory,type=heap", "committed", committed, ts) );
		ret.add( new CaptureResult(proc, "Memory,type=heap", "max", max, ts) );
		ret.add( new CaptureResult(proc, "Memory,type=heap", "used", used, ts) );
		return ret;
	}
	
	public Heap diff( Heap other ) {
		if( other != null ) {
			return new Heap( 
				getInit(),
				getCommitted() - other.getCommitted(),
				getMax(),
				getUsed() - other.getUsed() );
		}
		return this;
	}
		
	public long getInit() {
		return init;
	}

	public long getCommitted() {
		return committed;
	}

	public long getMax() {
		return max;
	}

	public long getUsed() {
		return used;
	}
	
	public String toString() {
		return "Memory{ init=" + init + ", committed=" + committed + ", max=" + max + ", used=" + used + " }";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (committed ^ (committed >>> 32));
		result = prime * result + (int) (init ^ (init >>> 32));
		result = prime * result + (int) (max ^ (max >>> 32));
		result = prime * result + (int) (used ^ (used >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Heap other = (Heap) obj;
		if (committed != other.committed)
			return false;
		if (init != other.init)
			return false;
		if (max != other.max)
			return false;
		if (used != other.used)
			return false;
		return true;
	}

	
}
