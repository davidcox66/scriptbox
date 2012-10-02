package org.scriptbox.ui.shared.timed;

import java.util.Date;
import java.util.logging.Logger;

import com.sencha.gxt.data.shared.loader.DataProxy;
import com.sencha.gxt.data.shared.loader.DataReader;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.Loader;
import com.sencha.gxt.data.shared.loader.PagingLoadConfigBean;

public class TimeBasedLoader<C extends TimeBasedLoadConfig, D extends TimeBasedLoadResult<?>> extends Loader<C, D> {

	private static final Logger logger = Logger.getLogger("TimeBasedLoader");
	
	  private Date first;
	  private Date start;
	  private Date end;
	  private Date last;
	  
	  public TimeBasedLoader(DataProxy<C, D> proxy) {
	    super(proxy);
	  }

	  public <T> TimeBasedLoader(DataProxy<C, T> proxy, DataReader<D, T> reader) {
	    super(proxy, reader);
	  }

	  public Date getFirst() {
		return first;
	  }

	  public void setFirst(Date first) {
		  this.first = first;
	  }

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

	  public Date getLast() {
		  return last;
	  }

	  public void setLast(Date last) {
		  this.last = last;
	  }

	  public void load(Date start, Date end) {
		  this.start = start;
		  this.end = end;
		  load();
	  }

	  /**
	   * Use the specified LoadConfig for all load calls,
	   * {@link #setReuseLoadConfig(boolean)} will be set to true.
	   */
	  public void useLoadConfig(C loadConfig) {
	    setReuseLoadConfig(true);
	    setLastLoadConfig(loadConfig);

	    start = loadConfig.getStart();
	    end = loadConfig.getEnd();
	  }

	  @SuppressWarnings("unchecked")
	  @Override
	  protected C newLoadConfig() {
	    return (C) new PagingLoadConfigBean();
	  }

	  /**
	   * Called when the remote data has been received.
	   * <p/>
	   * Any subclass that overrides <code>onLoadSuccess</code> should call
	   * <code>super.onLoadSuccess</code> with the result to make sure the loader
	   * and the result have consistent data.
	   */
	  @Override
	  protected void onLoadSuccess(C loadConfig, D result) {
		logger.info( "onLoadSuccess: config=" + loadConfig + ", result=" + result );
	    first = result.getFirst();
	    last = result.getLast();
	    fireEvent(new LoadEvent<C, D>(loadConfig, result));
	  }

	  @Override
	  protected C prepareLoadConfig(C config) {
	    config = super.prepareLoadConfig(config);
	    config.setStart(start);
	    config.setEnd(end);
	    return config;
	  }

}
