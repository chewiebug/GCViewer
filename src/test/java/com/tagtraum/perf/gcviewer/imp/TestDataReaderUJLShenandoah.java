package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.time.ZonedDateTime;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.DateHelper;
import org.junit.Test;

/**
 * Created by Mart on 10/05/2017.
 */
public class TestDataReaderUJLShenandoah {
    private GCModel getGCModelFromLogFile(String fileName) throws IOException {
        return UnittestHelper.getGCModelFromLogFile(fileName, FOLDER.OPENJDK_UJL, DataReaderUnifiedJvmLogging.class);
    }

    @Test
    public void parseBasicEvent() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahBasic.txt");
        assertThat("size", model.size(), is(5));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(2));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(0));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(3));
        assertThat("total pause time", model.getPause().getSum(), closeTo(0.001318, 0.000001));
        assertThat("gc pause time", model.getGCPause().getSum(), closeTo(0.001318, 0.000001));
        assertThat("full gc pause time", model.getFullGCPause().getSum(), is(0.0));
        assertThat("heap size after concurrent cycle", model.getPostConcurrentCycleHeapUsedSizes().getMax(), is(33 * 1024));
        assertThat("max memory freed during STW pauses", model.getFreedMemoryByGC().getMax(), is(34 * 1024));

        AbstractGCEvent<?> initialMarkEvent = model.get(0);
        assertThat("isInitialMark", initialMarkEvent.isInitialMark(), is(true));

        AbstractGCEvent<?> finalMarkEvent = model.get(2);
        assertThat("isRemark", finalMarkEvent.isRemark(), is(true));

        AbstractGCEvent<?> concurrentMarkingEvent = model.get(1);
        assertThat("event is start of concurrent collection", concurrentMarkingEvent.isConcurrentCollectionStart(), is(true));

        AbstractGCEvent<?> concurrentResetEvent = model.get(4);
        assertThat("event is end of concurrent collection", concurrentResetEvent.isConcurrentCollectionEnd(), is(true));
    }

    @Test
    public void parseAllocationFailure() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahAllocationFailure.txt");
        assertThat("size", model.size(), is(1));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(0));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));
        assertThat("total pause time", model.getPause().getSum(), closeTo(14.289335, 0.000001));
        assertThat("gc pause time", model.getGCPause().getSum(), is(0.0));
        assertThat("full gc pause time", model.getFullGCPause().getSum(), closeTo(14.289335, 0.000001));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("type", event.getTypeAsString(), startsWith(Type.UJL_PAUSE_FULL.toString()));
        assertThat("preUsed heap size", event.getPreUsed(), is(7943 * 1024));
        assertThat("postUsed heap size", event.getPostUsed(), is(6013 * 1024));
        assertThat("total heap size", event.getTotal(), is(8192 * 1024));
        assertThat("timestamp", event.getTimestamp(), closeTo(43.948, 0.001));
        assertThat("generation", event.getGeneration(), is(AbstractGCEvent.Generation.ALL));
    }

    @Test
    public void parseDefaultConfiguration() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahDefaultConfiguration.txt");

        assertThat("size", model.size(), is(140));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(4));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(5));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("type", event.getTypeAsString(), equalTo(Type.UJL_PAUSE_FULL + " (System.gc())"));
        assertThat("is system gc", event.isSystem(), is(true));
        assertThat("preUsed heap size", event.getPreUsed(), is(10 * 1024));
        assertThat("postUsed heap size", event.getPostUsed(), is(1 * 1024));
        assertThat("total heap size", event.getTotal(), is(128 * 1024));
        assertThat("timestamp", event.getTimestamp(), closeTo(1.337, 0.001));
        assertThat("generation", event.getGeneration(), is(AbstractGCEvent.Generation.ALL));
    }

    @Test
    public void parsePassiveHeuristics() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahPassiveHeuristics.txt");
        assertThat("size", model.size(), is(0));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(0));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(0));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));
    }


    @Test
    public void parseAggressiveHeuristics() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahAggressiveHeuristics.txt");
        assertThat("size", model.size(), is(549));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(4));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(0));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(5));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("type", event.getTypeAsString(), is(AbstractGCEvent.Type.UJL_SHEN_INIT_MARK.toString()));

        ConcurrentGCEvent event2 = (ConcurrentGCEvent) model.get(1);
        assertThat("type", event2.getTypeAsString(), is(AbstractGCEvent.Type.UJL_SHEN_CONCURRENT_CONC_MARK.toString()));
        assertThat("preUsed heap size", event2.getPreUsed(), is(90 * 1024));
        assertThat("postUsed heap size", event2.getPostUsed(), is(90 * 1024));
        assertThat("total heap size", event2.getTotal(), is(128 * 1024));
        assertThat("timestamp", event2.getTimestamp(), closeTo(8.350, 0.001));
        assertThat("generation", event2.getGeneration(), is(AbstractGCEvent.Generation.TENURED));
    }

    @Test
    public void parseSingleSystemGCEvent() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahSingleSystemGC.txt");
        assertThat("size", model.size(), is(353));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(4));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(5));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("type", event.getTypeAsString(), equalTo(Type.UJL_PAUSE_FULL + " (System.gc())"));
        assertThat("is system gc", event.isSystem(), is(true));
        assertThat("preUsed heap size", event.getPreUsed(), is(10 * 1024));
        assertThat("postUsed heap size", event.getPostUsed(), is(1 * 1024));
        assertThat("total heap size", event.getTotal(), is(128 * 1024));
        assertThat("timestamp", event.getTimestamp(), closeTo(1.481, 0.001));
        assertThat("generation", event.getGeneration(), is(AbstractGCEvent.Generation.ALL));
    }

    @Test
    public void parseSeveralSystemGCEvents() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahSeveralSystemGC.txt");
        assertThat("size", model.size(), is(438));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(0));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("type", event.getTypeAsString(), equalTo(Type.UJL_PAUSE_FULL + " (System.gc())"));
        assertThat("is system gc", event.isSystem(), is(true));
        assertThat("preUsed heap size", event.getPreUsed(), is(10 * 1024));
        assertThat("postUsed heap size", event.getPostUsed(), is(1 * 1024));
        assertThat("total heap size", event.getTotal(), is(128 * 1024));
        assertThat("generation", event.getGeneration(), is(AbstractGCEvent.Generation.ALL));
        assertThat("timestamp", event.getTimestamp(), closeTo(1.303, 0.001));
    }

    @Test
    public void parseDateTimeStamps() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahDateTimeStamps.txt");
        assertThat("size", model.size(), is(557));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("datestamp", event.getDatestamp(), is(ZonedDateTime.parse("2017-08-30T23:22:47.357+0300",
                DateHelper.DATE_TIME_FORMATTER)));
        assertThat("timestamp", event.getTimestamp(), is(0.0));
        assertThat("type", event.getTypeAsString(), is(AbstractGCEvent.Type.UJL_SHEN_INIT_MARK.toString()));
        assertThat("generation", event.getGeneration(), is(AbstractGCEvent.Generation.TENURED));

        ConcurrentGCEvent event2 = (ConcurrentGCEvent) model.get(1);
        assertThat("datestamp", event.getDatestamp(), is(ZonedDateTime.parse("2017-08-30T23:22:47.357+0300",
                DateHelper.DATE_TIME_FORMATTER)));
        assertThat("timestamp", event2.getTimestamp(), closeTo(0.003, 0.001));
        assertThat("type", event2.getTypeAsString(), is(AbstractGCEvent.Type.UJL_SHEN_CONCURRENT_CONC_MARK.toString()));
        assertThat("preUsed heap size", event2.getPreUsed(), is(90 * 1024));
        assertThat("postUsed heap size", event2.getPostUsed(), is(90 * 1024));
        assertThat("total heap size", event2.getTotal(), is(128 * 1024));
        assertThat("generation", event2.getGeneration(), is(AbstractGCEvent.Generation.TENURED));
    }

}
