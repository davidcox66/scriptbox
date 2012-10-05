package org.scriptbox.panopticon.capture;

import org.scriptbox.box.jmx.proc.GenericProcess;


public class CaptureResult {

	  /**
	   * The outermost identifier/container/source of the statistic. This may be a process name or an object name.
	   * It is relatively user-defined and can be customized by altering/constructing CaptureReults manually 
	   * during statistics collection.
	   */
	  public GenericProcess process;
	  
	  /**
	   * The attribute name under observations
	   */
	  public String attribute;

	  /**
	   * The statistic of the associated attribute  (totals, averages, min, max, etc)
	   */
	  public String statistic;
	  
	  /**
	   * The value of the statistic. This could be potentially a complex object, not just a simple value. This
	   * allows for some flexibility but requires Sink implementations to be aware of this.
	   */
	  public Object value; 

	  /**
	   * A timestamp to associate with the metric. Not normally needed unless the statistics are already gathered
	   * with timestamps in cases such as Amazon
	   */
	  public long millis;
	  
	  public CaptureResult( GenericProcess process, String attribute, String statistic, Object value ) {
	    this.process = process;
	    this.attribute = attribute;
	    this.statistic = statistic;
	    this.value = value;
	  }
	  
	  public CaptureResult( GenericProcess process, String attribute, String statistic, Object value, long millis ) {
	    this.process = process;
	    this.attribute = attribute;
	    this.statistic = statistic;
	    this.value = value;
	    this.millis = millis;
	  }
	  
	  public String toString() {
		  return "CaptureResult{ process=" + process + ", attribute=" + attribute + ", statistics=" + statistic + ", value=" + value + ", millis=" + millis + " }";
	  }
}
