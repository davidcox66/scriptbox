package org.scriptbox.util.common.io;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SingleFileSource implements FileSource {

	private Set<File> files;
	
	public SingleFileSource( File file ) {
		Set<File> f = new HashSet<File>(1);
		f.add( file );
		files = Collections.unmodifiableSet(f);
	}
	
	@Override
	public Set<File> getFiles() {
		return files;
	}

}
