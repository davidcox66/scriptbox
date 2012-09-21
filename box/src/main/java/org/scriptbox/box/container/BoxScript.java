package org.scriptbox.box.container;

import java.util.List;

import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BoxScript {

    private static final Logger LOGGER = LoggerFactory.getLogger( BoxScript.class );
    
    private static ThreadLocal<BoxScript> current = new ThreadLocal<BoxScript>(); 
    
    private BoxContext context; 
    private String name;
    private String scriptText;
    private List<String> arguments;
    
    public static BoxScript getCurrentScript() {
    	BoxScript ret = current.get();
    	if( ret == null ) {
    		throw new RuntimeException( "Not currently executing in BoxScript" );
    	}
    	return ret;
    }
    
	public static void with( BoxScript script, ParameterizedRunnable<BoxScript> runnable ) throws Exception {
		current.set( script );
		try {
			runnable.run( script );
		}
		finally {
			current.remove();
		}
	}
	
    public BoxScript( BoxContext context, String name, String scriptText, List<String> arguments ) {
        this.context = context;
        this.name = name;
        this.scriptText = scriptText;
        this.arguments = arguments;
    }
    
	public BoxContext getContext() {
		return context;
	}

	public String getName() {
		return name;
	}

	public String getScriptText() {
		return scriptText;
	}

	public List<String> getArguments() {
		return arguments;
	}
    
    
}
