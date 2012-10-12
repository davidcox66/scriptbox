package org.scriptbox.metrics.query.groovy;

class ReportBuilder {

	private String script;
	private Report report;

	ReportBuilder( File file ) {
		this( file.text );
	}
	ReportBuilder( String script ) {
		this.script = script;
	}

	Report compile() {
		return compile( new GroovyShell().evaluate("{ it ->\n${script}}") );
	}

	Report compile( Closure closure ) {
		report = new Report();
		closure.setDelegate( this );
		closure.call();
		return report;
	}

	void tree( String tree ) {
		report.tree = tree;
	}
	void name( String name ) {
		report.name = name;
	}
	void start( Date start ) {
		report.start = start;
	}
	void end( Date end ) {
		report.end = end;
	}
	void chunk( int chunk ) {
		report.chunk = chunk;
	}
	void chart( String name, Closure closure ) {
		report.add( name, closure );
	}

	Date now() {
		return new Date();
	}

	Date minutesAgo( int minutes ) {
		Calendar cal = Calendar.getInstance();
		cal.add( Calendar.MINUTE, -minutes );
		return cal.getTime();
	}
	Date hoursAgo( int hours ) {
		Calendar cal = Calendar.getInstance();
		cal.add( Calendar.HOUR_OF_DAY, -hours );
		return cal.getTime();
	}
}
