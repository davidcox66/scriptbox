package org.scriptbox.metrics.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricQueries {
	  static final Logger LOGGER = LoggerFactory.getLogger( MetricQueries.class );
	  
	  public static Object query( MetricStore store, MetricQueryExp exp, Date start, Date end, int chunk ) {
	    List metrics = new ArrayList();
	    eachMetric(mr) { metrics.add(it); }
	    if( LOGGER.isDebugEnabled() ) {
		    LOGGER.debug( "query: run=${mr}, expression=${exp}, start=${start}, end=${end}, chunk=${chunk}, metricsEvaluated=${metrics.size()}");
	    }
	    long begin = LOGGER.isInfoEnabled() ? System.currentTimeMillis() : 0;
	    
	    SourceQueryContext ctx = new SourceQueryContext(
	      start:start,
	      end:end, 
	      chunk:chunk,
	      metrics:metrics );
	    
	    try {
		    return exp.evaluate( ctx );
	    }
	    finally {
	      if( begin ) {
	        long millis = System.currentTimeMillis() - begin;
	        LOGGER.info( "query: time=${millis}, run=${mr}, expression=${exp}, start=${start}, end=${end}, chunk=${chunk}, " +
	          "metricsEvaluated=${metrics.size()}, totalmetricsProcessed=${ctx.totalMetricsProcessed}, totalValuesProcessed=${ctx.totalValuesProcessed}" );
	      }
	    }
	  }
	  
	  static void eachMetric( MetricRun mr, Closure closure )  {
	    eachMetricImpl( mr, closure );
	  }
	  
	  private static void eachMetricImpl( SourceNode node, Closure closure ) {
	    if( node ) {
	      if( node instanceof SourceMetric ) {
		      closure.call( node );
	      }
	      else {
			    node?.children.each{ SourceNode child -> eachMetricImpl( child, closure ); }
		    }
	    }
	  } 

	  
	  static SourceQueryExp values( SourceQueryExp child ) {
	    return new ValuesQueryExp( child );
	  }
	  
	  static SourceQueryExp total( SourceQueryExp child ) {
	    return new TotalQueryExp( child );
	  }
	  
	  static SourceQueryExp average( SourceQueryExp child ) {
	    return new AverageQueryExp( child );
	  }
	  
	  static SourceQueryExp min( SourceQueryExp child ) {
	    return new MinQueryExp( child );
	  }
	  
	  static SourceQueryExp max( SourceQueryExp child ) {
	    return new MaxQueryExp( child );
	  }
	  
	  static SourceQueryExp run( String pattern ) {
	    return run( ~pattern );
	  }
	  static SourceQueryExp run( Pattern pattern ) {
	    return new SourceQueryTypeExp( 'run', pattern.toString(), MetricRun, 
	      { MetricRun mr -> return pattern.matcher(mr.name).matches() } );
	  }
	  
	  static SourceQueryExp source( String pattern ) {
	    return source( ~pattern );
	  }
	  static SourceQueryExp source( Pattern pattern ) {
	    return new SourceQueryTypeExp( 'source', pattern.toString(), Source, 
	      { Source src -> return pattern.matcher(src.name).matches() } );
	  }
	  
	  static SourceQueryExp instance( String pattern ) {
	    return instance( ~pattern );
	  }
	  static SourceQueryExp instance( Pattern pattern ) {
	    return new SourceQueryTypeExp( 'instance', pattern.toString(), SourceInstance, 
	      { SourceInstance inst -> return pattern.matcher(inst.name).matches() } );
	  }
	  
	  static SourceQueryExp attr( String pattern ) {
	    return attr( ~pattern );
	  }
	  static SourceQueryExp attr( Pattern pattern ) {
	    return new SourceQueryTypeExp( 'attr', pattern.toString(), SourceAttribute, 
	      { SourceAttribute attr -> return pattern.matcher(attr.name).matches() } );
	  }
	  
	  static SourceQueryExp metric( String pattern ) {
	    return metric( ~pattern );
	  }
	  static SourceQueryExp metric( Pattern pattern ) {
	    return new SourceQueryTypeExp( 'metric', pattern.toString(), SourceMetric, 
	      { SourceMetric metric -> return pattern.matcher(metric.name).matches() } );
	  }
	  
	  static SourceQueryExp and( SourceQueryExp lhs, SourceQueryExp rhs ) {
	    return new BinarySourceQueryExp( 'and', lhs, rhs, { lresult, rresult, SourceQueryContext ctx -> 
	      Set set = new HashSet(lresult);
	      set.retainAll(rresult);
	      return set;
	    } ); 
	  }
	  
	  static SourceQueryExp or( SourceQueryExp lhs, SourceQueryExp rhs ) {
	    return new BinarySourceQueryExp( 'or', lhs, rhs, { lresult, rresult, SourceQueryContext ctx -> 
	      Set set = new HashSet(lresult);
	      set.addAll(rresult);
	      return set;
	    } );
	  }
	  
	  static SourceQueryExp diff( int chunk, SourceQueryExp lhs, SourceQueryExp rhs ) {
	    return new DiffQueryExp( chunk, lhs, rhs ); 
	  }
	  
	  static SourceQueryExp not( SourceQueryExp exp ) {
	    return new UnarySourceQueryExp( 'not', exp, { result, SourceQueryContext ctx -> 
	      Set metrics = new HashSet( ctx.metrics ); 
	      metrics.removeAll( result );
	      return metrics;
	    } );
	  }
	  
	  static SourceQueryExp delta( SourceQueryExp exp ) {
	    return new DeltaQueryExp( exp );
	  }
	  static SourceQueryExp persecond( SourceQueryExp exp ) {
	    return new PerSecondQueryExp( exp );
	  }
	  static SourceQueryExp tps( SourceQueryExp exp ) {
	    return new TotalPerSecondQueryExp( exp );
	  }
	  static SourceQueryExp divide( double divisor, SourceQueryExp exp ) {
	    return new DivideQueryExp( divisor, exp );
	  }
	  static SourceQueryExp multiply( double multiplier, SourceQueryExp exp ) {
	    return new MultiplyQueryExp( multiplier, exp );
	  }
	  static SourceQueryExp top( int count, SourceQueryExp exp ) {
	    return new TopQueryExp( count, exp );
	  }
	  static SourceQueryExp bottom( int count, SourceQueryExp exp ) {
	    return new BottomQueryExp( count, exp );
	  }
	  static SourceQueryExp topavg( int count, SourceQueryExp exp ) {
	    return new TopAverageQueryExp( count, exp );
	  }
	  static SourceQueryExp bottomavg( int count, SourceQueryExp exp ) {
	    return new BottomAverageQueryExp( count, exp );
	  }
	}

	class SourceMetricCriticalValue {
	  SourceMetric metric;
	  int value;
	}  
	  
	class ValuesQueryExp extends SourceQueryExp {
	 
	  private SourceQueryExp child; 
	  
	  ValuesQueryExp( SourceQueryExp child ) {
	    this.child = child;
	  }
	  def evaluate( SourceQueryContext ctx ) {
	      def stuff =  child.evaluate( ctx );
	      def values = null;
	      if( stuff instanceof SourceMetric ) { 
		      values = ((SourceMetric)stuff).getValues( ctx.start, ctx.end, true );
	      }
	      else if( stuff instanceof Map ) { // Map<SourceMetric,List<MetricValue>>
	          values = ValueGrouper.mergeMetrics( ((Map)stuff).values() );
	      }
	      else if( stuff instanceof Set ) { // Set<SourceMetric>
	          Map<SourceMetric,List<MetricValue>> allValues = ValueGrouper.toValues( stuff, ctx );
	          values = ValueGrouper.mergeMetrics( allValues.values() );
	      }
	      else {
	          throw new Exception( "Unexpected type for values() expression: " + stuff );
	      }
	      ctx.recordValues( values );
	      return values;
	  }
	  String toString() {
	    return "values(${child})";
	  }
	}

	class TotalQueryExp extends SourceQueryExp {
	 
	  private SourceQueryExp child; 
	  
	  TotalQueryExp( SourceQueryExp child ) {
	    this.child = child;
	  }
	  def evaluate( SourceQueryContext ctx ) {
		  return ValueGrouper.evaluateAndGroup( "total", child, ctx ) { long millis, List<MetricValue> mvs ->
		      long total = 0;
		      mvs.each{ MetricValue mv -> total += mv.value; }
		      if( SourceQuery.LOGGER.isTraceEnabled() ) {
		          SourceQuery.LOGGER.trace( "evaluate: total for millis ${millis} is ${total} with ${mvs.size()} metrics");
		      }
		      return new MetricValue( new Date(millis), total);
		  }
	  }
	  String toString() {
	    return "total(${child})";
	  }
	}

	class DeltaQueryExp extends SourceQueryExp {
	 
	  private SourceQueryExp child; 
	  
	  DeltaQueryExp( SourceQueryExp child ) {
	    this.child = child;
	  }
	  def evaluate( SourceQueryContext ctx ) {
	      Set<SourceMetric> ret = new HashSet<SourceMetric>(); 
	      Map<SourceMetric,List<MetricValue>> metrics = ValueGrouper.toValues( child.evaluate(ctx), ctx );
	      metrics.each{ SourceMetric metric, List<MetricValue> values ->
		      List<MetricValue> deltas = new ArrayList<MetricValue>( values.size() );
		      if( values.size() > 1 ) {
		          MetricValue prev =values[0];
			      values[1..-1].each{ MetricValue value ->
	                  double diff = value.value - prev.value;
	                  long sec = (value.date.time - prev.date.time) / 1000;
	                  // don't really see this as a possibility but just being anal retentive
	                  if( sec < 1 ) { 
	                      sec = 1; 
	                  }
	                  if( SourceQuery.LOGGER.isDebugEnabled() ) { SourceQuery.LOGGER.debug( "delta: value=${value.value}, prev=${prev.value}, diff=${diff}"); }
		              deltas.add( new MetricValue(value.date, diff / sec, value.reference) );
	                  prev = value;
			      } 
		      }
	          ret.add( new SyntheticSourceMetric(metric.attribute,"${metric.name}(delta)",deltas) );
	      }
	      return ret;
	  }
	  String toString() {
	    return "delta(${child})";
	  }
	}

	class PerSecondQueryExp extends SourceQueryExp {
	 
	  private SourceQueryExp child; 
	  
	  PerSecondQueryExp( SourceQueryExp child ) {
	    this.child = child;
	  }
	  def evaluate( SourceQueryContext ctx ) {
	      Set<SourceMetric> ret = new HashSet<SourceMetric>(); 
	      Map<SourceMetric,List<MetricValue>> metrics = ValueGrouper.toValues( child.evaluate(ctx), ctx );
	      metrics.each{ SourceMetric metric, List<MetricValue> values ->
		      List<MetricValue> persec = new ArrayList<MetricValue>( values.size() );
		      if( values.size() > 1 ) {
		          MetricValue prev =values[0];
			      values[1..-1].each{ MetricValue value ->
	                  long sec = (value.date.time - prev.date.time) / 1000;
	                  // don't really see this as a possibility but just being anal retentive
	                  if( sec < 1 ) { 
	                      sec = 1; 
	                  }
		              persec.add( new MetricValue(value.date, value.value / sec, value.reference) );
	                  prev = value;
			      } 
		      }
	          ret.add( new SyntheticSourceMetric(metric.attribute,"${metric.name}(per-second)",persec) );
	      }
	      return ret;
	  }
	  String toString() {
	    return "persecond(${child})";
	  }
	}

	class TotalPerSecondQueryExp extends SourceQueryExp {
	 
	  private SourceQueryExp child; 
	  
	  TotalPerSecondQueryExp( SourceQueryExp child ) {
	    this.child = child;
	  }
	  def evaluate( SourceQueryContext ctx ) {
	      int seconds = ctx.chunk / 1000; 
		  return ValueGrouper.evaluateAndGroup( "total-per-second", child, ctx ) { long millis, List<MetricValue> mvs ->
		      double total = 0;
		      mvs.each{ MetricValue mv -> total += mv.value; }
	          if( SourceQuery.LOGGER.isTraceEnabled() ) {
	              SourceQuery.LOGGER.trace( "evaluate: total for millis ${millis} is ${total} with ${mvs.size()} metrics");
	          }
		      return new MetricValue( new Date(millis), total / seconds );
		  }
	  }
	  String toString() {
	    return "tps(${child})";
	  }
	}

	class DivideQueryExp extends SourceQueryExp {
	 
	  private SourceQueryExp child; 
	  private double divisor;
	   
	  DivideQueryExp( double divisor, SourceQueryExp child ) {
	    this.child = child;
	    this.divisor = divisor;
	  }
	  def evaluate( SourceQueryContext ctx ) {
	      Set<SourceMetric> ret = new HashSet<SourceMetric>(); 
	      Map<SourceMetric,List<MetricValue>> metrics = ValueGrouper.toValues( child.evaluate(ctx), ctx );
	      metrics.each{ SourceMetric metric, List<MetricValue> values ->
		      List<MetricValue> divided = new ArrayList<MetricValue>( values.size() );
	          values.each{ MetricValue mv ->
		          divided.add( new MetricValue(mv.date, mv.value / divisor, mv.reference) );
		      }
	          ret.add( new SyntheticSourceMetric(metric.attribute,"${metric.name}(divided)",divided) );
	      }
	      return ret;
	  }
	  String toString() {
	    return "divide(${child})";
	  }
	}
	class MultiplyQueryExp extends SourceQueryExp {
	 
	  private SourceQueryExp child; 
	  private double multiplier;
	   
	  MultiplyQueryExp( double multiplier, SourceQueryExp child ) {
	    this.child = child;
	    this.multiplier = multiplier;
	  }
	  def evaluate( SourceQueryContext ctx ) {
	      Set<SourceMetric> ret = new HashSet<SourceMetric>(); 
	      Map<SourceMetric,List<MetricValue>> metrics = ValueGrouper.toValues( child.evaluate(ctx), ctx );
	      metrics.each{ SourceMetric metric, List<MetricValue> values ->
		      List<MetricValue> divided = new ArrayList<MetricValue>( values.size() );
	          values.each{ MetricValue mv ->
		          divided.add( new MetricValue(mv.date, mv.value * multiplier, mv.reference) );
		      }
	          ret.add( new SyntheticSourceMetric(metric.attribute,"${metric.name}(multiplied)",divided) );
	      }
	      return ret;
	  }
	  String toString() {
	    return "multiply(${child})";
	  }
	}

	class AverageQueryExp extends SourceQueryExp {
	 
	  private SourceQueryExp child; 
	  
	  AverageQueryExp( SourceQueryExp child ) {
	    this.child = child;
	  }
	  def evaluate( SourceQueryContext ctx ) {
		  return ValueGrouper.evaluateAndGroup( "average", child, ctx ) { long millis, List<MetricValue> mvs ->
		      double total = 0;
		      mvs.each{ MetricValue mv -> total += mv.value; }
	        if( SourceQuery.LOGGER.isTraceEnabled() ) {
	          SourceQuery.LOGGER.trace( "evaluate: total for millis ${millis} is ${total} with ${mvs.size()} metrics");
	        }
		      return new MetricValue( new Date(millis), total / mvs.size());
		  }
	  }
	  String toString() {
	    return "average(${child})";
	  }
	}

	class MinQueryExp extends SourceQueryExp {
	 
	  private SourceQueryExp child; 
	  
	  MinQueryExp( SourceQueryExp child ) {
	    this.child = child;
	  }
	  def evaluate( SourceQueryContext ctx ) {
		  return ValueGrouper.evaluateAndGroup( "min", child, ctx ) { long millis, List<MetricValue> mvs ->
	          double min = Double.MAX_VALUE;
	          List<SourceMetric> mins = new ArrayList<SourceMetric>();
	          mvs.each{ MetricValue mv -> 
	              double prev = min;
	              min = Math.min(min,mv.value); 
	              if( prev != min ) {
	                  mins.clear();
	              } 
	              if( mv.value == min ) {
	                  mins.add( mv.reference );
	              }
	          }
	          return new MetricValue( new Date(millis), min, mins );
			}
	  }
	  String toString() {
	    return "min(${child})";
	  }
	}

	class DiffQueryExp extends SourceQueryExp {
	 
	  private int chunk;
	  private SourceQueryExp child1; 
	  private SourceQueryExp child2; 
	  
	  DiffQueryExp( int chunk, SourceQueryExp child1, SourceQueryExp child2 ) {
	    this.chunk = chunk;
	    this.child1 = child1;
	    this.child2 = child2;
	  }
	  def evaluate( SourceQueryContext ctx ) {
	      List<MetricValue> values1 = child1.evaluate( ctx );
	      List<MetricValue> values2 = child2.evaluate( ctx );
		  return ValueGrouper.groupSeparately( chunk, [values1, values2] ) { long millis, List<List<MetricValue>> mvs ->
	          double avg1 = computeAverage( mvs[0] );
	          double avg2 = computeAverage( mvs[1] );
	          double diff = avg1 - avg2;
	          Date dt = new Date(millis);
	          if( SourceQuery.LOGGER.isDebugEnabled() ) { 
	              SourceQuery.LOGGER.debug("evaluate: dt=${dt}, avg1=${avg1}, avg2=${avg2}, diff=${diff}, values1=${mvs[0]}, values2=${mvs[1]}");
	          } 
	          return new MetricValue( dt, diff )
		  }
	  }

	  private double computeAverage( List<MetricValue> values ) {
	      if( values ) {
		      double accum = 0.0;
		      values.each{ MetricValue value ->
		          accum += value.value;
		      }
		      return accum / values.size();
		  }
	      return 0.0;
	  }
	  
	  String toString() {
	    return "diff(child1=${child1}, child2=${child2})";
	  }
	}

	abstract class FilteringQueryExp extends SourceQueryExp {

	  private String name; 
	  private SourceQueryExp child; 
	  private int count;

	  FilteringQueryExp( String name, int count, SourceQueryExp child ) {
	    this.name = name;
	    this.count = count;
	    this.child = child;
	  }
	  
	  abstract double findCriticalValue( List<MetricValue> values );
	  abstract void sort( List<SourceMetricCriticalValue> cvs );
	  
	  def evaluate( SourceQueryContext ctx ) {
	      Map<SourceMetric,List<MetricValue>> metrics = ValueGrouper.toValues( child.evaluate(ctx), ctx );
	      List<SourceMetricCriticalValue> cvs = new ArrayList<SourceMetricCriticalValue>();
	      metrics.each{ SourceMetric metric, List<MetricValue> values ->
	          double cv = findCriticalValue( values );
	          cvs.add( new SourceMetricCriticalValue(metric:metric,value:cv) ) ;
	      }
	      sort( cvs );
	      int countOrLast = Math.min(count,cvs.size()) - 1;
	      return new HashSet<SourceMetric>(cvs[0..countOrLast].collect{ SourceMetricCriticalValue  cv -> cv.metric } );
	  }
	  String toString() {
	    return "${name}(${child})";
	  }
	}

	class BottomQueryExp extends FilteringQueryExp {
	 
	  private SourceQueryExp child; 
	  private int count;

	  BottomQueryExp( int count, SourceQueryExp child ) {
	    super( "bottom", count, child );
	  }
	  double findCriticalValue( List<MetricValue> values ) {
	      double min = Double.MAX_VALUE;
	      values.each{ MetricValue value ->
	          min = Math.min( min, value.value );
	      }
	      return min;
	  }
	  void sort( List<SourceMetricCriticalValue> cvs ) {
	      cvs.sort{ SourceMetricCriticalValue a, SourceMetricCriticalValue b ->
	          return a.value <=> b.value;
	      }
	  }
	}

	class BottomAverageQueryExp extends FilteringQueryExp {
	 
	  private SourceQueryExp child; 
	  private int count;

	  BottomAverageQueryExp( int count, SourceQueryExp child ) {
	    super( "bottomavg", count, child );
	  }
	  double findCriticalValue( List<MetricValue> values ) {
	      double total = 0;
	      values.each{ MetricValue value ->
	          total += value.value;
	      }
	      return total / values.size();
	  }
	  void sort( List<SourceMetricCriticalValue> cvs ) {
	      cvs.sort{ SourceMetricCriticalValue a, SourceMetricCriticalValue b ->
	          return a.value <=> b.value;
	      }
	  }
	}

	class MaxQueryExp extends SourceQueryExp {
	 
	  private SourceQueryExp child; 
	  
	  MaxQueryExp( SourceQueryExp child ) {
	    this.child = child;
	  }
	  def evaluate( SourceQueryContext ctx ) {
		  return ValueGrouper.evaluateAndGroup( "max", child, ctx ) { long millis, List<MetricValue> mvs ->
	          double max = Double.MIN_VALUE;
	          List<SourceMetric> maxs = new ArrayList<SourceMetric>();
			  mvs.each{ MetricValue mv -> 
	              double prev = max;
	              max = Math.max(max,mv.value); 
	              if( prev != max ) {
	                  maxs.clear();
	              } 
	              if( mv.value == max ) {
	                  maxs.add( mv.reference );
	              }
	          }
			  return new MetricValue( new Date(millis), max, maxs);
	      }
	  }
	  String toString() {
	    return "max(${child})";
	  }
	}

	class TopQueryExp extends FilteringQueryExp {
	 
	  private SourceQueryExp child; 
	  private int count;

	  TopQueryExp( int count, SourceQueryExp child ) {
	    super( "top", count, child );
	  }
	  double findCriticalValue( List<MetricValue> values ) {
	      double max = Double.MIN_VALUE;
	      values.each{ MetricValue value ->
	          max = Math.max( max, value.value );
	      }
	      return max;
	  }
	  void sort( List<SourceMetricCriticalValue> cvs ) {
	      cvs.sort{ SourceMetricCriticalValue a, SourceMetricCriticalValue b ->
	          return b.value <=> a.value;
	      }
	  }
	}

	class TopAverageQueryExp extends FilteringQueryExp {
	 
	  private SourceQueryExp child; 
	  private int count;

	  TopAverageQueryExp( int count, SourceQueryExp child ) {
	    super( "topavg", count, child );
	  }
	  double findCriticalValue( List<MetricValue> values ) {
	      double total = 0;
	      values.each{ MetricValue value ->
	          total += value.value;
	      }
	      return total / values.size();
	  }
	  void sort( List<SourceMetricCriticalValue> cvs ) {
	      cvs.sort{ SourceMetricCriticalValue a, SourceMetricCriticalValue b ->
	          return b.value <=> a.value;
	      }
	  }
	}

	class SourceQueryTypeExp<X> extends SourceQueryExp
	{
	  private String operator;
	  private String parameter;
	  private Class<X> cls;
	  private Closure closure;
	  
	  SourceQueryTypeExp( String operator, String parameter, Class<X> cls, Closure closure ) {
	    this.operator = operator;
	    this.parameter = parameter;
	    this.cls = cls;
	    this.closure = closure;
	  }
	  def evaluate( SourceQueryContext ctx ) {
	    def ret = [];
	    for( SourceMetric metric : ctx.metrics ) {
		    SourceNode current = metric;
		    while( current ) {
			    if( cls.isInstance(current) ) {
			      if( closure.call(current) ) {
	            ret.add( metric );
	            break;
		        }
			    }
		      current = current.parent;
		    }
	    }
	    if( SourceQuery.LOGGER.isTraceEnabled() ) {
		    SourceQuery.LOGGER.trace( "evaluate(type): " + toString() + " result=${ret}" );
	    }
	    return ret;
	  }
	  String toString() {
	    return "${operator}(${cls}, ${parameter})";
	  }
	}

	class UnarySourceQueryExp extends SourceQueryExp {
	 
	  private String operator; 
	  private SourceQueryExp child; 
	  private Closure closure;
	  
	  UnarySourceQueryExp( String operator, SourceQueryExp child, Closure closure ) {
	    this.operator = operator;
	    this.child = child;
	    this.closure = closure;
	  }
	  def evaluate( SourceQueryContext ctx ) {
	      def result =  child.evaluate( ctx );
	      def ret =  closure.call( result, ctx );
	      if( SourceQuery.LOGGER.isTraceEnabled() ) {
	        SourceQuery.LOGGER.trace( "evaluate(unary): " + toString() + " result=${ret}" );
	      }
	      return ret;
	  }
	  
	  String toString() {
	    return "${operator}(${child})";
	  }
	}

	class BinarySourceQueryExp extends SourceQueryExp {

	  private String operator; 
	  private SourceQueryExp lhs; 
	  private SourceQueryExp rhs; 
	  private Closure closure;
	  
	  BinarySourceQueryExp( String operator, SourceQueryExp lhs, SourceQueryExp rhs, Closure closure ) {
	    this.operator = operator;
	    this.lhs = lhs;
	    this.rhs = rhs;
	    this.closure = closure;
	  }
	  def evaluate( SourceQueryContext ctx ) {
	      def lresult =  lhs.evaluate( ctx );
	      def rresult =  rhs.evaluate( ctx );
	      def ret = closure.call( lresult, rresult, ctx );
	      if( SourceQuery.LOGGER.isTraceEnabled() ) {
	        SourceQuery.LOGGER.trace( "evaluate(binary): " + toString() + "lresult=${lresult}, rresult=${rresult}, result=${ret}" );
	      }
	      return ret;
	  }
	  
	  String toString() {
	    return "${operator}(${lhs}, ${rhs} )";
	  }
	}

