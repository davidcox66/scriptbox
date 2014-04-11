package org.scriptbox.metrics.beans;

public interface Recordable {

	 public void record( boolean success, long millis );
}
