package org.scriptbox.panopticon.jmx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.jmx.proc.JmxProcess;
import org.scriptbox.panopticon.capture.CaptureResult;

public class GarbageCollection implements Storable {
	
	private String type;
	private String name;
	private long count;
	private long time;
	
	public Collection<CaptureResult> getResults() {
		JmxProcess proc = ExecContext.getNearestEnclosing(JmxProcess.class);
		List<CaptureResult> ret = new ArrayList<CaptureResult>(2);
		long ts = System.currentTimeMillis();
		ret.add( new CaptureResult(proc, "GC,type=" + type, "count", count, ts) );
		ret.add( new CaptureResult(proc, "GC,type=" + type, "time", time, ts) );
		return ret;
	}
	
	public GarbageCollection diff( GarbageCollection other ) {
		if( other != null ) {
			GarbageCollection ret = new GarbageCollection();
			ret.setType( getType() );
			ret.setName( getName() );
			ret.setCount( getCount() - other.getCount() );
			ret.setTime( getTime() - other.getTime() );
			return ret;
		}
		return this;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	
	public String toString() {
		return "GC{ type=" + type + ", name=" + name + ", count=" + count + ", time=" + time + " }";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (count ^ (count >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (int) (time ^ (time >>> 32));
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		GarbageCollection other = (GarbageCollection) obj;
		if (count != other.count)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (time != other.time)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
}