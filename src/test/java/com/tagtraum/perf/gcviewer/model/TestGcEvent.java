package com.tagtraum.perf.gcviewer.model;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.ExtendedType;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for class {@link GCEvent}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 04.02.2012</p>
 */
class TestGcEvent {

    private GCEvent gcEvent;
    private GCEvent fullGcEvent;
    
    @BeforeEach
    void setUp() {
        // 87.707: [GC 87.707: [DefNew: 139904K->5655K(157376K), 0.0543079 secs] 194540K->60292K(506944K), 0.0544020 secs] [Times: user=0.03 sys=0.02, real=0.06 secs]
        gcEvent = new GCEvent(87.707, 194540, 60292, 506944, 0.0544020, Type.GC);
        GCEvent defNewEvent = new GCEvent(87.707, 139904, 5655, 157376, 0.0543079, Type.DEF_NEW);
        gcEvent.add(defNewEvent);
        
        // 83.403: [Full GC 83.403: [Tenured: 38156K->54636K(349568K), 0.6013150 secs] 141564K->54636K(506944K), [Perm : 73727K->73727K(73728K)], 0.6014256 secs] [Times: user=0.58 sys=0.00, real=0.59 secs] 
        fullGcEvent = new GCEvent(83.403, 141564, 54636, 506944, 0.6014256, Type.FULL_GC);
        GCEvent tenured = new GCEvent(83.403, 38156, 54636, 349568, 0.6013150, Type.TENURED);
        GCEvent perm = new GCEvent(83.403, 73727, 73727, 73728, 0.6014256, Type.PERM);
        fullGcEvent.add(tenured);
        fullGcEvent.add(perm);
    }
    
    @Test
    void testAddGc() {
        // when GC was parsed, only "young" information really is present; "tenured" must be inferred
        assertEquals(1, gcEvent.details.size(), "number of details");
        
        GCEvent defNewEvent = gcEvent.details().next();
        assertEquals(Type.DEF_NEW.getName(), defNewEvent.getExtendedType().getName(), "type");
        assertEquals(defNewEvent, gcEvent.getYoung(), "getYoung");
        
        GCEvent tenured = gcEvent.getTenured();
        assertNotNull(tenured, "tenured");
    }

    @Test
    void testAddFullGc() {
        // when Full GC was parsed, "young" information was deferred, other were parsed.
        assertEquals(2, fullGcEvent.details.size(), "number of details");
        
        GCEvent tenured = fullGcEvent.details.get(0);
        assertEquals(Type.TENURED.getName(), tenured.getExtendedType().getName(), "type");
        assertEquals(tenured, fullGcEvent.getTenured(), "getTenured");
        
        GCEvent perm = fullGcEvent.details.get(1);
        assertEquals(Type.PERM.getName(), perm.getExtendedType().getName(), "type");
        assertEquals(perm, fullGcEvent.getPerm(), "getPerm");
        
        GCEvent young = fullGcEvent.getYoung();
        assertNotNull(young, "young");
    }

    @Test
    void testGetInferredYoungFullGcEvent() {
        GCEvent young = fullGcEvent.getYoung();
        assertEquals(ExtendedType.UNDEFINED, young.getExtendedType(), "type");
        assertEquals(141564 - 38156, young.getPreUsed(), "preused");
        assertEquals(54636 - 54636, young.getPostUsed(), "postused");
        assertEquals(506944 - 349568, young.getTotal(), "total");
        assertEquals(0.6013150, young.getPause(), 0.00000001, "pause");
    }

    @Test
    void testGetInferredTenuredGcEvent() {
        GCEvent tenured = gcEvent.getTenured();
        assertEquals(ExtendedType.UNDEFINED, tenured.getExtendedType(), "tenured type");
        assertEquals(194540 - 139904, tenured.getPreUsed(), "preused");
        assertEquals(60292 - 5655, tenured.getPostUsed(), "postused");
        assertEquals(506944 - 157376, tenured.getTotal(), "total tenured");
        assertEquals(0.0543079, tenured.getPause(), 0.000001, "pause");
    }

    @Test
    void testCloneAndMerge() {
        // 87.707: [GC 87.707: [DefNew: 139904K->5655K(157376K), 0.0543079 secs] 194540K->60292K(506944K), 0.0544020 secs] [Times: user=0.03 sys=0.02, real=0.06 secs]
        // 83.403: [Full GC 83.403: [Tenured: 38156K->54636K(349568K), 0.6013150 secs] 141564K->54636K(506944K), [Perm : 73727K->73727K(73728K)], 0.6014256 secs] [Times: user=0.58 sys=0.00, real=0.59 secs]
        GCEvent detailEvent1 = new GCEvent(0.01, 100, 90, 1000, 0.25, Type.G1_YOUNG);
        GCEvent detailEvent2 = new GCEvent(0.01, 500, 200, 1000, 0.29, Type.TENURED);
        GCEvent clonedEvent = detailEvent1.cloneAndMerge(detailEvent2);
        assertThat("name", clonedEvent.getTypeAsString(), Matchers.equalTo("GC pause (young)+Tenured"));
        assertThat("heap before", clonedEvent.getPreUsed(), Matchers.is(100 + 500));
    }

}
