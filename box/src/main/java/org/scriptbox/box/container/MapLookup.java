package org.scriptbox.box.container;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapLookup implements Lookup {

	private Map<String,Object> beans = new HashMap<String,Object>();
	
	@Override
	public void put( String name, Object obj ) {
		beans.put( name,  obj );
	}
	@Override
	@SuppressWarnings("unchecked")
	public <X> X get( String name, Class<X> cls ) {
		return (X)beans.get( name );
	}
	
	@Override
	public <X> X getEx( String name, Class<X> cls ) {
		X ret = get( name, cls );
		if( ret == null ) {
			throw new RuntimeException( "Could not find bean: '" + name + "' of type: " + cls );
		}
		return ret;
	}
	@Override
	public Object remove( String name ) {
		return beans.remove( name );
	}
	@Override
	@SuppressWarnings("unchecked")
	public <X> X remove( String name, Class<X> cls ) {
		return (X)beans.remove( name );
	}
	@Override
	public Map<String,Object> getAll() {
		return beans;
	}
	
	@SuppressWarnings("unchecked")
	public <X> Set<X> getAll( Class<X> cls ) {
		Set<X> ret = new HashSet<X>();
		for( Object obj : beans.values() ) {
			if( obj != null && cls.isInstance(obj) ) {
				ret.add( (X)obj );
			}
		}
		return ret;
	}
}
