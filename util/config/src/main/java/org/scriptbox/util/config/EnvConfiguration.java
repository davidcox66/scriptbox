package org.scriptbox.util.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SubsetConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.interpol.ConfigurationInterpolator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;

public abstract class EnvConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger( EnvConfiguration.class );

	// Guard against infinite recursion when using interpolation
	private static ThreadLocal<Set<Configuration>> lookupStack = new ThreadLocal<Set<Configuration>>();
	
	private Configuration configuration;
	private boolean useSystemProperties = true;
    private List<String> environments;
	private List<URL> resources = new ArrayList<URL>();
	private Map<String,Configuration> configurationsByPrefix = new HashMap<String,Configuration>();
	
    public EnvConfiguration() {
    }
    
    public EnvConfiguration( String... environments ) {
    	this.environments = Arrays.asList(environments);
    }
   
    public EnvConfiguration( List<String> environments ) {
    	this.environments = environments;
    }
    
    public abstract AbstractConfiguration load( URL resource ) throws ConfigurationException;
    
    public void setEnvironments( String...environments ) {
    	this.environments = Arrays.asList(environments);
    }
    public void setEnvironments( List<String> environments ) {
    	this.environments = environments;
    }
    
    public List<String> getEnvironments() {
    	return environments;
    }

    public boolean isUseSystemProperties() {
		return useSystemProperties;
	}

	public void setUseSystemProperties(boolean useSystemProperties) {
		this.useSystemProperties = useSystemProperties;
	}

	synchronized public Configuration getConfiguration() throws ConfigurationException {
		if( configuration == null ) {
			build();
		}
		return configuration;
	}
	
	public void add( URL url ) {
    	resources.add( url );
    }
    
    public void add( File file ) throws MalformedURLException {
    	resources.add( file.toURL() );
    }
    
    public void add( String name ) throws ConfigurationException {
		URL resource = EnvConfiguration.class.getResource( name );
		if( resource == null ) {
		    resource = EnvConfiguration.class.getResource( name );
		}
	    if( resource == null && !name.endsWith(".properties") ) {
	        resource = EnvConfiguration.class.getResource( name + ".properties" );
	    }
	    
		if( resource == null ) {
		    resource = EnvConfiguration.class.getResource( "/" + name );
		}
	    if( resource == null && !name.endsWith(".properties") ) {
	        resource = EnvConfiguration.class.getResource( "/" + name + ".properties" );
	    }
	    
		if( resource == null ) {
			throw new ConfigurationException( "Could not find resource: " + name );
		}
		else {
			LOGGER.debug( "add: found resource: '" + name + "' at: " + resource );
		}
		resources.add( resource );
    }
   
    public EnvConfiguration build() throws ConfigurationException {
    	if( environments == null || environments.size() == 0 ) {
    		throw new ConfigurationException( "No environments defined" );
    	}
    	if( resources.size() == 0 ) {
    		throw new ConfigurationException( "No resources defined" );
    	}
    	if( configuration != null ) {
    		throw new ConfigurationException( "Cannot build configuration more than once");
    	}
    	
		CompositeConfiguration all = new CompositeConfiguration();
		if( useSystemProperties ) {
			SystemConfiguration sys = new SystemConfiguration();
		    sys.setDelimiterParsingDisabled( true );
		    all.addConfiguration( sys );
		}
	
    	for( URL resource : resources ) {
			CompositeConfiguration combined = new CompositeConfiguration();
			combined.setDelimiterParsingDisabled( true );
		
			AbstractConfiguration cfg = load( resource );
			
			for( String env : environments ) {
	    		SubsetConfiguration subset = new SubsetConfiguration( cfg, env, ".");
		        subset.setDelimiterParsingDisabled( true );
	    		combined.addConfiguration( subset );
			}
			// For just plain 'property' access without environment parts
			combined.addConfiguration( cfg );
			
			all.addConfiguration( combined );
			
			String baseName = resource.getPath();
			int pos;
			if( (pos = baseName.lastIndexOf("/")) != -1 ) {
				baseName = baseName.substring(pos+1);
			}
			if( (pos = baseName.indexOf(".")) != -1 ) {
				baseName = baseName.substring(0,pos);
			}
			configurationsByPrefix.put(baseName , combined);
    	}
   
    	// Allow one property file to explicity refer to another by name in the
    	// form <other config base name>:<property name>
    	for( Configuration configuration : configurationsByPrefix.values() ) {
    		for( Map.Entry<String, Configuration> entry : configurationsByPrefix.entrySet() ) {
    			String prefix = entry.getKey();
    			Configuration conf = entry.getValue();
    			if( conf != configuration ) {
					ConfigurationInterpolator interp = ((AbstractConfiguration)configuration).getInterpolator();
					interp.registerLookup(prefix, new DelegateConfigurationLookup(conf) );
    			}
    		}
    	}	
    	configuration = all;
		return this;
    }
    
	public ResourceBundle asResourceBundle()
	    throws ConfigurationException, MalformedURLException
	{
	    return new ResourceBundleFacade( getConfiguration() );
	}
	
	public Properties asProperties()
	    throws ConfigurationException, MalformedURLException
	{
	    return new PropertiesFacade( getConfiguration() );
	}
	
	public Properties asCopiedProperties() throws ConfigurationException
	{
		Configuration config = getConfiguration();
		Properties props = new Properties();
		for( Iterator<String> iter = config.getKeys() ; iter.hasNext() ; ) {
		    String key = (String)iter.next();
		    String value = config.getString(key);
	        if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "asCopiedProperties: " + key + "='" + value + "'" ); }
		    props.setProperty( key, value );
		}
		return props;
	}

	public void dump( PrintStream stream ) throws ConfigurationException
	{
		Configuration config = getConfiguration();
		Iterator<String> iter = config.getKeys();
		while( iter.hasNext() ) {
		    String key = iter.next();
		    Object value = config.getString( key ); 
		    stream.println( key + "=" + value );
		}
	}

	public List<String> getKeyList() throws ConfigurationException
	{
		Configuration config = getConfiguration();
		List<String> ret = new ArrayList<String>();
		for( Iterator<String> iter = config.getKeys() ; iter.hasNext() ; ) {
	        String key = iter.next();
	        ret.add( key );
		}
		return ret;
	}

	public void copyInto( Properties properties )  throws ConfigurationException
	{
		Configuration config = getConfiguration();
		for( Iterator<String> iter = config.getKeys() ; iter.hasNext() ; ) {
	        String key = iter.next();
	        String value = config.getString(key);
	        if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "copyInto: " + key + "='" + value + "'" ); }
	        properties.setProperty(key, value );
		}
		
	}
	private class DelegateConfigurationLookup extends StrLookup
	{
	    private Configuration delegate;
	    
		public DelegateConfigurationLookup( Configuration delegate )
		{
			this.delegate = delegate;
		}
		
		public String lookup( String key ) 
		{
			if( StringUtils.isEmpty(key) ) {
				return null;
			}
			
			boolean top = false;
			Set<Configuration> stack = lookupStack.get();
			if( stack == null ) {
				top = true;
				stack = new HashSet<Configuration>();
				lookupStack.set( stack );
			}
			try {
				if( !stack.contains(delegate) ) {
					stack.add( delegate );
			        return delegate.getString( key );	
				}
				else {
				    // logger.error( "Detected recursion in configuration: " + key, new Exception() );
				    return null;
				}
			}
			catch( Exception ex ) {
				LOGGER.error( "Error interpolating value", ex );
				return null;
			}
			finally {
				if( top ) {
					lookupStack.remove();
				}
			}
		}
	}
}	