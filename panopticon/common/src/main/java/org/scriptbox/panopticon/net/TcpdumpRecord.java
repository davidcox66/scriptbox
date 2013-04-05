package org.scriptbox.panopticon.net;

import java.util.HashMap;
import java.util.Map;

public class TcpdumpRecord {

	public String time;
	public String source;
	public String destination;
	public String flags;
	public Map<String,String> fields = new HashMap<String,String>(10);
	public String line;
}
