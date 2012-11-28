package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the implementation of {@link DataReaderFacade}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 28.11.2012</p>
 */
public class TestDataReaderFacade {

    private static final Logger IMP_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.imp");
    private static final String PARENT_PATH = "src/test/resources/com/tagtraum/perf/gcviewer/imp";
    
    private DataReaderFacade dataReaderFacade;
    
    @Before
    public void setUp() throws Exception {
        dataReaderFacade = new DataReaderFacade();
    }
    
    /**
     * Tests {@link DataReaderFacade#loadModel(java.net.URL, boolean, java.awt.Component)}
     * with filename that does not exist.
     */
    @Test
    public void loadModelFileDoesntExists() throws Exception {
        try {
            dataReaderFacade.loadModel(new File("dummy.txt").toURI().toURL(), false, null);
            fail("DataReaderException expected");
        }
        catch (DataReaderException e) {
            assertNotNull("cause", e.getCause());
            assertEquals("expected exception in cause", 
                    FileNotFoundException.class.getName(), 
                    e.getCause().getClass().getName());
        }
    }

    /**
     * Tests {@link DataReaderFacade#loadModel(java.net.URL, boolean, java.awt.Component)}
     * with filename that does exist.
     */
    @Test
    public void loadModelFileExistsNoWarnings() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);

        dataReaderFacade.loadModel(new File(PARENT_PATH, "SampleSun1_6_0PrintHeapAtGC.txt").toURI().toURL(), false, null);
        
        assertEquals("has no errors", 0, handler.getCount());
    }

}
