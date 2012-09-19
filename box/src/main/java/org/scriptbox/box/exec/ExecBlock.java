package org.scriptbox.box.exec;


public interface ExecBlock<X extends ExecRunnable> extends ExecRunnable {

	public void add( X child );
}
