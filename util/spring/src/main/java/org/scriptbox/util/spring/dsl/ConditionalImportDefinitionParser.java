package org.scriptbox.util.spring.dsl;

import org.apache.commons.lang.StringUtils;
import org.scriptbox.util.spring.context.eval.Evaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;


/**
 * Implements a conditional import tag
 * 
 */
public class ConditionalImportDefinitionParser implements BeanDefinitionParser {

  private static Logger LOGGER = LoggerFactory.getLogger( ConditionalImportDefinitionParser.class );
  
	public BeanDefinition parse(Element element, ParserContext parserContext) {
	    if (DomUtils.nodeNameEquals(element, "import")) {
	        if( Evaluator.evaluateElement(element) ) {
		        String resource = element.getAttribute("resource");
		        if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "parse: importing resource: " + resource); }
		        if( StringUtils.isNotEmpty(resource) ) {
		          parserContext.getReaderContext().getReader().loadBeanDefinitions( resource );
		        }
	        }
	    }
	    return null;
	}
}