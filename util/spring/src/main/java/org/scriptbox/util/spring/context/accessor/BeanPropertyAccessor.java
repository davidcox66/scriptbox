package org.scriptbox.util.spring.context.accessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;

public class BeanPropertyAccessor extends ReflectivePropertyAccessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(BeanPropertyAccessor.class);

	private BeanFactory beanFactory;

	public BeanPropertyAccessor(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		return target != null ? super.canRead(context, target, name) : beanFactory.containsBean(name);
	}

	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
		Object ret = target != null ? super.read(context, target, name) : beanFactory.getBean(name);
		if (LOGGER.isDebugEnabled()) { LOGGER.debug("read: target=" + target + ", name=" + name + ", ret=" + ret); }
		return new TypedValue(ret);
	}

	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return false;
	}

	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
	}
}
