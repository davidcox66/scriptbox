package org.scriptbox.panopticon.util;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

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
		vars.put( "boxInstance", System.getProperty("box.instance") );
		vars.put( "contextName", context.getName() );
		vars.put( "context", this );
		
		vars.put( "getContextBean", new MethodClosure( this, "getContextBean")  );
		vars.put( "setContextBean", new MethodClosure( this, "setContextBean")  );
		vars.put( "getContextBean", new MethodClosure( this, "getContextBeanEx")  );
		
		vars.put( "getContextProperty", new MethodClosure( this, "getContextProperty")  );
		vars.put( "setContextProperty", new MethodClosure( this, "setContextProperty")  );
		vars.put( "getContextPropertyEx", new MethodClosure( this, "getContextPropertyEx")  );
		
		vars.put( "inject", new MethodClosure( this, "inject")  );
		vars.put( "toDate", new MethodClosure( this, "toDate")  );
		vars.put( "toTime", new MethodClosure( this, "toTime")  );
	}
	
	public void inject( String name, Object value ) {
		Lookup vars = BoxContext.getCurrentContext().getScriptVariables();
		vars.put( name, value );
	}
	
	public Object getContextBean( String name ) {
		return BoxContext.getCurrentContext().getBeans().get(name, Object.class);
	}
	
	public void setContextBean( String name, Object value ) {
		BoxContext.getCurrentContext().getBeans().put(name, value);
	}
	
	public Object getContextBeanEx( String name ) {
		return BoxContext.getCurrentContext().getBeans().getEx(name, Object.class);
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
	
	public DateTime toDate( String format, String text ) {
		return plugin.toDate( format, text );
	}
	
	public LocalTime toTime( String format, String text ) {
		return plugin.toTime( format, text );
	}
}
