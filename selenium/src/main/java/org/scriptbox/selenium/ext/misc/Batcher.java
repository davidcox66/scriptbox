package org.scriptbox.selenium.ext.misc;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 6/13/15.
 */
public class Batcher {

    int size;
    List elements;
    Closure closure;

    public Batcher( int size, Closure closure ) {
        this.size = size;
        this.closure = closure;
        this.elements = new ArrayList( size );
    }

    public void add( Object obj ) {
        elements.add( obj );
        if( elements.size() == size ) {
            flush();
        }
    }

    public void flush() {
        if( elements.size() > 0 ) {
            closure.call( elements );
            elements = new ArrayList( size );
        }
    }

    void close() {
        flush();
    }
}
