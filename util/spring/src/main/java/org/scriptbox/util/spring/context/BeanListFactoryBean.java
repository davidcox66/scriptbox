package org.scriptbox.util.spring.context;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class BeanListFactoryBean<T> extends AbstractFactoryBean<Collection<T>> {

	private static final Logger LOGGER = LoggerFactory.getLogger( BeanListFactoryBean.class );
	
    private Class<T> beanType;

    @Required
    public void setBeanType(Class<T> beanType) {
        this.beanType = beanType;
    }

    @Override
    protected Collection<T> createInstance() throws Exception {
        Collection<T> ret = ((ListableBeanFactory)getBeanFactory()).getBeansOfType(beanType).values();
        if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "createInstance: beanType=" + beanType + ", ret=" + ret ); }
        return ret;
    }

    @Override
    public Class<?> getObjectType() {
        return Collection.class;
    }    
}