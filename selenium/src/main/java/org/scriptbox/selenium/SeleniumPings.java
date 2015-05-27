package org.scriptbox.selenium;

import java.util.regex.Pattern;

/**
 * Created by david on 5/21/15.
 */
public class SeleniumPings {

    public static class AtBaseUrl implements SeleniumPing {
        private String url;

        public AtBaseUrl( String url ) {
            this.url = url;
        }

        public boolean ping( SeleniumController controller ) {
            return controller.isAtLocation( url );
        }
    }

    public static class AtUrlPattern implements SeleniumPing {
        private Pattern pattern;

        public AtUrlPattern( Pattern pattern ) {
            this.pattern = pattern;
        }

        public boolean ping( SeleniumController controller ) {
            return controller.isAtLocation( pattern );
        }
    }
}
