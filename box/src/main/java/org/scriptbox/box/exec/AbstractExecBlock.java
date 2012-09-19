package org.scriptbox.box.exec;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractExecBlock<X extends ExecRunnable> implements ExecBlock<X> {

	protected List<X> children = new ArrayList<X>();

	public void with( Object enclosing ) throws Exception {
		ExecContext.with(enclosing, new ExecRunnable() {
			public void run() throws Exception {
				for( X runnable : children ) {
					runnable.run();
				}
			}
		} );
	}
	
	@Override
	public void add(X child) {
		children.add( child );
	}

	public List<X> getChildren() {
		return children;
	}
}
