package org.scriptbox.box.sys;

import java.util.regex.Pattern;

public class SystemConfiguration {
	
	private static final Pattern linuxPattern = Pattern.compile( ".*Linux.*" );
	private static final Pattern darwinPattern = Pattern.compile( ".*Darwin.*" );
	
	public static boolean isLinux() {
		 return linuxPattern.matcher(System.getProperty("os.name")).matches();
	}
	
	public static boolean isDarwin() {
		 return darwinPattern.matcher(System.getProperty("os.name")).matches();
	}
}
