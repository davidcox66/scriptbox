package org.scriptbox.ui.server.chart;

import java.util.ArrayList;
import java.util.List;

import org.scriptbox.metrics.query.groovy.ReportElement;
import org.scriptbox.ui.shared.chart.MetricDescriptionDto;

public class MinifyingPostProcessor implements MetricDescriptionPostProcessor {

	@Override
	public void process(ReportElement element, List<MetricDescriptionDto> descriptions) {
		if( descriptions.size() > 1 ) {
			List<String> strs = new ArrayList<String>( descriptions.size() );
			for( MetricDescriptionDto desc : descriptions ) {
				strs.add( desc.getShortText() );
			}
			IntRange indices = StringMinifier.getStringCollectionUniqueNamePortion( strs );
			for( MetricDescriptionDto desc : descriptions ) {
				String text = desc.getShortText();
				desc.setShortText( text.substring(indices.getStart(),text.length()+indices.getEnd()) );
			}
		}
	}

}
