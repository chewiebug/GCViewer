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
 * @author Ruwin Veldwijk
 */
public class TestDataReaderIBMi5OS1_4_2 {

    @Test
    public void testParse1() throws Exception {
        final InputStream in = UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_IBM, "SampleIBMi5OS1_4_2.txt");
        final DataReader reader = new DataReaderIBMi5OS1_4_2(in);
        GCModel model = reader.read();
        
        assertEquals("number of events", 53, model.size());
    }

}
