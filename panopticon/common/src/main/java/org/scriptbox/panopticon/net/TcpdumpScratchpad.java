package org.scriptbox.panopticon.net;

import groovy.lang.Closure;
import groovy.lang.MapWithDefault;

import java.util.HashMap;
import java.util.Map;

public class TcpdumpScratchpad {
	public Map<String,Number> results;
	public TcpdumpRecord record = new TcpdumpRecord();;
	
	public TcpdumpScratchpad() {
		// Groovy cheat to make dealing with null entries easier
		results =  MapWithDefault.newInstance( new HashMap<String,Number>(), new Closure(this) {
		public Object doCall(Object[] args) {
			return 0.0f;
		}
		} );
	}
}
