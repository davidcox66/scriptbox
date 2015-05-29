package org.scriptbox.selenium.remoting;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.io.Serializable;

/**
 * Created by david on 5/19/15.
 */
public abstract class RemotableCondition<X> implements ExpectedCondition<X>, Serializable {

    protected transient ExpectedCondition<X> localized;

    @Override
    public boolean equals(Object obj) {
        ExpectedCondition<X> d = getLocal();
        return d != this ? d.equals(obj) : super.equals(obj);
    }

    @Override
    public int hashCode() {
        ExpectedCondition<X> d = getLocal();
        return d != this ? d.hashCode() : super.hashCode();
    }

    @Override
    public String toString() {
        ExpectedCondition<X> d = getLocal();
        return d != this ? d.toString() : super.toString();
    }

    @Override
    public X apply(WebDriver input) {
        ExpectedCondition<X> d = getLocal();
        if( d == this ) {
            throw new RuntimeException("Must localize() new instance or override apply()");
        }
        return d.apply( input );
    }

    public ExpectedCondition<X> getLocal() {
        if( localized == null )  {
            localized = localize( null );
        }
        return localized;
    }

    public ExpectedCondition<X> getLocal( ServerSeleniumService service ) {
        if( localized == null )  {
            localized = localize( service );
        }
        return localized;
    }

    public ExpectedCondition<X> localize( ServerSeleniumService service ) {
        return this;
    }
}
