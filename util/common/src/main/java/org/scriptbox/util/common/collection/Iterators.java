package org.scriptbox.util.common.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.scriptbox.util.common.error.Errors;
import org.scriptbox.util.common.obj.ParameterizedRunnable;


public class Iterators {

    public static <X> Errors<X> callAndCollectExceptions( Collection<X> objs, ParameterizedRunnable<X> runnable ) {
    	Map<X,Throwable> ret = new HashMap<X,Throwable>();
    	if( objs != null ) {
	    	for( X obj : objs ) {
	    		try {
		    		runnable.run( obj );
		    	}
	    		catch( Throwable ex ) {
	    			ret.put( obj, ex );
	    		}
	    	}
    	}
    	return new Errors<X>( ret );
    }

}
