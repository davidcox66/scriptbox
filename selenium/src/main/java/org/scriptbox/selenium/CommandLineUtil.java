package org.scriptbox.selenium;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by david on 5/29/15.
 */
public class CommandLineUtil {

    public static List<File> resolveFiles( String filespec, String extension ) {
        List<File> ret = new ArrayList<File>();
        String[] specs = filespec.split(",");
        for( String spec : specs ) {
            File file = new File( spec );
            String name = file.getName();
            if( name.indexOf("*") == -1 && name.indexOf("?") == -1 )	 {
                if( file.isDirectory() ) {
                    ret.addAll( Arrays.asList(
                        StringUtils.isNotEmpty(extension) ?
                            file.listFiles((FileFilter) new SuffixFileFilter(extension)) :
                            file.listFiles()) );
                }
                else {
                    if( StringUtils.isNotEmpty(extension) && !name.endsWith(extension) ) {
                        file = new File( spec + extension );
                    }
                    if( file.exists() ) {
                        ret.add(file);
                    }
                    else {
                        throw new RuntimeException( "Could not find file: " + file );
                    }
                }
            }
            else {
                File parent = file.getParentFile();
                if( parent == null ) {
                    parent = new File( "." );
                }
                FileFilter filter = StringUtils.isNotEmpty(extension) ?
                     (FileFilter)new AndFileFilter(new WildcardFileFilter(name),new SuffixFileFilter(extension)) :
                     (FileFilter)new WildcardFileFilter(name);

                ret.addAll( Arrays.asList(parent.listFiles(filter)) );
            }
        }
        return ret;
    }

}
