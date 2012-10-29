package org.scriptbox.ui.server.chart;

import java.util.Collection;

public class StringMinifier {
	
	  public static IntRange getStringCollectionUniqueNamePortion( Collection<String> strings ) {
	      IntRange ret = new IntRange( findLeadingUniqueCharacterPosition(strings), findTrailingUniqueCharacterPosition(strings) );
	      // Check for all strings the same
	      if( ret.getStart() != Math.abs(ret.getEnd()) ) { 
	    	 return ret; 
	      }
	      else {
	    	  return new IntRange(0,0);
	      }
	  }
	 
	  public static int findLeadingUniqueCharacterPosition( Collection<String> collectionOfStrings ) {
	      int pos=0;
	      while( isLeadingCharacterAtPositionTheSameForAll(collectionOfStrings,pos) ) {
	          pos++;
	      }
	      return pos;
	  } 
	  
	  public static int findTrailingUniqueCharacterPosition( Collection<String> collectionOfStrings ) {
	      int pos=0;
	      while( isTrailingCharacterAtPositionTheSameForAll(collectionOfStrings,pos) ) {
	          pos++;
	      }
	      return -pos;
	  } 
	  
	  private static boolean isLeadingCharacterAtPositionTheSameForAll( Collection<String> collectionOfStrings, int pos ) {
	      boolean first=true;
	      char ch=0;
	      for( String str : collectionOfStrings ) {
	          if( pos >= str.length() ) {
	              return false;
	          }
	          if( first ) {
	              ch = str.charAt(pos);    
	              first = false;
	          }
	          else {
	              if( ch != str.charAt(pos) ) {
	                  return false;
	              }
	          }
	      }
	      return true;
	  }
	  
	  private static boolean isTrailingCharacterAtPositionTheSameForAll( Collection<String> collectionOfStrings, int pos ) {
	      boolean first=true;
	      char ch=0;
	      for( String str : collectionOfStrings ) {
	          if( pos >= str.length() ) {
	              return false;
	          }
	          if( first ) {
	              ch = str.charAt(str.length() - pos - 1);    
	              first = false;
	          }
	          else {
	              if( ch != str.charAt(str.length() - pos - 1) ) {
	                  return false;
	              }
	          }
	      }
	      return true;
	  }

}
