package com.tagtraum.perf.gcviewer.view.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * ExtensionFileFilter.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ExtensionFileFilter extends FileFilter {

    public static final ExtensionFileFilter[] EXT_FILE_FILTERS = {
        new ExtensionFileFilter("txt"), new ExtensionFileFilter("gc"), new ExtensionFileFilter("log"),
        new ExtensionFileFilter("txt.gz"), new ExtensionFileFilter("gc.gz"), new ExtensionFileFilter("log.gz")
    };

    protected final String extension;

    public ExtensionFileFilter(String extension) {
        this.extension = extension;
    }

    public boolean accept(File f) {
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
