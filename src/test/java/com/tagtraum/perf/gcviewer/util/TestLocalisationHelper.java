package com.tagtraum.perf.gcviewer.util;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for class {@link LocalisationHelper}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 13.09.2013</p>
 */
class TestLocalisationHelper {

    @Test
    void getStringWithoutParams() {
        String stringWithPlaceholders = LocalisationHelper.getString("datareader_parseerror_dialog_message");
        
        assertTrue(stringWithPlaceholders.indexOf("{0}") >= 0, "{0} is not present in string");
    }
    
    @Test
    void getStringWithParams() {
        Locale.setDefault(Locale.FRENCH);
        String stringWithoutPlaceholders = LocalisationHelper.getString("datareader_parseerror_dialog_message", "#1", "#2");
        
        assertFalse(stringWithoutPlaceholders.indexOf("{0}") >= 0, "{0} should not be present in string");
        assertTrue(stringWithoutPlaceholders.indexOf("#1") >= 0, "#1 is not present in string");
    }
}
