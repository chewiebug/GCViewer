package com.tagtraum.perf.gcviewer;

import com.tagtraum.perf.gcviewer.exp.CSVDataWriter;
import com.tagtraum.perf.gcviewer.exp.PlainDataWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 *
 * Date: Feb 1, 2002
 * Time: 10:34:39 AM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class DataWriterFactory {

    private static ResourceBundle localStrings = ResourceBundle.getBundle("com.tagtraum.perf.gcviewer.localStrings");

    public DataWriter getDataWriter(File file, String extension) throws IOException {
        if (extension.equals(".csv")) {
            return new CSVDataWriter(new FileOutputStream(file));
        } else if (extension.equals(".txt")) {
            return new PlainDataWriter(new FileOutputStream(file));
        } else
            throw new IOException(localStrings.getString("datawriterfactory_instantiation_failed") + " " + file);
    }

}
