package org.scriptbox.selenium.ext.contact;

import org.joda.time.DateTime;

import java.util.LinkedList;
import java.util.UUID;

public class Delivery {

	private String uuid;
	private DateTime time;
	private int minPriority;
	private int maxPriority;
	private int count;
	private LinkedList<Notification> notifications = new LinkedList<Notification>();
	
	public Delivery() {
		uuid = UUID.randomUUID().toString();
	}

	public String getUuid() {
		return uuid;
	}
	
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
	public LinkedList<Notification> getNotifications() {
		return notifications;
	}
	public void setNotifications(LinkedList<Notification> notifications) {
		this.notifications = notifications;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	public String toString() {
		return "Delivery{ uuid=" + uuid + ", time=" + time + ", min=" + minPriority + ", max=" + maxPriority + ", count=" + count + " }";
	}
	
}
