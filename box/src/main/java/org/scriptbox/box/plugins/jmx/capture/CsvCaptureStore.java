package org.scriptbox.box.plugins.jmx.capture;

import java.io.FileOutputStream;
import java.io.PrintStream;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.BoxScript;
import org.scriptbox.box.events.BoxContextListener;
import org.springframework.beans.factory.InitializingBean;

public class CsvCaptureStore implements CaptureStore, InitializingBean, BoxContextListener  {

	private String instance;
	private String path;
	private PrintStream output;
	
	public CsvCaptureStore() {
	}
	
	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void afterPropertiesSet() throws Exception {
		if( path == null && path.trim().length() == 0 ) {
			throw new RuntimeException( "Store path must be specified");
		}
		if( instance == null && instance.trim().length() == 0 ) {
			throw new RuntimeException( "Store instance must be specified");
		}
		output = new PrintStream( new FileOutputStream(path) );
	}
	
	public void contextCreated( BoxContext context ) throws Exception {
		context.getBeans().put( "store", this );
	}
	public void contextShutdown( BoxContext context ) throws Exception {
		context.getBeans().remove( "store" );
	}
	public void executingScript( BoxScript script ) throws Exception {
	}
	
	@Override
	public void store(CaptureResult result) throws Exception {
		output.println( instance + "," + result.process.getName() + "," +  result.attribute + "," + result.value + "," + result.millis );
	}

}
