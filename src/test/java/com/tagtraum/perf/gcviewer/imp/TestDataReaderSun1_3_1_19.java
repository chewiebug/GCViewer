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
class TestDataReaderSun1_3_1_19 {

    @Test
    void testCMSPrintGCDetails() throws Exception {
    	// does not seem to be implemented at all
    	
        String fileName = "SampleSun1_3_1_19SunOS.txt";
    	try (final InputStream in = UnittestHelper.getResourceAsStream(FOLDER.OPENJDK, fileName)) {
            final DataReader reader = new DataReaderSun1_3_1(new GcResourceFile(fileName), in, GcLogType.SUN1_3_1);
            GCModel model = reader.read();

            assertEquals(95.21, model.getThroughput(), 0.01, "throughput");
        }
    }

}
