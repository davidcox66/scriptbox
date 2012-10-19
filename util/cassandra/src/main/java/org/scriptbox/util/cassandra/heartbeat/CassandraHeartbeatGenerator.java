package org.scriptbox.util.cassandra.heartbeat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.prettyprint.cassandra.serializers.ObjectSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.template.SuperCfTemplate;
import me.prettyprint.cassandra.service.template.ThriftSuperCfTemplate;
import me.prettyprint.cassandra.service.tx.HTransactionTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.commons.lang.StringUtils;
import org.scriptbox.util.cassandra.Cassandra;
import org.scriptbox.util.cassandra.CassandraDownTemplate;
import org.scriptbox.util.cassandra.ColumnFamilyDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CassandraHeartbeatGenerator<X> {
    private static final Logger LOGGER = LoggerFactory.getLogger( CassandraHeartbeatGenerator.class );
    
	public static final String HEARTBEATS_CF = "Heartbeats";
 
	private Cluster cluster;
    private Keyspace keyspace;
    private String columnFamilyName = HEARTBEATS_CF;
    
    private String group;
    private String id;
    private String type;
    private List<String> tags;
    private int ttl;
    private int interval;
    
    private SuperCfTemplate<String,String,String> tmpl; 
    private CassandraDownTemplate down = new CassandraDownTemplate();    
    private boolean running;
    private boolean initialized;
    
    public CassandraHeartbeatGenerator() {
    }
   
    public abstract X data();
   
    public Cluster getCluster() {
		return cluster;
	}
	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}
	public Keyspace getKeyspace() {
		return keyspace;
	}
	public void setKeyspace(Keyspace keyspace) {
		this.keyspace = keyspace;
	}
	public String getColumnFamilyName() {
		return columnFamilyName;
	}
	public void setColumnFamilyName(String columnFamilyName) {
		this.columnFamilyName = columnFamilyName;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags( String tags ) {
		if( tags != null ) {
			String[] elems = tags.split( "," );
			this.tags = Arrays.asList(elems);
		}
		else {
			this.tags = null;
		}
	}
	
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getTtl() {
		return ttl;
	}
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	public void stop() {
    	running = false;
    }
	
    public void start() {
        if( StringUtils.isEmpty(group) ) {
            throw new IllegalArgumentException( "Group cannot be null");
        }
        if( StringUtils.isEmpty(type) ) {
            throw new IllegalArgumentException( "Type cannot be null");
        }
        if( StringUtils.isEmpty(id) ) {
            throw new IllegalArgumentException( "ID cannot be null");
        }
        if( tags == null || tags.size() == 0 ) {
            throw new IllegalArgumentException( "Tags cannot be empty");
        }
        if( StringUtils.isEmpty(columnFamilyName) ) {
        	throw new IllegalArgumentException( "Column family name cannot be empty" );
        }
        if( interval > ttl ) {
            throw new IllegalArgumentException( "Interval cannot be greater than TTL");
        }
        if( ttl <= 0 ) {
            throw new IllegalArgumentException( "TTL must be greater than 0");
        }
        if( cluster == null ) {
        	throw new IllegalArgumentException( "Cluster not set" );
        }
        if( keyspace == null ) {
        	throw new IllegalArgumentException( "Keyspace not set" );
        }
        
        StringSerializer ser = StringSerializer.get();
        this.tmpl = new ThriftSuperCfTemplate<String,String,String>( keyspace, columnFamilyName, ser, ser, ser );
    	this.running = true;
    	
        Thread thread = new Thread(new Runnable() { 
        	public void run() {
		        while( running ) {
		        	loop();
		        }
        	}
        } );
        thread.setDaemon(true);
        thread.start();
    }

    private void loop() {
        try {
            down.invoke( new Runnable() {
            	public void run() { 
            		initializeColumnFamilyIfNeeded();
				    try {
	                    X data = data();
	                    send( data );
				    }
				    catch( Exception ex ) {
				    	throw new RuntimeException( "Error sending heartbeat", ex );
				    }
                }
            } );
            Thread.sleep( interval*1000 );
        }
        catch( Exception ex ) {
            LOGGER.error( "send: unexpected exception", ex );
        }
    }
    
    private void initializeColumnFamilyIfNeeded() {
		if( !initialized ) {
    		Cassandra.createKeyspaceIfNeeded(cluster, keyspace.getKeyspaceName());
    		if( !Cassandra.isExistingColumnFamily(cluster, keyspace.getKeyspaceName(), columnFamilyName) ) {
				ColumnFamilyDefinition tree = new ColumnFamilyDefinition();
				tree.setColumnFamilyName(columnFamilyName);
				tree.setComparator(ComparatorType.UTF8TYPE);
				tree.setSubComparator(ComparatorType.UTF8TYPE);
				tree.create( cluster, keyspace.getKeyspaceName() );
    		}
    		initialized = true;
		}
    	
    }
    private void send( final X data ) {
      HTransactionTemplate.execute( new Runnable() {
    	  public void run() {
    		  try {
	    		  StringSerializer sser = StringSerializer.get();
	    		  ObjectSerializer oser = ObjectSerializer.get();
	    		  
	    		  List<HColumn<String,String>> scols = new ArrayList<HColumn<String,String>>();
				  scols.add( HFactory.createColumn("type", type, ttl, sser, sser) );
				  tmpl.createMutator().addInsertion(group, columnFamilyName, 
					  HFactory.createSuperColumn(id, scols, sser, sser, sser) );
				  
	    		  List<HColumn<String,Object>> dcols = new ArrayList<HColumn<String,Object>>();
				  dcols.add( HFactory.createColumn("data", data, ttl, sser, oser) );
				  dcols.add( HFactory.createColumn("tags", tags, ttl, sser, oser) );
				  tmpl.createMutator().addInsertion(group, columnFamilyName, 
					  HFactory.createSuperColumn(id, dcols, sser, sser, oser) );
    		  }
    		  catch( Exception ex ) {
    			  throw new RuntimeException( "Error saving heartbeat - " +
    			      "group: " + group + ", type: " + type + ", id: " + id + ", tags: " + tags + ", data: " + data, ex );
    		  }
    	  }
      } );
    }
}
