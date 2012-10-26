package org.scriptbox.panopticon.net;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.scriptbox.box.sys.SystemConfiguration;
import org.scriptbox.box.sys.BasicSystemExecRunnable;
import org.apache.commons.lang.StringUtils;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.exec.ExecBlock;
import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.box.inject.BoxContextInjectingListener;
import org.scriptbox.box.jmx.proc.GenericProcess;
import org.scriptbox.panopticon.capture.CaptureResult;
import org.scriptbox.panopticon.capture.CaptureStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpstatPlugin extends BoxContextInjectingListener {

    private static final Logger LOGGER = LoggerFactory.getLogger( TcpstatPlugin.class );

    private static GenericProcess PROC = new GenericProcess("System");
    
    private CaptureStore store;
   
	public CaptureStore getStore() {
		return store;
	}

	public void setStore(CaptureStore store) {
		this.store = store;
	}

	@Override
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors( context.getBox().getInjectorsOfType(TcpstatInjector.class) );
		super.contextCreated( context );
	}
	
   public void tcpstat( int delay, String interfaceName, String filter, String tag ) {
     /*
     %A    the number of ARP packets
     %a    the average packet size in bytes
     %B    the number of bytes per second
     %b    the number of bits per second
     %C    the number of ICMP and ICMPv6 packets
     %d    the standard deviation of the size of each packet in bytes
     %I    the number of IPv4 packets
     %l    the network "load" over the last minute, similar to uptime(1)
     %M    the maximum packet size in bytes
     %m    the minimum packet size in bytes
     %N    the number of bytes
     %n    the number of packets
     %p    the number of packets per second
     %R    same as %S, but relative to the first packet seen
     %r    same as %s, but relative to the first packet seen
     %S    the timestamp for the interval in seconds after the "UNIX epoch"
     %s    the timestamp for the interval in seconds.microseconds after the "UNIX epoch"
     %T    the number of TCP packets
     %U    the number of UDP packets
     %V    the number of IPv6 packets
      * 
      */
     String exe = "/usr/local/bin/tcpstat";
     if( SystemConfiguration.isLinux() ) {
      if( new File(exe).exists() ) {
        String format = "%A %a %B %C %d %I %l %M %m %N %n %p %T %U %V\\n";
        List<String> args = new ArrayList<String>();
        args.add( exe );
        args.add( "-i" );
        args.add( interfaceName );
        args.add( "-o" );
        args.add( format );
        
        if( StringUtils.isNotEmpty(filter) ) {
          args.add( "-f" );
          args.add( filter );
        }
        args.add( ""+delay ); 
      
        final String attr = "tcpstat" + ( StringUtils.isNotEmpty(tag) ?  "(" + tag + ")" : "");
        
        BasicSystemExecRunnable runnable = new BasicSystemExecRunnable( args ) {
        	public boolean eachLine( String line, int lineNumber ) throws Exception {
        		try {
				    String[] fields = split( line );
				    int i=0;
				    store( attr, "numberArpPackets", fields[i++] );
				    store( attr, "averagePacketSize", fields[i++] );
				    store( attr, "bytesPerSecond", fields[i++] );
				    store( attr, "numberIcmpPackets", fields[i++] );
				    store( attr, "packetSizeStdDev", fields[i++] );
				    store( attr, "numberIpv4Packets", fields[i++] );
				    store( attr, "networkLoad", fields[i++] );
				    store( attr, "maximumPacketSize", fields[i++] );
				    store( attr, "minimumPacketSize", fields[i++] );
				    store( attr, "numberBytes", fields[i++] );
				    store( attr, "numberPackets", fields[i++] );
				    store( attr, "packetsPerSecond", fields[i++] );
				    store( attr, "numberTcpPackets", fields[i++] );
				    store( attr, "numberUdpPackets", fields[i++] );
				    store( attr, "numberIpv6Packets", fields[i++] );
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
        LOGGER.warn( "tcpstat is not availabile on this machine");
      }
     }
     else {
       LOGGER.warn( "tcpstat only implemented on Linux");
     }
   }

	private void store( String attr, String metric, String value ) throws Exception {
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "store: attr=" + attr + ", metric=" + metric + ", value=" + value); }
	    store.store( new CaptureResult(PROC, attr, metric, Float.parseFloat(value)) );
	} 
	
   private String[] split( String str ) {
	   StringTokenizer st = new StringTokenizer(str);
	   String[] strings = new String[st.countTokens()];
	   for (int i = 0; i < strings.length; i++) {
	       strings[i] = st.nextToken();
	   }
	   return strings;
   }
}
