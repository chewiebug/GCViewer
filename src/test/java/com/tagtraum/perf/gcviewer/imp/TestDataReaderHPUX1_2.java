package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestDataReaderHPUX1_2 {

    @Test
    public void testParse1() throws Exception {
        InputStream in = UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_HP, "SampleHP-UX1_3.txt");
        DataReader reader = new DataReaderHPUX1_2(in);
        GCModel model = reader.read();
        
        assertEquals("number of events", 135, model.size());
    }

}
