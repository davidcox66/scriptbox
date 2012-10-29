package org.scriptbox.metrics.query.groovy;

import org.scriptbox.metrics.model.MetricTreeNode
import org.scriptbox.metrics.query.exp.MetricQueryExp
import org.scriptbox.metrics.query.main.MetricQueries

class MetricQueryBuilder
{
  public MetricQueryExp build( Closure closure ) {
    closure.delegate = this;
    return closure.call();
  }

  public MetricQueryExp list( MetricQueryExp... args ) { 
  	return list( (String)null, args );
  }
  
  public MetricQueryExp list( String name, MetricQueryExp... args ) { 
    if( args.size() < 2 ) {
      throw new Exception( "Invalid list expression");
    }
	return MetricQueries.list( name, args );
  }
  
  public MetricQueryExp and( MetricQueryExp... args ) { 
	  return and( (String)null, args );
  }
  
  public MetricQueryExp and( String name, MetricQueryExp... args ) { 
    if( args.size() < 2 ) {
      throw new Exception( "Invalid and expression");
    }
	return MetricQueries.and( name, args );
  }
  
  public MetricQueryExp or( MetricQueryExp... args ) { 
	  return or( (String)null, args );
  }
  
  public MetricQueryExp or( String name, MetricQueryExp... args ) { 
    if( args.size() < 2 ) {
      throw new Exception( "Invalid or expression");
    }
	return MetricQueries.or( name, args );
  }
  
  public MetricQueryExp not( MetricQueryExp arg, MetricQueryExp nots ) { 
	  return not( (String)null, arg, nots );
  }
  
  public MetricQueryExp not( String name, MetricQueryExp arg, MetricQueryExp nots ) { 
    return MetricQueries.not( name, arg, nots );
  }
  
  public MetricQueryExp total( MetricQueryExp arg )  {
	  return total( (String)null, arg );
  }
  public MetricQueryExp total( String name, MetricQueryExp arg )  {
    return MetricQueries.total( name, arg );  
  }
  
  public MetricQueryExp delta( MetricQueryExp arg )  {
	  return delta( (String)null, arg );
  }
  public MetricQueryExp delta( String name, MetricQueryExp arg )  {
    return MetricQueries.delta( name, arg );  
  }
  
  public MetricQueryExp diff( int chunk, MetricQueryExp arg1, MetricQueryExp arg2 )  {
  	return diff( (String)null, chunk, arg1, arg2 );
  }
  public MetricQueryExp diff( String name, int chunk, MetricQueryExp arg1, MetricQueryExp arg2 )  {
    return MetricQueries.diff( name, chunk, arg1, arg2 );  
  }
  public MetricQueryExp divisor( int chunk, MetricQueryExp totals, MetricQueryExp divisors )  {
	 return divisor( (String)null, chunk, totals, divisors ); 
  }
  public MetricQueryExp divisor( String name, int chunk, MetricQueryExp totals, MetricQueryExp divisors )  {
    return MetricQueries.divisor( name, chunk, totals, divisors );  
  }
  public MetricQueryExp multiplier( int chunk, MetricQueryExp totals, MetricQueryExp multiples )  {
	 return multiplier( (String)null, chunk, totals, multiples ) ;
  }
  public MetricQueryExp multiplier( String name, int chunk, MetricQueryExp totals, MetricQueryExp multiples )  {
    return MetricQueries.multiplier( name, chunk, totals, multiples );  
  }
  
  public MetricQueryExp persecond( MetricQueryExp arg )  {
	  return persecond( (String)null, arg );
  }
  public MetricQueryExp persecond( String name, MetricQueryExp arg )  {
    return MetricQueries.persecond( name, arg );  
  }
  public MetricQueryExp tps( MetricQueryExp arg )  {
	  return tps( (String)null, arg );
  }
  public MetricQueryExp tps( String name, MetricQueryExp arg )  {
    return MetricQueries.tps( name, arg );  
  }
  
  public MetricQueryExp divide( int divisor, MetricQueryExp arg )  {
	  return divide( (String)null, divisor, arg );
  }
  public MetricQueryExp divide( String name, int divisor, MetricQueryExp arg )  {
    return MetricQueries.divide( name, divisor, arg );  
  }
  
  public MetricQueryExp multiply( int multiplier, MetricQueryExp arg )  {
	  return multiply( (String)null, multiplier, arg );
  }
  
  public MetricQueryExp multiply( String name, int multiplier, MetricQueryExp arg )  {
    return MetricQueries.multiply( name, multiplier, arg );  
  }
  
  public MetricQueryExp top( int count, MetricQueryExp arg )  {
	  return top( (String)null, count, arg );
  }
  public MetricQueryExp top( String name, int count, MetricQueryExp arg )  {
    return MetricQueries.top( name, count, arg );  
  }
  
  public MetricQueryExp bottom( int count, MetricQueryExp arg )  {
	  return bottom( (String)null, count, arg );
  }
  public MetricQueryExp bottom( String name, int count, MetricQueryExp arg )  {
    return MetricQueries.bottom( name, count, arg );  
  }
  
  public MetricQueryExp topavg( int count, MetricQueryExp arg )  {
	  return topavg( (String)null, count, arg );
  }
  public MetricQueryExp topavg( String name, int count, MetricQueryExp arg )  {
    return MetricQueries.topavg( name, count, arg );  
  }
  
  public MetricQueryExp bottomavg( int count, MetricQueryExp arg )  {
	  return bottomavg( (String)null, count, arg );
  }
  public MetricQueryExp bottomavg( String name, int count, MetricQueryExp arg )  {
    return MetricQueries.bottomavg( name, count, arg );  
  }
  
  public MetricQueryExp average( MetricQueryExp arg )  {
	  return average( (String)null, arg );
  }
  public MetricQueryExp average( String name, MetricQueryExp arg )  {
    return MetricQueries.average( name, arg );  
  }
  
  public MetricQueryExp min( MetricQueryExp arg )  {
	  return min( (String)null, arg );
  }
  public MetricQueryExp min( String name, MetricQueryExp arg )  {
    return MetricQueries.min( name, arg );  
  }
  
  public MetricQueryExp max( MetricQueryExp arg )  {
	 return max( (String)null, arg ); 
  }
  public MetricQueryExp max( String name, MetricQueryExp arg )  {
    return MetricQueries.max( name, arg );  
  }
  
  public MetricQueryExp source( String pattern )  {
    return MetricQueries.source(pattern);  
  }
  
  public MetricQueryExp instance( String pattern )  {
    return MetricQueries.instance(pattern);  
  }
 
  // An alias for instance since 'instance' is high-overloaded in usage and may conflict with use
  // in MonitorConfiguration.instance 
  public MetricQueryExp inst( String pattern )  {
    return MetricQueries.instance(pattern);  
  }
  
  public MetricQueryExp attr( String pattern )  {
    return MetricQueries.attr(pattern);  
  }
  
  public MetricQueryExp metric( String pattern )  {
    return MetricQueries.metric(pattern);  
  }
}
