package org.scriptbox.panopticon.util;

import java.util.Date;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

public class UtilGroovyInjector implements UtilInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger( UtilGroovyInjector.class );
	
	private UtilPlugin plugin;

	public UtilPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(UtilPlugin plugin) {
		this.plugin = plugin;
	}

	public String getLanguage() {
		return "groovy";
	}
	
	public void inject( BoxContext context ) {
		Lookup vars = context.getScriptVariables();
		vars.put( "context", context.getBeans().get("Properties", HashMap.class) );
		vars.put( "getContextProperty", new MethodClosure( this, "getContextProperty")  );
		vars.put( "setContextProperty", new MethodClosure( this, "setContextProperty")  );
		vars.put( "getContextPropertyEx", new MethodClosure( this, "getContextPropertyEx")  );
		vars.put( "toDate", new MethodClosure( this, "toDate")  );
		vars.put( "toTime", new MethodClosure( this, "toTime")  );
	}
	
	public Object getContextProperty( String name ) {
		return getProperties().get( name );
	}

	public Object setContextProperty( String name, Object value ) {
		return getProperties().put( name, value );
	}
	
	public Object getContextPropertyEx( String name ) {
		Object ret = getProperties().get( name );
		if( ret == null ) {
			throw new RuntimeException( "Unable to get property: '" + name + "'" );
		}
		return ret;
	}

	private Map<String,Object> getProperties() {
		return BoxContext.getCurrentContext().getBeans().get("Properties", HashMap.class);
	}
	
	public Date toDate( String format, String text ) {
		return plugin.toDate( format, text );
	}
	
	public Date toTime( String format, String text ) {
		return plugin.toTime( format, text );
	}
}
