package org.scriptbox.selenium.ext;

import groovy.lang.Binding;
import org.scriptbox.selenium.SeleniumService;
import org.scriptbox.selenium.bind.BindUtils;
import org.scriptbox.selenium.bind.Bindable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by david on 5/28/15.
 */
public class HttpConnectorExtension implements Bindable, SeleniumExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnectorExtension.class);

    private File downloadDirectory;
    private SeleniumService service;

    public HttpConnectorExtension() {
    }

    public void init( SeleniumExtensionContext ctx ) {
        service = ctx.getService();
        downloadDirectory = ctx.getService().getOptions().getDownloadDirectory();
        bind(ctx.getBinding());
    }

    @Override
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
