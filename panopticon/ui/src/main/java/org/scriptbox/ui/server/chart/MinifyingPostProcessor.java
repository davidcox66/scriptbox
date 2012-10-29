package org.scriptbox.ui.server.chart;

import java.util.ArrayList;
import java.util.List;

import org.scriptbox.metrics.query.groovy.ReportElement;
import org.scriptbox.ui.shared.chart.MetricDescriptionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinifyingPostProcessor implements MetricDescriptionPostProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger( MinifyingPostProcessor.class );
	
	@Override
	public void process(ReportElement element, List<MetricDescriptionDto> descriptions) {
		if( descriptions.size() > 1 ) {
			List<String> strs = new ArrayList<String>( descriptions.size() );
			for( MetricDescriptionDto desc : descriptions ) {
				strs.add( desc.getShortText() );
			}
			if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "process: strings=" + strs ); }
			
			IntRange indices = StringMinifier.getStringCollectionUniqueNamePortion( strs );
			for( MetricDescriptionDto desc : descriptions ) {
				String text = desc.getShortText();
				try {
					desc.setShortText( text.substring(indices.getStart(),text.length()+indices.getEnd()) );
				}
				catch( Exception ex ) {
					throw new RuntimeException( "Error minifying string: '" + text + 
						"', start=" + indices.getStart() + ", end=" + indices.getEnd(), ex );
				}
			}
		}
	}

}
