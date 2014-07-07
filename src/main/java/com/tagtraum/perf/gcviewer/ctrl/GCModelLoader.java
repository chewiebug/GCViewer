package com.tagtraum.perf.gcviewer.ctrl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
import com.tagtraum.perf.gcviewer.imp.MonitoredBufferedInputStream;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.util.LoggerHelper;

/**
 * Loads the model in a background thread (progress can be tracked by propertyChangeListeners).
 *
 * @author Hans Bausewein
 * <p>Date: November 8, 2013</p>
 */
public class GCModelLoader extends SwingWorker<GCModel, Object> implements PropertyChangeListener {
	
	private final DataReaderFacade dataReaderFacade;
    private final GCResource gcResource;
	
    public GCModelLoader(final GCResource gcResource) {
		super();

		this.gcResource = gcResource;
		this.dataReaderFacade = new DataReaderFacade();
	    this.dataReaderFacade.addPropertyChangeListener(this); // receive progress updates from loading
	}

	@Override
	protected GCModel doInBackground() throws Exception {
	    firePropertyChange("loggername", "", Thread.currentThread().getName());
		setProgress(0);
		final GCModel result;
		try {
			result = dataReaderFacade.loadModel(gcResource.getResourceName());			
		}
		catch (DataReaderException | RuntimeException e) {
			final Logger logger = LoggerHelper.getThreadSpecificLogger(this);
			if (logger.isLoggable(Level.FINE)) {
			    logger.log(Level.FINE, "Failed to load GCModel from " + gcResource.getResourceName(), e);
			}
			throw e;
		}
		return result;
	}

	protected void done() {
        // remove special handler after we are done with reading.
		final Logger logger = LoggerHelper.getThreadSpecificLogger(this);

		try {
			gcResource.setModel(get());
            // TODO delete
            gcResource.getModel().printDetailedInformation();
		} 
		catch (InterruptedException e) {
			logger.log(Level.FINE, "model get() interrupted", e);
		} 
		catch (ExecutionException | RuntimeException e) {
			if (logger.isLoggable(Level.FINE))
				logger.log(Level.WARNING, "Failed to create GCModel from " + gcResource.getResourceName(), e);			
		}
		
        // TODO SWINGWORKER
//		if (modelLoaderView != null) {
//			logger.removeHandler(modelLoaderView.getTextAreaLogHandler());
//		}
		//gcDocument.gcModelLoaderDone(this, model);
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