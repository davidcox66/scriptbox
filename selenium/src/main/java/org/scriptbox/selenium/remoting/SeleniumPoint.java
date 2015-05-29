package org.scriptbox.selenium.remoting;

import org.openqa.selenium.Point;

import java.io.Serializable;

/**
 * Created by david on 5/26/15.
 */
public class SeleniumPoint extends Point implements Serializable {
    public SeleniumPoint(int x, int y) {
        super(x, y);
    }
}
