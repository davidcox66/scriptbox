package org.scriptbox.util.remoting.jetty;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.scriptbox.util.remoting.jetty.JettyService;
import org.scriptbox.util.spring.context.ContextBuilder;

public class TestServiceTest {

	@Test
	public void testService() throws Exception {
		
		JettyService jetty = new JettyService("classpath:/META-INF/spring/remoting-server-context.xml");
		jetty.start();
	
		Map<String,Object> vars = new HashMap<String,Object>();
		vars.put( "jetty", jetty );
		ApplicationContext clientContext = ContextBuilder.create( vars, new String[] { "classpath:/META-INF/spring/remoting-client-context.xml" });
		TestServiceInterface service = clientContext.getBean( "service", TestServiceInterface.class );
		Assert.assertEquals( "abc", service.testMe("abc") );
	}
}
