package org.scriptbox.util.spring.dsl;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/spring/test-base-context.xml" })
public class TestConditionBean
{

  @Autowired(required=false)
  private String testString1;
  
  @Autowired(required=false)
  private String testString2;
  
  @Autowired(required=false)
  private String testString3;
  
  @Autowired(required=false)
  private String testString4;
  
  @Autowired(required=false)
  private String testString5;
  
  @Autowired(required=false)
  private String testString6;
  
  @Test
  public void testConditionalBeans() {
    String env = System.getenv("ENV");
    if( "local".equals(env) ) {
	    Assert.assertEquals( "ABC", testString1 );
    }
    else if("dev".equals(env) ) {
	    Assert.assertEquals( "DEF", testString1 );
    }
  }
  
  @Test
  public void testConditionalImports() {
    String env = System.getenv("ENV");
    if( "local".equals(env) ) {
	    Assert.assertEquals( "GHI", testString2 );
    }
    else if("dev".equals(env) ) {
	    Assert.assertEquals( "JKL", testString2 );
    }
  }
  
  @Test
  public void testConditionalSwitch() {
    String env = System.getenv("ENV");
    if( "local".equals(env) ) {
	    Assert.assertEquals( "MNO", testString3 );
    }
    else if("dev".equals(env) ) {
	    Assert.assertEquals( "PQR", testString3 );
    }
  }
  
  @Test
  public void testConditionalSwitchDefault() {
    Assert.assertEquals( "ABC", testString4 );
  }
  
  @Test
  public void testConditionalMultipleBeans() {
    Assert.assertEquals( "WER", testString5 );
  }
  
  @Test
  public void testEL() {
    Assert.assertEquals( "WER", testString6 );
  }
  

}
