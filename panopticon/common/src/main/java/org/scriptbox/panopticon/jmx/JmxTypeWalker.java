package org.scriptbox.panopticon.jmx;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxTypeWalker {

    private static final Logger LOGGER = LoggerFactory.getLogger( JmxTypeWalker.class );
    
    private JmxTypeVisitor visitor;
    
    public static boolean isTraversableType( Object obj ) {
    	return 
    		obj != null && (
    		    obj.getClass().isArray() ||
    		    obj instanceof CompositeData ||
    		    obj instanceof TabularData ||
    		    obj instanceof Map ||
    		    obj instanceof Collection);
    }
    
    public JmxTypeWalker( JmxTypeVisitor visitor )  {
      this.visitor = visitor;
    }
    
    public void walk(String prefix, String name, Object data ) {
        if (data == null) { 
          walkSimple(prefix,name,null);
        }
        else if (data.getClass().isArray())  {
            walkArray(prefix,name,data);
        }
        else if (data instanceof CompositeData) {
            walkCompositeData(prefix,name,(CompositeData)data);
        }
        else if (data instanceof TabularData) {
            walkTabularData(prefix,name,(TabularData)data);
        }
        else if (data instanceof Map) {
            walkMap(prefix,name,(Map)data);
        }
        else if (data instanceof Collection) { 
            walkArray(prefix,name,((Collection)data).toArray());
        }
        else {
	        walkSimple(prefix,name,data);
        }
    }
    
    private void walkSimple(String prefix, String name, Object data ) {
      visitor.value( prefix, name, data );
    }
    
    private void walkArray(String prefix, String name, Object array) {
        visitor.enter( prefix, name, array );
	      try {
	        final int length = Array.getLength(array);
	        for (int i=0;i<length;i++) {
	            final Object data = Array.get(array,i);
	            walk(prefix,name+"["+i+"]",data);
	        }
	      }
        finally {
          visitor.exit( prefix, name, array );
        }
          
    }
    
    private void walkCompositeData(String prefix, String name, CompositeData data) {
	    visitor.enter( prefix, name, data );
	      try {
	      for (String key : data.getCompositeType().keySet()) {
	        walk(prefix,name+"."+key,data.get(key));
		      }
	    }
	      finally {
	      visitor.exit( prefix, name, data );
      }
    }
    
    private void walkTabularData(String prefix, String name, TabularData data) {
        visitor.enter( prefix, name, data );
        try {
	        // walkSimple(prefix,name,"TabularData("+ data.getTabularType().getTypeName()+")",true);
	        final List<String> keyNames = data.getTabularType().getIndexNames();
	        final int indexCount = keyNames.size();
	        for (Object keys : data.keySet()) {
	            final Object[] keyValues = ((List<?>)keys).toArray();
	            final StringBuilder b = new StringBuilder(name);
	            b.append("[");
	            for (int i=0;i<indexCount;i++) {
	                if (i>0) b.append(", ");
	                b.append(keyNames.get(i)+"="+keyValues[i]);
	            }
	            b.append("]");
	            walkCompositeData(prefix,b.toString(),data.get(keyValues));
	        }
        }
        finally {
          visitor.exit( prefix, name, data );
        }
    }
    
    private void walkMap(String prefix, String name, Map<Object,Object> data) {
        visitor.enter( prefix, name, data );
        try {
	        // walkSimple(prefix,name,"java.util.Map",true);
	        for (Entry<Object,Object> e : data.entrySet()) {
	            walk(prefix,name+"["+e.getKey()+"]",e.getValue());
	        }
        }
        finally {
          visitor.exit( prefix, name, data );
        }
    }
}
