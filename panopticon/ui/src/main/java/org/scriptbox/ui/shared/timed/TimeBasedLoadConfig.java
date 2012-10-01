package org.scriptbox.ui.shared.timed;

import java.util.Date;

public interface TimeBasedLoadConfig {
	
	public Date getStart();
	public void setStart( Date start );
	
	public Date getEnd();
	public void setEnd( Date end );
}
