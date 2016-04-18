package com.tagtraum.perf.gcviewer.model;

import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

/**
 * Interface for GC resources
 *
 * @author Hans Bausewein
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 *         <p>Date: November 8, 2013</p>
 */
public interface GCResource {
    void addPropertyChangeListener(PropertyChangeListener listener);

    Logger getLogger();

    void setLogger(Logger logger);

    GCModel getModel();

    String getResourceName();

    /**
     * Returns true, if the underlying resource has changed.
     *
     * @return true, if the underlying resource has changed.
     */
    boolean hasUnderlyingResourceChanged();

    /**
     * If this resource is being reloaded, this will return <code>true</code>.
     *
     * @return <code>true</code> if this resource is being reloaded.
     */
    boolean isReload();

    /**
     * Returns <code>true</code>, if reading of this GCResource should be cancelled.
     *
     * @return <code>true</code>, if reading should be cancelled
     */
    boolean isReadCancelled();

    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * if this resource is being reloaded, set this property to <code>true</code>.
     *
     * @param isReload <code>true</code>, if this resource is being reloaded
     */
    void setIsReload(boolean isReload);

    /**
     * Indicate, that reading of this GCResource should be cancelled.
     *
     * @param isReadCancelled <code>true</code>, if read should be cancelled
     */
    void setIsReadCancelled(boolean isReadCancelled);

    void setModel(GCModel model);

    /**
     * reset internal boolean state to default (e.g. readCancelled)
     */
    void reset();
}
