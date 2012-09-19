package org.scriptbox.util.spring.context.accessor;

import org.scriptbox.util.spring.context.eval.MapProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;

public class PropOrEnvPropertyAccessor extends ReflectivePropertyAccessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(PropOrEnvPropertyAccessor.class);

	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		return name.equals("system") || name.equals("env"); 
	}

	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
		Object ret = null;
		if ("system".equals(name)) {
			ret = new MapProxy("SystemProperties", System.getProperties());
		} 
		else if ("env".equals(name)) {
			ret = new MapProxy("Environment", System.getenv());
		}
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug("resolve: target=" + target + ", name=" + name + ", ret=" + ret); }
		return new TypedValue(ret);

	}

	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return false;
	}

	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
	}
}
