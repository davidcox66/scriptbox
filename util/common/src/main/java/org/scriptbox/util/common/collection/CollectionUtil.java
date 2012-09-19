package org.scriptbox.util.common.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionUtil {

	@SuppressWarnings("unchecked")
	public static <X,Y extends X> List<Y> getObjectsOfType( Collection<X> objs, Class<Y> cls ) {
		List<Y> ret = new ArrayList<Y>();
		if( objs != null ) {
			for( X obj : objs ) {
				if( cls.isInstance(obj) ) {
					ret.add( (Y)obj );
				}
			}
		}
		return ret;
	}
	

}
