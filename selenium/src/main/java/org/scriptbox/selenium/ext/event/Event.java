package org.scriptbox.selenium.ext.event;

import java.util.Arrays;

/**
 * Created by david on 6/10/15.
 */
public class Event {

    private Object[] elements;

    public Event( Object... args ) {
        elements = args;
    }

    public Object[] getElements() {
        return elements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(elements, event.elements);

    }

    @Override
    public int hashCode() {
        return elements != null ? Arrays.hashCode(elements) : 0;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for( Object obj : elements ) {
           if( builder.length() > 0 ) {
               builder.append( "," );
           }
            builder.append( obj );
        }
        return "Event{ " + builder + " }";
    }
}
