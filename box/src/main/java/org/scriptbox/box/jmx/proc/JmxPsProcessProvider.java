package org.scriptbox.box.jmx.proc;

import java.util.List;

import org.scriptbox.box.jmx.conn.JmxConnection;
import org.scriptbox.box.jmx.conn.JmxConnections;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.scriptbox.util.common.os.proc.ProcessStatus;
import org.scriptbox.util.common.os.proc.ProcessStatusParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxPsProcessProvider extends JmxProcessProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger( JmxPsProcessProvider.class );

	private ParameterizedRunnableWithResult<Boolean,ProcessStatus> finder;
	
	public JmxPsProcessProvider( JmxConnections connections, String name, ParameterizedRunnableWithResult<Boolean,ProcessStatus> finder ) {
		super( connections, name );
		this.finder = finder;
	}
	
	@Override
	public void run() throws Exception {
		List<ProcessStatus> procs = ProcessStatusParser.find( finder );
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "run: matching processes=" + procs ); }
		for( ProcessStatus proc : procs ) {
			JmxConnection connection = getConnections().getConnection( proc.pid, proc.command );
			with( new JmxProcess(getName(),connection) );
		}
	}
}
