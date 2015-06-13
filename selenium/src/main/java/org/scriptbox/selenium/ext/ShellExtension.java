package org.scriptbox.selenium.ext;

import org.scriptbox.selenium.GroovySeleniumMethods;
import org.scriptbox.selenium.GroovySeleniumShell;
import org.scriptbox.selenium.remoting.ClientSeleniumService;
import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Created by david on 6/12/15.
 */
public class ShellExtension extends AbstractSeleniumExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellExtension.class);

    private SeleniumExtensionContext context;


    public void init( SeleniumExtensionContext context ) {
        this.context = context;
        context.addUsage( "--script=<file> [timeout=<seconds>]");
    }

    public void configure() throws Exception {
        String hostPort = context.getCommandLine().consumeArgValue("client", true);
        ClientSeleniumService service = new ClientSeleniumService( hostPort );
        GroovySeleniumShell shell = new GroovySeleniumShell( context.getBinding() );

        GroovySeleniumMethods methods = new GroovySeleniumMethods( service );
        methods.bind( context.getBinding() );

        context.setService(service);
        context.setShell(shell);
        context.setMethods(methods);

        int wait = context.getCommandLine().consumeArgValueAsInt("timeout", false);
        if( wait > 0 ) {
            methods.setWait( wait );
        }
    }

    public void run() throws Exception {
        CommandLine cmd = context.getCommandLine();
        List<String> parameters = cmd.getParameters();
        File file = getScriptFile();
        cmd.checkUnusedArgs();

        setScriptName( file );

        LOGGER.trace("client: running script=" + file);
        context.getShell().run(file, parameters);
    }

    private File getScriptFile() throws CommandLineException {
        String script = context.getCommandLine().consumeArgValue("script", true);
        if (!script.endsWith(".groovy")) {
            script += ".groovy";
        }
        return new File(script);
    }

    private void setScriptName( File file ) {
        String scriptName = file.getName();
        int pos = scriptName.lastIndexOf(".");
        if( pos != -1 ) {
            scriptName = scriptName.substring(0,pos);
        }
        context.getBinding().setVariable("scriptName", scriptName);
    }

}
