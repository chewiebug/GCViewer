package com.tagtraum.perf.gcviewer.view.util;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 01.08.2018</p>
 */
public class ExtensionFileFilterTest {

    private ExtensionFileFilter extensionFileFilter;

    @Before
    public void setup() {
        extensionFileFilter = new ExtensionFileFilter("txt");
    }

    @Test
    public void description() {
        assertThat("extension", extensionFileFilter.getDescription(), equalTo("*.txt"));
    }

    @Test
    public void acceptFalse() {
        boolean doesAccept = extensionFileFilter.accept(new File("dummy-file-name"));

        assertThat("accepts", doesAccept, is(false));
    }

    @Test
    public void acceptTrue() {
        boolean doesAccept = extensionFileFilter.accept(new File("dummy-file-name.txt"));

        assertThat("accepts", doesAccept, is(true));
    }

    @Test
    public void acceptNull() {
        assertThat("null parameter", extensionFileFilter.accept(null), is(false));
    }
}
