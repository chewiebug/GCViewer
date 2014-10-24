package com.tagtraum.perf.gcviewer.view;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.VmOperationEvent;

/**
 * Test {@link ModelMetricsPanel}. The tests all just check that no Exception occurs. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 25.01.2012</p>
 */
public class ModelMetricsPanelTest {
    
    @Test
    public void testEmptyModel() {
        GCModel model = new GCModel();
        ModelMetricsPanel panel = new ModelMetricsPanel();
        panel.setModel(model);
    }
    
    @Test
    public void testOneElementModel() {
        GCEvent event = new GCEvent();
        event.setTimestamp(0.5);
        event.setType(Type.G1_YOUNG_INITIAL_MARK);
        event.setPause(0.245);
        event.setPreUsed(900);
        event.setPostUsed(400);
        event.setTotal(1024);
        
        GCModel model = new GCModel();
        model.add(event);
        
        ModelMetricsPanel panel = new ModelMetricsPanel();
        // first add empty model; is done when opened
        panel.setModel(new GCModel());
        // only later a model with entries is set
        panel.setModel(model);
    }

    @Test
    public void testVmOpModel() {
        GCEvent event = new GCEvent();
        event.setTimestamp(0.5);
        event.setType(Type.G1_YOUNG_INITIAL_MARK);
        event.setPause(0.245);
        event.setPreUsed(900);
        event.setPostUsed(400);
        event.setTotal(1024);
        
        GCModel model = new GCModel();
        model.add(event);
        
        VmOperationEvent vmOpEvent = new VmOperationEvent();
        vmOpEvent.setTimestamp(0.7);
        vmOpEvent.setType(Type.APPLICATION_STOPPED_TIME);
        vmOpEvent.setPause(0.26);
        model.add(vmOpEvent);
        
        ModelMetricsPanel panel = new ModelMetricsPanel();
        // first add empty model; is done when opened
        panel.setModel(new GCModel());
        // only later a model with entries is set
        panel.setModel(model);
    }
    
    @Test
    public void testTwoElementsModel() {
        GCModel model = new GCModel();

        GCEvent event = new GCEvent();
        event.setTimestamp(0.5);
        event.setType(Type.G1_YOUNG_INITIAL_MARK);
        event.setPause(0.245);
        event.setPreUsed(900);
        event.setPostUsed(400);
        event.setTotal(1024);
        
        model.add(event);

        event = new GCEvent();
        event.setTimestamp(0.75);
        event.setType(Type.G1_YOUNG_INITIAL_MARK);
        event.setPause(0.245);
        event.setPreUsed(800);
        event.setPostUsed(300);
        event.setTotal(1024);
        
        model.add(event);
        
        ModelMetricsPanel panel = new ModelMetricsPanel();
        panel.setModel(model);
    }

}
