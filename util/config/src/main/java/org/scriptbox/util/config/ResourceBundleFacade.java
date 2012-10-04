package org.scriptbox.util.config;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.Configuration;

public class ResourceBundleFacade extends ResourceBundle {

	private static final Logger LOGGER = LoggerFactory.getLogger( ResourceBundleFacade.class );
	
	private static class ConfigEnumeration implements Enumeration<String>
	{
		private Iterator<String> iter;
		
		public ConfigEnumeration( Iterator<String> iter ) {
			this.iter = iter;
		}
	    public boolean hasMoreElements() {
	        return iter.hasNext();	
	    }
	    public String nextElement() {
	    	return iter.next();
	    }
	}
	
	private Configuration config;
	
	public ResourceBundleFacade( Configuration config ) {
		this.config = config;
	}
	
	public Enumeration<String> getKeys() {
		return new ConfigEnumeration( (Iterator<String>)config.getKeys() );
	}

	protected Object handleGetObject(String key) {
		Object ret = config.getString( key );
		if( LOGGER.isDebugEnabled() ) {
			String cfg = "";
			Object prop = config.getProperty( key );
			if( ret == null || !ret.equals(prop) ) {
				cfg = " (" + prop + ")";
			}
			LOGGER.debug( "handleGetObject: " + key + "=" + ret + cfg);
		}
		return ret;
	}
}
