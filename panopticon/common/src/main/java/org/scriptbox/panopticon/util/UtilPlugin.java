package org.scriptbox.panopticon.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.inject.BoxContextInjectingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilPlugin extends BoxContextInjectingListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(UtilPlugin.class);

	private Map<String,SimpleDateFormat> formats = new HashMap<String,SimpleDateFormat>();
	
	@Override
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors(context.getBox().getInjectorsOfType(UtilInjector.class));
		context.getBeans().put( "Properties", new HashMap<String,Object>() );
		super.contextCreated(context);
	}

	synchronized public Date toDate( String format, String text ) {
		try {
			SimpleDateFormat sdf = formats.get( format );
			if( sdf == null ) {
				sdf = new SimpleDateFormat( format );
				formats.put( format, sdf );
			}
			return sdf.parse( text );
		}
		catch( Exception ex ) {
			throw new RuntimeException( "Failed parsing date: '" + text + "', format: '" + format + "'");
		}
	}

	synchronized public Date toTime( String format, String text ) {
		return toDate( format, text );
	}
}
