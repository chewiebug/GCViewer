package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests unified jvm logging parser for safepoint events.
 */
public class TestDataReaderUJLSafepoint {

    private GCModel getGCModelFromLogFile(String fileName) throws IOException {
        return UnittestHelper.getGCModelFromLogFile(fileName, FOLDER.OPENJDK_UJL, DataReaderUnifiedJvmLogging.class);
    }

    @Test
    public void parseGcDefaults() throws Exception {
        GCModel model = getGCModelFromLogFile("sample-ujl-safepoint-defaults.txt");
        assertThat("size", model.size(), is(46));
        assertThat("amount of gc event types", model.getGcEventPauses().size(), is(1));
        assertThat("amount of gc events", model.getGCPause().getN(), is(20));
        assertThat("amount of full gc event types", model.getFullGcEventPauses().size(), is(0));
        assertThat("amount of full gc events", model.getFullGCPause().getN(), is(0));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));

        List<AbstractGCEvent<?>> vmOperationsEvents = toList(model.getVmOperationsEvents());
	assertThat("amount of application stopped types", vmOperationsEvents.size(), is(26));


        AbstractGCEvent<?> event = model.get(0);
        assertThat(event.getTypeAsString(),is("Total time for which application threads were stopped"));
        assertThat(event.getPause(),is(0.0000000235));
    }
    
    private static <T> List<T> toList(Iterator<T> iterator){
	ArrayList<T> list = new ArrayList<>();
	iterator.forEachRemaining(list::add);
	return list;
    }

}
