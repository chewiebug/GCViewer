package com.tagtraum.perf.gcviewer.ctrl.impl;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoader;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;

/**
 * @author martin.geldmacher
 */
public class GCModelLoaderFactory {
    /**
     * Creates an appropriate {@link GCModelLoader} for the given {@link GCResource}
     *
     * @param gcResource the {@link GCResource}
     * @return an appropriate {@link GCModelLoader}
     */
    public static GCModelLoader createFor(GCResource gcResource) {
        if (gcResource instanceof GcResourceFile) {
            return new GCModelLoaderImpl(gcResource);
        }
        else if (gcResource instanceof GcResourceSeries) {
            return new GCModelSeriesLoaderImpl((GcResourceSeries) gcResource);
        }
        else
            throw new IllegalArgumentException("Unknown GcResource: " + gcResource);
    }
}
