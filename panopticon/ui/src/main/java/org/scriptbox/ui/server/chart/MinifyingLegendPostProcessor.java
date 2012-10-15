package org.scriptbox.ui.server.chart;

import java.util.ArrayList;
import java.util.List;

public class MinifyingLegendPostProcessor implements LegendPostProcessor {

	@Override
	public List<String> process(List<String> legends) {
		if( legends.size() > 1 ) {
			IntRange indices = StringMinifier.getStringCollectionUniqueNamePortion( legends );
			List<String> ret = new ArrayList<String>( legends.size() ); 
			for( String legend : legends ) {
				ret.add( legend.substring(indices.getStart(),legend.length()+indices.getEnd()) );
			}
			return ret;
		}
		return legends;
	}

}
