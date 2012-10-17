package org.scriptbox.ui.server.chart;

import groovy.lang.Closure;

import java.util.List;
import java.util.Map;

import org.scriptbox.metrics.query.groovy.ReportElement;
import org.scriptbox.ui.shared.chart.MetricDescriptionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportElementProcessor implements MetricDescriptionPostProcessor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger( ReportElementProcessor.class );
	@Override
	public void process(ReportElement element, List<MetricDescriptionDto> descriptions) {
		Map params = (Map)element.getParams();
		if( params != null ) {
			Closure textFilter = (Closure)params.get( "textFilter" );
			if( textFilter != null ) {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "process: filtering descriptions for: " + element ); }
				textFilter.call( descriptions );
			}
			else {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "process: no textFilter defined for: " + element ); }
			}
		}
		else {
			if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "process: no parameters defined for: " + element ); }
		}
	}

}
