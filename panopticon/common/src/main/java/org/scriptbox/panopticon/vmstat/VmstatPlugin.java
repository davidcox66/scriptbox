package org.scriptbox.panopticon.vmstat;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.scriptbox.box.sys.BasicSystemExecRunnable;
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

public class VmstatPlugin extends BoxContextInjectingListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(VmstatPlugin.class);

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
		setInjectors(context.getBox().getInjectorsOfType(Vmstatnjector.class));
		super.contextCreated(context);
	}

	public void vmstat(int delay) {

		/* 
	     *  Procs
	       r: The number of processes waiting for run time.
	       b: The number of processes in uninterruptible sleep.

		   Memory
		       swpd: the amount of virtual memory used.
		       free: the amount of idle memory.
		       buff: the amount of memory used as buffers.
		       cache: the amount of memory used as cache.
		       inact: the amount of inactive memory. (-a option)
		       active: the amount of active memory. (-a option)
	
		   Swap
		       si: Amount of memory swapped in from disk (/s).
		       so: Amount of memory swapped to disk (/s).
	
		   IO
		       bi: Blocks received from a block device (blocks/s).
		       bo: Blocks sent to a block device (blocks/s).
	
		   System
		       in: The number of interrupts per second, including the clock.
		       cs: The number of context switches per second.
	
		   CPU
		       These are percentages of total CPU time.
		       us: Time spent running non-kernel code. (user time, including nice time)
		       sy: Time spent running kernel code. (system time)
		       id: Time spent idle. Prior to Linux 2.5.41, this includes IO-wait time.
		       wa: Time spent waiting for IO. Prior to Linux 2.5.41, included in idle.
		       st: Time stolen from a virtual machine. Prior to Linux 2.6.11, unknown.
	
	
		 
	     */
		List<String> args = new ArrayList<String>();
		args.add("vmstat");
		args.add("-n");
		args.add("" + delay);

		BasicSystemExecRunnable runnable = new BasicSystemExecRunnable(args) {
			public boolean eachLine(String line, int lineNumber) throws Exception {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("parse: reading line[" + lineNumber + "] : " + line);
				}
				try {
					// Non-cumulative data doesn't start until row 4
					if (lineNumber >= 4) {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("parse: processing line[" + lineNumber + "]: " + line);
						}
						String[] fields = split(line);
						int i = 0;
						store("vmstat", "processesWaiting", fields[i++]);
						store("vmstat", "processesSleeping", fields[i++]);
						store("vmstat", "virtualMemory", fields[i++]);
						store("vmstat", "idleMemory", fields[i++]);
						store("vmstat", "bufferMemory", fields[i++]);
						store("vmstat", "cacheMemory", fields[i++]);
						store("vmstat", "swapIn", fields[i++]);
						store("vmstat", "swapOut", fields[i++]);
						store("vmstat", "blockDeviceIn", fields[i++]);
						store("vmstat", "blockDeviceOut", fields[i++]);
						store("vmstat", "interruptsPerSecond", fields[i++]);
						store("vmstat", "contextSwitchesPerSecond", fields[i++]);
						store("vmstat", "userTime", fields[i++]);
						store("vmstat", "systemTime", fields[i++]);
						store("vmstat", "idleTime", fields[i++]);
						store("vmstat", "stolenTime", fields[i++]);
					}
				} 
				catch (Exception ex) {
					LOGGER.error( "Failed processing line[${lineNumber}]: ${line}", ex);
				}
				finally {
					store.flush();
				}
				return true;
			}
		};
		ExecBlock<ExecRunnable> container = ExecContext.getEnclosing(ExecBlock.class);
		container.add(runnable);

		// Add this as a BoxServiceListener to get notification of stopping and
		// shutdown
		BoxContext.getCurrentContext().getBeans().put(runnable.toString(), runnable);

	}

	private void store(String attr, String metric, String value) throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("store: attr=${attr}, metric=${metric}, value=${value}");
		}
		store.store(new CaptureResult(PROC, attr, metric, Float.parseFloat(value)));
	}

	private String[] split(String str) {
		StringTokenizer st = new StringTokenizer(str);
		String[] strings = new String[st.countTokens()];
		for (int i = 0; i < strings.length; i++) {
			strings[i] = st.nextToken();
		}
		return strings;
	}
}
