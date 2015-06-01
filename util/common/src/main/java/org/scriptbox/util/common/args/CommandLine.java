package org.scriptbox.util.common.args;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandLine {

	private Map<String,String> map = new HashMap<String,String>(); 
	private List<String> parameters = new ArrayList<String>();
	
	public CommandLine( String[] args ) throws CommandLineException {
		int i=0;
		boolean rem = false;
		for(  ; i < args.length ; i++ ) {
			if( !rem && args[i].startsWith("--") ) {
				int pos = args[i].indexOf("=");
				if( pos != -1 ) {
					String name = args[i].substring(2,pos);
					String value = args[i].substring(pos+1);
					if( !isBlank(name) && !isBlank(value) ) {  
						map.put(name, value );
					}
					else {
						throw new CommandLineException( "Invalid command line argument: '" + args[i] + "'" );
					}
				}
				else {
					String name = args[i].substring(2);
					map.put( name, "");
					
				}
			}
			else {
				rem = true;
				parameters.add( args[i] );
			}
		}
	}

	public List<String> getParameters() {
		return parameters;
	}
	
	public String getParameter( int ind ) throws CommandLineException {
		if( ind < 0 || ind >= parameters.size() )  {
			throw new CommandLineException( "Missing parameter: " + (ind+1) );
		}
		return parameters.get( ind );
	}
	
	public void checkParameters( int count ) throws CommandLineException {
		if( parameters.size() != count ) {
			throw new CommandLineException( "Expected " + count + " parameters" );
		}
	}
	
	public void checkParameters( String name, int count ) throws CommandLineException {
		if( parameters.size() != count ) {
			throw new CommandLineException( "Expected " + count + " parameters for " + name );
		}
	}
	
	public void checkParameters( String name, int min, int max ) throws CommandLineException {
		if( min > 0 && parameters.size() < min ) {
			throw new CommandLineException( "Expected at least " + min + " parameters for " + name );
		}
		if( max > 0 && parameters.size() > max ) {
			throw new CommandLineException( "Expected no more than" + max + " parameters for " + name );
		}
	}
	
	public boolean hasArg( String name ) throws CommandLineException {
		String value = map.get(name);
		return checkArg( name, value );
	}
	
	public boolean hasArgValue( String name ) throws CommandLineException {
		String value = map.get( name );
		return !isBlank( value );
	}

	public String getArgValue( String name, boolean required ) throws CommandLineException {
		String value = map.get(name);
		checkArgValue( name, value );
		if( value == null && required ) {
			throw new CommandLineException( "Argument is required: '" + name + "'" );
		}
		return value;
	}

	public String getArgValue( String name, String defaultValue ) throws CommandLineException {
		String val = getArgValue( name, false );
		if( isBlank(val) ) {
			val = defaultValue;
		}
		return val;
	}

	public int getArgValueAsInt( String name, int defaultValue ) throws CommandLineException {
		String value = getArgValue( name, false );
		if( !isBlank(value) ) {
			try {
				return Integer.parseInt( value );
			}
			catch( Exception ex ) {
				throw new CommandLineException( "Invalid integer parameter: '" + value + "'");
			}
		}
		return defaultValue;
	}

	public int getArgArgValueAsInt( String name, boolean required ) throws CommandLineException {
		String value = getArgValue( name, required );
		if( !isBlank(value) ) {
			try {
				return Integer.parseInt( value );
			}
			catch( Exception ex ) {
				throw new CommandLineException( "Invalid integer parameter: '" + value + "'");
			}
		}
		return 0;
	}

	public boolean consumeArg( String name ) throws CommandLineException {
		String value = map.remove(name);
		return checkArg( name, value );
	}
	
	public boolean consumeArgWithMinParameters( String name, int count ) throws CommandLineException {
		return consumeArgWithParameters( name, count, -1 );
	}
	
	public boolean consumeArgWithParameters( String name, int count ) throws CommandLineException {
		if( consumeArg(name) ) {
			checkParameters(name, count);
			return true;
		}
		return false;
	}
	public boolean consumeArgWithParameters( String name, int min, int max ) throws CommandLineException {
		if( consumeArg(name) ) {
			checkParameters(name, min, max);
			return true;
		}
		return false;
	}

	public int consumeArgValueAsInt( String name, int defaultValue ) throws CommandLineException {
		String value = consumeArgValue( name, false );
		if( !isBlank(value) ) {
			try {
				return Integer.parseInt( value );
			}
			catch( Exception ex ) {
				throw new CommandLineException( "Invalid integer parameter: '" + value + "'");
			}
		}
		return defaultValue;
	}
	
	public int consumeArgValueAsInt( String name, boolean required ) throws CommandLineException {
		String value = consumeArgValue( name, required );
		if( !isBlank(value) ) {
			try {
				return Integer.parseInt( value );
			}
			catch( Exception ex ) {
				throw new CommandLineException( "Invalid integer parameter: '" + value + "'");
			}
		}
		return 0;
	}

	public String consumeArgValue( String name, String defaultValue ) throws CommandLineException {
		String val = consumeArgValue( name, false );
		if( isBlank(val) ) {
			val = defaultValue;
		}
		return val;
	}
	
	public String consumeArgValue( String name, boolean required ) throws CommandLineException {
		String value = map.remove(name);
		checkArgValue( name, value );
		if( value == null && required ) {
			throw new CommandLineException( "Argument is required: '" + name + "'" );
		}
		return value;
	}

	public void checkUnusedArgs() throws CommandLineException {
		if( map.size() > 0 )  {
			StringBuilder builder = new StringBuilder();
			for( String arg : map.keySet() ) {
				if( builder.length() > 0 ) {
					builder.append( "," );
				}
				builder.append( arg );
			}
			throw new CommandLineException( "Unknown parameters: " + builder );
		}
	}
	private boolean checkArg( String name, String value ) throws CommandLineException {
		if( value != null ) {
			if( !isBlank(value) ) {
				throw new CommandLineException( "Argument does not take a value: '" + name + "'" );
			}
			return true;
		}
		return false;
	}
	
	public boolean checkArgValue( String name, String value ) throws CommandLineException {
		if( value != null ) {
			if( isBlank(value) ) {
				throw new CommandLineException( "Argument expects a parameter: '" + name + "'" );
			}
			return true;
		}
		return false;
	}
	
	private static boolean isBlank( String str ) {
		return str == null || str.trim().length() == 0;
	}
}
