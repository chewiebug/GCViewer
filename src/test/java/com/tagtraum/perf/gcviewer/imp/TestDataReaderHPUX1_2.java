package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestDataReaderHPUX1_2 {

    @Test
    public void testParse1() throws Exception {
        String fileName = "SampleHP-UX1_3.txt";
        InputStream in = UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_HP, fileName);
        DataReader reader = new DataReaderHPUX1_2(new GCResource(fileName), in);
        GCModel model = reader.read();
        
        assertEquals("number of events", 135, model.size());
    }

}
