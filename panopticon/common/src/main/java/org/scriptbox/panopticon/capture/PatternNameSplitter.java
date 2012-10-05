package org.scriptbox.panopticon.capture;

public class PatternNameSplitter implements NameSplitter {

	private String pattern;
	
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public String[] split(String name) {
		return name.split( pattern );
	}

}
