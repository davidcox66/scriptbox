package org.scriptbox.panopticon.jmx;

public interface JmxTypeVisitor {

	public void value( String prefix, String name, Object value );
	public void enter( String prefix, String name, Object object );
	public void exit( String prefix, String name, Object object );
}
