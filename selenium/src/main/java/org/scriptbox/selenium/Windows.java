package org.scriptbox.selenium;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by david on 5/29/15.
 */
public class Windows implements Serializable {
    private String current;
    private Set<String> all;

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public Set<String> getAll() {
        return all;
    }

    public void setAll(Set<String> all) {
        this.all = all;
    }

    public Set<String> getNonCurrentWindows() {
        Set<String> ret = new HashSet<String>( all );
        all.remove( current );
        return ret;
    }

    public String getAnyNonCurrentWindow() {
        Set<String> wins = getNonCurrentWindows();
        return !wins.isEmpty() ? wins.iterator().next() : null;
    }

    public boolean isCurrentWindow( String win ) {
        return current.equals( win );
    }

    public String toString() {
        return "Windows{ current=" + current + ", all=" + all + " }";
    }
}
