package org.scriptbox.selenium.ext.misc;

import java.io.Serializable;

/**
 * Created by david on 6/12/15.
 */
public class Credentials implements Serializable{

    private String name;
    private String user;
    private String password;

    public Credentials() { } // for jackson

    public Credentials( String name, String user, String password ) {
        this.name = name;
        this.user = user;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
