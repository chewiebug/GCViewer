package com.tagtraum.perf.gcviewer.model;

import java.beans.PropertyChangeListener;
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
	    return resourceName.equals(obj);
	}
	
	public GCModel getModel() {
	    return model;
	}
	
	public String getResourceName() {
		return resourceName;
	}
	
	public Logger getLogger() {
	    return logger;
	}
	
	@Override
	public int hashCode() {		
		return resourceName.hashCode();
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
	    this.propertyChangeSupport.removePropertyChangeListener(listener);
	}
	
	public void setModel(GCModel model) {
	    GCModel oldModel = this.model;
	    this.model = model;
	    propertyChangeSupport.firePropertyChange(PROPERTY_MODEL, oldModel, model);
	}
    
}
