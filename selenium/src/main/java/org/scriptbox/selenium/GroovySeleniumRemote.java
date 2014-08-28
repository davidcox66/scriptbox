package org.scriptbox.selenium;

import java.util.List;

public interface GroovySeleniumRemote {

	public void run( String scriptText, List<String> parameters ) throws Exception;
}
