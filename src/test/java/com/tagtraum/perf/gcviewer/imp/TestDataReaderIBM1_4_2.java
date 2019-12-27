/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.perf.gcviewer.imp;

import java.io.InputStream;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
@Disabled
class TestDataReaderIBM1_4_2 {

    @Test
    void testParse1() throws Exception {
        String fileName = "SampleIBM1_4_2.txt";
        try (final InputStream in = UnittestHelper.getResourceAsStream(FOLDER.IBM, fileName)) {
            final DataReader reader = new DataReaderIBM1_4_2(new GcResourceFile(fileName), in);
            GCModel model = reader.read();

            assertEquals(2884, model.size(), "number of events");
        }
    }
}
