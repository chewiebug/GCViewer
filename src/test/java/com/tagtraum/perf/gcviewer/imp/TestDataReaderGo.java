package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

public class TestDataReaderGo {

    @Test
    public void test() throws IOException {
        String gcLog = ""
                + "gc starting...\n" // Such a line is not produced by the Go GC; it is just for testing
                + "gc 1 @997.597s 3%: 68+0.36+0.51 ms clock, 205+0/16/89+1.5 ms cpu, 84->84->42 MB, 86 MB goal, 3 P\n"
                + "a line unrelated to GC logging\n"
                + "gc 2 @997.597s 3%: 68+0.36+0.51 ms clock, 205+0/16/89+1.5 ms cpu, 11111111111111111111111111111111111->84->42 MB, 86 MB goal, 3 P\n"
                + "gc 3 @997.597s 3%: 68+0.36+0.51 ms clock, 205+0/16/89+1.5 ms cpu, 84->84->42 MB, 86 MB goal, 3 P\n";
        ByteArrayInputStream in = new ByteArrayInputStream(gcLog.getBytes("US-ASCII"));
        DataReader reader = new DataReaderGo(new GcResourceFile("byteArray"), in);
        GCModel model = reader.read();

        assertEquals(2, model.size());
    }

    @Test
    public void exampleLog() throws IOException {
        String fileName = "go1.9.txt";
        InputStream in = UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER.GO, fileName);
        DataReader reader = new DataReaderGo(new GcResourceFile(fileName), in);
        GCModel model = reader.read();

        assertEquals(635, model.size());
    }
}
