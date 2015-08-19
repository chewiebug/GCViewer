package com.tagtraum.perf.gcviewer.ctrl.impl;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoader;
import com.tagtraum.perf.gcviewer.ctrl.GCModelLoaderGroupTracker;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Helperclass to track several modelLoaders and get an event, when all loaders have finished 
 * loading. GCModelLoaders should be added to the tracker and then be started via
 * {@link #execute()}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 02.02.2014</p>
 */
public class GCModelLoaderGroupTrackerImpl implements GCModelLoaderGroupTracker {
    private PropertyChangeSupport propertyChangeSupport;
    private List<GCModelLoader> loaderList = new ArrayList<GCModelLoader>();
    private int finishedCount;
    
    public GCModelLoaderGroupTrackerImpl() {
        super();
        
        propertyChangeSupport = new PropertyChangeSupport(this);
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    @Override
    public void addGcModelLoader(GCModelLoader loader) {
        loader.addPropertyChangeListener(this);
        loaderList.add(loader);
    }
    
    @Override
    public void execute() {
        if (loaderList.isEmpty()) {
            fireStateDone();
        }
        else {
            loaderList.forEach((GCModelLoader loader) -> {
                    loader.execute();
            });
        }
    }

    private void fireStateDone() {
        propertyChangeSupport.firePropertyChange("state",
                SwingWorker.StateValue.STARTED,
                SwingWorker.StateValue.DONE);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("state".equals(evt.getPropertyName()) 
            && SwingWorker.StateValue.DONE == evt.getNewValue()) {
            
            ++finishedCount;
            if (finishedCount == loaderList.size()) {
                fireStateDone();
            }
        }

    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    @Override
    public int size() {
        return loaderList.size();
    }

}
