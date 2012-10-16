package org.scriptbox.metrics.query.groovy;


import groovy.lang.Closure

import java.util.List

import org.scriptbox.metrics.query.exp.MetricQueryExp


class Report
{
  String name;
  String tree;
  Date start = new Date(Long.MIN_VALUE);
  Date end = new Date(Long.MAX_VALUE);
  int chunk = 60;
  List<ReportElement> elements = new ArrayList<ReportElement>();

  Report() {
  }   
  
  Report( String name ) {
    this.name = name;  
  }
  
  Report( String name, String script ) {
    this( name );
    add( name, script );
  }
  
  Report( String name, Closure closure ) {
    this( name );
    add( name, closure );
  }
 
  void add( def params ) {
	  def title = params['title'];
	  if( !title ) {
		  throw new Exception( "Invalid chart: title must be specified");
	  }
	  def expression = params['expression'];
	  if( expression ) {
		  if( !(expression instanceof Closure) && !(expression instanceof MetricQueryExp) ) {
			  throw new Exception( "Invalid chart: expression is not valid");
		  }
	  }
	  else {
		  throw new Exception( "Invalid chart: expression must be specified");
	  }
		 
	  Map cp = new HashMap( params );
	  cp.remove( 'title' );
	  cp.remove( 'expression' );
	  add( title, expression, cp );
  }
  
  void add( String title, String script ) {
    add( title, new GroovyShell().evaluate("{${script}}") );
  }
  
  void add( String title, Closure closure, def params=null ) {  
    MetricQueryBuilder builder = new MetricQueryBuilder();
    closure.setDelegate(builder);
    add( title, closure.call(), params );
  }
  
  void add( String title, MetricQueryExp expression, def params=null ) {
    elements.add( new ReportElement(title:title, expression:expression, params:params) );
  }

  String toString() {
    return "Report{ name=${name}, elements=${elements} }";
  }
}
