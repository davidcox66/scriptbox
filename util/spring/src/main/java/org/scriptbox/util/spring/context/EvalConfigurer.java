package org.scriptbox.util.spring.context;

import java.util.Properties;

import org.scriptbox.util.spring.context.accessor.BeanPropertyAccessor;
import org.scriptbox.util.spring.context.accessor.JsonPropertyAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class EvalConfigurer extends PropertyPlaceholderConfigurer {

	private static final Logger LOGGER = LoggerFactory.getLogger( EvalConfigurer.class );

	private BeanFactory beanFactory;
	
	protected String resolvePlaceholder(final String placeholder, Properties props, int systemPropertiesMode) {
		String ret = super.resolvePlaceholder(placeholder, props, systemPropertiesMode);
		if( ret == null ) {
			ExpressionParser parser = new SpelExpressionParser();
		    Expression exp = parser.parseExpression( placeholder );
			StandardEvaluationContext ctx = new StandardEvaluationContext();
		    ctx.addPropertyAccessor( new BeanPropertyAccessor(beanFactory) );
		    ctx.addPropertyAccessor( new JsonPropertyAccessor() );
			String result = exp.getValue(ctx,String.class);
			if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "resolvePlaceholder: placeholder=" + placeholder + ", result=" + result ); }
			return result;
		}
		return ret;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory( beanFactory );
		this.beanFactory = beanFactory;
	}
}
