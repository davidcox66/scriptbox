package org.scriptbox.util.spring.context.accessor;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypedValue;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;

public class JsonPropertyAccessor extends ReflectivePropertyAccessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonPropertyAccessor.class);

	public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
		return target != null && (target instanceof JSONObject || target instanceof JSONArray || target instanceof JSONString);
	}

	public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
		Object ret = null;
		if( target instanceof JSONObject ) {
			ret = ((JSONObject)target).get( name );
		}
		else if( target instanceof JSONString ) {
			ret = ((JSONString)target).toJSONString();
		}
		if (LOGGER.isDebugEnabled()) { LOGGER.debug("read: target=" + target + ", name=" + name + ", ret=" + ret); }
		return new TypedValue(ret);
	}

	public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
		return false;
	}

	public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
	}
}
