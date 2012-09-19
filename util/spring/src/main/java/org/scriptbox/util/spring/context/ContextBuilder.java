package org.scriptbox.util.spring.context;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;


public class ContextBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger( ContextBuilder.class );
	
	public static ApplicationContext create( final String name, final Object object, final String... configLocations ) {
		Map<String,Object> objects = new HashMap<String,Object>(1);
		objects.put( name, object);
		return create( objects, configLocations );
	}
	
	public static ApplicationContext create( final Map<String,Object> objects, final String... configLocations ) {
		final GenericApplicationContext context = new GenericApplicationContext();
		ContextBeans.with( objects, new Runnable() { 
			public void run() { 
				registerSingletons( context, objects );
				XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader( context );
				reader.loadBeanDefinitions( configLocations );
				context.refresh();
			}
		} );
		return context;
	}
		
	public static void registerSingletons( GenericApplicationContext context, Map<String,Object> objects ) {
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
		for( Map.Entry<String,Object> entry : objects.entrySet() ) {
			String key = entry.getKey();
			Object obj = entry.getValue();
			beanFactory.registerSingleton(key, obj );
		}
	}
}
