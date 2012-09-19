package org.scriptbox.box.exec;



public class BasicExecBlock<X extends ExecRunnable> extends AbstractExecBlock<X> {

	@Override
	public void run() throws Exception {
		with( this );
	}
}
