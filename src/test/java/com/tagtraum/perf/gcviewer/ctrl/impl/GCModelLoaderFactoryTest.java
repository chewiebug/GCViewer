package com.tagtraum.perf.gcviewer.ctrl.impl;

import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * @author martin.geldmacher
 */
class GCModelLoaderFactoryTest {

    @Test
    void createFor_GcResourceFile() {
        GCResource gcResource = mock(GcResourceFile.class);
        assertThat(GCModelLoaderFactory.createFor(gcResource), instanceOf(GCModelLoaderImpl.class));
    }

    @Test
    void createFor_GcResourceSeries() {
        GCResource gcResource = mock(GcResourceSeries.class);
        assertThat(GCModelLoaderFactory.createFor(gcResource), instanceOf(GCModelSeriesLoaderImpl.class));
    }

    @Test
    void createFor_GcResourceUnknown() {
        GCResource gcResource = mock(GCResource.class);
        assertThrows(IllegalArgumentException.class, () -> GCModelLoaderFactory.createFor(gcResource));
    }
}
