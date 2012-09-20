package org.scriptbox.util.common.regex;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.scriptbox.util.common.obj.ParameterizedRunnable;

public class Matching {

    public static boolean isMatch( String value, Set<String> values, Set<Pattern> patterns ) {
        return isValueMatch(value,values) || isPatternMatch(value,patterns);
    }
    
    public static boolean isValueMatch( String value, Set<String> values ) {
        if( values != null && values.size() > 0 ) {
          if( values.contains(value) ) {
            return true;
          }
        }
        return false;
    }
    
    public static boolean isPatternMatch( String value, Set<Pattern> patterns ) {
        if( patterns != null ) {
          for( Pattern pattern : patterns ) {
            if( pattern.matcher(value).matches() ) {
              return true;
            }
          }
        }
        return false;
    }
   
    public static Set<Pattern> toPatternSet( final Collection elems ) {
        final Set<Pattern> ret = new HashSet<Pattern>( elems.size() );
        iterateOverElementOrCollection( elems, new ParameterizedRunnable<Object>() {
        	public void run( Object obj ) {
        		ret.add( toPattern(obj) );
        	}
        } );
        return ret;
    } 
    
    public static Pattern toPattern( Object elem ) {  
        if( elem instanceof Pattern ) {
            return (Pattern)elem;
        }
        else {
            return Pattern.compile(String.valueOf(elem));
        }
    }
    
    public static boolean isRegex( Object elem ) {
        if( elem instanceof String ) {
            String str = (String)elem;
	        return str.indexOf('*') != -1 || str.indexOf('?') != -1;
        }
        else if( elem instanceof Pattern ) {
            return true;
        }
        return false;
    }
     
    
    /**
    * Hide the fact that we are iterating over a single string or a collection. The default behavior for iterating
    * over a string is to go through each character which is not what we want.
    *
    * @param item
    * @param closure
    */
   public static <T extends Object> void iterateOverElementOrCollection( Object item, ParameterizedRunnable<T> closure ) {
	   try {
	       if( item != null ) {
	           if( item instanceof Collection ) {
	        	   Collection<T> coll = (Collection<T>)item;
	        	   for( T obj : coll ) {
	        		   closure.run( obj );
	        	   }
	           }
	           else {
	               closure.run( (T)item );
	           }
	       }
	   }
	   catch( Exception ex ) {
		   throw new RuntimeException( "Error while iterating over collection", ex );
	   }
   }
}