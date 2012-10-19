package org.scriptbox.box.sys;

import java.util.List;

import org.scriptbox.box.controls.BoxServiceListener;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.util.common.io.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SystemExecRunnable implements ExecRunnable, BoxServiceListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemExecRunnable.class);

	private List<String> command;
	private Process process;
	private boolean suspended;
	
	public SystemExecRunnable(List<String> command) {
		this.command = command;
	}

	public abstract boolean run(Process process) throws Exception;

	public boolean isSuspended() {
		return suspended;
	}

	public List<String> getCommand() {
		return command;
	}

	public Process getProcess() {
		return process;
	}

	public void starting( List arguments ) throws Exception {
	}
	public void stopping() throws Exception {
		suspended = true;
	}
	public void shutdown() throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("shutdown: killing child process: " + process );
		}
		try {
			if (process != null) {
				process.destroy();
			}
		}
		finally {
			process = null;
		}
	}

	public void run() throws Exception {
		if( process != null ) {
			throw new RuntimeException( "Cannot start multiple process" );
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("execute: command=" + command);
		}
		String[] commandArray = new String[command.size()];
		command.toArray(commandArray);
		process = Runtime.getRuntime().exec(commandArray);

		IoUtil.consumeProcessErrorStream(process,System.err);
		boolean ret = false;
		try {
			ret = run(process);
		} 
		finally {
			// gobble up any remaining output so child process doesn't wait for
			// parent to consume
			if (!ret) {
				IoUtil.consumeProcessOutputStream(process,System.out);
			}
			int rvalue = process.waitFor();
			if (rvalue != 0) {
				LOGGER.error("execute: command terminated abnormally - command=" + command + ", exit status=" + rvalue);
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("execute: command=" + command + ", exit status=" + rvalue);
				}
			}
			process = null;
		}
	}


}
