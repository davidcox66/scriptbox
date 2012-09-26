package org.scriptbox.util.cassandra.heartbeat;

import java.util.HashMap;
import java.util.Map;

class Heartbeat {

    private int ttl;
    private String group;
    private String id;
    private Map<String,String> parameters = new HashMap<String,String>();
    
	public Heartbeat( String group, String id, int ttl ) {
		this.group = group;
		this.id = id;
		this.ttl = ttl;
	}
    
	
    public int getTtl() {
		return ttl;
	}


	public String getGroup() {
		return group;
	}


	public String getId() {
		return id;
	}


	public Map<String, String> getParameters() {
		return parameters;
	}


	public String toString() {
        return "Heartbeat{ group=" + group + ", id=" + id + " , ttl=" + ttl + ", parameters=" + parameters + " }";
    }
}

