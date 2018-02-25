package com.tagtraum.perf.gcviewer.ctrl.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GcSeriesLoaderTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private GcSeriesLoader loader;
    private DataReaderFacade dataReader;

    @Before
    public void setUp() throws Exception {
        dataReader = new DataReaderFacade();
        loader = new GcSeriesLoader(dataReader);
    }

    @Test
    public void merge_EmptyFile() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        loader.load(new GcResourceSeries(new ArrayList<>()));
    }

    @Test
    public void merge_OneFile() throws Exception {
        GCResource input = getGcResource("SampleSun1_8_0Series-Part1.txt");
        GCModel expectedModel = createModel(input);

        List<GCResource> resources = new ArrayList<>();
        resources.add(input);
        GcResourceSeries series = new GcResourceSeries(resources);
        GCModel result = loader.load(series);

        assertThat(result, is(expectedModel));
    }

    @Test
    public void merge_FilesInRightOrder() throws Exception {
        GCResource file1 = getGcResource("SampleSun1_8_0Series-Part1.txt");
        GCResource file2 = getGcResource("SampleSun1_8_0Series-Part2.txt");
        GCResource file3 = getGcResource("SampleSun1_8_0Series-Part3.txt");
        GCResource file4 = getGcResource("SampleSun1_8_0Series-Part4.txt");
        GCResource file5 = getGcResource("SampleSun1_8_0Series-Part5.txt");
        GCResource file6 = getGcResource("SampleSun1_8_0Series-Part6.txt");
        GCResource file7 = getGcResource("SampleSun1_8_0Series-Part7.txt");
        GCResource expectedResult = getGcResource("SampleSun1_8_0Series-ManuallyMerged.txt");
        GCModel expectedModel = createModel(expectedResult);

        List<GCResource> resources = new ArrayList<>();
        resources.add(file1);
        resources.add(file2);
        resources.add(file3);
        resources.add(file4);
        resources.add(file5);
        resources.add(file6);
        resources.add(file7);
        GcResourceSeries series = new GcResourceSeries(resources);
        GCModel result = loader.load(series);

        assertThat(result, is(expectedModel));
    }

    @Test
    public void merge_FilesInWrongOrder() throws Exception {
        GCResource file1 = getGcResource("SampleSun1_8_0Series-Part1.txt");
        GCResource file2 = getGcResource("SampleSun1_8_0Series-Part2.txt");
        GCResource file3 = getGcResource("SampleSun1_8_0Series-Part3.txt");
        GCResource file4 = getGcResource("SampleSun1_8_0Series-Part4.txt");
        GCResource file5 = getGcResource("SampleSun1_8_0Series-Part5.txt");
        GCResource file6 = getGcResource("SampleSun1_8_0Series-Part6.txt");
        GCResource file7 = getGcResource("SampleSun1_8_0Series-Part7.txt");
        GCResource expectedResult = getGcResource("SampleSun1_8_0Series-ManuallyMerged.txt");
        GCModel expectedModel = createModel(expectedResult);

        List<GCResource> resources = new ArrayList<>();
        resources.add(file4);
        resources.add(file3);
        resources.add(file6);
        resources.add(file1);
        resources.add(file7);
        resources.add(file2);
        resources.add(file5);
        GcResourceSeries series = new GcResourceSeries(resources);
        GCModel result = loader.load(series);

        assertThat(result, is(expectedModel));
    }

    @Test
    public void getCreationDate_WhenDateStampIsAvailable() throws Exception {
        GCModel withDatestamp = new GCModel();
        GCEvent event = new GCEvent(1.0, 0, 0, 0, 0.0, AbstractGCEvent.Type.GC);
        ZonedDateTime datestamp = ZonedDateTime.now();
        event.setDateStamp(datestamp);
        withDatestamp.add(event);

        assertThat(loader.getCreationDate(withDatestamp), is(new GcSeriesLoader.GcDateStamp(datestamp)));
    }

    @Test
    public void getCreationDate_WhenTimeStampIsAvailable() throws Exception {
        GCModel withTimestamp = new GCModel();
        double timestamp = 13.37;
        GCEvent event = new GCEvent(timestamp, 0, 0, 0, 0.0, AbstractGCEvent.Type.GC);
        withTimestamp.add(event);

        assertThat(loader.getCreationDate(withTimestamp), is(new GcSeriesLoader.GcTimeStamp(timestamp)));
    }

    @Test
    public void getCreationDate_WhenNeitherDateNorTimestampIsAvailable() throws Exception {
        GCModel withoutDateOrTimestamp = new GCModel();
        File file = temporaryFolder.newFile();
        withoutDateOrTimestamp.setURL(file.toURI().toURL());

        // precision is millis, not nanos, i.e. Instant can't be used directly
        ZonedDateTime expectedTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Files.readAttributes(file.toPath(), BasicFileAttributes.class).creationTime().toMillis()), ZoneId.systemDefault());
        assertThat(loader.getCreationDate(withoutDateOrTimestamp), is(new GcSeriesLoader.GcDateStamp(expectedTime)));
    }

    @Test
    public void getFirstDateStampFromModel() throws Exception {
        GCResource resource = getGcResource("SampleSun1_8_0Series-Part1.txt");
        GCModel model = createModel(resource);
        ZonedDateTime expectedTime = ZonedDateTime.of(2016, 4, 14, 22, 30, 9, 108000000, ZoneOffset.ofHours(2));
        assertThat(loader.getFirstDateStampFromModel(model).get(), is(new GcSeriesLoader.GcDateStamp(expectedTime)));
    }

    @Test
    public void getDateStampFromResource() throws Exception {
        GCModel model = new GCModel();
        File file = temporaryFolder.newFile();
        model.setURL(file.toURI().toURL());

        // precision is millis, not nanos, i.e. Instant can't be used directly
        ZonedDateTime expectedTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Files.readAttributes(file.toPath(), BasicFileAttributes.class).creationTime().toMillis()), ZoneId.systemDefault());
        assertThat(loader.getCreationDateFromFile(model), is(new GcSeriesLoader.GcDateStamp(expectedTime)));
    }

    @Test
    public void sortResources() throws Exception {
        expectedException.expect(DataReaderException.class);

        Map<GcSeriesLoader.Timestamp, GCModel> map = new HashMap<>();
        map.put(new GcSeriesLoader.GcDateStamp(ZonedDateTime.now()), new GCModel());
        map.put(new GcSeriesLoader.GcTimeStamp(13.37), new GCModel());
        loader.sortResources(map);
    }

    private GCModel createModel(GCResource resource) throws DataReaderException {
        return dataReader.loadModel(resource);
    }

    private GcResourceFile getGcResource(String name) throws IOException {
        return new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, name).getPath());
    }
}
