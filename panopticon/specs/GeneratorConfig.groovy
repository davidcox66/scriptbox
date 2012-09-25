every(30*1000) {
	
	  ps( 'Generator', ~/.*main.HordeMain.*/ ) {
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
      		capture('.*');
    	}
	}
}
