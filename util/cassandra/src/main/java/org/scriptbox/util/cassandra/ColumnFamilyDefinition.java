package org.scriptbox.util.cassandra;

import java.util.ArrayList;

import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.ColumnDefinition;
import me.prettyprint.hector.api.ddl.ColumnType;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.factory.HFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColumnFamilyDefinition {

	private static final Logger LOGGER = LoggerFactory.getLogger( ColumnFamilyDefinition.class );
	
    /*
     * Column family name
     */
    private String columnFamilyName;

    /*
     * ComparatorType of the key
     */
    private ComparatorType comparator;

    /*
     * Comparator type of the value
     */
    private ComparatorType subComparator;

    /**
     * Get the column family name
     * 
     * @return the columnFamilyName
     */
    public String getColumnFamilyName() {
        return columnFamilyName;
    }

    /**
     * Set the column family name
     * 
     * @param - columnFamilyName the columnFamilyName to set
     */
    public void setColumnFamilyName(String columnFamilyName) {
        this.columnFamilyName = columnFamilyName;
    }

    /**
     * Get the ComparatorType of the key
     * 
     * @return the comparator
     */
    public ComparatorType getComparator() {
        return comparator;
    }

    /**
     * Set the ComparatorType of the key.
     * 
     * @param - comparator the comparator to set
     */
    public void setComparator(ComparatorType comparator) {
        this.comparator = comparator;
    }

    /**
     * Get the ComparatorType of the Value.
     * 
     * @return the subComparator
     */
    public ComparatorType getSubComparator() {
        return subComparator;
    }

    /**
     * Set the ComparatorType of the Value.
     * 
     * @param - subComparator the subComparator to set
     */
    public void setSubComparator(ComparatorType subComparator) {
        this.subComparator = subComparator;
    }

    public void create( final String keyspace ) {
    	create( Cassandra.getOrCreateCluster(), keyspace );
    }
    public void create( final Cluster cluster, final String keyspace ) {
    	if( subComparator == null ) {
	        LOGGER.info("createColumnFamily: creating column family '{}' with comparator '{}'", columnFamilyName, comparator);
	        Cassandra.withSchemaSynchronization(cluster, new Runnable() {
	            public void run() {
	            	me.prettyprint.hector.api.ddl.ColumnFamilyDefinition cfDefinition = HFactory.createColumnFamilyDefinition(keyspace, columnFamilyName,
	                        comparator, new ArrayList<ColumnDefinition>());
	                cfDefinition.setReadRepairChance(Cassandra.getReadRepairChance());
	                cluster.addColumnFamily(cfDefinition);
	            }
	        });
    	}
    	else {
            LOGGER.info("createSuperColumnFamily: creating column family '{}' with comparator '{}' and subcomparator '{}'",
                    new Object[] { columnFamilyName, comparator, subComparator });
            // Hector interface forgot access to key data, so have to refer to Thrift implementation directly :-(
            Cassandra.withSchemaSynchronization(cluster, new Runnable() {
                public void run() {
                    ThriftCfDef cfDefinition = (ThriftCfDef) HFactory.createColumnFamilyDefinition(keyspace,
                            columnFamilyName, comparator, new ArrayList<ColumnDefinition>());
                    cfDefinition.setReadRepairChance(Cassandra.getReadRepairChance());
                    cfDefinition.setColumnType(ColumnType.SUPER);
                    cfDefinition.setSubComparatorType(subComparator);
                    cluster.addColumnFamily(cfDefinition);
                }
            });
        }
    }

}
