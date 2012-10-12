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
 
  void add( String title, String script ) {
    add( title, new GroovyShell().evaluate("{${script}}") );
  }
  
  void add( String title, Closure closure ) {  
    MetricQueryBuilder builder = new MetricQueryBuilder();
    closure.setDelegate(builder);
    add( title, closure.call() );
  }
  
  void add( String title, MetricQueryExp expression ) {
    elements.add( new ReportElement(title:title, expression:expression) );
  }

  String toString() {
    return "Report{ name=${name}, elements=${elements} }";
  }
}
