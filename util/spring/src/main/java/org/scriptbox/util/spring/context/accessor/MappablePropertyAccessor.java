package org.scriptbox.util.spring.context.accessor;

import org.scriptbox.util.spring.context.eval.Mappable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;

public class MappablePropertyAccessor extends ReflectivePropertyAccessor {

	private static final Logger LOGGER = LoggerFactory .getLogger(MappablePropertyAccessor.class);

	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		return target != null && target instanceof Mappable;
	}

	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
		Object ret = ((Mappable) target).get(name);
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug("resolve: target=" + target + ", name=" + name + ", ret=" + ret); }
		return new TypedValue(ret);

	}

	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return false;
	}

	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
	}
}
