package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
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

    private static final String SAMPLE_GCLOG_SUN1_6_0 = "SampleSun1_6_0PrintHeapAtGC.txt";
    
    private static final Logger IMP_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.imp");
    private static final String PARENT_PATH = "src/test/resources/" + UnittestHelper.FOLDER_OPENJDK + "/";

    private DataReaderFacade dataReaderFacade;
    
    @Before
    public void setUp() throws Exception {
        dataReaderFacade = new DataReaderFacade();
    }
    
    /**
     * Tests {@link DataReaderFacade#loadModel(String, boolean, java.awt.Component)}
     * with filename that does exist.
     */
    @Test
    public void loadModelStringFileExistsNoWarnings() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);

        dataReaderFacade.loadModel(PARENT_PATH + SAMPLE_GCLOG_SUN1_6_0, false, null);
        
        assertEquals("has no errors", 0, handler.getCount());
    }

    /**
     * Tests {@link DataReaderFacade#loadModel(String, boolean, java.awt.Component)}
     * with a malformed url.
     */
    @Test
    public void loadModelStringMalformedUrl() throws Exception {

        try {
            dataReaderFacade.loadModel("httpblabla", false, null);
        }
        catch (DataReaderException e) {
            assertNotNull("cause", e.getCause());
            assertEquals("expected exception in cause",
                    MalformedURLException.class.getName(),
                    e.getCause().getClass().getName());
        }
    }

    /**
     * Tests {@link DataReaderFacade#loadModel(String, boolean, java.awt.Component)}
     * with a malformed url.
     */
    @Test
    public void loadModelStringIllegalArgument() throws Exception {

        try {
            dataReaderFacade.loadModel("http://", false, null);
        }
        catch (DataReaderException e) {
            assertNotNull("cause", e.getCause());
            assertEquals("expected exception in cause",
                    IllegalArgumentException.class.getName(),
                    e.getCause().getClass().getName());
        }
    }

    /**
     * Tests {@link DataReaderFacade#loadModel(java.net.URL, boolean, java.awt.Component)}
     * with filename that does not exist.
     */
    @Test
    public void loadModelUrlFileDoesntExists() throws Exception {
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
    public void loadModelUrlFileExistsNoWarnings() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);

        dataReaderFacade.loadModel(new File(PARENT_PATH, SAMPLE_GCLOG_SUN1_6_0).toURI().toURL(), false, null);
        
        assertEquals("has no errors", 0, handler.getCount());
    }

}
