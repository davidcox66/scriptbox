package org.scriptbox.ui.server.chart;

import java.util.List;

import org.scriptbox.metrics.query.groovy.ReportElement;
import org.scriptbox.ui.shared.chart.MetricDescriptionDto;
import org.springframework.beans.factory.InitializingBean;

public class MaxLengthPostProcessor implements MetricDescriptionPostProcessor, InitializingBean {

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
	public void process( ReportElement element, List<MetricDescriptionDto> descriptions) {
		for( MetricDescriptionDto desc  : descriptions ) {
			String text = desc.getShortText();
			if( text.length() > maximumLength ) {
				int overage = text.length() - maximumLength + 3; // 3 == length of '...'
				int mid = text.length() / 2;
				desc.setShortText( text.substring(0,mid-overage/2) + "..." + text.substring(mid+overage/2) );
			}
		}
	}

}
