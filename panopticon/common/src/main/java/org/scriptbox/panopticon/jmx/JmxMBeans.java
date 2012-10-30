package org.scriptbox.panopticon.jmx;

import java.rmi.ConnectException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;
import javax.management.QueryExp;

import org.scriptbox.box.exec.AbstractExecBlock;
import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.jmx.conn.JmxConnection;
import org.scriptbox.box.jmx.proc.JmxProcess;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxMBeans extends AbstractExecBlock<JmxCapture> {

	private static final Logger LOGGER = LoggerFactory.getLogger( JmxMBeans.class );
	
	private String objectName;
	private QueryExp expression;
	private boolean caching;
	private ParameterizedRunnableWithResult<Boolean,ObjectName> filter;

	private Map<JmxConnection,Set<ObjectName>> processObjectNames = new HashMap<JmxConnection,Set<ObjectName>>();
	   
	/**
	 * A flag that indicates the process being monitored is down so we don't keep logging the fact
	 */
	private boolean down;
	   

	public JmxMBeans( 
		String objectName, 
		QueryExp expression, 
		boolean caching,
		ParameterizedRunnableWithResult<Boolean,ObjectName> filter ) 
	{
		this.objectName = objectName;
		this.expression = expression;
		this.caching = caching;
		this.filter = filter;
	}
	
	public void run() throws Exception {
		JmxConnection connection = ExecContext.getEnclosing(JmxProcess.class).getConnection();
        if( LOGGER.isDebugEnabled() ) { 
            LOGGER.debug( "execute: querying objectName={" + objectName + "}, expression={" + expression + "}");
        }
        try {
        	Set<ObjectName> objectNames = null;
            if( caching ) {
                  objectNames = processObjectNames.get( connection );
            }
            if( objectNames == null ) {
		        objectNames = connection.queryNames(new ObjectName(objectName), expression );
		        if( filter != null ) {
		            Set<ObjectName> filtered = new HashSet<ObjectName>();
		            for( ObjectName objectName : objectNames ) {
		                if( filter.run(objectName) ) {
		                    filtered.add( objectName );
		                }
		            }
		            objectNames = filtered;
		        }
                if( caching ) {
                	processObjectNames.put( connection, objectNames );
                }
              }
              if( objectNames != null && objectNames.size() > 0 ) {
		          with( objectNames );
              }
              down = false;
          }
          catch( ConnectException ex ) {
        	  if( !down ) {
        		  LOGGER.error( "Error connecting to ${objectName}, will keep trying as statistics are collected...", ex );
        		  down = true;
        	  }
          } 
	}
}
