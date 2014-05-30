package org.scriptbox.panopticon.gauntlet;

import org.joda.time.DateTime;

public class Message {

	private DateTime time;
	private String source;
	private int priority;
	private Object data;
	
	public Message( String source, int priority, Object data ) {
		this.time = new DateTime();
		this.source = source;
		this.priority = priority;
		this.data = data;
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
		return "Message{ time=" + time + ", source=" + source + ", priority=" + priority + ", data=" + data + "}";
	}
}
