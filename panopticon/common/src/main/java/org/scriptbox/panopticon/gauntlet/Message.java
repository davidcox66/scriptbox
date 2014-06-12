package org.scriptbox.panopticon.gauntlet;

import java.util.UUID;
import org.joda.time.DateTime;

public class Message {

	private String uuid;
	private DateTime time;
	private String source;
	private int priority;
	private Object data;
	
	public Message( String source, int priority, Object data ) {
		uuid = UUID.randomUUID().toString();
		this.time = new DateTime();
		this.source = source;
		this.priority = priority;
		this.data = data;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public DateTime getTime() {
		return time;
	}
	
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
	public String toString() {
		return "Message{ uuid=" + uuid + ", time=" + time + ", source=" + source + ", priority=" + priority + ", data=" + data + "}";
	}
}
