package org.scriptbox.ui.server.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ReplacementLegendPostProcessor implements LegendPostProcessor, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger( ReplacementLegendPostProcessor.class );
	
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
	
	public List<String> process(List<String> legends) {
		List<String> ret = new ArrayList<String>( legends.size() );
		for( String legend : legends ) {
			String modified = pattern.matcher(legend).replaceAll(replacement);
			ret.add( modified );
			if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "process: replaced='" + legend + "', with='" + modified + "'" ); }
		}
		return ret;
	}

}
