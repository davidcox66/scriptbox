package org.scriptbox.panopticon.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.inject.BoxContextInjectingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;

public class UtilPlugin extends BoxContextInjectingListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(UtilPlugin.class);

	private Map<String,DateTimeFormatter> formats = new HashMap<String,DateTimeFormatter>();
	
	@Override
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors(context.getBox().getInjectorsOfType(UtilInjector.class));
		context.getBeans().put( "util.properties", new HashMap<String,Object>() );
		super.contextCreated(context);
	}

	public DateTime toDate( String format, String text ) {
		return getFormatter(format).parseDateTime( text );
	}

	public LocalTime toTime( String format, String text ) {
		return getFormatter(format).parseLocalTime(text);
	}
	
	synchronized private DateTimeFormatter getFormatter( String format ) {
		DateTimeFormatter sdf = formats.get( format );
		if( sdf == null ) {
			sdf = DateTimeFormat.forPattern( format );
			formats.put( format, sdf );
		}
		return sdf;
	}
}
