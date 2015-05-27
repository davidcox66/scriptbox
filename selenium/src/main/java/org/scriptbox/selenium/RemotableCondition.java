package org.scriptbox.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.io.Serializable;

/**
 * Created by david on 5/19/15.
 */
public abstract class RemotableCondition<X> implements ExpectedCondition<X>, Serializable {

    protected transient ExpectedCondition<X> delegate;

    @Override
    public boolean equals(Object obj) {
        return getDelegate().equals(obj);
    }

    @Override
    public int hashCode() {
        return getDelegate().hashCode();
    }

    @Override
    public String toString() {
        return getDelegate().toString();
    }

    @Override
    public X apply(WebDriver input) {
        return getDelegate().apply( input );
    }

    public ExpectedCondition<X> getDelegate() {
        if( delegate == null )  {
            delegate = create();
        }
        return delegate;
    }

    protected abstract ExpectedCondition<X> create();
}
