package org.scriptbox.panopticon.apache;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApacheStatusParser {

    private static final Logger LOGGER = LoggerFactory.getLogger( ApacheStatusParser.class );
   
    /*
    static ApacheStatus parse( URL url ) {
        ApacheStatus ret = new ApacheStatus();
        Map<String,String> values = new HashMap<String,String>();
        url.splitEachLine(':') { List<String> tokens ->
            if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "parse: tokens=${tokens}"); }
            values.putAt( tokens[0], tokens[1].trim() );
        }
        ret.totalAccesses = getLong(values,'Total Accesses');
        ret.totalKbytes = getLong(values,'Total kBytes');
        ret.cpuLoad = getDouble(values,'CPULoad');
        ret.uptime = getInt(values,'Uptime');
        ret.requestsPerSecond = getDouble(values,'ReqPerSec');
        ret.bytesPerSecond = getDouble(values,'BytesPerSec');
        ret.bytesPerRequest = getDouble(values,'BytesPerReq');
        ret.busyWorkers = getInt(values,'BusyWorkers');
        ret.idleWorkers = getInt(values,'IdleWorkers');

        String scorecard = values.getAt('Scoreboard');
        char[] chars = scorecard.getChars();
        int len = chars.length;
        
        final char waitingForConnection = '_' as char;
        final char startingUp = 'S' as char;
        final char readingRequest = 'R' as char;
        final char sendingReply = 'W' as char;
        final char keepAlive = 'K' as char;
        final char closingConnection = 'C' as char;
        final char logging = 'L'  as char;
        final char gracefullyFinishing = 'G' as char;
        final char idleCleanup = 'I' as char;
        final char openSlot = '.' as char;
        
        for( int i=0 ; i < len ; i++ ) {
            char ch = chars[i];
            switch( ch ) {
                case waitingForConnection: ret.waitingForConnection++;  break;
                case startingUp: ret.startingUp++;  break;
                case readingRequest: ret.readingRequest++;  break;
                case sendingReply: ret.sendingReply++;  break;
                case keepAlive: ret.keepAlive++;  break;
                case closingConnection: ret.closingConnection++;  break;
                case logging: ret.logging++;  break;
                case gracefullyFinishing: ret.gracefullyFinishing++;  break;
                case idleCleanup: ret.idleCleanup++;  break;
                case openSlot: ret.openSlot++;  break;
                default: ret.unknown++; 
            }
        }
        return ret;
    }
   
    static double getDouble( Map<String,String> values, String key ) {
        String val = values.get(key);
        return val ? val.toDouble() : 0.0;
    }
    static double getLong( Map<String,String> values, String key ) {
        String val = values.get(key);
        return val ? val.toLong() : 0L;
    }
    static double getInt( Map<String,String> values, String key ) {
        String val = values.get(key);
        return val ? val.toInteger() : 0;
    }
    
    static void main( String[] args ) {
        if( args.length != 1 ) {
            System.err.println( "Usage: ApacheStatusParser <url>");
            System.exit( 1 );
        }
        ApacheStatus status = parse( new URL(args[0]) );
        println status;
    }     
    */
}