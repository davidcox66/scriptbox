package org.scriptbox.panopticon.net;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    private static GenericProcess PROC = new GenericProcess("System");
   
    private static final String FLAGS = ": Flags [";
    
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
        		if( LOGGER.isDebugEnabled() ) { 
        			LOGGER.debug( "tcpdump: processing[" + lineNumber + "] '" + line + "'" );
        		}
        		long now = System.currentTimeMillis();
        		try {
        			if( "IP".equals(getProtocol(line)) ) {
        				int pos = line.indexOf( FLAGS );
        				if( pos != -1 ) {
        					String[] parts = line.substring(0,pos).split( "\\s+" );
        					rec.line = line;
        					rec.time = parts[0];
        					rec.source = parts[2];
        					rec.destination = parts[4];
        					
        					int pos2 = line.indexOf("],", FLAGS.length());
        					rec.flags = line.substring(pos+FLAGS.length(),pos2);
        				
        					rec.fields.clear();
        					String[] fields = line.substring(pos2+3).split( ", " );
        					for( int i=0 ; i < fields.length ; i++ ) {
        						pos2 = fields[i].indexOf(" " );
        						String name = fields[i].substring(0,pos2);
        						String value = fields[i].substring(pos2+1);
        						rec.fields.put( name, value );
        					}
        					
        					processor.run( rec );
        					
        					if( (now - last) / 1000 >= delay ) {
        						summary.run( results );
	        					for( Map.Entry<String, Number> entry : results.entrySet() ) {
	        						store( attr, entry.getKey(), entry.getValue().floatValue() );
	        					}
        						results.clear();
        						last = now;
        					}
        				}
        				else {
	        				if( LOGGER.isDebugEnabled() ) {
	        					LOGGER.debug( "Unable to parse tcpdump line: '" + line + "'");
	        				}
        				}
        			}
        			else {
        				if( LOGGER.isDebugEnabled() ) {
        					LOGGER.debug( "Unsupported tcpdump line: '" + line + "'");
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

	private void store( String attr, String metric, Float value ) throws Exception {
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "store: attr=" + attr + ", metric=" + metric + ", value=" + value); }
	    store.store( new CaptureResult(PROC, attr, metric, value) );
	} 

	private String getProtocol( String line ) {
		int start = line.indexOf(" " );
		if( start != -1 ) {
			int end = line.indexOf(" ", start+1);
			if( end != -1 ) {
				return line.substring(start+1,end);
			}
		}
		return null;
	}
}
