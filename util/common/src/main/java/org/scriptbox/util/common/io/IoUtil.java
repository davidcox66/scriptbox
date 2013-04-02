package org.scriptbox.util.common.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger( IoUtil.class );
	
    public static Object splitEachLine(URL url, String pattern, ParameterizedRunnableWithResult<Object,List<String>>  closure) throws Exception {
        return splitEachLine(newReader(url), Pattern.compile(pattern), closure);
    }

    public static Object splitEachLine(URL url, Pattern pattern, ParameterizedRunnableWithResult<Object,List<String>>  closure) throws Exception {
        return splitEachLine(newReader(url), pattern, closure);
    }

    public static Object splitEachLine(Reader reader, String pattern, ParameterizedRunnableWithResult<Object,List<String>> closure) throws Exception {
    	return splitEachLine( reader, Pattern.compile(pattern), closure ); 
    }

    public static Object splitEachLine(Reader reader, Pattern pattern, ParameterizedRunnableWithResult<Object,List<String>> closure) throws Exception {
        BufferedReader br;
        Object result = null;

        if (reader instanceof BufferedReader)
            br = (BufferedReader) reader;
        else
            br = new BufferedReader(reader);

        try {
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                } else {
                    List vals = Arrays.asList(pattern.split(line));
                    result = closure.run(vals);
                }
            }
            Reader temp = reader;
            reader = null;
            temp.close();
            return result;
        } 
        finally {
            closeQuietly(reader);
            closeQuietly(br);
        }
    }

    public static BufferedReader newReader(URL url) throws MalformedURLException, IOException {
        return newReader(url.openConnection().getInputStream());
    }

    public static BufferedReader newReader(URL url, String charset) throws MalformedURLException, IOException {
        return new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), charset));
    }
    
    public static BufferedReader newReader(InputStream self) {
        return new BufferedReader(new InputStreamReader(self));
    }

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
	
	public static void closeQuietly( Reader reader ) {
		if( reader != null ) {
			try {
				reader.close();
			}
			catch( Exception ex ) {
				LOGGER.error( "Error closing reader: " + reader, ex );
			}
		}
	}
	public static void closeQuietly( Writer writer ) {
		if( writer != null ) {
			try {
				writer.close();
			}
			catch( Exception ex ) {
				LOGGER.error( "Error closing writer: " + writer, ex );
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

	public static Thread consumeProcessErrorStream(Process process, OutputStream err) {
		Thread thread = new Thread(new ByteDumper(process.getErrorStream(), err));
		thread.start();
		return thread;
	}

	public static Thread consumeProcessOutputStream(Process process, OutputStream output) {
		Thread thread = new Thread( new ByteDumper(process.getInputStream(), output));
		thread.start();
		return thread;
	}

	private static class ByteDumper implements Runnable {
		InputStream in;
		OutputStream out;

		public ByteDumper(InputStream in, OutputStream out) {
			this.in = new BufferedInputStream(in);
			this.out = out;
		}

		public void run() {
			byte[] buf = new byte[8192];
			int next;
			try {
				while ((next = in.read(buf)) != -1) {
					if (out != null) {
						out.write(buf, 0, next);
					}
				}
			} 
			catch (IOException e) {
				throw new RuntimeException( "Exception while dumping process stream", e);
			}
		}
	}
}
