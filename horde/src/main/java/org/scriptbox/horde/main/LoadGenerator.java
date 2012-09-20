package org.scriptbox.horde.main;

import java.lang.management.ManagementFactory
import java.rmi.registry.LocateRegistry

import javax.management.MBeanServer
import javax.management.remote.JMXConnectorServerFactory
import javax.management.remote.JMXServiceURL
import javax.management.remote.rmi.RMIConnectorServer

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.ihg.atp.crs.loadgen.remoting.Controller

class LoadGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger( LoadGenerator );
   
    public static final String EXPORT_HOST = System.getProperty("controller.hostname.export",InetAddress.getLocalHost().getHostName());
    public static final String REGISTRY_HOST = System.getProperty("controller.hostname.registry",InetAddress.getLocalHost().getHostName());
    public static final int PORT = Integer.parseInt(System.getProperty("JMX_PORT","7206"));

    static void main( String[] args ) {
        try {
          setupRmiRegistry();
          Controller.register();
          LOGGER.debug( "Load generator STARTED.")
          while( true ) {
              print "."
              Thread.currentThread().sleep( 60 * 1000 );
          }
        }
        catch( Exception ex ) {
          LOGGER.error( "Error starting load generator", ex );
          System.exit( 1 );
        }
      }
    static void setupRmiRegistry() {
        // Ensure cryptographically strong random number generator used
        // to choose the object number - see java.rmi.server.ObjID
        //
        System.setProperty("java.rmi.server.randomIDs", "true");

        // Start an RMI registry on port specified by example.rmi.agent.port
        // (default 3000).
        //
        final int port = PORT;
        LOGGER.info("Create RMI registry on port "+port);
        LocateRegistry.createRegistry(port);

        // Retrieve the PlatformMBeanServer.
        //
        LOGGER.info("Get the platform's MBean server");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        // Environment map.
        //
        LOGGER.info("Initialize the environment map");
        HashMap<String,Object> env = new HashMap<String,Object>();
        
        // This where we would enable security - left out of this
        // for the sake of the example....
        //

        // Create an RMI connector server.
        //
        // As specified in the JMXServiceURL the RMIServer stub will be
        // registered in the RMI registry running in the local host on
        // port 3000 with the name "jmxrmi". This is the same name the
        // out-of-the-box management agent uses to register the RMIServer
        // stub too.
        //
        // The port specified in "service:jmx:rmi://"+hostname+":"+port
        // is the second port, where RMI connection objects will be exported.
        // Here we use the same port as that we choose for the RMI registry.
        // The port for the RMI registry is specified in the second part
        // of the URL, in "rmi://"+hostname+":"+port
        //
        final String localhost = InetAddress.getLocalHost().getHostName();
        String url = "service:jmx:rmi://"+EXPORT_HOST+
            ":"+port+"/jndi/rmi://"+REGISTRY_HOST+":"+port+"/jmxrmi";
        LOGGER.info("Create an RMI connector server: ${url}");
        JMXServiceURL jmxurl = new JMXServiceURL( url );
        
        // Now create the server from the JMXServiceURL
        //
        // JMXConnectorServer cs =
        RMIConnectorServer cs =
            JMXConnectorServerFactory.newJMXConnectorServer(jmxurl, env, mbs);

        // Start the RMI connector server.
        //
        LOGGER.info("Starting the RMI connector server on port "+port + "...");
        cs.start();
        LOGGER.info("RMI connector started");
    }
}
