package org.scriptbox.horde.metrics;

public class ThreadCount extends ScriptMetric {

    @Override
    public int getValue() {
        return script.getNumRunners();
    }

    @Override
    public String getName() {
        return "threads";
    }

    public String getDescription() {
    	return "Thread Count";
    }
}
