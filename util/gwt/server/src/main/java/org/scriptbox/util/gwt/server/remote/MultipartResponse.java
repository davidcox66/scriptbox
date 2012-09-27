package org.scriptbox.util.gwt.server.remote;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipartResponse {

	private static final Logger LOGGER = LoggerFactory.getLogger( MultipartResponse.class );
	
	private HttpServletResponse res;
	private ServletOutputStream out;
	private boolean endedLastResponse = true;
	private String separator;

	public MultipartResponse(HttpServletResponse response) throws IOException {
		this(response, "End");
	}

	public MultipartResponse(HttpServletResponse response, String separator) throws IOException {
		this.separator = separator;
		// Save the response object and output stream
		res = response;
		out = res.getOutputStream();

		// Set things up
		// res.setContentType("multipart/x-mixed-replace;boundary=End");
		res.setContentType("multipart/related;boundary=End");
		out.println();
		out.println("--" + separator);
	}

	public ServletOutputStream getOutputStream() {
		return out;
	}
	public void startResponse(String contentType) throws IOException {
		startResponse(contentType, null, null, null);
	}

	public void startResponse(
		String contentType, 
		String disposition, 
		String encoding, 
		String location) 
			throws IOException 
	{
		// End the last response if necessary
		if (!endedLastResponse) {
			endResponse();
		}
		LOGGER.debug( "startResponse: contentType=" + contentType + ", disposition=" + disposition + ", encoding=" + encoding + ", location=" + location );
		// Start the next one
		out.println("Content-Type: " + contentType);
		if (encoding != null) {
			// out.println("Content-Transfer-Encoding: " + encoding);
			out.println("Content-Encoding: " + encoding);
		}
		if (disposition != null) {
			out.println("Content-Disposition: " + disposition);
		}
		if (location != null) {
			out.println("Content-Location: " + location);
		}
		out.println();
		endedLastResponse = false;
	}

	public void endResponse() throws IOException {
		LOGGER.debug( "endResponse: ending");
		// End the last response, and flush so the client sees the content
		out.println();
		out.println("--" + separator);
		out.flush();
		endedLastResponse = true;
	}

	public void finish() throws IOException {
		out.println("--" + separator + "--");
		out.flush();
	}
}