package org.scriptbox.util.spring.context;

import java.util.HashMap;
import java.util.Map;

public class ContextBeans {

	private static ThreadLocal<Map<String,Object>> beans = new ThreadLocal<Map<String,Object>>() {
		public Map<String,Object> initialValue() {
			return new HashMap<String,Object>();
		}
	};
	
	public static void with( Map<String,Object> values, Runnable runnable ) {
		try {
			putAll( values );
			runnable.run();
		}
		finally {
			clear();
		}
	}
	
	public static Object get( String name ) {
		return beans.get().get( name );
	}
	public static void put( String name, Object value ) {
		beans.get().put( name, value );
	}
	public static void putAll( Map<String,Object> values ) {
		Map<String,Object> map = beans.get();
		map.putAll( values );
	}
	
	public static void clear() {
		beans.get().clear();
	}
	public static Map<String,Object> getAll() {
		return beans.get();
	}
}
