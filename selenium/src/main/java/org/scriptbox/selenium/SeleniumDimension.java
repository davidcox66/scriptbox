package org.scriptbox.selenium;

import org.openqa.selenium.Dimension;

import java.io.Serializable;

/**
 * Created by david on 5/26/15.
 */
public class SeleniumDimension extends Dimension implements Serializable {

    public SeleniumDimension(int width, int height) {
        super(width, height);
    }
}
