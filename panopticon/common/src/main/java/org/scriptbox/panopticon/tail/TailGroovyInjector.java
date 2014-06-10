package org.scriptbox.panopticon.tail;

import groovy.lang.Closure;
import groovy.util.Expando;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.scriptbox.box.groovy.ClosureWrapper;
import org.scriptbox.util.common.io.FileSource;
import org.scriptbox.util.common.io.GlobFileSource;
import org.scriptbox.util.common.io.Tailer;
import org.scriptbox.util.common.io.TailerListener;
import org.scriptbox.util.common.io.TailerListenerFactory;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.dev.util.collect.HashSet;

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
		class Objectify extends ClosureWrapper {
			public Objectify() {
				super( closure );
			}
			public Object doCall(Object... arguments) {
				Object ret = null;
				String line = (String)arguments[0];
				Matcher matcher = expression.matcher(line);
				boolean matching = matcher.matches(); 
				if( LOGGER.isTraceEnabled() ) { LOGGER.trace( "objectify: match: " + matching + ", line: '" + line + "'" ); }
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
							ret = delegate.call( obj );
						}
						else if( arguments.length == 2 ) {
							ret = delegate.call( obj, arguments[1] );
						}
						else if( arguments.length == 3 ) {
							ret = delegate.call( obj, arguments[1], line );
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
		class CoalesceClosure extends ClosureWrapper {
			private boolean matched;
			private StringBuilder builder = new StringBuilder();
			public CoalesceClosure() {
				super( closure );
			}
			public Object doCall(Object... arguments) {
				String line = (String)arguments[0];
				Object ret = null;
				boolean matching = expression.matcher(line).matches(); 
				if( LOGGER.isTraceEnabled() ) { LOGGER.trace( "coalesce: match: " + matching + ", line: '" + line + "'"); }
				
				if( matching ) {
					if( builder.length() > 0 ) {
						String str = builder.toString();
						if( LOGGER.isTraceEnabled() ) { LOGGER.trace( "coalesce: processing queued text: '" + str + "'"); }
						if( arguments.length == 1 ) {
							ret = delegate.call( str );
						}
						else if( arguments.length == 2 ) {
							ret = delegate.call( str, arguments[1] );
						}
						else {
							throw new RuntimeException( "Must pass closure with (line,[file]) to coalesce()");
						}
						builder.setLength(0);
					}
					else {
						LOGGER.trace( "coalesce: no queued text yet");
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
    	tail( seconds, false, exprs, tailer );
    }
    public void tail( int seconds, boolean newer, Collection<String> exprs, final Closure tailer ) {
    	tail( seconds, newer, new GlobFileSource(exprs), tailer );
    }
    
    public void tail( int seconds, boolean newer, final Closure files, final Closure tailer ) {
    	FileSource src = new FileSource() {
    		public Set<File> getFiles() {
    			return new HashSet<File>( (Collection<File>)files.call() );
    		}
    	};
    	tail( seconds, newer, src, tailer );
    }
    public void tail( int seconds, final boolean newer, FileSource source, final Closure tailer ) {
    	final BoxContext context = BoxContext.getCurrentContext();
    	plugin.tail( 
    		seconds*1000,
    		source,
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
    						LOGGER.debug( "File rotated: " + file );
    					}
    					public void fileNotFound() { 
    						LOGGER.debug( "File not found: " + file );
    						
    					}
    					public void init(Tailer tailer) {}
    					
    					public boolean newer( File file ) {
    						LOGGER.info( "File is newer: " + file + ", time: " + new Date(file.lastModified()) );
    						return newer;
    					}
    				};
    			}
	    	}
	    );
    }
}
