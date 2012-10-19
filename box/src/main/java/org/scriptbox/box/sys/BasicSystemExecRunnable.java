package org.scriptbox.box.sys;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.scriptbox.util.common.io.IoUtil;

public abstract class BasicSystemExecRunnable extends SystemExecRunnable {

	public BasicSystemExecRunnable(List<String> command) {
		super( command );
	}
	
	public abstract boolean eachLine( String line, int lineNumber ) throws Exception;
	
	@Override
	public boolean run(Process process) throws Exception {
		BufferedReader reader = new BufferedReader( new InputStreamReader(process.getInputStream()) );
		try {
			String line = null;
			int lineNumber=0;
			do {
				line = reader.readLine();
	            if (line == null) {
	                break;
	            } 
	            lineNumber++;
				
			} 
			// skip processing of lines while in a suspended state - partial expression evaluation
			while( isSuspended() || eachLine(line,lineNumber) ); 
		}
		finally {
			IoUtil.closeQuietly(reader);
		}
		return true;
	}

}
