package org.scriptbox.ui.shared.timed;

import java.util.Date;

import com.sencha.gxt.data.shared.loader.ListLoadResult;

public interface TimeBasedLoadResult<X> extends ListLoadResult<X> {
	
	  public Date getFirst();
	  
	  public Date getStart();
	  public Date getEnd();

	  public Date getLast();
	  
}
