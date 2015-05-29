package org.scriptbox.selenium.bind;

import com.mongodb.*;
import groovy.lang.Binding;
import org.mongojack.JacksonDBCollection;
import org.mongojack.internal.stream.JacksonDBObject;

import java.util.Map;

/**
 * Created by david on 5/29/15.
 */
public class MongoBinder implements Bindable {

    private MongoClient client;

    public MongoBinder( String address ) {
        try {
            int pos = address.indexOf(":");
            if (pos != -1) {
                client = new MongoClient(address.substring(0, pos), Integer.parseInt(address.substring(pos + 1)));
            } else {
                client = new MongoClient(address);
            }
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed connecting to mongo at: '" + address + "'", ex);
        }
    }

    @Override
    public void bind(Binding binding) {
        BindUtils.bind( binding, this, "getDbCollection" );
        BindUtils.bind( binding, this, "bdo" );
        BindUtils.bind( binding, this, "jbdo" );
        BindUtils.bind( binding, this, "bdl" );
    }

    public MongoClient getMongoClient() {
        return client;
    }

    public BasicDBObject bdo( int size ) {
        return new BasicDBObject( size );
    }

    public BasicDBObject bdo( String key, Object value ) {
        return new BasicDBObject( key, value );
    }

    public BasicDBObject bdo( Map map ) {
        return new BasicDBObject( map );
    }

    public <T> JacksonDBObject<T> jbdo( T instance, Class<T> cls ) {
        return new JacksonDBObject( instance, cls );
    }

    public BasicDBList bdl() {
        return new BasicDBList();
    }

    public DBCollection getDbCollection( String dbName, String collectionName ) {
        DB db = client.getDB(dbName);
        return db.getCollection(collectionName);
    }

    public <E,K> JacksonDBCollection<E,K> getDbCollection(
        String dbName,
        String collectionName,
        Class<E> elementType,
        Class<K> keyType,
        boolean stream )
    {
        DBCollection coll = getDbCollection(dbName,collectionName);
        JacksonDBCollection<E, K> jack = JacksonDBCollection.wrap(coll, elementType, keyType);
        if( stream ) {
            jack.enable(JacksonDBCollection.Feature.USE_STREAM_SERIALIZATION);
            jack.enable(JacksonDBCollection.Feature.USE_STREAM_DESERIALIZATION);
        }
        return jack;
    }
}
