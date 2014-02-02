package com.tagtraum.perf.gcviewer.ctrl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

/**
 * Helperclass to track several modelLoaders and get an event, when all loaders have finished 
 * loading. GCModelLoaders should be added to the tracker and then be started via
 * {@link #execute()}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 02.02.2014</p>
 */
public class GCModelLoaderGroupTracker implements PropertyChangeListener {
    private PropertyChangeSupport propertyChangeSupport;
    private List<GCModelLoader> loaderList = new ArrayList<GCModelLoader>();
    private int finishedCount;
    
    public GCModelLoaderGroupTracker() {
        super();
        
        propertyChangeSupport = new PropertyChangeSupport(this);
    }
    
    /**
     * @see PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    /**
     * Add a <code>GCModelLoader</code> to be tracked by this tracker.
     * 
     * @param loader <code>GCModelLoader</code> to be tracked by this tracker.
     */
    public void addGcModelLoader(GCModelLoader loader) {
        loader.addPropertyChangeListener(this);
        loaderList.add(loader);
    }
    
    /**
     * Start execution of all loaders tracked by this tracker.
     */
    public void execute() {
        for (GCModelLoader loader : loaderList) {
            loader.execute();
        }
    }
    
    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("state".equals(evt.getPropertyName()) 
            && SwingWorker.StateValue.DONE == evt.getNewValue()) {
            
            ++finishedCount;
            if (finishedCount == loaderList.size()) {
                propertyChangeSupport.firePropertyChange("state", 
                        SwingWorker.StateValue.STARTED, 
                        SwingWorker.StateValue.DONE);
            }
        }

    }
    
    /**
     * @see PropertyChangeSupport#removePropertyChangeListener(PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

}
