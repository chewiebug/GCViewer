package com.tagtraum.perf.gcviewer.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestGCEventUJL {
    private GCEventUJL parentGCEvent;

    @Before
    public void setUp() {
        parentGCEvent = createGcEventUJL(87.11, 120390, 67880, 194540, 0, Type.UJL_ZGC_GARBAGE_COLLECTION);
    }

    @After
    public void tearDown() {
        parentGCEvent = null;
    }

    @Test
    public void addPhaseSerial() {
        GCEvent gcEventSerial = createGcEventUJL(101.53, 0, 0, 0, 0.0053923, Type.UJL_ZGC_PAUSE_MARK_START);
        parentGCEvent.addPhase(gcEventSerial);

        assertEquals("size of phases list", 1, parentGCEvent.getPhases().size());
        assertEquals("phase event", gcEventSerial, parentGCEvent.getPhases().get(0));

        assertThat("total phases pause time", 0.0053923, closeTo(parentGCEvent.getPause(), 0.00001));
    }

    @Test
    public void addPhaseNonSerial() {
        ConcurrentGCEvent gcEventConcurrent = createConcurrentGcEvent(101.53, 0, 0, 0, 0.0053923, Type.UJL_ZGC_CONCURRENT_MARK);
        parentGCEvent.addPhase(gcEventConcurrent);

        assertEquals("size of phases list", 1, parentGCEvent.getPhases().size());
        assertEquals("phase event", gcEventConcurrent, parentGCEvent.getPhases().get(0));

        assertThat("total phases pause time", 0d, closeTo(parentGCEvent.getPause(), 0.00001));
    }

    @Test
    public void addMultiplePhases() {
        GCEvent gcEventSerialMarkStart = createGcEventUJL(101.53, 0, 0, 0, 0.0053923, Type.UJL_ZGC_PAUSE_MARK_START);
        GCEvent gcEventSerialMarkEnd = createGcEventUJL(107.67, 0, 0, 0, 0.0037829, Type.UJL_ZGC_PAUSE_MARK_END);
        GCEvent gcEventSerialRelocateStart = createGcEventUJL(108.17, 0, 0, 0, 0.0061948, Type.UJL_ZGC_PAUSE_RELOCATE_START);
        ConcurrentGCEvent gcEventConcurrentMark = createConcurrentGcEvent(109.24, 0, 0, 0, 0.7164831, Type.UJL_ZGC_CONCURRENT_MARK);
        ConcurrentGCEvent gcEventConcurrentRelocate = createConcurrentGcEvent(109.88, 0, 0, 0, 0.6723152, Type.UJL_ZGC_CONCURRENT_RELOCATE);

        parentGCEvent.addPhase(gcEventSerialMarkStart);
        parentGCEvent.addPhase(gcEventSerialMarkEnd);
        parentGCEvent.addPhase(gcEventSerialRelocateStart);
        parentGCEvent.addPhase(gcEventConcurrentMark);
        parentGCEvent.addPhase(gcEventConcurrentRelocate);

        assertEquals("size of phases list", 5, parentGCEvent.getPhases().size());
        assertEquals("phase event 1", gcEventSerialMarkStart, parentGCEvent.getPhases().get(0));
        assertEquals("phase event 2", gcEventSerialMarkEnd, parentGCEvent.getPhases().get(1));
        assertEquals("phase event 3", gcEventSerialRelocateStart, parentGCEvent.getPhases().get(2));
        assertEquals("phase event 4", gcEventConcurrentMark, parentGCEvent.getPhases().get(3));
        assertEquals("phase event 5", gcEventConcurrentRelocate, parentGCEvent.getPhases().get(4));

        assertThat("total phases pause time", 0.01537, closeTo(parentGCEvent.getPause(), 0.00001));
    }

    private GCEventUJL createGcEventUJL(double timestamp, int preUsed, int postUsed, int total, double pause, Type type) {
        GCEventUJL gcEventUJL = new GCEventUJL();
        gcEventUJL.setTimestamp(timestamp);
        gcEventUJL.setPreUsed(preUsed);
        gcEventUJL.setPostUsed(postUsed);
        gcEventUJL.setTotal(total);
        gcEventUJL.setPause(pause);
        gcEventUJL.setType(type);

        return gcEventUJL;
    }

    private ConcurrentGCEvent createConcurrentGcEvent(double timestamp, int preUsed, int postUsed, int total, double pause, Type type) {
        ConcurrentGCEvent concurrentGCEvent = new ConcurrentGCEvent();
        concurrentGCEvent.setTimestamp(timestamp);
        concurrentGCEvent.setPreUsed(preUsed);
        concurrentGCEvent.setPostUsed(postUsed);
        concurrentGCEvent.setTotal(total);
        concurrentGCEvent.setPause(pause);
        concurrentGCEvent.setType(type);

        return concurrentGCEvent;
    }
}
