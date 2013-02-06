package org.scriptbox.horde.metrics;

public interface Recordable {

	 public void record( boolean success, long millis );
}
