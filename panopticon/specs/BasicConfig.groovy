every(30*1000) {
	
	  remote( 'ProcessName', 'localhost', 7901 ) {
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
		mbeans('ActionMetrics:*') {
			capture( '.*' );
		}
	  }
	}
