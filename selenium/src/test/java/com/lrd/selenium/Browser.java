package com.lrd.selenium;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to apply to a test method or class of a test class which is using the {@linke SeleniumRunner}.
 * This will allow inclusion/exclusion of specific browsers.
 * 
 * @author david
 * @since Jan 12, 2012
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Browser {

    /**
     * Will only run the test with browsers specified in this list
     */
    public String[] only() default {};
    
    /**
     * Will run the test with all browser 
     */
    public String[] except() default {};
}
