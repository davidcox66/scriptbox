package org.scriptbox.plugins.jmx;

import javax.management.Attribute
import javax.management.ObjectName

import org.scriptbox.box.jmx.conn.JmxConnection

/**
 * This object is returned from an mbean() call. It allows you to declare an mbean method invocation in a 
 * natural way in what looks like a typical method call. This method call doesn't actually call the
 * mbean directly. It stores the method call specification so that it may be called by the load monitor.
 * 
 */
class MBeanProxy
{
  JmxConnection connection;
  ObjectName objectName;
  
  MBeanProxy( JmxConnection connection, ObjectName objectName ) {
    this.connection = connection;
	this.objectName = objectName;
  }
 
  /**
   * Called by Groovy when it can't locate the specified method name. This is how we intercept arbitrary
   * method calls and store the specification for later.
   *  
   * @param name
   * @param args
   * @return
   */
  def methodMissing( String name, def args ) {
    def signature = args.collect{ it.class.name };
    String[] signatureArray = new String[ signature.size() ];
    signature.toArray( signatureArray );
    return connection.invoke( objectName, name, args, signatureArray ); 
  }
  
  def propertyMissing(String name, value) {
      return connection.server.setAttribute( objectName, new Attribute(name,value) );
  }
  def propertyMissing(String name) {
      return connection.server.getAttribute( objectName, name );
  }
  def getAt( String name ) {
      return connection.server.getAttribute( objectName, name );
  }
  def putAt( String name, value ) {
      return connection.server.setAttribute( objectName, new Attribute(name,value) );
  }
}
