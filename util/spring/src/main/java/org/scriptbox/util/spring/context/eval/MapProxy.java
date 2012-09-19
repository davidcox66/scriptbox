package org.scriptbox.util.spring.context.eval;

import java.util.Map;

public class MapProxy implements Mappable {
	private Map map;
	String name;

	public MapProxy(String name, Map map) {
		this.name = name;
		this.map = map;
	}

	public Object get(String name) {
		return map.get(name);
	}
	
	public String toString() {
		return "MapProxy{" + name + "}";
	}
}
