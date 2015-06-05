package org.scriptbox.selenium;

/**
 * Created by david on 6/4/15.
 */
public class Timeout {
    private int wait;
    private int script;
    private int load;

    public int getWait() {
        return wait;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }

    public int getScript() {
        return script;
    }

    public void setScript(int script) {
        this.script = script;
    }

    public int getLoad() {
        return load;
    }

    public void setLoad(int load) {
        this.load = load;
    }
}
