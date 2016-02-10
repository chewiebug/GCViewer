package com.tagtraum.perf.gcviewer.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

/**
 * Tests for class {@link LocalisationHelper}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 13.09.2013</p>
 */
public class TestLocalisationHelper {

    @Test
    public void getStringWithoutParams() {
        String stringWithPlaceholders = LocalisationHelper.getString("datareader_parseerror_dialog_message");
        
        assertTrue("{0} is not present in string", stringWithPlaceholders.indexOf("{0}") >= 0);
        assertTrue("{1} is not present in string", stringWithPlaceholders.indexOf("{1}") >= 0);
    }
    
    @Test
    public void getStringWithParams() {
        Locale.setDefault(Locale.FRENCH);
        String stringWithoutPlaceholders = LocalisationHelper.getString("datareader_parseerror_dialog_message", "#1", "#2");
        
        assertFalse("{0} should not be present in string", stringWithoutPlaceholders.indexOf("{0}") >= 0);
        assertFalse("{1} should not be present in string", stringWithoutPlaceholders.indexOf("{1}") >= 0);
        assertTrue("#1 is not present in string", stringWithoutPlaceholders.indexOf("#1") >= 0);
        assertTrue("#2 is not present in string", stringWithoutPlaceholders.indexOf("#2") >= 0);
    }
}
