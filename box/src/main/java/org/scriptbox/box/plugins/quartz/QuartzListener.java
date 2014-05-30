package org.scriptbox.box.plugins.quartz;


public interface QuartzListener {

	public void jobStarted( QuartzInvocationContext ctx ) throws Exception;
}
