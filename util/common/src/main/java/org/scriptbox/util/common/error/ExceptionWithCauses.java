package org.scriptbox.util.common.error;

import java.util.Map;

public class ExceptionWithCauses extends Exception {

	private static final long serialVersionUID = 1L;

	private Map<String,String> causes;
	
	public ExceptionWithCauses( String msg, Map<String,String> causes ) {
		super( msg );
		this.causes = causes;
	}

	public Map<String, String> getCauses() {
		return causes;
	}
	
}
