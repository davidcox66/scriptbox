package org.scriptbox.util.common.obj;

public interface ParameterizedRunnableWithResult<RET,PARM> {

	public RET run( PARM obj ) throws Exception;
}
