package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Test;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestDataReaderIBM1_2_2 {

    @Test
    public void testParse1() throws Exception {
        String fileName = "SampleIBM1_2_2.txt";
        InputStream in = UnittestHelper.getResourceAsStream(FOLDER.IBM, fileName);
        DataReader reader = new DataReaderIBM1_3_0(new GcResourceFile(fileName), in);
        GCModel model = reader.read();
        
        assertEquals("number of events", 28, model.size());
    }
}
