package org.scriptbox.util.common.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger( IoUtil.class );
	
	public static String readFile( File file ) throws IOException {
		if( !file.exists() ) {
			throw new IOException( "File " + file + " does not exist");
		}
        Reader reader = new BufferedReader( new FileReader(file) );
		char[] buffer = new char[(int)file.length()];
		 
		int i = 0;
		while( i < buffer.length - 1 ) {
			int count = reader.read( buffer, i, buffer.length - i );
			if( count == -1 ) {
				throw new IOException( "Couldn't read to end of file");
			}
			i += count;
		}
	    return new String(buffer);
	}
	
	public static void closeQuietly( OutputStream stream ) {
		if( stream != null ) {
			try {
				stream.close();
			}
			catch( Exception ex ) {
				LOGGER.error( "Error closing output stream: " + stream, ex );
			}
		}
	}
	public static void closeQuietly( InputStream stream ) {
		if( stream != null ) {
			try {
				stream.close();
			}
			catch( Exception ex ) {
				LOGGER.error( "Error closing input stream: " + stream, ex );
			}
		}
	}
	
	public static void consumeAllOutput( BufferedReader reader ) {
		try {
			if( reader != null ) { 
				while( reader.readLine() != null );
			}
		}
		catch( IOException ex ) {
			LOGGER.error( "consumeAllOutput: error consuming output", ex );
		}
	}
	
}
