package org.scriptbox.box.container;

import java.util.Map;
import java.util.Set;

public interface Lookup {

	public void put(String name, Object obj);
	public <X> X get(String name, Class<X> cls);
	public <X> X getEx(String name, Class<X> cls);
	public Object remove(String name);
	public <X> X remove(String name, Class<X> cls);
	public Map<String, Object> getAll();
	public <X> Set<X> getAll( Class<X> cls );
}