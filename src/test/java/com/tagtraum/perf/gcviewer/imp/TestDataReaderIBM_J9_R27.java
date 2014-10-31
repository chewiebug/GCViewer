package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import org.junit.Test;

/**
 * @author <a href="gcviewer@gmx.ch">Joerg Wuethrich</a>
 *         <p>created on 08.10.2014</p>
 */
public class TestDataReaderIBM_J9_R27 {

    @Test
    public void testFull() throws Exception {
        InputStream in = UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_IBM, "SampleIBMJ9_R27_SR1_full_header.txt");
        DataReader reader = new DataReaderIBM_J9_R27(in);
        GCModel model = reader.read();
        
        assertThat("model size", model.size(), is(1));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("pause", event.getPause(), closeTo(0.042303, 0.0000001));

        assertThat("total before", event.getTotal(), is(toKiloBytes(1073741824)));
        assertThat("free before", event.getPreUsed(), is(toKiloBytes(1073741824 - 804158480)));
        assertThat("free after", event.getPostUsed(), is(toKiloBytes(1073741824 - 912835672)));

        assertThat("total young before", event.getYoung().getTotal(), is(toKiloBytes(268435456)));
        assertThat("young before", event.getYoung().getPreUsed(), is(toKiloBytes(268435456)));
        assertThat("young after", event.getYoung().getPostUsed(), is(toKiloBytes(268435456 - 108677192)));

        assertThat("total tenured before", event.getTenured().getTotal(), is(toKiloBytes(805306368)));
        assertThat("tenured before", event.getTenured().getPreUsed(), is(toKiloBytes(805306368 - 804158480)));
        assertThat("tenured after", event.getTenured().getPostUsed(), is(toKiloBytes(805306368 - 804158480)));

        System.out.println(model.toString());
    }

    private int toKiloBytes(long bytes) {
        return (int)Math.rint(bytes / (double)1024);
    }
}
