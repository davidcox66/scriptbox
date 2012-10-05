package org.scriptbox.util.config;

import junit.framework.Assert;

import org.junit.Test;

import org.apache.commons.configuration.ConfigurationException;

public class EnvConfigurationTest {

	@Test
	public void testIniDevConfiguration() throws ConfigurationException {
		EnvConfiguration config = new IniEnvConfiguration( "dev" );
		config.setUseSystemProperties( false );
		config.add("testconfig.ini" );
		config.build();
		System.out.println( "testIniDevConfiguration ------------------------");
		config.dump( System.out );
		Assert.assertEquals("foobar", config.getConfiguration().getString("mystuff..prop1") );
		Assert.assertEquals("foobar", config.getConfiguration().getString("mystuff..prop2") );
	}
	
	@Test
	public void testIniLocalConfiguration() throws ConfigurationException {
		EnvConfiguration config = new IniEnvConfiguration( "local" );
		config.setUseSystemProperties( false );
		config.add("testconfig.ini" );
		config.build();
		System.out.println( "testIniLocalConfiguration ------------------------");
		config.dump( System.out );
		Assert.assertEquals("foo", config.getConfiguration().getString("mystuff..prop1") );
		Assert.assertEquals("foo", config.getConfiguration().getString("mystuff..prop2") );
	}
	
	@Test
	public void testPropDevConfiguration() throws ConfigurationException {
		EnvConfiguration config = new PropertiesEnvConfiguration( "dev" );
		config.setUseSystemProperties( false );
		config.add("testconfig.properties" );
		config.build();
		System.out.println( "testPropDevConfiguration ------------------------");
		config.dump( System.out );
		Assert.assertEquals("foobar", config.getConfiguration().getString("mystuff.prop1") );
		Assert.assertEquals("foobar", config.getConfiguration().getString("mystuff.prop2") );
	}
	
	@Test
	public void testPropLocalConfiguration() throws ConfigurationException {
		EnvConfiguration config = new PropertiesEnvConfiguration( "local" );
		config.setUseSystemProperties( false );
		config.add("testconfig.properties" );
		config.build();
		System.out.println( "testPropLocalConfiguration ------------------------");
		config.dump( System.out );
		Assert.assertEquals("foo", config.getConfiguration().getString("mystuff.prop1") );
		Assert.assertEquals("foo", config.getConfiguration().getString("mystuff.prop2") );
	}
}
