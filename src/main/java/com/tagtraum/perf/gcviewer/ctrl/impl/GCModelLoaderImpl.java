package com.tagtraum.perf.gcviewer.ctrl.impl;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoader;
import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
import com.tagtraum.perf.gcviewer.imp.MonitoredBufferedInputStream;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads the model in a background thread (progress can be tracked by propertyChangeListeners).
 *
 * @author Hans Bausewein
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>Date: November 8, 2013</p>
 */
public class GCModelLoaderImpl extends SwingWorker<GCModel, Object> implements GCModelLoader {
	
	private final DataReaderFacade dataReaderFacade;
    private final GCResource gcResource;
	
    public GCModelLoaderImpl(final GCResource gcResource) {
		super();

		this.gcResource = gcResource;
		this.dataReaderFacade = new DataReaderFacade();
	    this.dataReaderFacade.addPropertyChangeListener(this); // receive progress updates from loading
	}

	@Override
	protected GCModel doInBackground() throws Exception {
		setProgress(0);
		final GCModel result;
		try {
			result = dataReaderFacade.loadModel(gcResource);			
		}
		catch (DataReaderException | RuntimeException e) {
			Logger logger = gcResource.getLogger();
			if (logger.isLoggable(Level.FINE)) {
			    logger.log(Level.FINE, "Failed to load GCModel from " + gcResource.getResourceName(), e);
			}
			throw e;
		}
		return result;
	}

	protected void done() {
		Logger logger = gcResource.getLogger();

		try {
			gcResource.setModel(get());
            // TODO delete
            gcResource.getModel().printDetailedInformation();
		} 
		catch (InterruptedException e) {
			logger.log(Level.FINE, "model get() interrupted", e);
		} 
		catch (ExecutionException | RuntimeException e) {
			if (logger.isLoggable(Level.WARNING))
				logger.log(Level.WARNING, "Failed to create GCModel from " + gcResource.getResourceName(), e);			
		}
	}
	
	public GCResource getGcResource() {
		return gcResource;
	}

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == MonitoredBufferedInputStream.PROGRESS) {
            setProgress((int)evt.getNewValue());
        }
    }    	
}