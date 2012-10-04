package org.scriptbox.util.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

public class PropertiesFacade extends Properties
{
	protected static final long serialVersionUID = 1L;
	
    private Configuration config;
   
    private class Entry<K,V> implements Map.Entry<K,V> {
    	int hash;
    	K key;
    	Entry<K,V> next;
    
    	protected Entry(int hash, K key, Entry<K,V> next) {
    	    this.hash = hash;
    	    this.key = key;
    	    this.next = next;
    	}
    
    	protected Object clone() {
    	    return new Entry<K,V>(hash, key, 
    				  (next==null ? null : (Entry<K,V>) next.clone()));
    	}
    
    	// Map.Entry Ops 
    
    	public K getKey() {
    	    return key;
    	}
    
    	public V getValue() {
    	    return (V)get(key);
    	}
    
    	public V setValue(V value) {
    	    if (value == null)
    		throw new NullPointerException();
    
    	    V oldValue = (V)get(key);
    	    put(key,value);
    	    return oldValue;
    	}
    
    	public boolean equals(Object o) {
    	    if (!(o instanceof Map.Entry))
    		return false;
    	    Map.Entry e = (Map.Entry)o;
    
    	    return (key==null ? e.getKey()==null : key.equals(e.getKey()));
    	}
    
    	public int hashCode() {
    	    return hash ^ (key==null ? 0 : key.hashCode());
    	}
    
    	public String toString() {
    	    return key.toString()+"="+getValue();
    	}
    }
    
	public PropertiesFacade( Configuration config )
	{
		this.config = config;
	}
	
    @Override
	public boolean containsKey( Object key )
	{
		return config.containsKey( String.valueOf(key) );
	}
	
    @Override
	public boolean contains( Object value )
	{
    	for( Iterator iter = config.getKeys() ; iter.hasNext() ; ) {
    		String key = String.valueOf(iter.next());
    		Object cvalue = config.getString(key);
    	    if( value != null )	 {
    	    	if( value.equals(cvalue) ) {
    	    		return true;
    	    	}
    	    }
    	    else {
    	    	if( cvalue == null ) {
    	    		return true;
    	    	}
    	    }
    	}
    	return false;
	}
    
    @Override
	public Object get( Object key )
	{
		return config.getString( String.valueOf(key) );
	}

    @Override
	public Object put( Object key, Object value )
	{
		Object ret = get( key );
		config.setProperty( String.valueOf(key), value );
		return ret;
	}
    
    @Override
    public String getProperty(String key) {
    	Object oval = get(key);
    	return (oval instanceof String) ? (String)oval : null;
    }
	
    @Override
    public synchronized Enumeration keys() {
		return propertyNames();
	}

    @Override
    public synchronized Enumeration elements() {
		return Collections.enumeration( values() );
    }
    
    @Override
    public synchronized Collection values() {
    	List<Object> values = new ArrayList<Object>();
		for( Iterator iter = config.getKeys() ; iter.hasNext() ; ) {
			values.add( config.getString((String)iter.next()) );
		}
		return values;
    }
    
    @Override
	public synchronized Enumeration propertyNames() 
	{
		Set<String> keys = new HashSet<String>();
		for( Iterator iter = config.getKeys() ; iter.hasNext() ; ) {
			keys.add( (String)iter.next() );
		}
		return Collections.enumeration( keys );
	}
	
    @Override
	public synchronized Set<Map.Entry<Object,Object>> entrySet() 
	{
		Map<Object,Object> entries = new HashMap<Object,Object>();
		for( Iterator iter = config.getKeys() ; iter.hasNext() ; ) {
			String key = (String)iter.next();
			Object value = config.getString( key ); 
			entries.put( key,value );
		}
		return entries.entrySet();
	}
}

