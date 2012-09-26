package org.scriptbox.util.cassandra.heartbeat;

import java.util.ArrayList;
import java.util.List;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.template.SuperCfResult;
import me.prettyprint.cassandra.service.template.SuperCfTemplate;
import me.prettyprint.cassandra.service.template.ThriftSuperCfTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;

import org.scriptbox.util.cassandra.Cassandra;
import org.scriptbox.util.cassandra.CassandraDownTemplate;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraHeartbeatReader {
    private static final Logger LOGGER = LoggerFactory.getLogger( CassandraHeartbeatReader.class );
    
	public static final String HEARTBEATS_CF = "Heartbeats";
 
	private Cluster cluster;
    private Keyspace keyspace;
   
    private SuperCfTemplate<String,String,String> tmpl; 
    
    public CassandraHeartbeatReader( Cluster cluster, Keyspace keyspace ) {
        this.keyspace = keyspace;
        StringSerializer ser = StringSerializer.get();
        this.tmpl = new ThriftSuperCfTemplate<String,String,String>( keyspace, HEARTBEATS_CF, ser, ser, ser );
    }
   
    public List<Heartbeat> list( String group ) throws Exception {
    	return list( group, null );
    }
    
    public List<Heartbeat> list( String group, ParameterizedRunnableWithResult<Boolean,Heartbeat> matcher ) throws Exception {
      List<Heartbeat> ret = new ArrayList<Heartbeat>();
      if( Cassandra.isExistingColumnFamily(cluster, keyspace.getKeyspaceName(), HEARTBEATS_CF) ) {
    	  SuperCfResult<String,String,String> result = tmpl.querySuperColumns(group);
    	  if( result.hasResults() ) {
    		  for( String id : result.getSuperColumns() ) {
    			  result.applySuperColumn(id);
    			  Heartbeat beat = new Heartbeat( group, id, 0 );
    			  for( String col : result.getColumnNames() ) {
    				 beat.getParameters().put( col, result.getString(col) ); 
    			  }
    			  if( matcher == null || matcher.run(beat) ) {
    				  ret.add( beat );
    			  }
    		  }
    	  }
      }
      return ret;
    }
}
