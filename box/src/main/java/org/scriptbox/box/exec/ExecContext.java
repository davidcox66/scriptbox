package org.scriptbox.box.exec;

import java.util.ArrayList;
import java.util.List;

public class ExecContext {

	private static final ThreadLocal<List<Object>> stack = new ThreadLocal<List<Object>>() {
		public List<Object> initialValue() {
			return new ArrayList<Object>();
		}
	};
	
	public static void with( Object parent, ExecRunnable child ) throws Exception {
		List<Object> r = stack.get();
		r.add( parent );
		try {
			child.run();
		}
		finally {
			r.remove( r.size() - 1 );
		}
	}
	
	public static Object getEnclosing() {
		List<Object> r = stack.get();
		if( r.size() == 0 ) {
			throw new RuntimeException( "No enclosing object");
		}
		return r.get( r.size() - 1 );
	}
	
	@SuppressWarnings("unchecked")
	public static <X> X getEnclosing( Class<X> cls ) {
		Object last = getEnclosing();
		if( !cls.isInstance(last) ) {
			throw new RuntimeException( "Enclosing runnable: " + last + " is not of the type: " + cls );
		}
		return (X)last;
	}
	
	@SuppressWarnings("unchecked")
	public static <X> X getNearestEnclosing( Class<X> cls ) {
		List<Object> r = stack.get();
		for( int i=r.size()-1 ; i >= 0 ; i-- ) {
			Object elem = r.get(i);
			if( cls.isInstance(elem) ) {
				return (X)elem;
			}
		}
		throw new RuntimeException( "No enclosing runnable of the type: " + cls );
	}
}