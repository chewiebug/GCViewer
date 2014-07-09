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

	private final String resourceName;
	private GCModel model;
	private SwingPropertyChangeSupport propertyChangeSupport;
	private Logger logger;
    private boolean isReload; 
	
	public GCResource(String resourceName) {
		super();
		
		if (resourceName == null) {
			throw new IllegalArgumentException("resourceName cannot be null");
		}
		
		this.resourceName = resourceName;
		this.propertyChangeSupport = new SwingPropertyChangeSupport(this);
		this.model = new GCModel(false);

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
        else if (!resourceName.equals(other.resourceName)) {
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
    
	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((resourceName == null) ? 0 : resourceName.hashCode());
        return result;
    }

	/**
	 * If this resource is being reloaded, this will return <code>true</code>.
	 * @return <code>true</code> if this resource is being reloaded.
	 */
	public boolean isReload() {
	    return isReload;
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
	}
	
	public void setModel(GCModel model) {
	    GCModel oldModel = this.model;
	    this.model = model;
	    propertyChangeSupport.firePropertyChange(PROPERTY_MODEL, oldModel, model);
	}

    @Override
    public String toString() {
        return "GCResource [resourceName=" + resourceName + ", isReload=" + isReload
                + ", logger=" + logger + ", model=" + model + "]";
    }

}
