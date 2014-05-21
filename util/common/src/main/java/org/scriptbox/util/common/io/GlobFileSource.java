package org.scriptbox.util.common.io;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.filefilter.WildcardFileFilter;

public class GlobFileSource implements FileSource {

	private Map<File,FileFilter> filters = new HashMap<File,FileFilter>();
	
	public GlobFileSource( String... exprs ) {
		add( Arrays.asList(exprs) );
	}
	
	public GlobFileSource( Collection<String> exprs ) {
		add( exprs );
	}

	public void add( String...exprs ) {
		add( Arrays.asList(exprs) );
	}
	
	public void add( Collection<String> exprs ) {
		for( String expr : exprs ) {
			File f = new File(expr);
			File dir = f.getParentFile();
			String name = f.getName();
			filters.put( dir, new WildcardFileFilter(name) );
		}
	}
	@Override
	public Set<File> getFiles() {
		Set<File> files = new HashSet<File>();
		for( Map.Entry<File,FileFilter> entry : filters.entrySet() ) {
			File dir = entry.getKey();
			FileFilter filter = entry.getValue();
			File[] matched = dir.listFiles( filter );
			for( File f : matched ) {
				files.add( f );
			}
		}
		return files;
	}
}
