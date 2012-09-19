package org.scriptbox.box.container;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BoxScript {

    private static final Logger LOGGER = LoggerFactory.getLogger( BoxScript.class );
    
    /*
    private static ThreadLocal<Map<String,Object>> attributes = new ThreadLocal<Map<String,Object>>() {
        public Map<String,Object> initialValue() {
            return new HashMap<String,Object>();
        }
    };
    */
    
    private BoxContext context; 
    private String name;
    private String scriptText;
    private List<String> args;
    
    public BoxScript( BoxContext context, String name, String scriptText, List<String> args ) {
        this.context = context;
        this.name = name;
        this.scriptText = scriptText;
        this.args = args;
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

	public List<String> getArgs() {
		return args;
	}
    
    
}
