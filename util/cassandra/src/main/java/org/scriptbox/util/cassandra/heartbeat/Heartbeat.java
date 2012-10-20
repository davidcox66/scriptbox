package org.scriptbox.util.cassandra.heartbeat;

import java.io.Serializable;
import java.util.List;


public class Heartbeat<X> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String group;
    private String id;
    private String type;
    private List<String> tags;
    private X data;
    
	public Heartbeat( String group, String type, String id, List<String> tags, X data ) {
		this.group = group;
		this.type = type;
		this.id = id;
		this.tags = tags;
		this.data = data;
	}
	

	public String getGroup() {
		return group;
	}

	public String getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	public List<String> getTags() {
		return tags;
	}

	public Object getData() {
		return data;
	}

	public String toString() {
        return "Heartbeat{ group=" + group + ", type=" + type + ", id=" + id + ", tags=" + tags + ", data=" + data + " }";
    }
}

