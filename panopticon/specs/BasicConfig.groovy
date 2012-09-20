every(30*1000) {
	
	  remote( 'ProcessName', 'localhost', 7500 ) {
		mbeans('java.lang:type=Memory') {
		  capture('.*');
		}
		mbeans('java.lang:type=Threading') {
			  capture( [
				  'DaemonThreadCount',
				  'PeakThreadCount',
				  'ThreadCount',
				  'TotalStartedThreadCount'
			  ] );
		}
	  }
	}
