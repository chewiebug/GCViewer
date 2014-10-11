package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.model.GCModel;
import org.junit.Test;

import java.io.InputStream;

/**
 * @author <a href="gcviewer@gmx.ch">Joerg Wuethrich</a>
 *         <p>created on 08.10.2014</p>
 */
public class TestDataReaderIBM_J9_R27 {

    @Test
    public void testFull() throws Exception {
        InputStream in = UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_IBM, "SampleIBMJ9_R27_full.txt");
        DataReader reader = new DataReaderIBM_J9_R27(in);
        GCModel model = reader.read();
        System.out.println(model.toString());
    }
}
