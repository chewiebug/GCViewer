package com.tagtraum.perf.gcviewer.exp.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.exp.DataWriterType;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;

/**
 * Factory for all available {@link DataWriter} implementations.
 *
 * <p>Date: Feb 1, 2002
 * <p>Time: 10:34:39 AM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class DataWriterFactory {
    public static final String GC_PREFERENCES = "gcPreferences";

    /**
     * Standard factory method to retrieve one of the <code>DataWriter</code> implementations.
     * If a DataWriter implementation needs additional configuration, use the other method.
     *
     * @param file file, where to write output to
     * @param type type of DataWriter
     * @return instance of DataWriter according to <code>type</code> parameter
     * @throws IOException unknown DataWriter or problem creating file
     * @see DataWriterFactory#getDataWriter(File, DataWriterType, Map)
     */
    public static DataWriter getDataWriter(File file, DataWriterType type) throws IOException {
        return getDataWriter(file, type, null);
    }

    /**
     * Factory method to retrieve one of the <code>DataWriter</code> implementations including
     * the option to add a map of configuration objects. The map will be passed to the DataWriter,
     * which can use its contents.
     *
     * @param file file, where to write output to
     * @param type type of DataWriter
     * @param configuration Map containing additional configuration objects that will be passed
     * to the DataWriter. All DataWriters supporting this parameter, need to document, what
     * they expect here.
     * @return instance of DataWriter accorting to <code>type</code> parameter
     * @throws IOException unknown DataWriter or problem creating file
     */
    public static DataWriter getDataWriter(File file, DataWriterType type, Map<String, Object> configuration) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        switch (type) {
            case PLAIN   : return new PlainDataWriter(outputStream);
            case CSV     : return new CSVDataWriter(outputStream);
            case CSV_TS  : return new CSVTSDataWriter(outputStream);
            case SIMPLE  : return new SimpleGcWriter(outputStream);
            case SUMMARY : return new SummaryDataWriter(outputStream, configuration);
            case PNG     : return new PNGDataWriter(outputStream, configuration);
            default : throw new IOException(LocalisationHelper.getString("datawriterfactory_instantiation_failed") + " " + file);
        }
    }

}
