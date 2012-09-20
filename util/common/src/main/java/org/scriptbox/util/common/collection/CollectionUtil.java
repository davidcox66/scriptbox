package org.scriptbox.util.common.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
	public static boolean isEmpty( Collection<?> coll ) {
		return coll == null || coll.size() == 0;
	}
	public static boolean isEmpty( Map coll ) {
		return coll == null || coll.size() == 0;
	}
}
