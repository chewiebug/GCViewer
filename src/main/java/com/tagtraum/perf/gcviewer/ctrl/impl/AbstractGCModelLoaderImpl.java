package com.tagtraum.perf.gcviewer.ctrl.impl;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoader;
import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.MonitoredBufferedInputStream;
import com.tagtraum.perf.gcviewer.model.GCModel;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for {@link GCModelLoader}s
 *
 * @author martin.geldmacher (refactored)
 */
public abstract class AbstractGCModelLoaderImpl extends SwingWorker<GCModel, Object> implements GCModelLoader {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == MonitoredBufferedInputStream.PROGRESS) {
            setProgress((int) evt.getNewValue());
        }
    }

    protected void done() {
        Logger logger = getGcResource().getLogger();

        try {
            getGcResource().setModel(get());
            // TODO delete
            getGcResource().getModel().printDetailedInformation();
        }
        catch (InterruptedException e) {
            logger.log(Level.FINE, "model get() interrupted", e);
        }
        catch (ExecutionException | RuntimeException e) {
            if (logger.isLoggable(Level.WARNING))
                logger.log(Level.WARNING, "Failed to create GCModel from " + getGcResource().getResourceName(), e);
        }
    }

    @Override
    protected GCModel doInBackground() throws Exception {
        setProgress(0);
        final GCModel result;
        try {
            result = loadGcModel();
        }
        catch (DataReaderException | RuntimeException e) {
            Logger logger = getGcResource().getLogger();
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Failed to load GCModel from " + getGcResource().getResourceName(), e);
            }
            throw e;
        }
        return result;
    }

    protected abstract GCModel loadGcModel() throws DataReaderException;
}
