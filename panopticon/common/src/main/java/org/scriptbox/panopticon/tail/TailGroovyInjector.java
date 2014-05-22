package org.scriptbox.panopticon.tail;

import groovy.lang.Closure;
import groovy.util.Expando;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.scriptbox.util.common.io.GlobFileSource;
import org.scriptbox.util.common.io.TailerListenerFactory;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TailGroovyInjector implements TailInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger( TailGroovyInjector.class );
	
	private TailPlugin plugin;

	
	public TailPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(TailPlugin plugin) {
		this.plugin = plugin;
	}

	public String getLanguage() {
		return "groovy";
	}
	
	public void inject( BoxContext context ) {
		Lookup vars = context.getScriptVariables();
		vars.put( "tail", new MethodClosure( this, "tail")  );
		vars.put( "coalesce", new MethodClosure( this, "coalesce")  );
		vars.put( "objectify", new MethodClosure( this, "objectify")  );
	}
	
	public Closure objectify( final Pattern expression, final List<String> fields, final Closure closure ) {
		return objectify( expression, fields, closure, null );
	}
	
	public Closure objectify( final Pattern expression, final List<String> fields, final Closure closure, final Closure rejects ) {
		LOGGER.debug( "objectify: expression: " + expression );
		class Objectify extends Closure {
			public Objectify() {
				super( closure, closure.getThisObject() );
				maximumNumberOfParameters = closure.getMaximumNumberOfParameters();
				parameterTypes = closure.getParameterTypes();
			}
			public Object doCall(Object... arguments) {
				Object ret = null;
				String line = (String)arguments[0];
				Matcher matcher = expression.matcher(line);
				boolean matching = matcher.matches(); 
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "objectify: match: " + matching + ", line: '" + line + "'" ); }
				boolean accepted = false;
				if( matching ) {
					if( matcher.groupCount() == fields.size() ) {
						accepted = true;
						Expando obj = new Expando();
						int i=1;
						for( String field : fields ) {
							obj.setProperty( field, matcher.group(i++) );
						}
						if( arguments.length == 1 ) {
							ret = closure.call( obj );
						}
						else if( arguments.length == 2 ) {
							ret = closure.call( obj, arguments[1] );
						}
						else if( arguments.length == 3 ) {
							ret = closure.call( obj, arguments[1], line );
						}
						else {
							throw new RuntimeException( "Must pass closure with (fields,[file],[line]) to objectify()");
						}
					}
					else {
						if( LOGGER.isDebugEnabled() ) { 
							String parsed = "";
							if( matching ) {
								Map<String,String> obj = new HashMap<String,String>();
								int i=1;
								for( String field : fields ) {
									if( i > matcher.groupCount() || i > fields.size() ) {
										break;
									}
									obj.put( field, matcher.group(i++) );
								}
								parsed = obj.toString();
							}
							LOGGER.debug( "objectify: rejected - " +
								", count: " + matcher.groupCount() + 
								", expected: " + fields.size() + 
								", parsed: " + parsed + 
								", line: '" + line + "'");
						}
					}
				}
				if( !accepted && rejects != null ) {
					rejects.call( arguments );
				}
				return ret;
			}
		};
		return new Objectify();
	}
	
	public Closure coalesce( final Pattern expression, final Closure closure ) {
		LOGGER.debug( "coalesce: expression: " + expression );
		class CoalesceClosure extends Closure {
			private boolean matched;
			private StringBuilder builder = new StringBuilder();
			public CoalesceClosure() {
				super( closure, closure.getThisObject() );
				maximumNumberOfParameters = closure.getMaximumNumberOfParameters();
				parameterTypes = closure.getParameterTypes();
			}
			public Object doCall(Object... arguments) {
				String line = (String)arguments[0];
				Object ret = null;
				boolean matching = expression.matcher(line).matches(); 
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "coalesce: match: " + matching + ", line: '" + line + "'"); }
				if( matching ) {
					if( builder.length() > 0 ) {
						String str = builder.toString();
						if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "coalesce: processing queued text: '" + str + "'"); }
						if( arguments.length == 1 ) {
							ret = closure.call( str );
						}
						else if( arguments.length == 2 ) {
							ret = closure.call( str, arguments[1] );
						}
						else {
							throw new RuntimeException( "Must pass closure with (line,[file]) to coalesce()");
						}
						builder.setLength(0);
					}
					else {
						LOGGER.info( "coalesce: no queued text yet");
					}
				}
				else if( builder.length() > 0 ) {
					builder.append( "\n" );
				}
				builder.append( line );
				matched = matching;
				return ret;
			}
		};
		return new CoalesceClosure();
	}
	
    public void tail( int seconds, Collection<String> exprs, final Closure tailer ) {
    	final BoxContext context = BoxContext.getCurrentContext();
    	plugin.tail( 
    		seconds*1000,
    		new GlobFileSource(exprs), 
	    	new TailerListenerFactory() {
    			public TailerListener create( final File file ) {
    				return new TailerListener() {
    					public void handle(final String line) {
    						try {
	    						BoxContext.with(context, new ParameterizedRunnable<BoxContext>() {
	    							public void run( BoxContext ctx ) {
						    			if( tailer.getMaximumNumberOfParameters() == 2 ) {
						    				tailer.call( line, file );
						    			}
						    			else {
						    				tailer.call( line );
						    			}
	    							}
	    						} );
    						}
    						catch( Exception ex ) {
    							LOGGER.error( "Error processing tail: '" + line + "'", ex );
    						}
    					}
    					public void handle(Exception ex) {
    						LOGGER.error( "Error tailing file: " + file, ex );
    					}
    					public void fileRotated() {
    						LOGGER.info( "File rotated: " + file );
    					}
    					public void fileNotFound() { }
    					public void init(Tailer tailer) {}
    				};
    			}
	    	}
	    );
    }
}
