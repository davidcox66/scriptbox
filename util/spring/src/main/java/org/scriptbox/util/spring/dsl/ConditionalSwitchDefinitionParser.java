package org.scriptbox.util.spring.dsl;

import java.util.List;

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
 * Implements a switch/case like statement where the first case that matches the current value of
 * a system property/environment variable is evaluated and it's nested bean element is added to 
 * the ApplicationContext.
 *
 * By using this, you may be able to store various configurations of the same beans in a single
 * context.xml file and only activate specific ones.
 * 
 */
public class ConditionalSwitchDefinitionParser implements BeanDefinitionParser {

  private static Logger LOGGER = LoggerFactory.getLogger( ConditionalSwitchDefinitionParser.class );
  

	/**
	 * Parse the "cond" element and check the mandatory "test" attribute. If
	 * the system property named by test is null or empty (i.e. not defined)
	 * then return null, which is the same as not defining the bean.
	 */
	public BeanDefinition parse(Element element, ParserContext parserContext) {
	    if (DomUtils.nodeNameEquals(element, "switch")) {
	        String propOrEnvName = element.getAttribute( "property" );
	        String propOrEnvValue = Evaluator.evaluateExpression( propOrEnvName, String.class );
	        List<Element> caseElements = DomUtils.getChildElementsByTagName( element, "case" ); 
	        if( LOGGER.isDebugEnabled() ) { 
	          LOGGER.debug( "parse: " + propOrEnvName + "=" + propOrEnvValue + ", caseElements.size()=" + caseElements.size() ); 
	        }
	        for( Element child : caseElements ) {
	          BeanDefinition bd = parseCase( propOrEnvValue, child, parserContext );
	          if( bd != null ) {
	            return bd;
	          }
	        }
	        Element def =  DomUtils.getChildElementByTagName(element, "default");
	        if( def != null ) {
	          BeanDefinition bd = Registrar.parseAndRegisterNestedBean( def, parserContext );
	          if( bd != null ) {
	            return bd;
	          }
	        }
	        throw new RuntimeException( "Did not match any of the cases for the switch - name=" + propOrEnvName + ", value=" + propOrEnvValue );
	    }
	
	    return null;
	}
	
	private BeanDefinition parseCase( String propOrEnvValue, Element child, ParserContext parserContext )
	{
      String valueAttr = child.getAttribute( "value" );
      if( StringUtils.isNotEmpty(valueAttr) ) {
        if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "parseCase: checking value attribute [" + valueAttr + "]" ); }
        if( valueAttr.equals(propOrEnvValue) ) {
          if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "parseCase: found matching value attribute [" + valueAttr + "]"); }
	          return Registrar.parseAndRegisterNestedBean(child, parserContext);
        }
      }
      else {
          Element values = DomUtils.getChildElementByTagName(child, "values");
          if( values == null ) {
            throw new RuntimeException( "Must specify a value attribute or a nested set of values for a case condition");
          }
          List<Element> valueElements = DomUtils.getChildElements( values ); 
	        if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "parseCase: checking value elements - valueElements.size()=" + valueElements.size() ); }
	          for( Element value : valueElements ) {
	            String valueText = value.getTextContent();
	            if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "parseCase: checking value element [" + valueText + "]" ); }
	            if( StringUtils.isNotEmpty(valueText) && valueText.equals(propOrEnvValue) ) {
	              if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "parseCase: found matching value element [" + valueText + "]"); }
			          return Registrar.parseAndRegisterNestedBean(child, parserContext);
	            }
	         }
      }
      return null;
	}
}