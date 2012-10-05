package org.scriptbox.box.plugins.jmx.proc;

import java.util.List;

import org.scriptbox.box.plugins.jmx.JmxConnection;
import org.scriptbox.box.plugins.jmx.JmxPlugin;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.scriptbox.util.common.os.proc.ProcessStatus;
import org.scriptbox.util.common.os.proc.ProcessStatusParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxLocalProcessProvider extends JmxProcessProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger( JmxLocalProcessProvider.class );

	private ParameterizedRunnableWithResult<Boolean,ProcessStatus> finder;
	
	public JmxLocalProcessProvider( JmxPlugin plugin, String name, ParameterizedRunnableWithResult<Boolean,ProcessStatus> finder ) {
		super( plugin, name );
		this.finder = finder;
	}
	
	@Override
	public void run() throws Exception {
		List<ProcessStatus> procs = ProcessStatusParser.find( finder );
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "ps: matching processes=" + procs ); }
		for( ProcessStatus proc : procs ) {
			JmxConnection connection = plugin.getConnections().getConnection( proc.pid, proc.command );
			with( new JmxProcess(getName(),connection) );
		}
	}
}
