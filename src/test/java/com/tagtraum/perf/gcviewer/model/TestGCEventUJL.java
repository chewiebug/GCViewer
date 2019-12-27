package com.tagtraum.perf.gcviewer.model;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestGCEventUJL {
    private GCEventUJL parentGCEvent;

    @BeforeEach
    void setUp() {
        parentGCEvent = createGcEventUJL(87.11, 120390, 67880, 194540, 0, Type.UJL_ZGC_GARBAGE_COLLECTION);
    }

    @AfterEach
    void tearDown() {
        parentGCEvent = null;
    }

    @Test
    void addPhaseSerial() {
        GCEvent gcEventSerial = createGcEventUJL(101.53, 0, 0, 0, 0.0053923, Type.UJL_ZGC_PAUSE_MARK_START);
        parentGCEvent.addPhase(gcEventSerial);

        assertEquals(1, parentGCEvent.getPhases().size(), "size of phases list");
        assertEquals(gcEventSerial, parentGCEvent.getPhases().get(0), "phase event");

        assertThat("total phases pause time", 0.0053923, closeTo(parentGCEvent.getPause(), 0.00001));
    }

    @Test
    void addPhaseNonSerial() {
        ConcurrentGCEvent gcEventConcurrent = createConcurrentGcEvent(101.53, 0, 0, 0, 0.0053923, Type.UJL_ZGC_CONCURRENT_MARK);
        parentGCEvent.addPhase(gcEventConcurrent);

        assertEquals(1, parentGCEvent.getPhases().size(), "size of phases list");
        assertEquals(gcEventConcurrent, parentGCEvent.getPhases().get(0), "phase event");

        assertThat("total phases pause time", 0d, closeTo(parentGCEvent.getPause(), 0.00001));
    }

    @Test
    void addMultiplePhases() {
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

        assertEquals(5, parentGCEvent.getPhases().size(), "size of phases list");
        assertEquals(gcEventSerialMarkStart, parentGCEvent.getPhases().get(0), "phase event 1");
        assertEquals(gcEventSerialMarkEnd, parentGCEvent.getPhases().get(1), "phase event 2");
        assertEquals(gcEventSerialRelocateStart, parentGCEvent.getPhases().get(2), "phase event 3");
        assertEquals(gcEventConcurrentMark, parentGCEvent.getPhases().get(3), "phase event 4");
        assertEquals(gcEventConcurrentRelocate, parentGCEvent.getPhases().get(4), "phase event 5");

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
