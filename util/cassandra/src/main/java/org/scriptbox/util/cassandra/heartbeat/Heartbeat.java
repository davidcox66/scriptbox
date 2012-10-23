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
    
	public Heartbeat( String id, String group, String type, List<String> tags, X data ) {
		this.id = id;
		this.group = group;
		this.type = type;
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

	public X getData() {
		return data;
	}

	public String toString() {
        return "Heartbeat{ group=" + group + ", type=" + type + ", id=" + id + ", tags=" + tags + ", data=" + data + " }";
    }


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Heartbeat other = (Heartbeat) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (tags == null) {
			if (other.tags != null)
				return false;
		} else if (!tags.equals(other.tags))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	
}

