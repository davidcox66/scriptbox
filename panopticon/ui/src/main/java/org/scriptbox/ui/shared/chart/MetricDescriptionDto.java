package org.scriptbox.ui.shared.chart;

import java.io.Serializable;

public class MetricDescriptionDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String fullText;
	private String shortText;

	public MetricDescriptionDto() {
	}
	
	public MetricDescriptionDto( String text ) {
		this.fullText = text;
		this.shortText = text;
	}
	
	public String getFullText() {
		return fullText;
	}
	public void setFullText(String fullText) {
		this.fullText = fullText;
	}
	public String getShortText() {
		return shortText;
	}
	public void setShortText(String shortText) {
		this.shortText = shortText;
	}

	public String toString() {
		return "MetricDescriptionDto{ short=" + shortText + ", full=" + fullText + " }";
	}
	
	
	
}
