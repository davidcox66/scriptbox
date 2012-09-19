package org.scriptbox.util.spring.context.eval;

import org.apache.commons.lang.StringUtils;
import org.scriptbox.util.spring.context.accessor.ContextBeansAccessor;
import org.scriptbox.util.spring.context.accessor.FallbackPropertyAccessor;
import org.scriptbox.util.spring.context.accessor.JsonPropertyAccessor;
import org.scriptbox.util.spring.context.accessor.MappablePropertyAccessor;
import org.scriptbox.util.spring.context.accessor.PropOrEnvPropertyAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;


/**
 * Implements much of the underlying condition evaluating logic for the conditional tags in this package.
 * 
 */
public class Evaluator
{
  private static final Logger LOGGER = LoggerFactory.getLogger( Evaluator.class );

  public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
  public static final String EL_PLACEHOLDER_PREFIX = "#{";
  public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";
 
  /**
   * Looks at the expression attribute (or sub-element) or the property and value attributes to
   * deterine of the condition is true. If using an expression, Spring EL will be used. Otherwise,
   * it will compare a system property/environment variable to the supplied value.
   *  
   * @param element
   * @return
   */
  public static boolean evaluateElement( Element element ) {
    String property = element.getAttribute("property");
    String value = element.getAttribute("value");
    String expression = element.getAttribute("expression");
    if( StringUtils.isEmpty(expression) ) {
      // May be useful if the expression uses tokens that are invalid in XML and enclosed in a CDATA section
      Element expressionElement = DomUtils.getChildElementByTagName(element, "expression");
      if( expressionElement != null ) {
        expression = expressionElement.getTextContent();
      }
    }
        
    boolean ret = evaluatePropertyOrExpression(property,value,expression);
    if( LOGGER.isDebugEnabled() ) { 
      LOGGER.debug( "evaluateElement: property=" + property + ", value=" + value + ", expression=" + expression + ", ret=" + ret); 
    }
    return ret;
  }
 
  /**
   * Evaluates the given Spring EL expression, if supplied, or compares the given system property/environment
   * variable to the expectedValue.
   *  
   * @param propOrEnvName
   * @param expectedValue
   * @param expression
   * @return
   */
  public static boolean evaluatePropertyOrExpression( String propOrEnvName, String expectedValue, String expression ) {
    if( StringUtils.isEmpty( propOrEnvName) && StringUtils.isEmpty(expression) ) {
      throw new RuntimeException( "Must supply either a property or expression for a conditional element");
    }
    return (StringUtils.isNotEmpty(expression) && evaluateBooleanExpression(expression)) || isPropertyOrEnvEqual( propOrEnvName, expectedValue );
  }
 
  /**
   * Evaluates a Spring EL expression, with system properties and environment variables available for
   * use in the expression. Any unqualified values are assumed to be either a system property or
   * and environment variable (if the system property is not available). You may explicitly specify
   * the system properties or environment to be used by the following syntax: system['mySystemProp']
   * or env['myEnvVar']. This notation allows '.' to be embedded the the name without being interpreted
   * as object property syntax.
   * 
   * @param expression
   * @return
   */
  public static boolean evaluateBooleanExpression( String expression ) {
	  Boolean result = evaluateExpression( expression, Boolean.class );
	  return result != null ? result.booleanValue() : null;
  }
  
  public static <X> X evaluateExpression( String expression, Class<X> cls ) {
    if( StringUtils.isNotEmpty(expression) ) {
      ExpressionParser parser = new SpelExpressionParser();
      Expression exp = parser.parseExpression( stripPlaceholders(expression) );
	  StandardEvaluationContext ctx = new StandardEvaluationContext();
	  ctx.addPropertyAccessor( new PropOrEnvPropertyAccessor() );
	  ctx.addPropertyAccessor( new MappablePropertyAccessor() );
	  ctx.addPropertyAccessor( new ContextBeansAccessor() );
	  ctx.addPropertyAccessor( new JsonPropertyAccessor() );
	  ctx.addPropertyAccessor( new FallbackPropertyAccessor() );
	  X result = exp.getValue(ctx,cls);
	  if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "evaluateExpression: expression=" + expression + ", result=" + result ); }
	  return result;
    }
    return null;
  }
 
  /**
   * Determines if the supplied system property or environment variable matches the specified value. If the value is empty,
   * then this merely checks for the existence of the property.
   * 
   * @param propOrEnvName
   * @param expectedValue
   * @return
   */
  public static boolean isPropertyOrEnvEqual( String propOrEnvName, String expectedValue ) {
    
	String propOrEnvValue = getPropertyOrEnvValue( propOrEnvName );
    boolean ret = StringUtils.isNotEmpty(propOrEnvValue) && (StringUtils.isEmpty(expectedValue) || expectedValue.equals(propOrEnvValue)); 
    if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "isPropertyOrEnvEqual: " + propOrEnvName + "=" + expectedValue + " [" + propOrEnvValue + "]"); }
    return ret;
  }
 
  /** 
   * Fetches the given system property, falling back to the environment if not found.
   * 
   * @param envOrPropName
   * @return
   */
  public static String getPropertyOrEnvValue(String envOrPropName ) {
    String ret = null;
    if (StringUtils.isNotEmpty(envOrPropName)) {
      String name = stripPlaceholders( envOrPropName );
      if( name != null ) {
        ret = System.getProperty( name );
        if( StringUtils.isEmpty(ret) ) {
          ret = System.getenv( name );
        }
      }
    }
    return ret;
  }

  /**
   * Strips '${}' tokens in a string
   * @param strVal
   * @return
   */
  private static String stripPlaceholders( String strVal ) {
    if (strVal.startsWith(DEFAULT_PLACEHOLDER_PREFIX) || strVal.startsWith(EL_PLACEHOLDER_PREFIX)) {
        if(strVal.endsWith(DEFAULT_PLACEHOLDER_SUFFIX)) {
            return strVal.substring(DEFAULT_PLACEHOLDER_PREFIX.length(), strVal.length() - DEFAULT_PLACEHOLDER_SUFFIX.length());
        }
    }
    return strVal;
  }
}
  