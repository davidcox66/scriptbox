package org.scriptbox.util.common.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.lang.StringUtils;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTailReader {

	private static final Logger LOGGER = LoggerFactory.getLogger( SimpleTailReader.class );
	
    private static int charBufferSize = 4096;     // half the default stream buffer size
    private static int expectedLineLength = 160;  // double the default line length
    private static int EOF = -1;                  // End Of File

    private boolean stop = false;
    	    
    public void stop () {
      stop = true;
    }
  
    public void tail (final File file, final int interval, final ParameterizedRunnable<String> closure ) {
      new Thread( new Runnable() {
    	  public void run() {
    		  FileReader reader = null;
  
    		  try {
    			  reader = new FileReader( file );
    			  reader.skip(file.length());
  
    			  String line = null;
  
    			  while (!stop) {
    				  line = readLine( reader );
    				  if (StringUtils.isNotEmpty(line)) {
			              closure.run( line );
    				  }
    				  else {
    					  Thread.sleep(interval);
    				  }
    			  }
  
    		  }
    		  catch( Exception ex ) {
    			  LOGGER.error( "Error tailing file: " + file );
    		  }
    		  finally {
    			  if( reader != null ) {
    				  IoUtil.closeQuietly( reader );
    			  }
    		  }
    	  } 
      } ).start();
    }
    
    public static String readLine(Reader self) throws IOException {
        if (self instanceof BufferedReader) {
            BufferedReader br = (BufferedReader) self;
            return br.readLine();
        }
        if (self.markSupported()) {
            return readLineFromReaderWithMark(self);
        }
        return readLineFromReaderWithoutMark(self);
    }

    private static String readLineFromReaderWithMark(final Reader input)
            throws IOException {
        char[] cbuf = new char[charBufferSize];
        try {
            input.mark(charBufferSize);
        } 
        catch (IOException e) {
            // this should never happen
            LOGGER.warn("Caught exception setting mark on supporting reader", e);
            // fallback
            return readLineFromReaderWithoutMark(input);
        }

        // could be changed into do..while, but then
        // we might create an additional StringBuffer
        // instance at the end of the stream
        int count = input.read(cbuf);
        if (count == EOF) // we are at the end of the input data
            return null;

        StringBuffer line = new StringBuffer(expectedLineLength);
        // now work on the buffer(s)
        int ls = lineSeparatorIndex(cbuf, count);
        while (ls == -1) {
            line.append(cbuf, 0, count);
            count = input.read(cbuf);
            if (count == EOF) {
                // we are at the end of the input data
                return line.toString();
            }
            ls = lineSeparatorIndex(cbuf, count);
        }
        line.append(cbuf, 0, ls);

        // correct ls if we have \r\n
        int skipLS = 1;
        if (ls + 1 < count) {
            // we are not at the end of the buffer
            if (cbuf[ls] == '\r' && cbuf[ls + 1] == '\n') {
                skipLS++;
            }
        } else {
            if (cbuf[ls] == '\r' && input.read() == '\n') {
                skipLS++;
            }
        }

        //reset() and skip over last linesep
        input.reset();
        input.skip(line.length() + skipLS);
        return line.toString();
    }
    
    private static String readLineFromReaderWithoutMark(Reader input)
            throws IOException {

        int c = input.read();
        if (c == -1)
            return null;
        StringBuffer line = new StringBuffer(expectedLineLength);

        while (c != EOF && c != '\n' && c != '\r') {
            char ch = (char) c;
            line.append(ch);
            c = input.read();
        }
        return line.toString();
    }

    private static int lineSeparatorIndex(char[] array, int length) {
        for (int k = 0; k < length; k++) {
            if (isLineSeparator(array[k])) {
                return k;
            }
        }
        return -1;
    }
    
    private static boolean isLineSeparator(char c) {
        return c == '\n' || c == '\r';
    }
}
