package org.scriptbox.util.cassandra;

import java.util.HashMap;
import java.util.Map;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;

public class CassandraKeyspaceFactory {

	private InheritableThreadLocal<Map<String,Keyspace>> keyspaces = new InheritableThreadLocal<Map<String,Keyspace>>() {
		@Override
        protected Map<String,Keyspace> initialValue() {
            return new HashMap<String,Keyspace>();
        }

	};

	private Cluster cluster;
	
	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public Keyspace getInstance( String keyspaceName ) {
		Keyspace keyspace = keyspaces.get().get( keyspaceName );
		if( keyspace == null ) {
			keyspace = HFactory.createKeyspace(keyspaceName, cluster);
			keyspaces.get().put( keyspaceName, keyspace );
		}
		return keyspace;
	}
}
