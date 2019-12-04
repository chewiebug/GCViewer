package com.tagtraum.perf.gcviewer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.tagtraum.perf.gcviewer.util.BuildInfoReader;
import org.junit.jupiter.api.Test;

/**
 * Tests the class {@link BuildInfoReader} - makes sure that the properties from the file
 * can be read. 
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 05.12.2012</p>
 */
class TestBuildInfoReader {

    @Test
    void readVersion() {
        String version = BuildInfoReader.getVersion();
        assertThat("version", version, notNullValue());
        assertThat("must not be empty", version, not(isEmptyOrNullString()));
    }

    @Test
    void readBuildDate() {
        String buildDate = BuildInfoReader.getBuildDate();
        assertThat("buildDate", buildDate, notNullValue());
        assertThat("must not be empty", buildDate, not(isEmptyOrNullString()));
    }
}
