package org.scriptbox.util.spring.context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.scriptbox.util.config.EnvConfiguration;
import org.scriptbox.util.config.PropertiesEnvConfiguration;
import org.scriptbox.util.spring.context.accessor.BeanPropertyAccessor;
import org.scriptbox.util.spring.context.accessor.JsonPropertyAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class EvalConfigurer extends PropertyPlaceholderConfigurer {

	private static final Logger LOGGER = LoggerFactory.getLogger( EvalConfigurer.class );

	private boolean environmentAware;
	private Resource[] locations;
	private boolean ignoreResourceNotFound = false;
	private BeanFactory beanFactory;
	
	protected String resolvePlaceholder(final String placeholder, Properties props, int systemPropertiesMode) {
		String ret = super.resolvePlaceholder(placeholder, props, systemPropertiesMode);
		if( ret == null ) {
			ExpressionParser parser = new SpelExpressionParser();
		    Expression exp = parser.parseExpression( placeholder );
			StandardEvaluationContext ctx = new StandardEvaluationContext();
		    ctx.addPropertyAccessor( new BeanPropertyAccessor(beanFactory) );
		    ctx.addPropertyAccessor( new JsonPropertyAccessor() );
		    // ctx.addPropertyAccessor( new ContextBeansAccessor() );
			String result = exp.getValue(ctx,String.class);
			if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "resolvePlaceholder: placeholder=" + placeholder + ", result=" + result ); }
			return result;
		}
		return ret;
	}

	
	public boolean isEnvironmentAware() {
		return environmentAware;
	}


	public void setEnvironmentAware(boolean environmentAware) {
		this.environmentAware = environmentAware;
	}


	protected void loadProperties(Properties props) throws IOException {
		if( !environmentAware ) {
			if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "loadProperties: calling standard loader" ); }
			super.loadProperties( props );
			return;
		}
		try {
			if (this.locations != null) {
				List<String> environments = getEnvironmentList();
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "loadProperties: environments=" + environments ); }
				EnvConfiguration config = new PropertiesEnvConfiguration( getEnvironmentList() );
				config.setUseSystemProperties( false );
				for (Resource location : this.locations) {
					config.add( location.getURL() );
				}
				config.build();
				config.copyInto( props );
			}
		}
		catch( ConfigurationException ex ) {
			throw new IOException( "Error reading properties", ex );
		}
	}

	private List<String> getEnvironmentList() {
		String env = System.getProperty("ENV");
		if( StringUtils.isEmpty(env) ) {
			env = System.getenv("ENV");
			if( StringUtils.isNotEmpty(env) ) {
				LOGGER.debug( "getEnvironmentList: found environment variable ENV='" + env + "'" );
			}
		}
		else {
			LOGGER.debug( "getEnvironmentList: found system property ENV='" + env + "'" );
		}
		if( StringUtils.isEmpty(env) ) {
			throw new RuntimeException( "No ENV property or environment variable set");
		}
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug("getEnvironmentList: ENV='" + env + "'");	}
		
		String[] parts = env.split( "," );
		List<String> ret = new ArrayList<String>();
		for( String part : parts ) {
			part = part.trim();
			if( StringUtils.isEmpty(part) ) {
				throw new RuntimeException( "Invalid ENV - empty environment component: '" + env + "'" );
			}
			ret.add( part );
		}
		if( ret.size() == 0 ) {
			throw new RuntimeException( "Invalid ENV - empty environment" ) ;
		}
		return ret;
	}
	
	public void setLocation(Resource location) {
		this.locations = new Resource[] {location};
		super.setLocation( location );
	}

	public void setLocations(Resource[] locations) {
		this.locations = locations;
		super.setLocations( locations );
	}
	public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
		this.ignoreResourceNotFound = ignoreResourceNotFound;
		super.setIgnoreResourceNotFound(ignoreResourceNotFound);
	}
	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory( beanFactory );
		this.beanFactory = beanFactory;
	}
}
