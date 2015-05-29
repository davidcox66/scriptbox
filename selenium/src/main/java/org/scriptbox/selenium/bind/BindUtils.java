package org.scriptbox.selenium.bind;

import groovy.lang.Binding;
import org.codehaus.groovy.runtime.MethodClosure;

/**
 * Created by david on 5/29/15.
 */
public class BindUtils {

    public static void bind( Binding binding, Object instance, String name ) {
        binding.setVariable(name, new MethodClosure(instance, name));
    }

    public static void bind( Binding binding, Object instance, String alias, String name ) {
        binding.setVariable(alias, new MethodClosure(instance, name));
    }
}
