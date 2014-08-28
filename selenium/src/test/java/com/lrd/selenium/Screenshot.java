package com.lrd.selenium;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to apply to a test class or test method of a class run by {@link SeleniumRunner}. When
 * added, the conclusion of a test will generate a screenshot when there is an error. A screenshot
 * can be generated unconditionally if the always attribute is true.
 * 
 * @author david
 * @since Jan 12, 2012
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Screenshot {

    boolean always() default false;
}
