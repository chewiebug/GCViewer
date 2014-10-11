package com.tagtraum.perf.gcviewer.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.ExtendedType;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;

/**
 * Tests for class {@link GCEvent}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 04.02.2012</p>
 */
public class TestGcEvent {

    private GCEvent gcEvent;
    private GCEvent fullGcEvent;
    
    @Before
    public void setUp() {
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
    public void testAddGc() {
        // when GC was parsed, only "young" information really is present; "tenured" must be inferred
        assertEquals("number of details", 1, gcEvent.details.size());
        
        GCEvent defNewEvent = gcEvent.details().next();
        assertEquals("type", Type.DEF_NEW.getName(), defNewEvent.getExtendedType().getName());
        assertEquals("getYoung", defNewEvent, gcEvent.getYoung());
        
        GCEvent tenured = gcEvent.getTenured();
        assertNotNull("tenured", tenured);
    }

    @Test
    public void testAddFullGc() {
        // when Full GC was parsed, "young" information was deferred, other were parsed.
        assertEquals("number of details", 2, fullGcEvent.details.size());
        
        GCEvent tenured = fullGcEvent.details.get(0);
        assertEquals("type", Type.TENURED.getName(), tenured.getExtendedType().getName());
        assertEquals("getTenured", tenured, fullGcEvent.getTenured());
        
        GCEvent perm = fullGcEvent.details.get(1);
        assertEquals("type", Type.PERM.getName(), perm.getExtendedType().getName());
        assertEquals("getPerm", perm, fullGcEvent.getPerm());
        
        GCEvent young = fullGcEvent.getYoung();
        assertNotNull("young", young);
    }

    @Test
    public void testGetInferredYoungFullGcEvent() {
        GCEvent young = fullGcEvent.getYoung();
        assertEquals("type", ExtendedType.UNDEFINED, young.getExtendedType());
        assertEquals("preused", 141564 - 38156, young.getPreUsed());
        assertEquals("postused", 54636 - 54636, young.getPostUsed());
        assertEquals("total", 506944 - 349568, young.getTotal());
        assertEquals("pause", 0.6013150, young.getPause(), 0.00000001);
    }

    @Test
    public void testGetInferredTenuredGcEvent() {
        GCEvent tenured = gcEvent.getTenured();
        assertEquals("tenured type", ExtendedType.UNDEFINED, tenured.getExtendedType());
        assertEquals("preused", 194540 - 139904, tenured.getPreUsed());
        assertEquals("postused", 60292 - 5655, tenured.getPostUsed());
        assertEquals("total tenured", 506944 - 157376, tenured.getTotal());
        assertEquals("pause", 0.0543079, tenured.getPause(), 0.000001);
    }

}
