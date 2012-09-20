package com.ihg.atp.crs.loadgen.metrics

class ThreadCount extends ScriptMetric {

    @Override
    public int getValue() {
        return script.getNumRunners();
    }

    @Override
    public String getName() {
        return "threads";
    }

}
