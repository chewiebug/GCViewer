package com.tagtraum.perf.gcviewer.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.ExtendedType;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Generation;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for methods written in {@link AbstractGCEvent}.
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 30.09.2012</p>
 */
public class TestAbstractGCEvent {

    @Test
    public void getGenerationParNew() {
        // 6.727: [GC 6.727: [ParNew: 1610619K->7990K(22649280K), 0.0379110 secs] 1610619K->7990K(47815104K), 0.0380570 secs] [Times: user=0.59 sys=0.04, real=0.04 secs] 
        GCEvent event = new GCEvent();
        event.setType(Type.GC);

        GCEvent parNewEvent = new GCEvent();
        parNewEvent.setType(Type.PAR_NEW);

        event.add(parNewEvent);

        assertEquals("generation", Generation.YOUNG, event.getGeneration());
    }

    @Test
    public void getGenerationCmsInitialMark() {
        // 6.765: [GC [1 CMS-initial-mark: 0K(25165824K)] 410644K(47815104K), 0.0100670 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
        GCEvent event = new GCEvent();
        event.setType(Type.GC);

        GCEvent CmsInitialMarkEvent = new GCEvent();
        CmsInitialMarkEvent.setType(Type.CMS_INITIAL_MARK);

        event.add(CmsInitialMarkEvent);

        assertEquals("generation", Generation.TENURED, event.getGeneration());
    }

    @Test
    public void getGenerationCmsRemark() {
        // 12.203: [GC[YG occupancy: 11281900 K (22649280 K)]12.203: [Rescan (parallel) , 0.3773770 secs]12.580: [weak refs processing, 0.0000310 secs]12.580: [class unloading, 0.0055480 secs]12.586: [scrub symbol & string tables, 0.0041920 secs] [1 CMS-remark: 0K(25165824K)] 11281900K(47815104K), 0.3881550 secs] [Times: user=17.73 sys=0.04, real=0.39 secs] 
        GCEvent event = new GCEvent();
        event.setType(Type.GC);

        GCEvent CmsRemarkEvent = new GCEvent();
        CmsRemarkEvent.setType(Type.CMS_REMARK);

        event.add(CmsRemarkEvent);

        assertEquals("generation", Generation.TENURED, event.getGeneration());
    }

    @Test
    public void getGenerationConcurrentMarkStart() {
        // 3749.995: [CMS-concurrent-mark-start]
        ConcurrentGCEvent event = new ConcurrentGCEvent();
        event.setType(Type.CMS_CONCURRENT_MARK_START);

        assertEquals("generation", Generation.TENURED, event.getGeneration());
    }

    @Test
    public void getGenerationFullGc() {
        // 2012-04-07T01:14:29.222+0000: 37571.083: [Full GC [PSYoungGen: 21088K->0K(603712K)] [PSOldGen: 1398086K->214954K(1398144K)] 1419174K->214954K(2001856K) [PSPermGen: 33726K->33726K(131072K)], 0.4952250 secs] [Times: user=0.49 sys=0.00, real=0.49 secs] 
        GCEvent event = new GCEvent();
        event.setType(Type.FULL_GC);

        GCEvent detailedEvent = new GCEvent();
        detailedEvent.setType(Type.PS_YOUNG_GEN);
        event.add(detailedEvent);

        detailedEvent = new GCEvent();
        detailedEvent.setType(Type.PS_OLD_GEN);
        event.add(detailedEvent);

        detailedEvent = new GCEvent();
        detailedEvent.setType(Type.PS_PERM_GEN);
        event.add(detailedEvent);

        assertEquals("generation", Generation.ALL, event.getGeneration());
    }

    @Test
    public void addExtendedTypePrintGcCause() {
        // 2013-05-25T17:02:46.238+0200: 0.194: [GC (Allocation Failure) [PSYoungGen: 16430K->2657K(19136K)] 16430K->15759K(62848K), 0.0109373 secs] [Times: user=0.05 sys=0.02, real=0.02 secs]
        GCEvent event = new GCEvent();
        event.setExtendedType(ExtendedType.lookup(Type.GC, "GC (Allocation Failure)"));

        GCEvent detailedEvent = new GCEvent();
        detailedEvent.setType(Type.PS_YOUNG_GEN);

        event.add(detailedEvent);

        assertEquals("typeAsString", "GC (Allocation Failure); PSYoungGen", event.getTypeAsString());
    }

    @Test
    public void isFullShenandoah() {
        AbstractGCEvent event = getNewAbstractEvent();

        event.setType(Type.UJL_PAUSE_FULL);
        assertThat("should be full gc", event.isFull(), Matchers.is(true));
    }

    @Test
    public void testInitialPhaseList() {
        AbstractGCEvent event = getNewAbstractEvent();

        assertTrue("phases list is empty", event.getPhases().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullPhase() {
        AbstractGCEvent event = getNewAbstractEvent();
        event.addPhase(null);
    }

    @Test
    public void testAddValidPhase() {
        AbstractGCEvent event = getNewAbstractEvent();

        GCEvent phaseEvent = new GCEvent(161.23, 0, 0, 0, 0.0004235, Type.UJL_ZGC_PAUSE_MARK_START);

        event.addPhase(phaseEvent);

        assertEquals("number of phase events", 1, event.getPhases().size());
        assertEquals("get phase event", phaseEvent, event.getPhases().get(0));
    }

    private AbstractGCEvent getNewAbstractEvent() {
        AbstractGCEvent event = new AbstractGCEvent() {
            @Override
            public void toStringBuffer(StringBuffer sb) {
                // do nothing
            }
        };

        return event;
    }
}
