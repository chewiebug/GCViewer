package com.tagtraum.perf.gcviewer.ctrl.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.jupiter.api.Test;

/**
 * Unittest for {@link GCModelLoaderImpl}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 05.01.2014</p>
 */
class TestGCModelLoader {
    
    @Test
    void loadExistingFile() throws Exception {
        GCResource gcResource = new GcResourceFile(
                UnittestHelper.getResourceAsString(FOLDER.OPENJDK, "SampleSun1_6_0CMS.txt"));
        GCModelLoaderImpl loader = new GCModelLoaderImpl(gcResource);
        loader.execute();
        GCModel model = loader.get();
        assertThat("model", model, notNullValue());
        assertThat("model.size", model.size(), not(0));
    }
}
