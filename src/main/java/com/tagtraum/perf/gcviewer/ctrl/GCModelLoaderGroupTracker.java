package com.tagtraum.perf.gcviewer.ctrl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

  /**
   * Helperclass to track several modelLoaders and get an event, when all loaders have finished
   * loading. {@link GCModelLoader}s should be added to the tracker and then be started via
   * {@link #execute()}.
   *
   * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
   * <p>created on: 16.08.2014</p>
  */
public interface GCModelLoaderGroupTracker extends PropertyChangeListener {
    /**
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Add a <code>GCModelLoader</code> to be tracked by this tracker.
     *
     * @param loader <code>GCModelLoader</code> to be tracked by this tracker.
     */
    void addGcModelLoader(GCModelLoader loader);

    /**
     * Start execution of all loaders tracked by this tracker.
     */
    void execute();

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    void propertyChange(PropertyChangeEvent evt);

    /**
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Returns count of GCModelLoader instances in this tracker.
     * @return count of GCModelLoader instances
     */
    int size();
}
