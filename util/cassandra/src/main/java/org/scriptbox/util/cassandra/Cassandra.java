package org.scriptbox.util.cassandra;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;

import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HInvalidRequestException;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.Table;
import org.apache.cassandra.service.EmbeddedCassandraService;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cassandra {
    private static final Logger LOGGER = LoggerFactory.getLogger(Cassandra.class);

    private static final int CASSANDRA_RETRIES = 30;
    private static final String EMBEDDED_CONF = "/cassandra-conf/cassandra.yaml";
    private static final String CLUSTER_NAME = "Test Cluster";
    private static final int MAX_SCHEMA_RETRIES = 30;

    private static EmbeddedCassandraService cassandra;

    public static void setEmbeddedEnabled(boolean enabled) {
        setStandalone(!enabled);
    }

    public static boolean isEmbeddedEnabled() {
        return !Boolean.parseBoolean(System.getProperty("cassandra.standalone"));
    }

    public static void setStandalone(boolean enabled) {
        System.setProperty("cassandra.standalone", "" + enabled);
    }

    public static String getHost() {
        return System.getProperty("cassandra.host", "127.0.0.1");
    }

    public static int getPort() {
        return Integer.parseInt(System.getProperty("cassandra.port", "9160"));
    }

    public static double getReadRepairChance() {
        String readRepairChanceStr = System.getProperty("cassandra.read_repair_chance", "1.0");
        return Double.parseDouble(readRepairChanceStr);
    }

    public static int getReplicationFactor() {
        String replication = System.getProperty("cassandra.replication");
        return StringUtils.isNotEmpty(replication) ? Integer.parseInt(replication) : 1;
    }

    public static String getStrategyClass() {
        return System.getProperty("cassandra.strategy.class");
    }

    public static boolean isExistingColumnFamily( Cluster cluster, String keyspaceName, String columnFamilyName ) {
    	List<KeyspaceDefinition> kss = cluster.describeKeyspaces();
    	for( KeyspaceDefinition ks : kss ) {
    		if( ks.getName().equals(keyspaceName) ) {
    			for(  me.prettyprint.hector.api.ddl.ColumnFamilyDefinition cf : ks.getCfDefs() ) {
    				if( cf.getName().equals(columnFamilyName) ) {
    					return true;
    				}
    			}
    		}
        }
		return false;
    }
    
    public static boolean isExistingKeyspace( Cluster cluster, String keyspaceName ) {
    	List<KeyspaceDefinition> kss = cluster.describeKeyspaces();
    	for( KeyspaceDefinition ks : kss ) {
    		if( ks.getName().equals(keyspaceName) ) {
    			return true;
    		}
        }
    	return false;
    }
    
    public static void createKeyspaceIfNeeded( Cluster cluster, String keyspaceName ) {
    	if( !isExistingKeyspace(cluster, keyspaceName) ) {
    		createKeyspace( cluster, keyspaceName );
    	}
    }
    public static Map<String, String> getStrategyOptions() {
        String prop = System.getProperty("cassandra.strategy.options");
        if (StringUtils.isEmpty(prop)) {
            return null;
        }
        Map<String, String> options = new HashMap<String, String>();
        String[] opts = prop.split(",");
        for (String opt : opts) {
            String[] dataCenterReplication = opt.split(":");
            if (dataCenterReplication.length != 2) {
                throw new RuntimeException("Invalid strategy option: " + opt);
            }
            options.put(dataCenterReplication[0], dataCenterReplication[1]);
        }
        return options;
    }

    public static void setConfigurationLocation() {
        if (isEmbeddedEnabled()) {
            // Tell cassandra where the configuration files are.
            URL url = Cassandra.class.getResource(EMBEDDED_CONF);
            if (url == null) {
                throw new RuntimeException("Could not find cassandra.yaml in the classpath");
            }
            System.setProperty("cassandra.config", url.toString());
        }
    }

    public static boolean isConfigurationFound() {
        return Cassandra.class.getResource(EMBEDDED_CONF) != null;
    }

    public static void waitForAvailable() throws RuntimeException {
        waitForAvailable(getOrCreateCluster());
    }

    public static void waitForAvailable(Cluster cluster) throws RuntimeException {
        // TODO: DCC Find a better way to wait for Casandra to start
        for (int count = 0; count < CASSANDRA_RETRIES; count++) {
            if (isAvailable(cluster)) {
                LOGGER.info("waitForAvailable: cassandra started");
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
                LOGGER.debug("waitForAvailable: interrupted while waiting for start", ex);
            }
        }

        throw new RuntimeException("Timed out waiting for Cassandra to start");
    }

    public static boolean isAvailable() {
        return isAvailable(getOrCreateCluster());
    }

    public static boolean isAvailable(Cluster cluster) {
        try {
            cluster.describeKeyspace(Table.SYSTEM_TABLE);
            String name = cluster.getName();
            LOGGER.info("isAvailable: cassandra cluster '{}' is running", name);
            return true;
        } catch (Exception ex) {
            LOGGER.info("isAvailable: ignoring exception ", ex);
            return false;
        }
    }

    /**
     * Set embedded cassandra up and spawn it in a new thread.
     * 
     * <p>
     * NOTE: This code has been copied to product-service and product-core. REV-77 should allow this code to be shared
     * in those other projects.
     * </p>
     * 
     * @throws ConfigurationException
     * @throws IOException
     * @throws TTransportException
     */
    synchronized public static void start() throws Exception {
    	try {
	        if (isEmbeddedEnabled()) {
	        	LOGGER.debug( "start: initializing cassandra" );
	            setConfigurationLocation();
	
	            if (cassandra == null) {
	                // loadSchemaFromYaml();
	                // clean();
	                cassandra = startDaemonAndWait();
	                dropAllKeyspaces(getOrCreateCluster());
	            }
	        	LOGGER.debug( "start: initializing complete" );
	        }
	        else {
	        	LOGGER.debug( "start: not using embedded cassandra" );
	        }
    	}
    	catch( Exception ex ) {
    		LOGGER.error( "Error starting cassandra", ex );
    		throw ex;
    	}
    }

    public static Keyspace getKeyspace( String keyspaceName ) {
    	return HFactory.createKeyspace(keyspaceName, getOrCreateCluster());
    }
    
    private static EmbeddedCassandraService startDaemonAndWait() throws TTransportException, IOException {
        EmbeddedCassandraService service = startDaemon();
        Cluster cluster = getOrCreateCluster();
        waitForAvailable(cluster);
        LOGGER.info("startDaemonAndWait: cassandra finished starting");
        return service;
    }

    private static EmbeddedCassandraService startDaemon() throws TTransportException, IOException {
        LOGGER.info("startDaemon: starting");
        EmbeddedCassandraService service = new EmbeddedCassandraService();
        service.start();
        return service;
    }

    public static void dropAllKeyspaces() {
        dropAllKeyspaces(getOrCreateCluster());
    }

    public static void dropAllKeyspaces(Cluster cluster) {
        List<KeyspaceDefinition> keyspaces = cluster.describeKeyspaces();
        for (KeyspaceDefinition keyspace : keyspaces) {
            if (!Table.SYSTEM_TABLE.equals(keyspace.getName())) {
                LOGGER.info("dropAllKeyspaces: dropping '{}'", keyspace.getName());
                cluster.dropKeyspace(keyspace.getName());
            }
        }
    }

    public static void dropKeyspace(final Cluster cluster, final String keyspace) {
        withSchemaSynchronization(cluster, new Runnable() {
            public void run() {
                try {
                    LOGGER.info("dropKeyspace: dropping '{}'", keyspace);
                    cluster.dropKeyspace(keyspace);
                } catch (HInvalidRequestException ex) {
                    if (isKeyspaceNotExistException(ex)) {
                        LOGGER.info("Keyspace does not exist yet");
                    } else {
                        throw ex;
                    }
                }
            }
        });
    }

    public static void createKeyspace(final Cluster cluster, final String keyspace) {
        LOGGER.info("createKeyspace: creating keyspace '{}'", keyspace);
        withSchemaSynchronization(cluster, new Runnable() {
            public void run() {

                KeyspaceDefinition ksDefinition = HFactory.createKeyspaceDefinition(keyspace);
                ThriftKsDef tKsDef = (ThriftKsDef) ksDefinition;

                tKsDef.setReplicationFactor(getReplicationFactor());

                String strategy = getStrategyClass();
                if (StringUtils.isNotEmpty(strategy)) {
                    Map<String, String> options = getStrategyOptions();
                    if (options == null) {
                        throw new RuntimeException("Must set cassandra.strategy.options");
                    }
                    LOGGER.info("createKeyspace: setting strategy=" + strategy + ", options=" + options);
                    tKsDef.setStrategyClass(strategy);
                    tKsDef.setStrategyOptions(options);
                }
                cluster.addKeyspace(ksDefinition);
            }
        });
    }

    public static void initializeKeyspace(final String keyspace) {
        initializeKeyspace(getOrCreateCluster(), keyspace);
    }

    public static void initializeKeyspace(Cluster cluster, final String keyspace) {
        try {
            dropKeyspace(cluster, keyspace);
            createKeyspace(cluster, keyspace);
        } catch (RuntimeException ex) {
            LOGGER.error("Error initializing keyspace", ex);
            throw ex;
        }
    }

    private static boolean isKeyspaceNotExistException(HInvalidRequestException ex) {
        return "Keyspace does not exist.".equals(ex.getWhy());
    }

    public static Cluster getOrCreateCluster() {
        setConfigurationLocation();
        String name = CLUSTER_NAME;
        int port = 9160;
        InetAddress address = null;
        if (isEmbeddedEnabled() && isConfigurationFound()) {
            name = DatabaseDescriptor.getClusterName();
            address = DatabaseDescriptor.getListenAddress();
            port = DatabaseDescriptor.getRpcPort();
        } else {
            LOGGER.warn("getOrCreateCluster: could not find cassandra config, using default localhost/9160");
            try {
                // address = InetAddress.getLocalHost();
                address = InetAddress.getByName(getHost());
                port = getPort();
            } catch (Exception ex) {
                LOGGER.error("getOrCreateCluster: could not get localhost address", ex);
                throw new RuntimeException("Unable to get localhost address", ex);
            }
        }
        LOGGER.info("getOrCreateCluster: host=" + address.getHostAddress() + ", port=" + port);
        return HFactory.getOrCreateCluster(name, address.getHostAddress() + ":" + port);
    }

    public static void withSchemaSynchronization(Cluster cluster, Runnable closure) {
        waitForAllSchemaVersionsSynchronized(cluster);
        closure.run();
        waitForAllSchemaVersionsSynchronized(cluster);
    }

    public static void waitForAllSchemaVersionsSynchronized(Cluster cluster) {
        try {
            for (int i = 0; i < MAX_SCHEMA_RETRIES; i++) {
                if (isAllSchemaVersionsSynchronized(cluster)) {
                    LOGGER.debug("waitForAllSchemaVersionsSynchronized: versions are synchronized");
                    return;
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException ex) {
            LOGGER.warn(
                    "waitForAllSchemaVersionsSynchronized: interrupted while waiting for schema synchronization, giving up",
                    ex);
            throw new RuntimeException("Interrupted while waiting for schema synchronization", ex);

        }
        LOGGER.warn("waitForAllSchemaVersionsSynchronized: gave up waiting for schema synchronization");
        throw new RuntimeException("Not all schema versions synchronized");

    }

    public static boolean isAllSchemaVersionsSynchronized(Cluster cluster) {
        Map<String, List<String>> versions = cluster.describeSchemaVersions();
        int count = 0;
        for (Map.Entry<String, List<String>> entry : versions.entrySet()) {
            String schemaUid = entry.getKey();
            List<String> hosts = entry.getValue();
            if (schemaUid == "UNREACHABLE") {
                LOGGER.warn("isAllSchemaVersionsSynchronized: nodes are down, skipping schema count: " + hosts);
            } else {
                count++;
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("isAllSchemaVersionsSynchronized: versions=" + versions + ", count=" + count);
        }
        return count == 1;
    }

    /**
     * Truncate the specified column families
     * 
     * @param keyspaceName
     *            Name of an existing keyspace that contains the column families
     *            to be truncated
     * @param columnFamilies
     *            Column families to be truncated
     * @return true if all the column families are truncated, false otherwise
     */
    public static boolean truncateColumnFamilies(String keyspaceName, String... columnFamilies) {
        boolean isAllInputColumnFamiliesTruncated = false;

        if (keyspaceName != null) {
            for (String columnFamily : columnFamilies) {
                try {
                    getOrCreateCluster().truncate(keyspaceName, columnFamily);
                    isAllInputColumnFamiliesTruncated = true;
                } catch (RuntimeException e) {
                    isAllInputColumnFamiliesTruncated = false;
                    LOGGER.error("Error when truncating column family {}. Exception: {}", keyspaceName + "."
                            + columnFamily, e.getMessage());
                }
            }
        }

        return isAllInputColumnFamiliesTruncated;
    }
}
