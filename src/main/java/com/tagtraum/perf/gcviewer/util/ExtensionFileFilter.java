package com.tagtraum.perf.gcviewer.util;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * ExtensionFileFilter.
 * <p/>
 * Date: Sep 24, 2005
 * Time: 7:14:23 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ExtensionFileFilter extends FileFilter {

    public static final ExtensionFileFilter TxtExtensionFilter = new ExtensionFileFilter("txt");
    public static final ExtensionFileFilter GcExtensionFilter = new ExtensionFileFilter("gc");
    public static final ExtensionFileFilter LogExtensionFilter = new ExtensionFileFilter("log");


    private String extension;

    public ExtensionFileFilter(String extension) {
        this.extension = extension;
    }

    public boolean accept(File f) {
    	// TODO: Can probably be refactored; seems to be the same as in Export class
    	try {
    		return f.isDirectory() || f.toString().toLowerCase().endsWith("." + extension);
    	} 
    	catch (NullPointerException e) {
    		return false;
    	}
    }

    public String getDescription() {
        return "*." + extension;
    }
}
