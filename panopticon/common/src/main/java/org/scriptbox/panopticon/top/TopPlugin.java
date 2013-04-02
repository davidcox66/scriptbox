package org.scriptbox.panopticon.top;

import java.util.ArrayList;
import java.util.List;
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

public class TopPlugin extends BoxContextInjectingListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(TopPlugin.class);

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
		setInjectors(context.getBox().getInjectorsOfType(TopInjector.class));
		super.contextCreated(context);
	}

	public void top(int delay) {
	    /* 
	    Tasks:  72 total,   1 running,  71 sleeping,   0 stopped,   0 zombie
	    Cpu(s):  0.1%us,  0.0%sy,  0.0%ni, 99.8%id,  0.1%wa,  0.0%hi,  0.0%si,  0.0%st
	    Mem:   7652552k total,  4558156k used,  3094396k free,    83496k buffers
	    Swap:        0k total,        0k used,        0k free,   208860k cached
	    */
	    
	    /*
	           us = user mode
	           sy = system mode
	           ni = low priority user mode (nice)
	           id = idle task
	           wa = I/O waiting
	           hi = servicing IRQs
	           si = servicing soft IRQs
	           st = steal (time given to other DomU instances)
	 
	     */


		final Pattern cpuPattern = Pattern.compile("^Cpu");
		final Pattern cpuLinePattern = Pattern
				.compile("^Cpu.*:\\s+(\\S+)%us,\\s+(\\S+)%sy,\\s+(\\S+)%ni,\\s+(\\S+)%id,\\s+(\\S+)%wa,\\s+(\\S+)%hi,\\s+(\\S+)%si,\\s+(\\S+)%st\\s*$");

		final Pattern memPattern = Pattern.compile("^Mem");
		final Pattern memLinePattern = Pattern
				.compile("^Mem:\\s+(\\d+)k\\s+total,\\s+(\\d+)k\\s+used,\\s+(\\d+)k\\s+free,\\s+(\\d+)k\\s+buffers\\s*$");

		final Pattern topPattern = Pattern.compile("^top");
		final Pattern loadAveragePattern = Pattern
				.compile(".*load average:\\s+(\\S+),\\s+(\\S+),\\s+(\\S+)\\s*$");

		final Pattern swapPattern = Pattern.compile("^Swap");
		final Pattern swapLinePattern = Pattern
				.compile("^Swap:\\s+(\\d+)k\\s+total,\\s+(\\d+)k\\s+used,\\s+(\\d+)k\\s+free,\\s+(\\d+)k\\s+cached\\s*$");

		final Pattern tasksPattern = Pattern.compile("^Tasks");
		final Pattern tasksLinePattern = Pattern
				.compile("^Tasks:\\s+(\\d+)\\s+total,\\s+(\\d+)\\s+running,\\s+(\\d+)\\s+sleeping,\\s+(\\d+)\\s+stopped,\\s+(\\d+)\\s+zombie\\s*$");

		List<String> args = new ArrayList<String>();
		args.add("top");
		args.add("-b");
		args.add("-d");
		args.add("" + delay);

		BasicSystemExecRunnable runnable = new BasicSystemExecRunnable(args) {
			public boolean eachLine(String line, int lineNumber)
					throws Exception {

				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("parse: processing line: " + line);
				}
				try {
					if (cpuPattern.matcher(line).find()) {
						/*
						 * Cpu(s): 0.1%us, 0.1%sy, 0.0%ni, 99.8%id, 0.0%wa,
						 * 0.0%hi, 0.0%si, 0.0%st
						 */
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("parse: found cpu line: " + line);
						}
						Matcher cpuLineMatcher = cpuLinePattern.matcher(line);
						if (cpuLineMatcher.matches()) {
							int i = 1;
							store("cpu", "userMode", cpuLineMatcher.group(i++));
							store("cpu", "systemMode", cpuLineMatcher.group(i++));
							store("cpu", "lowPriorityUserMode", cpuLineMatcher.group(i++));
							store("cpu", "idleTask", cpuLineMatcher.group(i++));
							store("cpu", "ioWaiting", cpuLineMatcher.group(i++));
							store("cpu", "serviceIrqs", cpuLineMatcher.group(i++));
							store("cpu", "serviceSoftIrqs", cpuLineMatcher.group(i++));
							store("cpu", "stealTime", cpuLineMatcher.group(i++));
						}
					} else if (memPattern.matcher(line).find()) {
						/*
						 * Mem: 7652552k total, 5809296k used, 1843256k free,
						 * 143452k buffers
						 */
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("parse: found mem line: " + line);
						}
						Matcher memLineMatcher = memLinePattern.matcher(line);
						if (memLineMatcher.matches()) {
							int i = 1;
							store("memory", "total", memLineMatcher.group(i++));
							store("memory", "used", memLineMatcher.group(i++));
							store("memory", "free", memLineMatcher.group(i++));
							store("memory", "buffers", memLineMatcher.group(i++));
						}
					} else if (topPattern.matcher(line).find()) {
						/*
						 * top - 10:52:52 up 9 days, 22:00, 3 users, load
						 * average: 0.08, 0.10, 0.07
						 */
						Matcher loadAverageMatcher = loadAveragePattern
								.matcher(line);
						if (loadAverageMatcher.matches()) {
							int i = 1;
							store("load", "minutes1", loadAverageMatcher.group(i++));
							store("load", "minutes5", loadAverageMatcher.group(i++));
							store("load", "minutes15", loadAverageMatcher.group(i++));
						}
					} else if (swapPattern.matcher(line).find()) {
						/*
						 * Swap: 0k total, 0k used, 0k free, 382504k cached
						 */
						Matcher swapLineMatcher = swapLinePattern.matcher(line);
						if (swapLineMatcher.matches()) {
							int i = 1;
							store("swap", "total", swapLineMatcher.group(i++));
							store("swap", "used", swapLineMatcher.group(i++));
							store("swap", "free", swapLineMatcher.group(i++));
							store("swap", "cached", swapLineMatcher.group(i++));
						}
					} else if (tasksPattern.matcher(line).find()) {
						/*
						 * Tasks: 80 total, 1 running, 79 sleeping, 0 stopped, 0
						 * zombie
						 */
						Matcher tasksLineMatcher = tasksLinePattern.matcher(line);
						if (tasksLineMatcher.matches()) {
							int i = 1;
							store("tasks", "total", tasksLineMatcher.group(i++));
							store("tasks", "running", tasksLineMatcher.group(i++));
							store("tasks", "sleeping", tasksLineMatcher.group(i++));
							store("tasks", "stopped", tasksLineMatcher.group(i++));
							store("tasks", "zombie", tasksLineMatcher.group(i++));
						}
					}
				} 
				finally {
					store.flush();
				}
				LOGGER.debug("parse: finished reading output");
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
			LOGGER.debug("store: attr=" + attr + ",, metric=" + metric + ", value=" + value );
		}
		store.store(new CaptureResult(PROC, attr, metric, Float.parseFloat(value)));
	}
}
