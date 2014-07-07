package com.tagtraum.perf.gcviewer.ctrl;

import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
import com.tagtraum.perf.gcviewer.imp.MonitoredBufferedInputStream;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.view.GCDocument;
import com.tagtraum.perf.gcviewer.view.GCModelLoaderView;

/**
 * Loads the model in a background thread.
 *
 * @author Hans Bausewein
 * <p>Date: November 8, 2013</p>
 */
public class GCModelLoader extends SwingWorker<GCModel, Object> implements MonitoredBufferedInputStream.ProgressCallback {
	
	private final DataReaderFacade dataReaderFacade;
	private final GCDocument gcDocument;
    private final GCResource gcResource;
    private final GCModelLoaderView modelLoaderView;// optional
	
    public GCModelLoader(final GCDocument gcDocument, 
    					 final GCResource gcResource,
    					 final GCModelLoaderView mlView) {
		super();

		this.gcDocument = gcDocument;
		this.gcResource = gcResource;
		this.modelLoaderView = mlView == null ? new GCModelLoaderView(this) : mlView;
		this.dataReaderFacade = new DataReaderFacade();
		
		if (this.modelLoaderView != null) {
			addPropertyChangeListener(this.modelLoaderView);
			gcResource.getLogger().addHandler(this.modelLoaderView.getTextAreaLogHandler());
		}
	}

    public GCModelLoader(final GCDocument gcDocument, 
			 			 final GCResource gcResource) {
    	this(gcDocument, gcResource, null);
    }
    
    public GCModelLoader(final GCModelLoader prevModel, final URL url) {
		this(prevModel.getGcDocument(), new GCResource(url));
    }
    
    public GCModelLoader(final String documentTitle, final URL url) {
        this(null, new GCResource(url), null);
    }
    
    public String getLoggerName() {
        return gcResource.getLogger().getName();
    }

	@Override
	protected GCModel doInBackground() throws Exception {
		setProgress(0);
		final GCModel result;
		try {
			result = dataReaderFacade.loadModel(gcResource.getUrl(), this);			
		} catch (DataReaderException | RuntimeException e) {
			final Logger logger = gcResource.getLogger();
			if (logger.isLoggable(Level.FINE))
				gcResource.getLogger().log(Level.FINE, "Failed to load GCModel from " + gcResource.getUrl().toExternalForm(), e);
			throw e;
		}
		return result;
	}

	protected void done() {
		GCModel model = null;
        // remove special handler after we are done with reading.
		final Logger logger = gcResource.getLogger();

		try {
			model = get();
		} catch (InterruptedException e) {
			logger.log(Level.FINE, "model get() interrupted", e);
		} catch (ExecutionException | RuntimeException e) {
			if (logger.isLoggable(Level.FINE))
				logger.log(Level.WARNING, "Failed to create GCModel from " + gcResource.getUrl().toExternalForm(), e);			
		}
		setProgress(100);
		
		if (modelLoaderView != null) {
			logger.removeHandler(modelLoaderView.getTextAreaLogHandler());
		}
		if (model != null) {			
			// TODO delete
			model.printDetailedInformation();
		}
		gcDocument.gcModelLoaderDone(this, model);
	}
	
	public GCDocument getGcDocument() {
		return gcDocument;
	}

	@Override
	public void publishP(Integer... chunks) {			
		if (gcResource.getLogger().isLoggable(Level.FINE))
			gcResource.getLogger().fine("Received " + Arrays.asList(chunks));
	}

	@Override
	public void updateProgress(int progress) {
		setProgress(progress);			
	}

	public GCModelLoaderView getModelLoaderView() {
		return modelLoaderView;
	}

	public GCResource getGcResource() {
		return gcResource;
	}    	
}