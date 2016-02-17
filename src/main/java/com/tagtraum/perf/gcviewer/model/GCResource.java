package com.tagtraum.perf.gcviewer.model;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.swing.event.SwingPropertyChangeSupport;

/**
 * Identifies a GC resource: a file or URL resource containing GC info.
 *
 * @author Hans Bausewein
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>Date: November 8, 2013</p>
 */
public class GCResource {
    public static final String PROPERTY_MODEL = "model";
    private static final AtomicInteger COUNT = new AtomicInteger(0);

	private String resourceName;
	private GCModel model;
	private SwingPropertyChangeSupport propertyChangeSupport;
	private Logger logger;
    private boolean isReload;
    private boolean isReadCancelled;

	public GCResource(String resourceName) {
		super();

		if (resourceName == null) {
			throw new IllegalArgumentException("resourceName cannot be null");
		}

		this.resourceName = resourceName;
		this.propertyChangeSupport = new SwingPropertyChangeSupport(this);
		this.model = new GCModel();

	    logger = Logger.getLogger("GCResource".concat(Integer.toString(COUNT.incrementAndGet())));
    }

	public void addPropertyChangeListener(PropertyChangeListener listener) {
	    this.propertyChangeSupport.addPropertyChangeListener(listener);
	}


	@Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GCResource other = (GCResource) obj;
        if (resourceName == null) {
            if (other.resourceName != null) {
                return false;
            }
        }
        else if (!getResourceNameAsUrlString().equals(other.getResourceNameAsUrlString())) {
            return false;
        }
        return true;
    }

    public Logger getLogger() {
        return logger;
    }

    public GCModel getModel() {
        return model;
    }

    public String getResourceName() {
		return resourceName;
	}

    public URL getResourceNameAsUrl() throws MalformedURLException {
        URL url = null;
        if (getResourceName().startsWith("http") || getResourceName().startsWith("file")) {
            url = new URL(getResourceName());
        }
        else {
            url = new File(getResourceName()).toURI().toURL();
        }

        return url;
    }

    /**
     * Same as {@link #getResourceNameAsUrl()}, but still returns a string, if MalFormedURLException occurred.
     * @return same as getResourceNameAsUrl(), but without Exception
     */
    public String getResourceNameAsUrlString() {
        try {
            return getResourceNameAsUrl().toString();
        }
        catch (MalformedURLException e) {
            return "malformed url: " + resourceName;
        }
    }

	@Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((resourceName == null) ? 0 : getResourceNameAsUrlString().hashCode());

        return result;
    }

    /**
     * Returns true, if the underlying resource has changed.
     * @return true, if the underlying resource has changed.
     */
    public boolean hasUnderlyingResourceChanged() {
        if (this.model.getURL() == null) {
            return true;
        }

        return this.model.isDifferent(this.model.getURL());
    }

	/**
	 * If this resource is being reloaded, this will return <code>true</code>.
	 * @return <code>true</code> if this resource is being reloaded.
	 */
	public boolean isReload() {
	    return isReload;
	}

    /**
     * Returns <code>true</code>, if reading of this GCResource should be cancelled.
     * @return <code>true</code>, if reading should be cancelled
     */
    public boolean isReadCancelled() {
        return isReadCancelled;
    }

	public void removePropertyChangeListener(PropertyChangeListener listener) {
	    this.propertyChangeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * if this resource is being reloaded, set this property to <code>true</code>.
	 * @param isReload <code>true</code>, if this resource is being reloaded
	 */
	public void setIsReload(boolean isReload) {
	    this.isReload = isReload;
        setIsReadCancelled(false);
	}

    /**
     * Indicate, that reading of this GCResource should be cancelled.
     * @param isReadCancelled <code>true</code>, if read should be cancelled
     */
    public void setIsReadCancelled(boolean isReadCancelled) {
        // TODO i18n
        this.getLogger().info("--> cancel requested");
        this.isReadCancelled = isReadCancelled;
	}

	public void setModel(GCModel model) {
	    GCModel oldModel = this.model;
	    this.model = model;
	    propertyChangeSupport.firePropertyChange(PROPERTY_MODEL, oldModel, model);
	}

    /**
     * reset internal boolean state to default (e.g. readCancelled)
     */
    public void reset() {
        this.isReadCancelled = false;
        this.isReload = false;
    }

    @Override
    public String toString() {
        return "GCResource [resourceNameAsUrlString=" + getResourceNameAsUrlString() + ", isReload=" + isReload
                + ", logger=" + logger + ", model=" + model + "]";
    }

}
