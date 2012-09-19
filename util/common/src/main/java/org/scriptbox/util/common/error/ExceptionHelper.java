package org.scriptbox.util.common.error;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

import org.scriptbox.util.common.io.IoUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger( ExceptionHelper.class );
	
	public static Throwable makeSerializable( Throwable ex ) throws Throwable {
		if( !isSerializable(ex) ) {
			return new Exception( "Converted to serializable:\n " + toString(ex) );
		}
		else {
			return ex;
		}
	}
	
	public static boolean isSerializable( Throwable ex ) {
		ObjectOutputStream stream = null;
		try {
			stream = new ObjectOutputStream( new ByteArrayOutputStream() );
			stream.writeObject( ex );
		}
		catch( Exception ex2 ) {
			LOGGER.error( "Exception is not serializble", ex );
			return false;
		}
		finally {
			IoUtil.closeQuietly(stream);
		}
		return true;
	}
	
    public static String toString( Throwable ex ) {
        ByteArrayOutputStream bstream = new ByteArrayOutputStream();
        PrintStream pstream = new PrintStream( bstream );
        ex.printStackTrace( pstream );
        String str = bstream.toString();
        pstream.close();
        return str;
    }
}
