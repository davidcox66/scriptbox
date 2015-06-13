package org.scriptbox.selenium.ext.contact;

import groovy.lang.Binding;
import org.apache.commons.lang.StringUtils;
import org.scriptbox.selenium.bind.BindUtils;
import org.scriptbox.selenium.ext.AbstractSeleniumExtension;
import org.scriptbox.selenium.ext.SeleniumExtension;
import org.scriptbox.selenium.ext.SeleniumExtensionContext;
import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.mail.MailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Authenticator;
import javax.mail.Session;
import java.util.Properties;

/**
 * Created by david on 5/29/15.
 */
public class ContactExtension extends AbstractSeleniumExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger( ContactExtension.class );

    private String host;
    private int port;
    private String user;
    private String password;

    private Session session;

    public void init(SeleniumExtensionContext context) {
        super.init( context );
        context.addUsage( "[--mail-host=<hostname> [--mail-port={<port>|25}] [--mail-user=<user> --mail-password=<pass>]]");
    }

    public void configure() throws Exception {

        CommandLine cmd = context.getCommandLine();

        String h = cmd.consumeArgValue( "mail-host", false );
        host = StringUtils.isNotEmpty(h) ? h : "localhost";

        int p = cmd.consumeArgValueAsInt( "mail-port", false );
        port = p != 0 ? p : 25;

        user = cmd.consumeArgValue( "mail-user", false );
        password = cmd.consumeArgValue( "mail-password", false );

        Binding binding = context.getBinding();
        BindUtils.bind(binding, this, "mailer");
        BindUtils.bind(binding, this, "message");
        BindUtils.bind(binding, this, "gauntlet");
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
        session = null;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
        session = null;
    }

    public ContactExtension mailer() {
        return this;
    }

    public MailMessage message() {
        return new MailMessage( getSession() );
    }

    public Gauntlet gauntlet() {
        return new Gauntlet();
    }

    public Gauntlet gauntlet( boolean immediate ) {
        return new Gauntlet( immediate );
    }

    private Session getSession() {
        if( session == null ) {
            if( StringUtils.isEmpty(host) || port == 0 ) {
                throw new RuntimeException( "Mail host and port must be set");
            }
            Authenticator auth = null;
            Properties props = new Properties();
            props.put("mail.smtp.port", ""+port);
            props.put("mail.smtp.host", host);
            if( StringUtils.isNotEmpty(user) ) {
                if( StringUtils.isEmpty(password) ) {
                    throw new RuntimeException( "Mail must specify password if user is present");
                }
                props.put("mail.smtp.auth", "true");
                auth = new MailAuthenticator( user, password );
            }
            // props.put("mail.smtp.starttls.enable", true);
            session = Session.getInstance( props, auth );
        }
        return session;
    }
}
