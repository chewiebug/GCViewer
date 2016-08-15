package com.tagtraum.perf.gcviewer.model;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

/**
 * Base class for {@link GCResource} implementations
 *
 * @author martin.geldmacher (refactored)
 */
public abstract class AbstractGcResource implements GCResource {
    private String resourceName;
    private GCModel model;
    private SwingPropertyChangeSupport propertyChangeSupport;
    private Logger logger;
    private boolean isReload;
    private boolean isReadCancelled;

    public AbstractGcResource(String resourceName, Logger logger) {
        super();
        this.resourceName = resourceName;
        this.logger = logger;
        this.model = new GCModel();
        this.propertyChangeSupport = new SwingPropertyChangeSupport(this);
    }

    @Override
    public String getResourceName() {
        return resourceName;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public GCModel getModel() {
        return model;
    }

    @Override
    public boolean isReload() {
        return isReload;
    }

    @Override
    public boolean isReadCancelled() {
        return isReadCancelled;
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    @Override
    public void setIsReload(boolean isReload) {
        this.isReload = isReload;
        setIsReadCancelled(false);
    }

    @Override
    public void setIsReadCancelled(boolean isReadCancelled) {
        // TODO i18n
        this.getLogger().info("--> cancel requested");
        this.isReadCancelled = isReadCancelled;
    }

    @Override
    public void setModel(GCModel model) {
        GCModel oldModel = this.model;
        this.model = model;
        propertyChangeSupport.firePropertyChange(GcResourceFile.PROPERTY_MODEL, oldModel, model);
    }

    @Override
    public void reset() {
        this.isReadCancelled = false;
        this.isReload = false;
    }
}
