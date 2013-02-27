package com.tagtraum.perf.gcviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.util.BuildInfoReader;

/**
 * Tests the class {@link BuildInfoReader} - makes sure that the properties from the file
 * can be read. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 05.12.2012</p>
 */
public class TestBuildInfoReader {

    @Test
    public void readVersion() {
        String version = BuildInfoReader.getVersion();
        assertNotNull("version", version);
        assertFalse("must not be n/a", version.equals("n/a"));
    }

    @Test
    public void readBuildDate() {
        String buildDate = BuildInfoReader.getBuildDate();
        assertNotNull("buildDate", buildDate);
        assertFalse("must not be n/a", buildDate.equals("n/a"));
    }
}
