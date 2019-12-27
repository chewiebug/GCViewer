package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the implementation of {@link DataReaderFacade}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 28.11.2012</p>
 */
class TestDataReaderFacade {

    private static final String SAMPLE_GCLOG_SUN1_6_0 = "SampleSun1_6_0PrintHeapAtGC.txt";
    
    private static final String PARENT_PATH = "src/test/resources/" + FOLDER.OPENJDK.getFolderName() + "/";

    private DataReaderFacade dataReaderFacade;
    
    @BeforeEach
    void setUp() {
        dataReaderFacade = new DataReaderFacade();
    }
    
    /**
     * Tests {@link DataReaderFacade#loadModel(GCResource)}
     * with filename that does exist.
     */
    @Test
    void loadModelStringFileExistsNoWarnings() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile(PARENT_PATH + SAMPLE_GCLOG_SUN1_6_0);
        gcResource.getLogger().addHandler(handler);

        final GCModel model = dataReaderFacade.loadModel(gcResource);

        assertEquals(0, handler.getCount(), "has no errors");
        assertNotNull(model, "Model returned");
        assertNotNull(model.getURL(), "Model returned contains URL");
    }

    /**
     * Tests {@link DataReaderFacade#loadModel(GCResource)}
     * with a malformed url.
     */
    @Test
    void loadModelMalformedUrl() {
        DataReaderException e = Assertions.assertThrows(DataReaderException.class,
                () -> dataReaderFacade.loadModel(new GcResourceFile("httpblabla")));
        assertAll(() -> {
            assertNotNull(e.getCause(), "cause");
            assertEquals(MalformedURLException.class.getName(),
                    e.getCause().getClass().getName(),
                    "expected exception in cause");
        });
    }

    /**
     * Tests {@link DataReaderFacade#loadModel(GCResource)}
     * with a malformed url.
     */
    @Test
    void loadModelIllegalArgument() {
        DataReaderException e = Assertions.assertThrows(DataReaderException.class,
                () -> dataReaderFacade.loadModel(new GcResourceFile("http://")));
        assertAll(() -> {
            assertNotNull(e.getCause(), "cause");
            assertEquals(IllegalArgumentException.class.getName(),
                    e.getCause().getClass().getName(),
                    "expected exception in cause");
        });
    }

    /**
     * Tests {@link DataReaderFacade#loadModel(GCResource)}
     * with filename that does not exist.
     */
    @Test
    void loadModelFileDoesntExists() {
        DataReaderException e = Assertions.assertThrows(DataReaderException.class,
                () -> dataReaderFacade.loadModel(new GcResourceFile("dummy.txt")));
        assertAll(() -> {
            assertNotNull(e.getCause(), "cause");
            assertEquals(FileNotFoundException.class.getName(),
                    e.getCause().getClass().getName(),
                    "expected exception in cause");
        });
    }

    @Test
    void testLoadModel_forSeries() throws IOException, DataReaderException {
        GCResource file1 = new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part1.txt").getPath());
        GCResource file2 = new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part2.txt").getPath());
        GCResource file3 = new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part3.txt").getPath());
        GCResource file4 = new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part4.txt").getPath());
        GCResource file5 = new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part5.txt").getPath());
        GCResource file6 = new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part6.txt").getPath());
        GCResource file7 = new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part7.txt").getPath());
        GCResource expectedResult = new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-ManuallyMerged.txt").getPath());
        GCModel expectedModel = dataReaderFacade.loadModel(expectedResult);

        List<GCResource> resources = new ArrayList<>();
        resources.add(file4);
        resources.add(file3);
        resources.add(file6);
        resources.add(file1);
        resources.add(file7);
        resources.add(file2);
        resources.add(file5);
        GcResourceSeries series = new GcResourceSeries(resources);

        GCModel result = dataReaderFacade.loadModelFromSeries(series);
        assertThat(result.toString(), is(expectedModel.toString()));
    }

    @Test
    void testLoadModelFromSeries() throws IOException, DataReaderException {
        GCResource file1 = new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part1.txt").getPath());
        GCResource file2 = new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part2.txt").getPath());
        GCResource file3 = new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part3.txt").getPath());
        GCResource file4 = new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part4.txt").getPath());
        GCResource file5 = new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part5.txt").getPath());
        GCResource file6 = new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part6.txt").getPath());
        GCResource file7 = new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part7.txt").getPath());
        GCResource expectedResult = new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-ManuallyMerged.txt").getPath());
        GCModel expectedModel = dataReaderFacade.loadModel(expectedResult);

        List<GCResource> resources = new ArrayList<>();
        resources.add(file4);
        resources.add(file3);
        resources.add(file6);
        resources.add(file1);
        resources.add(file7);
        resources.add(file2);
        resources.add(file5);
        GcResourceSeries series = new GcResourceSeries(resources);

        GCModel result = dataReaderFacade.loadModel(series);
        assertThat(result.toString(), is(expectedModel.toString()));
    }
}
