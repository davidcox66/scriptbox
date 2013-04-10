package org.scriptbox.panopticon.net;

import java.util.HashMap;
import java.util.Map;

public class TcpdumpRecord {

	public String line;
	
	public String time;
	public String protocol;
	public String source;
	public String destination;

	public String seqFirst;
	public String seqLast;
	
	public String text;
	public String flags;
	public Map<String,String> fields = new HashMap<String,String>(10);

	public void reset() {
		time = null;
		protocol = null;
		source = null;
		destination = null;
		seqFirst = null;
		seqLast = null;
		text = null;
		flags = null;
		fields.clear();
	}
	public String toString() {
		return "TcpdumpRecord{ " + 
			"time=" + time + ", "  +
			", protocol=" + protocol +
			", source=" + source +
			", destination=" + destination +
			", seqFirst=" + seqFirst +
			", seqLast=" + seqLast +
			", text=" + text +
			", flags=" + flags +
			", fields=" + fields +
			", line=" + line + " }";
	}
}
