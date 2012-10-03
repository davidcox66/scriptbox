package org.scriptbox.util.remoting.jetty;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.scriptbox.util.remoting.jetty.JettyJmxInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JettyJmxInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger( JettyJmxInitializer.class );

    private Server server;

    public Server getServer() {
		return server;
	}
	public void setServer( Server server ) {
        this.server = server;
    }

    public void start() throws Exception {
        LOGGER.info( "start: adding jetty JMX");
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
        server.getContainer().addEventListener(mBeanContainer);
        mBeanContainer.start();
    }
}
