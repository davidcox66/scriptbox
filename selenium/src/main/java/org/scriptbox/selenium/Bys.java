package org.scriptbox.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by david on 6/9/15.
 */
public class Bys {

    private static final String[] CSS_DELIM = {
            "#",
            ".",
            ">",
            "[",
            " "
    };

    private static final String[] HTML_TAGS = {
            "div",
            "span",
            "h1",
            "h2",
            "h3",
            "body",
            "form",
            "input",
            "button",
            "img",
            "select",
            "table",
            "tbody",
            "tr",
            "td",
            "ul",
            "li",
            "a",
            "p"
    };

    public static class ByAny extends By implements Serializable {

        private static final long serialVersionUID = 1L;

        private By[] bys;

        public ByAny(By... bys) {
            this.bys = bys;
        }

        @Override
        public WebElement findElement(SearchContext context) {
            List<WebElement> elements = findElements(context);
            if (elements.isEmpty())
                throw new NoSuchElementException("Cannot locate an element using " + toString());
            return elements.get(0);
        }

        @Override
        public List<WebElement> findElements(SearchContext context) {
            for (By by : bys) {
                List<WebElement> elems = by.findElements(context);
                if( elems != null && elems.size() > 0 ) {
                    return elems;
                }
            }
            return new ArrayList<WebElement>();
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder("By.any(");
            stringBuilder.append("{");

            boolean first = true;
            for (By by : bys) {
                stringBuilder.append((first ? "" : ",")).append(by);
                first = false;
            }
            stringBuilder.append("})");
            return stringBuilder.toString();
        }
    }

    public static By byGuess( String val ) {
        if( isXpath(val) ) {
            return By.xpath( val );
        }
        else if( isCss(val) ) {
            return By.cssSelector( val );
        }
        else {
            return By.id( val );
        }
    }

    public static boolean isXpath( String val ) {
        return val.startsWith( "/" );
    }

    public static boolean isCss( String val ) {
        if( isCssDelim(val.substring(0,1)) ) {
            return true;
        }
        else {
            String tag = getHtmlTag( val );
            if( tag != null ) {
                int len = tag.length();
                if( val.length() > len ) {
                    String next = val.substring(len, len+1);
                    return isCssDelim(next);
                }
                return true;

            }
            return false;
        }
    }

    public static boolean isCssDelim( String val ) {
        for( String sym : CSS_DELIM ) {
            if( val.equals(sym) ) {
                return true;
            }
        }
        return false;
    }

    public static String getHtmlTag( String val ) {
        for( String tag : HTML_TAGS ) {
            if( val.startsWith(tag) ) {
                return tag;
            }
        }
        return null;
    }

}
