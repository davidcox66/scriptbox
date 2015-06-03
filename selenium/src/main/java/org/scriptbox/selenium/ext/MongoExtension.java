package org.scriptbox.selenium.ext;

import com.mongodb.*;
import groovy.lang.Binding;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.internal.stream.JacksonDBObject;
import org.scriptbox.selenium.bind.BindUtils;
import org.scriptbox.selenium.bind.Bindable;
import org.scriptbox.util.common.args.CommandLine;

import java.util.Map;

/**
 * Created by david on 5/29/15.
 */
public class MongoExtension implements Bindable, SeleniumExtension {

    private MongoClient client;

    public MongoExtension() {
    }

    public MongoExtension(String address) {
        this();
        setAddress( address );
    }

    public void setAddress( String address ) {
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

    public void init( SeleniumExtensionContext ctx ) throws Exception {
        CommandLine cmd = ctx.getCommandLine();
        if( cmd.hasArgValue("mongo") ) {
            String address = cmd.consumeArgValue("mongo", false);
            if (address != null) {
                setAddress(address);
                bind(ctx.getBinding());
            }
        }
        else if( cmd.hasArg("mongo") ) {
            cmd.consumeArg("mongo");
            setAddress("localhost:27017");
            bind(ctx.getBinding());
        }

    }
    @Override
    public void bind(Binding binding) {
        BindUtils.bind(binding, this, "dbcoll");
        // com.mongo stuff
        BindUtils.bind( binding, this, "dbo" );
        BindUtils.bind( binding, this, "dbl" );
        BindUtils.bind( binding, this, "dbf" );
        BindUtils.bind( binding, this, "dbq" );
        // mongojack stuff
        BindUtils.bind( binding, this, "dboj" );
        BindUtils.bind( binding, this, "dbqj" );
    }

    public MongoClient getMongoClient() {
        return client;
    }

    public BasicDBObject dbo() {
        return new BasicDBObject();
    }

    public BasicDBObject dbo( int size ) {
        return new BasicDBObject( size );
    }

    public BasicDBObject dbo( String key, Object value ) {
        return new BasicDBObject( key, value );
    }

    public BasicDBObject dbo( Map map ) {
        return new BasicDBObject( map );
    }

    public BasicDBList dbl() {
        return new BasicDBList();
    }

    public QueryBuilder dbq() {
        return QueryBuilder.start();
    }

    public QueryBuilder dbq( String key ) {
        return QueryBuilder.start( key );
    }

    public DBObject dbf( String... fields ) {
        BasicDBObject ret = new BasicDBObject( fields.length );
        for( String field : fields ) {
            ret.put( field, 1 );
        }
        return ret;
    }

    public <T> JacksonDBObject<T> dboj( T instance, Class<T> cls ) {
        return new JacksonDBObject( instance, cls );
    }

    public DBQuery.Query dbqj() {
        return DBQuery.empty();
    }

    public DBCollection dbcoll( String dbName, String collectionName ) {
        DB db = client.getDB(dbName);
        return db.getCollection(collectionName);
    }

    public <E,K> JacksonDBCollection<E,K> dbcoll(
        String dbName,
        String collectionName,
        Class<E> elementType,
        Class<K> keyType,
        boolean stream )
    {
        DBCollection coll = dbcoll(dbName,collectionName);
        JacksonDBCollection<E, K> jack = JacksonDBCollection.wrap(coll, elementType, keyType);
        if( stream ) {
            jack.enable(JacksonDBCollection.Feature.USE_STREAM_SERIALIZATION);
            jack.enable(JacksonDBCollection.Feature.USE_STREAM_DESERIALIZATION);
        }
        return jack;
    }
}
