mb:ackage org.scriptbox.metrics.query.groovy;

import org.scriptbox.metrics.model.MetricTreeNode
import org.scriptbox.metrics.query.exp.MetricQueryExp
import org.scriptbox.metrics.query.main.MetricQueries

class MetricQueryBuilder 
{
  public MetricQueryExp build( Closure closure ) {
    closure.delegate = this;
    return closure.call();
  }

  public MetricQueryExp and( MetricQueryExp... args ) { 
    if( args.size() < 2 ) {
      throw new Exception( "Invalid and expression");
    }
	return MetricQueries.and( args );
  }
  
  public MetricQueryExp or( MetricQueryExp... args ) { 
    if( args.size() < 2 ) {
      throw new Exception( "Invalid or expression");
    }
	return MetricQueries.or( args );
  }
  
  public MetricQueryExp not( MetricQueryExp arg, MetricQueryExp nots ) { 
    return MetricQueries.not( arg, nots );
  }
  
  public MetricQueryExp total( MetricQueryExp arg )  {
    return MetricQueries.total(arg);  
  }
  
  public MetricQueryExp delta( MetricQueryExp arg )  {
    return MetricQueries.delta(arg);  
  }
  
  public MetricQueryExp diff( int chunk, MetricQueryExp arg1, MetricQueryExp arg2 )  {
    return MetricQueries.diff(chunk,arg1,arg2);  
  }
  public MetricQueryExp divisor( int chunk, MetricQueryExp totals, MetricQueryExp divisors )  {
    return MetricQueries.divisor(chunk,totals,divisors);  
  }
  public MetricQueryExp multiplier( int chunk, MetricQueryExp totals, MetricQueryExp multiples )  {
    return MetricQueries.multiplier(chunk,totals,multiples);  
  }
  
  public MetricQueryExp persecond( MetricQueryExp arg )  {
    return MetricQueries.persecond(arg);  
  }
  public MetricQueryExp tps( MetricQueryExp arg )  {
    return MetricQueries.tps(arg);  
  }
  
  public MetricQueryExp divide( int divisor, MetricQueryExp arg )  {
    return MetricQueries.divide(divisor,arg);  
  }
  
  public MetricQueryExp multiply( int multiplier, MetricQueryExp arg )  {
    return MetricQueries.multiply(multiplier,arg);  
  }
  
  public MetricQueryExp top( int count, MetricQueryExp arg )  {
    return MetricQueries.top(count,arg);  
  }
  
  public MetricQueryExp bottom( int count, MetricQueryExp arg )  {
    return MetricQueries.bottom(count,arg);  
  }
  
  public MetricQueryExp topavg( int count, MetricQueryExp arg )  {
    return MetricQueries.topavg(count,arg);  
  }
  
  public MetricQueryExp bottomavg( int count, MetricQueryExp arg )  {
    return MetricQueries.bottomavg(count,arg);  
  }
  
  public MetricQueryExp average( MetricQueryExp arg )  {
    return MetricQueries.average(arg);  
  }
  
  public MetricQueryExp min( MetricQueryExp arg )  {
    return MetricQueries.min(arg);  
  }
  
  public MetricQueryExp max( MetricQueryExp arg )  {
    return MetricQueries.max(arg);  
  }
  
  public MetricQueryExp run( String pattern )  {
    return MetricQueries.run(pattern);  
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
