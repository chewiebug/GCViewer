package com.tagtraum.perf.gcviewer.model;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import org.junit.Test;

import java.io.File;
import java.io.RandomAccessFile;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a> <p>created on: 19.08.2015</p>
 */
public class TestGCResource {

    @Test
    public void hasUnderlyingResourceChanged() throws Exception {
        File testFile = File.createTempFile("GCResourceUnittest", ".txt");
        try {
            try (RandomAccessFile file = new RandomAccessFile(testFile, "rws")) {
                GCResource resource = new GcResourceFile(testFile.getAbsolutePath());
                assertThat("before", resource.hasUnderlyingResourceChanged(), is(true));

                resource.getModel().add(new GCEvent(0.123, 100, 10, 1024, 0.2, Type.PAR_NEW));
                resource.getModel().setURL(testFile.toURI().toURL());

                assertThat("after initialisation", resource.hasUnderlyingResourceChanged(), is(false));

                file.write("hello world".getBytes());

                assertThat("after file change", resource.hasUnderlyingResourceChanged(), is(true));
            }
        }
        finally {
            assertThat(testFile.delete(), is(true));
        }
    }
}
