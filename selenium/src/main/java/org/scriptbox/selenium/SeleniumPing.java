package org.scriptbox.selenium;

import java.io.Serializable;

/**
 * Created by david on 5/21/15.
 */
public interface SeleniumPing extends Serializable {
    public boolean ping( SeleniumController controller );
}
