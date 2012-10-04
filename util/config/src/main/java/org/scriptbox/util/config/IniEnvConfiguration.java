package org.scriptbox.util.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;

public class IniEnvConfiguration extends EnvConfiguration {

	public IniEnvConfiguration() {
	}
	
	public IniEnvConfiguration( String... environments ) {
		super( environments );
	}
	
	public IniEnvConfiguration( List<String> environments ) {
		super( environments );
	}
	
	@Override
	public AbstractConfiguration load(URL resource) throws ConfigurationException {
		HierarchicalINIConfiguration ini = new HierarchicalINIConfiguration();
		ini.setDelimiterParsingDisabled( true );
		try {
    		ini.load( new InputStreamReader(resource.openStream()));
		}
		catch( IOException ex ) {
			throw new ConfigurationException( "Failed opening resource: " + resource, ex );
		}
		return ini;
	}
}
