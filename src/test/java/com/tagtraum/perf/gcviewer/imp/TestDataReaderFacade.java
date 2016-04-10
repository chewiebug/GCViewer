package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.logging.Level;

import org.junit.Before;
import org.junit.Test;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;

/**
 * Tests the implementation of {@link DataReaderFacade}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 28.11.2012</p>
 */
public class TestDataReaderFacade {

    private static final String SAMPLE_GCLOG_SUN1_6_0 = "SampleSun1_6_0PrintHeapAtGC.txt";
    
    private static final String PARENT_PATH = "src/test/resources/" + UnittestHelper.FOLDER_OPENJDK + "/";

    private DataReaderFacade dataReaderFacade;
    
    @Before
    public void setUp() throws Exception {
        dataReaderFacade = new DataReaderFacade();
    }
    
    /**
     * Tests {@link DataReaderFacade#loadModel(GCResource)}
     * with filename that does exist.
     */
    @Test
    public void loadModelStringFileExistsNoWarnings() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource(PARENT_PATH + SAMPLE_GCLOG_SUN1_6_0);
        gcResource.getLogger().addHandler(handler);

        final GCModel model = dataReaderFacade.loadModel(gcResource);
        
        assertEquals("has no errors", 0, handler.getCount());        
        assertNotNull("Model returned", model);        
        assertNotNull("Model returned contains URL", model.getURL());
    }

    /**
     * Tests {@link DataReaderFacade#loadModel(GCResource)}
     * with a malformed url.
     */
    @Test
    public void loadModelMalformedUrl() throws Exception {

        try {
            dataReaderFacade.loadModel(new GCResource("httpblabla"));
        }
        catch (DataReaderException e) {
            assertNotNull("cause", e.getCause());
            assertEquals("expected exception in cause",
                    MalformedURLException.class.getName(),
                    e.getCause().getClass().getName());
        }
    }

    /**
     * Tests {@link DataReaderFacade#loadModel(GCResource)}
     * with a malformed url.
     */
    @Test
    public void loadModelIllegalArgument() throws Exception {

        try {
            dataReaderFacade.loadModel(new GCResource("http://"));
        }
        catch (DataReaderException e) {
            assertNotNull("cause", e.getCause());
            assertEquals("expected exception in cause",
                    IllegalArgumentException.class.getName(),
                    e.getCause().getClass().getName());
        }
    }

    /**
     * Tests {@link DataReaderFacade#loadModel(GCResource)}
     * with filename that does not exist.
     */
    @Test
    public void loadModelFileDoesntExists() throws Exception {
        try {
            dataReaderFacade.loadModel(new GCResource("dummy.txt"));
            fail("DataReaderException expected");
        }
        catch (DataReaderException e) {
            assertNotNull("cause", e.getCause());
            assertEquals("expected exception in cause", 
                    FileNotFoundException.class.getName(), 
                    e.getCause().getClass().getName());
        }
    }

}
