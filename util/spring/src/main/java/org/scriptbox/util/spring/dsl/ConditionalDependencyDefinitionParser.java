package org.scriptbox.util.spring.dsl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Implements a conditional block in a spring config where the nested bean element will only
 * be added to the ApplicationContext if the dependent classes exist.
 * 
 * @author david
 */
public class ConditionalDependencyDefinitionParser implements BeanDefinitionParser {

  private static Logger LOGGER = LoggerFactory.getLogger( ConditionalDependencyDefinitionParser.class );
  
	public BeanDefinition parse(Element element, ParserContext parserContext) {
	    if (DomUtils.nodeNameEquals(element, "dependency")) {
	        String classNames = element.getAttribute("classes");
		    if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "parse: classes: " + classNames); }
		    String[] classNameArray = classNames.split( "," );
		    boolean enabled = true;
		    for( String className : classNameArray ) {
		    	className = className.trim();
		        if( StringUtils.isNotEmpty(className) ) {
		          try { 
		        	  Class.forName( className );
		          }
		          catch( ClassNotFoundException ex ) {
		        	  LOGGER.info( "Class '"  + className + "' not found, not instantiating beans" );
		        	  enabled = false;
		          }
		        }
	        }
		    if( enabled ) {
		    	if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "parse: dependencies resolved: " + classNames); }
			    return Registrar.parseAndRegisterNestedBean(element, parserContext);
		    }
	    }
	    return null;
	}
}