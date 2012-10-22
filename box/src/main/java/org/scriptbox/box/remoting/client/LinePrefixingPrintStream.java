package org.scriptbox.box.remoting.client;

import java.io.PrintStream;
import java.util.Locale;

public class LinePrefixingPrintStream extends PrintStream {

	private PrintStream delegate;
	private String prefix;
	private String cond;
	
	public LinePrefixingPrintStream( PrintStream delegate, String prefix ) {
		super( delegate );
		this.delegate = delegate;
		this.prefix = prefix;
		this.cond = prefix;
	}
	
    public void print(boolean b) {
    	delegate.print( prefix + b );
    }

    private String getPrefixOnce() {
    	String ret = cond != null ? cond : "";
    	cond = null;
    	return ret;
    }
    private String getPrefixLine() {
    	cond = prefix;
    	return prefix;
    }

    private String getPrefixFormat( String format ) {
    	return format.indexOf("\n") == -1 ? getPrefixOnce() : getPrefixLine();
    }
    
    public void print(char c) {
    	delegate.print( getPrefixOnce() + c );
    }

    public void print(int i) {
    	delegate.print( getPrefixOnce() + i );
    }

    public void print(long l) {
    	delegate.print( getPrefixOnce() + l );
    }

    public void print(float f) {
    	delegate.print( getPrefixOnce() + f );
    }

    public void print(double d) {
    	delegate.print( getPrefixOnce() + d );
    }

    public void print(char s[]) {
    	delegate.print( getPrefixOnce() + s );
    }

    public void print(String s) {
    	delegate.print( getPrefixOnce() + s );
    }

    public void print(Object obj) {
    	delegate.print( getPrefixOnce() + obj );
    }

    public void println() {
        delegate.println( getPrefixLine() );
    }

    public void println(boolean x) {
        delegate.println( getPrefixLine() + x );
    }

    public void println(char x) {
        delegate.println( getPrefixLine() + x );
    }

    public void println(int x) {
        delegate.println( getPrefixLine() + x );
    }

    public void println(long x) {
        delegate.println( getPrefixLine() + x );
    }

    public void println(float x) {
        delegate.println( getPrefixLine() + x );
    }

    public void println(double x) {
        delegate.println( getPrefixLine() + x );
    }

    public void println(char x[]) {
        delegate.println( getPrefixLine() + x );
    }

    public void println(String x) {
        delegate.println( getPrefixLine() + x );
    }

    public void println(Object x) {
        delegate.println( getPrefixLine() + x );
    }

    public PrintStream printf(String format, Object ... args) {
    	delegate.print( getPrefixOnce() );
        return format(format, args);
    }

    public PrintStream printf(Locale l, String format, Object ... args) {
    	delegate.print( getPrefixFormat(format) );
        return format(l, format, args);
    }

    public PrintStream format(String format, Object ... args) {
    	delegate.print( getPrefixFormat(format) );
        return delegate.format( format, args);
    }

    public PrintStream format(Locale l, String format, Object ... args) {
    	delegate.print( getPrefixFormat(format) );
        return delegate.format( l, format, args);
    }
}

