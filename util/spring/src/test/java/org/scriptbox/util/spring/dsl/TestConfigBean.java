package org.scriptbox.util.spring.dsl;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.scriptbox.util.spring.context.ContextBuilder;
import org.springframework.context.ApplicationContext;


public class TestConfigBean
{
  static class Config
  {
	  public boolean enabled;
	  public String value="GHI";
  }

  @Test
  public void testConditionalConfigBean() {
	  Config config = new Config();
	  config.enabled = false;
	  JSONObject json = new JSONObject();
	  JSONArray array = new JSONArray();
	  JSONObject child = new JSONObject();
	  child.put( "name", "FOO" );
	  array.add( child );
	  json.put( "array", array);
	  
	  Map<String,Object> beans = new HashMap<String,Object>();
	  beans.put( "config", config );
	  beans.put( "json", json );
	  ApplicationContext ctx = ContextBuilder.create(beans, new String[] { "classpath:META-INF/spring/test-config-context.xml"} );
	  Object testString6 = ctx.getBean( "testString6" );
	  Assert.assertEquals( "ABC", testString6 );
	  
	  config.enabled = true;
	  ctx = ContextBuilder.create(beans, new String[] { "classpath:META-INF/spring/test-config-context.xml"} );
	  testString6 = ctx.getBean( "testString6" );
	  Assert.assertEquals( "DEF", testString6 );
	  
	  Object testString7 = ctx.getBean( "testString7" );
	  Assert.assertEquals( "GHI", testString7 );
	  
	  // Object testString8 = ctx.getBean( "testString8" );
	  // Assert.assertEquals( "FOO", testString8 );
	  
	  Object testString9 = ctx.getBean( "testString8" );
	  Assert.assertEquals( "FOO", testString9 );
  }
}
