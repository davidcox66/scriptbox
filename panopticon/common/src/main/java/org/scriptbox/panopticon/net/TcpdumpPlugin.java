package org.scriptbox.panopticon.net;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.exec.ExecBlock;
import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.box.inject.BoxContextInjectingListener;
import org.scriptbox.box.jmx.proc.GenericProcess;
import org.scriptbox.box.sys.BasicSystemExecRunnable;
import org.scriptbox.panopticon.capture.CaptureResult;
import org.scriptbox.panopticon.capture.CaptureStore;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpdumpPlugin extends BoxContextInjectingListener {

    private static final Logger LOGGER = LoggerFactory.getLogger( TcpdumpPlugin.class );

    public static final String TCPDUMP_BIN = "/usr/sbin/tcpdump";
    
	private static final Pattern LINE_PATTERN = Pattern.compile( "^(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+>\\s+(\\S+):\\s+(.*)$");
	private static final Pattern LINUX_IP_PATTERN = Pattern.compile( "^(\\w+)\\s+(\\d+):(\\d+)\\((\\d+)\\)\\s+(.*)\\s+<mss (\\d+)>.*$");
    private static GenericProcess PROC = new GenericProcess("System");
   
    private static final String FLAGS = "Flags [";
    
    private CaptureStore store;
   
	public CaptureStore getStore() {
		return store;
	}

	public void setStore(CaptureStore store) {
		this.store = store;
	}

	@Override
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors( context.getBox().getInjectorsOfType(TcpdumpInjector.class) );
		super.contextCreated( context );
	}
	
   public void tcpdump( boolean sudo, final int delay, String interfaceName, String filter, String tag, 
      final ParameterizedRunnable<TcpdumpRecord> processor,
      final ParameterizedRunnable<Map<String,Number>> summary )
   {
      if( new File(TCPDUMP_BIN).exists() ) {
        List<String> args = new ArrayList<String>();
        if( sudo ) {
        	args.add( "sudo" );
        }
        args.add( TCPDUMP_BIN );
        args.add( "-i" );
        args.add( interfaceName );
        
        if( StringUtils.isNotEmpty(filter) ) {
          args.add( filter );
        }
        final String attr = "tcpdump" + ( StringUtils.isNotEmpty(tag) ?  "(" + tag + ")" : "");
       
		final Map<String,Number> results = new HashMap<String,Number>();
		final TcpdumpRecord rec = new TcpdumpRecord();
		
        BasicSystemExecRunnable runnable = new BasicSystemExecRunnable( args ) {
    		private long last = System.currentTimeMillis();
        	public boolean eachLine( String line, int lineNumber ) throws Exception {
        		long now = System.currentTimeMillis();
        		try {
        			Matcher matcher = LINE_PATTERN.matcher( line );
        			if( matcher.matches() ) {
        				rec.reset();
        				int g=1;
        				rec.line = line;
        				rec.time = matcher.group(g++);
        				rec.protocol = matcher.group(g++);
        				rec.source = matcher.group(g++);
        				rec.destination = matcher.group(g++);
    					rec.text = matcher.group(g++);
    					
    					if( "IP".equals(rec.protocol) ) {
							if( processDarwinIp(rec) || processLinuxIp(rec) ) {
								
				        		if( LOGGER.isDebugEnabled() ) { 
				        			LOGGER.debug( "Processing IP record [" + lineNumber + "] : " + rec );
				        		}
    					
								processor.run( rec );
					        					
								if( (now - last) / 1000 >= delay ) {
									summary.run( results );
									for( Map.Entry<String, Number> entry : results.entrySet() ) {
										store( attr, entry.getKey(), entry.getValue().floatValue() );
									}
									last = now;
								}
	        				}
	        				else {
		        				if( LOGGER.isDebugEnabled() ) {
		        					LOGGER.debug( "Unable to parse IP record [" + lineNumber + "] : " + rec );
		        				}
	        				}
	        			}
	        			else {
	        				if( LOGGER.isDebugEnabled() ) {
	        					LOGGER.debug( "Unsupported tcpdump protocol [" + lineNumber + "] : " + rec ); 
	        				}
	        			}
        			}
        			else {
		        		if( LOGGER.isDebugEnabled() ) { 
		        			LOGGER.debug( "Line does not match [" + lineNumber + "] '" + line + "'" );
		        		}
        			}
        		}
        		finally {
				    store.flush();  
        		}
        		return true;
        	}
        };
        ExecBlock<ExecRunnable> container = ExecContext.getEnclosing(ExecBlock.class);
		container.add( runnable ); 
		
		// Add this as a BoxServiceListener to get notification of stopping and shutdown
        BoxContext.getCurrentContext().getBeans().put(runnable.toString(),runnable);
      }
      else {
        LOGGER.warn( "tcpdump is not availabile on this machine");
      }
   }

	private boolean processDarwinIp( TcpdumpRecord rec ) {
		
		if( rec.text.indexOf(FLAGS) == 0 ) {  
			int pos = rec.text.indexOf("],", FLAGS.length());
			rec.flags = rec.text.substring(FLAGS.length(),pos);
        				
			String[] fields = rec.text.substring(pos+3).split( ", " );
			for( int i=0 ; i < fields.length ; i++ ) {
				int pos2 = fields[i].indexOf(" " );
				String name = fields[i].substring(0,pos2);
				String value = fields[i].substring(pos2+1);
				rec.fields.put( name, value );
			}
			rec.seqFirst = rec.seqLast = rec.fields.get( "seq" );
			return true;
		}
		return false;
	}

	private boolean processLinuxIp( TcpdumpRecord rec ) {
		
	    Matcher matcher = LINUX_IP_PATTERN.matcher(rec.text);
	    if( matcher.matches() ) {
	    	int g=1;
	    	rec.flags = matcher.group(g++);
	    	rec.seqFirst = matcher.group(g++);
	    	rec.seqLast = matcher.group(g++);
	    	g++;
	    	String[] fields = matcher.group(g++).split("\\s+");
	    	for( int i=0 ; i < fields.length ; i++ ) {
	    		String name = fields[i++];
	    		String value = fields[i];
	    		rec.fields.put( name, value );
	    	}
	    	return true;
	    }
	    return false;
	}
	
	private void store( String attr, String metric, Float value ) throws Exception {
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "store: attr=" + attr + ", metric=" + metric + ", value=" + value); }
	    store.store( new CaptureResult(PROC, attr, metric, value) );
	} 
}
