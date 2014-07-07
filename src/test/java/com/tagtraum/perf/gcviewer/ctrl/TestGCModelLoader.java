package com.tagtraum.perf.gcviewer.ctrl;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;

/**
 * Unittest for {@link GCModelLoader}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 05.01.2014</p>
 */
public class TestGCModelLoader {
    
    @Test
    public void loadExistingFile() throws Exception {
        GCResource gcResource = new GCResource(
                UnittestHelper.getResourceAsString(UnittestHelper.FOLDER_OPENJDK, "SampleSun1_6_0CMS.txt"));
        GCModelLoader loader = new GCModelLoader(gcResource);
        loader.execute();
        GCModel model = loader.get();
        assertThat("model", model, notNullValue());
        assertThat("model.size", model.size(), not(0));
    }
}
