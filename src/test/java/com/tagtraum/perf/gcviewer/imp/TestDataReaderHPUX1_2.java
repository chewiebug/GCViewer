package com.tagtraum.perf.gcviewer.imp;

import java.io.InputStream;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
class TestDataReaderHPUX1_2 {

    @Test
    void testParse1() throws Exception {
        String fileName = "SampleHP-UX1_3.txt";
        try (InputStream in = UnittestHelper.getResourceAsStream(FOLDER.HP, fileName)) {
            DataReader reader = new DataReaderHPUX1_2(new GcResourceFile(fileName), in);
            GCModel model = reader.read();

            assertEquals(135, model.size(), "number of events");
        }
    }
}
