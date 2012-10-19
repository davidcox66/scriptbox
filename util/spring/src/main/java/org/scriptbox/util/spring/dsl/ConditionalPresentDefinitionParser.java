package org.scriptbox.util.spring.dsl;

import org.scriptbox.util.spring.context.eval.Evaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;


/**
 * Implements a conditional block in a spring config where the nested bean element will only
 * be added to the ApplicationContext if the condition is true.
 * 
 * @author david
 * @since Jun 29, 2011
 */
public class ConditionalPresentDefinitionParser implements BeanDefinitionParser {

  private static Logger LOGGER = LoggerFactory.getLogger( ConditionalPresentDefinitionParser.class );
  
	public BeanDefinition parse(Element element, ParserContext parserContext) {
	    if (DomUtils.nodeNameEquals(element, "present")) {
	        if( Evaluator.evaluateElementPresent(element) ) {
	          return Registrar.parseAndRegisterNestedBean(element, parserContext);
	        }
	    }
	
	    return null;
	}
}