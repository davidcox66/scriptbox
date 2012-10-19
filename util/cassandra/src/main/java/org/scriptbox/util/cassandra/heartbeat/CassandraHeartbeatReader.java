package org.scriptbox.util.cassandra.heartbeat;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import me.prettyprint.cassandra.serializers.ObjectSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.template.SuperCfResult;
import me.prettyprint.cassandra.service.template.SuperCfTemplate;
import me.prettyprint.cassandra.service.template.ThriftSuperCfTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;

import org.scriptbox.util.cassandra.Cassandra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraHeartbeatReader<X> {
    private static final Logger LOGGER = LoggerFactory.getLogger( CassandraHeartbeatReader.class );
    
	public static final String HEARTBEATS_CF = "Heartbeats";
 
	private Cluster cluster;
    private Keyspace keyspace;
    private String columnFamilyName;
    private SuperCfTemplate<String,String,String> tmpl; 
    private Class<X> dataClass;
    
    public CassandraHeartbeatReader( Cluster cluster, Keyspace keyspace, Class<X> dataClass ) {
    	this( cluster, keyspace, HEARTBEATS_CF, dataClass );
    }
    public CassandraHeartbeatReader( Cluster cluster, Keyspace keyspace, String columnFamilyName, Class<X> dataClass ) {
        StringSerializer ser = StringSerializer.get();
        this.cluster = cluster;
        this.keyspace = keyspace;
        this.columnFamilyName = columnFamilyName;
        this.dataClass = dataClass;
        this.tmpl = new ThriftSuperCfTemplate<String,String,String>( keyspace, columnFamilyName, ser, ser, ser );
    }
   
    public List<Heartbeat<X>> list( String group ) throws Exception {
      List<Heartbeat<X>> ret = new ArrayList<Heartbeat<X>>();
      if( Cassandra.isExistingColumnFamily(cluster, keyspace.getKeyspaceName(), columnFamilyName) ) {
    	  SuperCfResult<String,String,String> result = tmpl.querySuperColumns(group);
    	  if( result.hasResults() ) {
    		  for( String id : result.getSuperColumns() ) {
    			  result.applySuperColumn(id);
    			  
    			  String type = result.getString("type");
    			  HColumn<String,ByteBuffer> tcol = result.getColumn("tags");
    			  HColumn<String,ByteBuffer> dcol = result.getColumn("data");
    			  
    			  List<String> tags = (List<String>)ObjectSerializer.get().fromByteBuffer(tcol.getValue());
    			  Object data = ObjectSerializer.get().fromByteBuffer(dcol.getValue());
    			  
    			  Heartbeat<X> beat = new Heartbeat<X>( group, id, type, tags, dataClass.cast(data) );
				  ret.add( beat );
    		  }
    	  }
      }
      return ret;
    }
}
