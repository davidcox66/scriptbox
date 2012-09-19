package org.scriptbox.util.common.os.proc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scriptbox.util.common.io.IoUtil;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProcessStatusParser {

	private static final Logger LOGGER = LoggerFactory.getLogger( ProcessStatusParser.class );
	
    private static final Pattern processPattern = Pattern.compile( "^\\s*(\\S+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(.*)" ); 

    public static List<ProcessStatus> find( final ParameterizedRunnableWithResult<Boolean,ProcessStatus> runner ) throws Exception {
        final List<ProcessStatus> ret = new ArrayList<ProcessStatus>();
        each( new ParameterizedRunnable<ProcessStatus>() {
        	public void run( ProcessStatus status ) throws Exception {
        		if( runner.run(status) ) {
        			ret.add( status );
        		}
        	}
        } );
        return ret;
    }    
    
    public static void each( ParameterizedRunnable<ProcessStatus> runnable ) throws Exception {
    	ProcessBuilder builder = new ProcessBuilder( "ps", "-ef");
    	builder.redirectErrorStream(true);
    	BufferedReader reader = null;
    	Process proc = null;
        try {
        	proc = builder.start();
        	reader = new BufferedReader( new InputStreamReader(proc.getInputStream()) );
        	String line = null;
        	while( (line = reader.readLine()) != null ) {
		      Matcher matcher = processPattern.matcher(line); 
		      if( matcher.matches() ) {
	            ProcessStatus pd = new ProcessStatus();
	            int i=1;
	            pd.user = matcher.group(i++);
	            pd.pid = Integer.parseInt( matcher.group(i++) );
	            pd.ppid = Integer.parseInt( matcher.group(i++) );
	            pd.cpu = Integer.parseInt( matcher.group(i++) );
	            pd.startTime = matcher.group(i++);
	            pd.tty = matcher.group(i++);
	            pd.time = matcher.group(i++);
	            pd.command = matcher.group(i++);
	           
	            runnable.run( pd );
		      }
		    }
        }
        catch( Exception ex ) {
            // gobble up the remainder in case we errored out
        	LOGGER.error( "each: error processing status", ex );
        	IoUtil.consumeAllOutput( reader );
            throw ex;
        }
        finally {
        	if( proc != null ) {
			    proc.waitFor();
        	}
        }
    }  
}
