package org.scriptbox.selenium.ext.offline;

import groovy.lang.Binding;
import org.scriptbox.selenium.SeleniumService;
import org.scriptbox.selenium.bind.BindUtils;
import org.scriptbox.selenium.bind.Bindable;
import org.scriptbox.selenium.ext.AbstractSeleniumExtension;
import org.scriptbox.selenium.ext.SeleniumExtension;
import org.scriptbox.selenium.ext.SeleniumExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by david on 5/28/15.
 */
public class HttpConnectorExtension extends AbstractSeleniumExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnectorExtension.class);

    private File downloadDirectory;
    private SeleniumService service;


    public void configure() throws Exception {
        service = context.getService();
        downloadDirectory = context.getService().getOptions().getDownloadDirectory();
        bind(context.getBinding());
    }

    public void bind(Binding binding) {
        BindUtils.bind( binding, this, "http" );

    }

    public HttpConnector http() {
        HttpConnector ret = new HttpConnector( service );
        if( downloadDirectory != null ) {
            ret.setDownloads(downloadDirectory);
        }
        return ret;
    }
}
