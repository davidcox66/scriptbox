package org.scriptbox.panopticon.gauntlet;

import java.util.List;
import java.util.LinkedList;

import org.joda.time.DateTime;

public class Delivery {

	private DateTime time;
	private int minPriority;
	private int maxPriority;
	private int count;
	private LinkedList<Message> messages = new LinkedList<Message>();
	
	public DateTime getTime() {
		return time;
	}
	public void setTime(DateTime time) {
		this.time = time;
	}
	public int getMinPriority() {
		return minPriority;
	}
	public void setMinPriority(int minPriority) {
		this.minPriority = minPriority;
	}
	public int getMaxPriority() {
		return maxPriority;
	}
	public void setMaxPriority(int maxPriority) {
		this.maxPriority = maxPriority;
	}
	public LinkedList<Message> getMessages() {
		return messages;
	}
	public void setMessages(LinkedList<Message> messages) {
		this.messages = messages;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	
}
