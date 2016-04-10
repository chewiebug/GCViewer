package com.tagtraum.perf.gcviewer.ctrl;

import com.tagtraum.perf.gcviewer.model.GCResource;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Loads the model in a background thread (progress can be tracked using propertyChangeListeners).
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 16.08.2014</p>
 */
public interface GCModelLoader extends PropertyChangeListener {
    GCResource getGcResource();

    void addPropertyChangeListener(PropertyChangeListener listener);
    void removePropertyChangeListener(PropertyChangeListener listener);
    
    @Override
    void propertyChange(PropertyChangeEvent evt);

    void execute();
}
