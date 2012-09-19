package org.scriptbox.util.common.regex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class RegexUtil {

	public static List<Pattern> toPatternCollection( Object obj ) {
		if( obj != null ) {
			List<Pattern> ret = new ArrayList<Pattern>();
			if( obj instanceof Collection ) {
				for( Object elem : ((Collection)obj) ) {
					ret.add( toPattern(elem) );
				}
			}
			else {
				ret.add( toPattern(obj) );
			}
			return ret;
		}
		return null;
	}
	
	public static Pattern toPattern( Object obj ) {
		if( obj instanceof Pattern ) {
			return (Pattern)obj;
		}
		else {
			return Pattern.compile( String.valueOf(obj) );
		}
	}
	
	public static boolean matchesAny( String str, Object obj ) {
		List<Pattern> patterns = toPatternCollection( obj ); 
		if( patterns != null ) {
			return matchesAnyPattern( str, patterns );
		}
		return false;
	}
	
	public static boolean matchesAnyPattern( String str, Collection<Pattern> patterns ) {
		for( Pattern pattern : patterns ) {
			if( pattern.matcher(str).matches() ) {
				return true;
			}
		}
		return false;
	}
}
