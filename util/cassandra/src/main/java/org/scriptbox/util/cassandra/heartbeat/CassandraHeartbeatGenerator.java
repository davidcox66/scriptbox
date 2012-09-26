package org.scriptbox.util.cassandra.heartbeat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public abstract class CassandraHeartbeatGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger( CassandraHeartbeatGenerator.class );
    
	public static final String HEARTBEATS_CF = "Heartbeats";
 
	private Cluster cluster;
    private Keyspace keyspace;
   
    private String group;
    private String id;
    private int ttl;
    private int interval;
    
    private SuperCfTemplate<String,String,String> tmpl; 
    private CassandraDownTemplate down = new CassandraDownTemplate();    
    private boolean running;
    private boolean initialized;
    
    public CassandraHeartbeatGenerator() {
    }
   
    public abstract void populate( Heartbeat beat );
   
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
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
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
        if( StringUtils.isEmpty(id) ) {
            throw new IllegalArgumentException( "ID cannot be null");
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
        this.tmpl = new ThriftSuperCfTemplate<String,String,String>( keyspace, HEARTBEATS_CF, ser, ser, ser );
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
				    Heartbeat beat = new Heartbeat( group, id, ttl );
				    try {
	                    populate( beat );
	                    send( beat );
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
    		if( !Cassandra.isExistingColumnFamily(cluster, keyspace.getKeyspaceName(), HEARTBEATS_CF) ) {
				ColumnFamilyDefinition tree = new ColumnFamilyDefinition();
				tree.setColumnFamilyName(HEARTBEATS_CF);
				tree.setComparator(ComparatorType.UTF8TYPE);
				tree.setSubComparator(ComparatorType.UTF8TYPE);
				tree.create( cluster, keyspace.getKeyspaceName() );
    		}
    		initialized = true;
		}
    	
    }
    private void send( final Heartbeat beat ) {
      HTransactionTemplate.execute( new Runnable() {
    	  public void run() {
    		  StringSerializer ser = StringSerializer.get();
    		  List<HColumn<String,String>> cols = new ArrayList<HColumn<String,String>>();
    		  for( Map.Entry<String,String> entry : beat.getParameters().entrySet() ) {
    			  cols.add( HFactory.createColumn(entry.getKey(), entry.getValue(), beat.getTtl(), ser, ser) );
    		  }
			  tmpl.createMutator().addInsertion(beat.getGroup(), HEARTBEATS_CF, 
			      HFactory.createSuperColumn(beat.getId(), cols, ser, ser, ser) );
    	  }
      } );
    }
}
