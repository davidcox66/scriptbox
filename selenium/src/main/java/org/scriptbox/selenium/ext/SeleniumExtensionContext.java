package org.scriptbox.selenium.ext;

import groovy.lang.Binding;
import org.scriptbox.selenium.GroovySeleniumMethods;
import org.scriptbox.selenium.GroovySeleniumShell;
import org.scriptbox.selenium.SeleniumService;
import org.scriptbox.util.common.args.CommandLine;

import java.util.List;

/**
 * Created by david on 5/29/15.
 */
public class SeleniumExtensionContext {

    private SeleniumService service;
    private Binding binding;
    private CommandLine commandLine;
    private GroovySeleniumShell shell;
    private GroovySeleniumMethods methods;
    private List<SeleniumExtension> extensions;

    public SeleniumService getService() {
        return service;
    }

    public void setService(SeleniumService service) {
        this.service = service;
    }

    public CommandLine getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    public Binding getBinding() {
        return binding;
    }

    public void setBinding(Binding binding) {
        this.binding = binding;
    }

    public GroovySeleniumShell getShell() {
        return shell;
    }

    public void setShell(GroovySeleniumShell shell) {
        this.shell = shell;
    }

    public GroovySeleniumMethods getMethods() {
        return methods;
    }

    public void setMethods(GroovySeleniumMethods methods) {
        this.methods = methods;
    }

    public List<SeleniumExtension> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<SeleniumExtension> extensions) {
        this.extensions = extensions;
    }
}
