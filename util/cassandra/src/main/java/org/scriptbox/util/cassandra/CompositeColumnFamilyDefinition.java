package org.scriptbox.util.cassandra;

import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.model.BasicColumnFamilyDefinition;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.ThriftCfDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.ddl.ColumnIndexType;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.factory.HFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a Composite column family by name, comparator, subComparator, comparatorTypeAlias, subComparatorTypeAlias and
 * keyValidationClass Supports composite column families with Key, Value or both as Composites
 * 
 * Usage: 1. Key is Composite of String, String. Value is a normal String. Set columnFamilyName String. Set comparator
 * as ComparatorType.UTF8TYPE Set keyValidationClass as ComparatorType(UTF8TYPE,UTF8TYPE) Do not set
 * comparatorTypeAlias, subComparator and subComparatorTypeAlias
 * 
 * 2. Key is a normal String. Value is a Composite of (Long, UUID). Set columnFamilyName String. Set comparator as
 * ComparatorType.UTF8TYPE Do not set comparatorTypeAlias Set subComparator as ComparatorType.COMPOSITE Set
 * subComparatorTypeAlias as (Long, UUID)
 * 
 */
public class CompositeColumnFamilyDefinition extends ColumnFamilyDefinition {

	private static final Logger LOGGER = LoggerFactory.getLogger( CompositeColumnFamilyDefinition.class );
	
    /*
     * ComparatorType Alias to describe the composites in the comparator
     */
    private String comparatorTypeAlias;

    /*
     * ComparatorType Alias to describe the composites in the subcomparator
     */
    private String subComparatorTypeAlias;

    /*
     * Validation class to be used by the composite CF
     */
    private String keyValidationClass;

 
    /**
     * Get the ComparatorTypeAlias of the Key.
     * 
     * @return the comparatorTypeAlias
     */
    public String getComparatorTypeAlias() {
        return comparatorTypeAlias;
    }

    /**
     * Set the ComparatorTypeAlias of the Key.
     * 
     * @param - comparatorTypeAlias the comparatorTypeAlias to set
     */
    public void setComparatorTypeAlias(String comparatorTypeAlias) {
        this.comparatorTypeAlias = comparatorTypeAlias;
    }

    /**
     * Get the ComparatorTypeAlias of the Value.
     * 
     * @return the subComparatorTypeAlias
     */
    public String getSubComparatorTypeAlias() {
        return subComparatorTypeAlias;
    }

    /**
     * Set the ComparatorTypeAlias of the Value.
     * 
     * @param - subComparatorTypeAlias the subComparatorTypeAlias to set
     */
    public void setSubComparatorTypeAlias(String subComparatorTypeAlias) {
        this.subComparatorTypeAlias = subComparatorTypeAlias;
    }

    /**
     * Get the validation type of the Value.
     * 
     * @return the keyValidationClass
     */
    public String getKeyValidationClass() {
        return keyValidationClass;
    }

    /**
     * Set the key validation class of the Column Family.
     * 
     * @param - keyValidationClass the ValidationClass to set
     */
    public void setKeyValidationClass(String keyValidationClass) {
        this.keyValidationClass = keyValidationClass;
    }
    
    public void create( final String keyspace ) {
    	create( Cassandra.getOrCreateCluster(), keyspace );
    }
    public void create( final Cluster cluster, final String keyspace ) {
    	if( comparatorTypeAlias == null || subComparatorTypeAlias == null ) {
	        LOGGER.info( "createCompositeColumnFamily: creating column family '{}' with comparator '{}' and subcomparator '{}'",
	                new Object[] { getColumnFamilyName(), getComparator(), getSubComparator() });
	
	        Cassandra.withSchemaSynchronization(cluster, new Runnable() {
	            public void run() {
	            	me.prettyprint.hector.api.ddl.ColumnFamilyDefinition cfd = HFactory.createColumnFamilyDefinition(keyspace, getColumnFamilyName(), getComparator());
	                if (comparatorTypeAlias != null && comparatorTypeAlias.trim().length() > 0) {
	                    cfd.setComparatorTypeAlias(comparatorTypeAlias);
	                }
	                if (subComparatorTypeAlias != null && subComparatorTypeAlias.trim().length() > 0) {
	                    cfd.setSubComparatorTypeAlias(subComparatorTypeAlias);
	                }
	                if (keyValidationClass != null && keyValidationClass.trim().length() > 0) {
	                    cfd.setKeyValidationClass(keyValidationClass);
	                }
	                
	                cluster.addColumnFamily(cfd);
	                
	            }
	        });
    	}
    	else {
	        LOGGER.info( "createCompositeColumnFamily: creating column family '{}' with comparator '{}' and subcomparator '{}'",
	                new Object[] { getColumnFamilyName(), getComparator(), getSubComparator() });
	
	        Cassandra.withSchemaSynchronization(cluster, new Runnable() {
	            public void run() {
	             
	            	BasicColumnFamilyDefinition columnFamilyDefinition = new BasicColumnFamilyDefinition();
	            	columnFamilyDefinition.setKeyspaceName(keyspace);
	            	columnFamilyDefinition.setName(getColumnFamilyName());
	            	columnFamilyDefinition.setKeyValidationClass(keyValidationClass);
	            	columnFamilyDefinition.setComparatorType(ComparatorType.UTF8TYPE);
	            	
	
	            	BasicColumnDefinition columnDefinition = new BasicColumnDefinition();
	            	columnDefinition.setName(StringSerializer.get().toByteBuffer("name_upper_case"));
	            	columnDefinition.setValidationClass(ComparatorType.UTF8TYPE.getClassName());
	            	columnDefinition.setIndexName("name_upper_case");
	            	columnDefinition.setIndexType(ColumnIndexType.KEYS);
	            	columnFamilyDefinition.addColumnDefinition(columnDefinition);
	            	me.prettyprint.hector.api.ddl.ColumnFamilyDefinition cfDef = new ThriftCfDef(columnFamilyDefinition);
	
	                cluster.addColumnFamily(cfDef);
	            }
	        });
    	}
    }
}
