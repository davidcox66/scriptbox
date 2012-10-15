package org.scriptbox.ui.server.chart;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;

public class MaxLengthLegendPostProcessor implements LegendPostProcessor, InitializingBean {

	private static final int MINIMUM_LENGTH = 8;
	
	private int maximumLength;
	
	public int getMaximumLength() {
		return maximumLength;
	}

	public void setMaximumLength(int maximumLength) {
		this.maximumLength = maximumLength;
	}

	public void afterPropertiesSet() throws Exception {
		if( maximumLength < MINIMUM_LENGTH ) {
			throw new IllegalArgumentException( "Maximum length must be greater than: " + MINIMUM_LENGTH );
		}
	}
	
	@Override
	public List<String> process(List<String> legends) {
		List<String> ret = new ArrayList<String>( legends.size() );
		for( String legend : legends ) {
			if( legend.length() > maximumLength ) {
				int overage = legend.length() - maximumLength + 3; // 3 == length of '...'
				int mid = legend.length() / 2;
				ret.add( legend.substring(0,mid-overage/2) + "..." + legend.substring(mid+overage/2) );
			}
			else {
				ret.add( legend );
			}
		}
		return ret;
	}

}
