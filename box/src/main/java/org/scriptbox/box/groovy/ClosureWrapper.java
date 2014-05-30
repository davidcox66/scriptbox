package org.scriptbox.box.groovy;

import groovy.lang.Closure;

public class ClosureWrapper extends Closure {

	protected Closure delegate;
	
	public ClosureWrapper( Closure delegate ) {
		super( delegate, delegate.getThisObject() );
		this.delegate = delegate;
		maximumNumberOfParameters = delegate.getMaximumNumberOfParameters();
		parameterTypes = delegate.getParameterTypes();
	}
}
