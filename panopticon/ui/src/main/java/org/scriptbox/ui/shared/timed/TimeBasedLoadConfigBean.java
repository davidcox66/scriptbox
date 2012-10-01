package org.scriptbox.ui.shared.timed;

import java.util.Date;

public class TimeBasedLoadConfigBean implements TimeBasedLoadConfig {
	
	private Date start;
	private Date end;
	
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
	

}
