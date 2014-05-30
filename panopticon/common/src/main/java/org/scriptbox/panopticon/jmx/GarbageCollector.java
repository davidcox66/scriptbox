package org.scriptbox.panopticon.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.jmx.conn.JmxConnection;
import org.scriptbox.box.jmx.proc.JmxProcess;
import org.scriptbox.panopticon.capture.CaptureResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class GarbageCollector {

	private static final Logger LOGGER = LoggerFactory.getLogger( GarbageCollector.class );
	
	enum Type {
		YOUNG("minor"),
		OLD("full");
		
		private String text;
		
		private Type( String text ) {
			this.text = text;
		}
		public String getText() {
			return text;
		}
	};
	
	public static class Info implements Storable {
		
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
			Info other = (Info) obj;
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
	private static Set<GarbageCollector> ALL = new HashSet<GarbageCollector>();
    private static List<String> ATTRS = new ArrayList<String>();
	
	
	private Type type;
	private String vm;
	private String option;
	private String name;
	
    static {
        // Oracle (Sun) HotSpot
    	ALL.add( new GarbageCollector(Type.YOUNG,"Hotspot", "-XX:+UseSerialGC", "Copy") );
    	ALL.add( new GarbageCollector(Type.YOUNG,"Hotspot", "-XX:+UseParNewGC", "ParNew") );
    	ALL.add( new GarbageCollector(Type.YOUNG,"Hotspot", "-XX:+UseParallelGC", "PS Scavenge") );
    	
    	ALL.add( new GarbageCollector(Type.OLD,"Hotspot", "-XX:+UseSerialGC", "MarkSweepCompact") );
    	ALL.add( new GarbageCollector(Type.OLD,"Hotspot", "-XX:+UseParallelGC", "PS MarkSweep") ); // (-XX:+UseParallelOldGC or -XX:+UseParallelOldGCCompacting)
    	ALL.add( new GarbageCollector(Type.OLD,"Hotspot", "-XX:+UseConcMarkSweepGC", "ConcurrentMarkSweep") );
 
        // Oracle (BEA) JRockit
    	ALL.add( new GarbageCollector(Type.YOUNG,"JRockit", "-XgcPrio:pausetime", "Garbage collection optimized for short pausetimes Young Collector") );
    	ALL.add( new GarbageCollector(Type.YOUNG,"JRockit", "-XgcPrio:throughput", "Garbage collection optimized for throughput Young Collector") );
    	ALL.add( new GarbageCollector(Type.YOUNG,"JRockit", "-XgcPrio:deterministic", "Garbage collection optimized for deterministic pausetimes Young Collector") );
    	
    	ALL.add( new GarbageCollector(Type.OLD,"JRockit", "-XgcPrio:pausetime", "Garbage collection optimized for short pausetimes Old Collector") );
    	ALL.add( new GarbageCollector(Type.OLD,"JRockit", "-XgcPrio:throughput", "Garbage collection optimized for throughput Old Collector") );
    	ALL.add( new GarbageCollector(Type.OLD,"JRockit", "--XgcPrio:deterministic", "Garbage collection optimized for deterministic pausetimes Old Collector") );
    	
    	ATTRS.add( "CollectionCount" );
    	ATTRS.add( "CollectionTime" );
    }
    
    public static List<GarbageCollector> getCollectors( JmxConnection connection ) throws MalformedObjectNameException, InstanceNotFoundException, AttributeNotFoundException, ReflectionException, MBeanException, IOException {
        // Get standard attribute "VmVendor"
    	if( LOGGER.isDebugEnabled() ) {
	        ObjectName objectName = new ObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME);
	        String vendor = (String) connection.getAttribute(objectName, "VmVendor");
	        LOGGER.debug( "vendor: " + vendor);
    	}
        
    	List<GarbageCollector> ret = new ArrayList<GarbageCollector>();
    	for( GarbageCollector coll : ALL ) {
    		try {
	            ObjectName objectName = new ObjectName("java.lang:type=GarbageCollector,name=" + coll.getName() );
	            Set<ObjectName> names = connection.queryNames( objectName, null );
	            if( names.size() > 0 ) {
	            	ret.add( coll );
	            }
    		}
    		catch( Exception ex ) {
    			LOGGER.info( "Could not get collector: '" + coll.getName() + "'");
    		}
    	}
    	return ret;
    }
    
    public Info getInfo( JmxConnection connection ) throws MalformedObjectNameException, InstanceNotFoundException, ReflectionException, IOException {
    	ObjectName objectName = new ObjectName("java.lang:type=GarbageCollector,name=" + name );
    	AttributeList attrs = connection.getAttributes(objectName, ATTRS );

    	Info ret = new Info();
        ret.setType( type.getText() );
        ret.setName( getName() );        
        
        for( Attribute attr : attrs.asList() ) {
        	if( "CollectionCount".equals(attr.getName()) ) {
        		ret.setCount( (Long)attr.getValue() );
        	}
        	else if( "CollectionTime".equals(attr.getName())) {
        		ret.setTime( (Long)attr.getValue() );
        	}
        	else {
        		throw new RuntimeException( "Unexpected attribute: '" + attr.getName() + "'" );
        	}
        }
        return ret;
    }
    
    private GarbageCollector( Type type, String vm, String option, String name ) {
    	this.type = type;
    	this.vm = vm;
    	this.option = option;
    	this.name = name;
    }

	public Type getType() {
		return type;
	}


	public String getVm() {
		return vm;
	}

	public String getOption() {
		return option;
	}

	public String getName() {
		return name;
	}
    
}
