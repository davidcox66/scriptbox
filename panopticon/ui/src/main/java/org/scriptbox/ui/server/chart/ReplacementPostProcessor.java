package org.scriptbox.ui.server.chart;

import java.util.List;
import java.util.regex.Pattern;

import org.scriptbox.metrics.query.groovy.ReportElement;
import org.scriptbox.ui.shared.chart.MetricDescriptionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ReplacementPostProcessor implements MetricDescriptionPostProcessor, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger( ReplacementPostProcessor.class );
	
	private Pattern pattern;
	private String replacement="";

	public void setPattern( String pattern ) {
		this.pattern = Pattern.compile( pattern );
	}
	
	public Pattern getPattern() {
		return pattern;
	}
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}
	public String getReplacement() {
		return replacement;
	}
	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}
	
	public void afterPropertiesSet() throws Exception {
		if( pattern == null ) {
			throw new IllegalArgumentException( "Pattern cannot be null");
		}
	}
	
	public void process( ReportElement element, List<MetricDescriptionDto> descriptions ) {
		for( MetricDescriptionDto desc : descriptions ) {
			try {
				String text = desc.getShortText();
				String modified = pattern.matcher(text).replaceAll(replacement);
				desc.setShortText( modified );
			}
			catch( Exception ex ) {
				throw new RuntimeException( "Failed replacing text: element=" + element + ", description=" + desc );
			}
		}
	}

}
