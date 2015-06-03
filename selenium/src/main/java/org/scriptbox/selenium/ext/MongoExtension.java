package org.scriptbox.selenium.ext;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.mongodb.*;
import groovy.lang.Binding;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.internal.MongoJackModule;
import org.mongojack.internal.query.QueryCondition;
import org.mongojack.internal.stream.JacksonDBObject;
import org.mongojack.internal.util.JacksonAccessor;
import org.mongojack.internal.util.SerializationUtils;
import org.scriptbox.selenium.bind.BindUtils;
import org.scriptbox.selenium.bind.Bindable;
import org.scriptbox.util.common.args.CommandLine;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by david on 5/29/15.
 */
public class MongoExtension implements Bindable, SeleniumExtension {

    private MongoClient client;
    private ObjectMapper objectMapper;
    private JavaType type;
    private Module module = new MongoJackModule();

    public MongoExtension() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        type = objectMapper.constructType( Object.class );
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
        BindUtils.bind(binding, this, "getDbCollection");
        BindUtils.bind( binding, this, "bdo" );
        BindUtils.bind( binding, this, "bdoj" );
        BindUtils.bind( binding, this, "bdl" );
        BindUtils.bind( binding, this, "bdf" );

        BindUtils.bind( binding, this, "qEmpty" );
        BindUtils.bind( binding, this, "qIs" );
        BindUtils.bind( binding, this, "qLessThan" );
        BindUtils.bind( binding, this, "qLessThanEquals" );
        BindUtils.bind( binding, this, "qGreaterThan" );
        BindUtils.bind( binding, this, "qGreaterThanEquals" );
        BindUtils.bind( binding, this, "qNotEquals" );
        BindUtils.bind( binding, this, "qIn" );
        BindUtils.bind( binding, this, "qNotIn" );
        BindUtils.bind( binding, this, "qAll" );
        BindUtils.bind( binding, this, "qSize" );
        BindUtils.bind( binding, this, "qExists" );
        BindUtils.bind( binding, this, "qNotExists" );
        BindUtils.bind( binding, this, "qOr" );
        BindUtils.bind( binding, this, "qAnd" );
        BindUtils.bind( binding, this, "qRegex" );
        BindUtils.bind( binding, this, "qElemMatch" );
    }

    public MongoClient getMongoClient() {
        return client;
    }

    public BasicDBObject bdo() {
        return new BasicDBObject();
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

    public <T> JacksonDBObject<T> bdoj( T instance, Class<T> cls ) {
        return new JacksonDBObject( instance, cls );
    }

    public BasicDBList bdl() {
        return new BasicDBList();
    }

    public DBObject bdf( String... fields ) {
        BasicDBObject ret = new BasicDBObject( fields.length );
        for( String field : fields ) {
            ret.put( field, 1 );
        }
        return ret;
    }

    public DBQuery.Query qEmpty() {
        return DBQuery.empty();
    }

    public DBQuery.Query qIs( String field, Object value ) {
        return DBQuery.is( field, value );
    }

    public DBQuery.Query qLessThan( String field, Object value ) {
        return DBQuery.lessThan(field, value);
    }

    public DBQuery.Query qLessThanEquals( String field, Object value ) {
        return DBQuery.lessThanEquals( field, value );
    }

    public DBQuery.Query qGreaterThan( String field, Object value ) {
        return DBQuery.greaterThan(field, value);
    }

    public DBQuery.Query qGreaterThanEquals( String field, Object value ) {
        return DBQuery.greaterThanEquals( field, value );
    }

    public DBQuery.Query qNotEquals( String field, Object value ) {
        return DBQuery.notEquals(field, value);
    }

    public DBQuery.Query qIn( String field, Object... values ) {
        return DBQuery.in(field, values);
    }

    public DBQuery.Query qIn( String field, Collection<?> values ) {
        return DBQuery.in( field, values );
    }

    public DBQuery.Query qNotIn( String field, Object... values ) {
        return DBQuery.notIn(field, values);
    }

    public DBQuery.Query qNotIn( String field, Collection<?> values ) {
        return DBQuery.notIn(field, values);
    }

    public DBQuery.Query qAll( String field, Object... values ) {
        return DBQuery.all(field, values);
    }

    public DBQuery.Query qAll( String field, Collection<?> values ) {
        return DBQuery.all(field, values);
    }

    public DBQuery.Query qSize( String field, int size ) {
        return DBQuery.size(field, size);
    }

    public DBQuery.Query qExists( String field ) {
        return DBQuery.in( field );
    }

    public DBQuery.Query qNotExists( String field ) {
        return DBQuery.in( field );
    }

    public DBQuery.Query qOr( DBQuery.Query... queries ) {
        return DBQuery.or( queries);
    }

    public DBQuery.Query qAnd( DBQuery.Query... queries ) {
        return DBQuery.and(queries);
    }

    public DBQuery.Query qNor( DBQuery.Query... queries ) {
        return DBQuery.nor(queries);
    }

    public DBQuery.Query qRegex( String field, Pattern pattern ) {
        return DBQuery.regex(field, pattern);
    }

    public DBQuery.Query qElemMatch( String field, DBQuery.Query query ) {
        return DBQuery.elemMatch(field, query);
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
