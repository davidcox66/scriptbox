package org.scriptbox.selenium.ext.misc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.scriptbox.selenium.bind.BindUtils;
import org.scriptbox.selenium.ext.AbstractSeleniumExtension;
import org.scriptbox.selenium.ext.SeleniumExtension;
import org.scriptbox.selenium.ext.SeleniumExtensionContext;
import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.security.KeyStore;
import java.util.Properties;

/**
 * Created by david on 6/12/15.
 */
public class CredentialsExtension extends AbstractSeleniumExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialsExtension.class);

    private File privateDirectory;
    private File keystoreFile;
    private String keystorePassword;

    public void init( SeleniumExtensionContext context ) {
        super.init( context );
        context.addUsage( "[--private-dir=<directory>|$HOME/.private] [--keystore=<file> --keystore-pass=<password>|stdin] [--set-credentials --entry=<name> --user=<user> --pass=<pass>]]");
    }

    public void configure() throws Exception {
        BindUtils.bind(context.getBinding(), this, "credentials");
        setup( context.getCommandLine() );
    }

    public void setup( CommandLine cmd ) throws Exception {
        String ks = cmd.consumeArgValue("keystore", false);
        if( StringUtils.isNotEmpty(ks) ) {
            keystoreFile = new File(ks);
            if( !keystoreFile.exists() )  {
                throw new CommandLineException( "Keystore file '" + ks + "' does not exist" );
            }
            if( cmd.hasArgValue("keystore-pass") ) {
                keystorePassword = cmd.consumeArgValue("keystore-pass",true);
            }
            else if( cmd.hasArg("keystore-pass") ) {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                while( StringUtils.isEmpty(keystorePassword) )  {
                    System.out.print( "Entry keystore password: " );
                    keystorePassword = br.readLine();
                }
            }
            else {
                throw new CommandLineException( "Keystore password is required");
            }
        }
        String pd = cmd.consumeArgValue("private-dir", false );
        if( StringUtils.isNotEmpty(pd) ) {
            privateDirectory = new File(pd);
            if( !privateDirectory.exists() )  {
                throw new CommandLineException( "Private directory '" + pd + "' does not exist" );
            }
        }
        LOGGER.debug( "setup: private=" + privateDirectory + ", keystore=" + keystoreFile );
    }

    public Credentials credentials( String name ) throws Exception {
        File dir = privateDirectory != null ? privateDirectory : new File( System.getProperty("user.home"), ".private" );
        if( dir.exists() )  {
            File file = new File( dir, name );
            if( file.exists() ) {
                Properties props = new Properties();
                props.load(new FileInputStream(file));

                String user = props.getProperty("user");
                String pass = props.getProperty("pass");
                if (StringUtils.isEmpty(user) || StringUtils.isEmpty(pass)) {
                    throw new RuntimeException("No user/pass in: '" + file.getAbsolutePath() + "'");
                }
                return new Credentials(name, user, pass);
            }
        }
        if( keystoreFile != null ) {
            return getKeystoreCredentials( name );
        }
        throw new RuntimeException( "Could not find credentials: '" + name + "' in private directory or keystore");
    }

    public Credentials getKeystoreCredentials( String entry ) throws Exception {
        String secret = getSecret( entry );
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue( new StringReader(secret), Credentials.class );
    }

    public void setKeystoreCredentials( Credentials creds ) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String secret = mapper.writeValueAsString( creds );
        setSecret( creds.getName(), secret );
    }

    public String getSecret(String entry) throws Exception{

        KeyStore ks = KeyStore.getInstance("JCEKS");
        ks.load(null, keystorePassword.toCharArray());
        KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(keystorePassword.toCharArray());

        LOGGER.debug( "getSecret: opening keystore file: '" + keystoreFile + "'");
        FileInputStream stream = new FileInputStream(keystoreFile);
        ks.load(stream, keystorePassword.toCharArray());

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
        KeyStore.SecretKeyEntry ske = (KeyStore.SecretKeyEntry)ks.getEntry(entry, keyStorePP);
        PBEKeySpec keySpec = (PBEKeySpec)factory.getKeySpec( ske.getSecretKey(), PBEKeySpec.class);

        LOGGER.debug( "getSecret: reading secret - entry='" + entry + "'");
        char[] password = keySpec.getPassword();
        return new String(password);
    }

    public void setSecret(String entry, String secret) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
        SecretKey generatedSecret = factory.generateSecret(new PBEKeySpec(secret.toCharArray()));

        KeyStore ks = KeyStore.getInstance("JCEKS");
        ks.load(null, keystorePassword.toCharArray());
        KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(keystorePassword.toCharArray());

        ks.setEntry(entry, new KeyStore.SecretKeyEntry( generatedSecret), keyStorePP);

        LOGGER.debug( "setSecret: opening keystore file: '" + keystoreFile + "'");
        FileOutputStream stream = new java.io.FileOutputStream(keystoreFile);
        LOGGER.debug( "setSecret: writing secret - entry='" + entry + "'");
        ks.store(stream, keystorePassword.toCharArray());
    }
}
