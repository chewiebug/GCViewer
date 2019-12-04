package com.tagtraum.perf.gcviewer.view.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;

/**
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 01.08.2018</p>
 */
class ExtensionFileFilterTest {

    private ExtensionFileFilter extensionFileFilter;

    @BeforeEach
    void setup() {
        extensionFileFilter = new ExtensionFileFilter("txt");
    }

    @Test
    void description() {
        assertThat("extension", extensionFileFilter.getDescription(), equalTo("*.txt"));
    }

    @Test
    void acceptFalse() {
        boolean doesAccept = extensionFileFilter.accept(new File("dummy-file-name"));

        assertThat("accepts", doesAccept, is(false));
    }

    @Test
    void acceptTrue() {
        boolean doesAccept = extensionFileFilter.accept(new File("dummy-file-name.txt"));

        assertThat("accepts", doesAccept, is(true));
    }

    @Test
    void acceptNull() {
        assertThat("null parameter", extensionFileFilter.accept(null), is(false));
    }
}
