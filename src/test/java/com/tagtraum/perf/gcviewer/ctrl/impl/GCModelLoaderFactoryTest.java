package com.tagtraum.perf.gcviewer.ctrl.impl;

import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author martin.geldmacher
 */
@RunWith(MockitoJUnitRunner.class)
public class GCModelLoaderFactoryTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void createFor_GcResourceFile() throws Exception {
        GCResource gcResource = mock(GcResourceFile.class);
        assertThat(GCModelLoaderFactory.createFor(gcResource), instanceOf(GCModelLoaderImpl.class));
    }

    @Test
    public void createFor_GcResourceSeries() throws Exception {
        GCResource gcResource = mock(GcResourceSeries.class);
        assertThat(GCModelLoaderFactory.createFor(gcResource), instanceOf(GCModelSeriesLoaderImpl.class));
    }

    @Test
    public void createFor_GcResourceUnknown() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        GCResource gcResource = mock(GCResource.class);
        GCModelLoaderFactory.createFor(gcResource);
    }
}
