package org.scriptbox.selenium;

import java.util.List;

public class GroovySeleniumRemoteImpl implements GroovySeleniumRemote {

	public GroovySeleniumRemoteImpl() {
	}

	@Override
	public void run(String scriptText, List<String> parameters) throws Exception {
		GroovySeleniumCli.getSelenium().run( scriptText, GroovySeleniumCli.getIncludeText(), parameters);
	}

}
