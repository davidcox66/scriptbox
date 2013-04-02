package org.scriptbox.panopticon.apache;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.scriptbox.util.common.io.IoUtil;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApacheStatusParser {

    private static final Logger LOGGER = LoggerFactory.getLogger( ApacheStatusParser.class );
   
    static ApacheStatus parse( URL url ) throws Exception {
        ApacheStatus ret = new ApacheStatus();
        final Map<String,String> values = new HashMap<String,String>();
        IoUtil.splitEachLine( url, ":", new ParameterizedRunnableWithResult<Object, List<String>>() {
        	public Object run( List<String> tokens ) { 
	            if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "parse: tokens=" + tokens); }
	            values.put( tokens.get(0), tokens.get(1).trim() );
	            return null;
        	}
        } );
        ret.totalAccesses = getLong(values,"Total Accesses");
        ret.totalKbytes = getLong(values,"Total kBytes");
        ret.cpuLoad = getDouble(values,"CPULoad");
        ret.uptime = getInt(values,"Uptime");
        ret.requestsPerSecond = getDouble(values,"ReqPerSec");
        ret.bytesPerSecond = getDouble(values,"BytesPerSec");
        ret.bytesPerRequest = getDouble(values,"BytesPerReq");
        ret.busyWorkers = getInt(values,"BusyWorkers");
        ret.idleWorkers = getInt(values,"IdleWorkers");

        String scorecard = values.get("Scoreboard");
        char[] chars = scorecard.toCharArray();
        int len = chars.length;
        
        final char waitingForConnection = '_';
        final char startingUp = 'S';
        final char readingRequest = 'R';
        final char sendingReply = 'W';
        final char keepAlive = 'K';
        final char closingConnection = 'C';
        final char logging = 'L';
        final char gracefullyFinishing = 'G';
        final char idleCleanup = 'I';
        final char openSlot = '.';
        
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
        return StringUtils.isNotEmpty(val) ? Double.parseDouble(val): 0.0;
    }
    static long getLong( Map<String,String> values, String key ) {
        String val = values.get(key);
        return StringUtils.isNotEmpty(val) ? Long.parseLong(val) : 0L;
    }
    static int getInt( Map<String,String> values, String key ) {
        String val = values.get(key);
        return StringUtils.isNotEmpty(val) ? Integer.parseInt(val) : 0;
    }
    
    static void main( String[] args ) {
    	try {
	        if( args.length != 1 ) {
	            System.err.println( "Usage: ApacheStatusParser <url>");
	            System.exit( 1 );
	        }
	        ApacheStatus status = parse( new URL(args[0]) );
	        System.out.println( status );
    	}
    	catch( Exception ex ) {
    		ex.printStackTrace( System.err );
    	}
    }     
}