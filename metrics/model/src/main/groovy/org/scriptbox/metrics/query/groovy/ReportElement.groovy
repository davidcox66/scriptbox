package org.scriptbox.metrics.query.groovy;

import org.scriptbox.metrics.query.exp.MetricQueryExp

class ReportElement 
{
	String title;
	MetricQueryExp expression;
	def params;
	
	String toString() {
		return "ReportElement{ title=${title}, expression=${expression} }";
	}
}
