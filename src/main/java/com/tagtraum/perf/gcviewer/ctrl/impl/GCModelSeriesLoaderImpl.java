package com.tagtraum.perf.gcviewer.ctrl.impl;

import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;

/**
 * An {@link AbstractGCModelLoaderImpl} that loads {@link GCResource}s as a series of logs
 *
 * @author gelder.
 */
public class GCModelSeriesLoaderImpl extends AbstractGCModelLoaderImpl {
    private final DataReaderFacade dataReaderFacade;
    private final GcResourceSeries gcResourceSeries;

    public GCModelSeriesLoaderImpl(GcResourceSeries gcResourceSeries) {
        this.dataReaderFacade = new DataReaderFacade();
        this.dataReaderFacade.addPropertyChangeListener(this); // receive progress updates from loading
        this.gcResourceSeries = gcResourceSeries;
    }

    @Override
    public GCResource getGcResource() {
        return gcResourceSeries;
    }

    @Override
    protected GCModel loadGcModel() throws DataReaderException {
        return dataReaderFacade.loadModel(gcResourceSeries);
    }
}
