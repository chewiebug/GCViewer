package com.tagtraum.perf.gcviewer.util;

import java.util.ResourceBundle;

/**
 * Helperclass to support localisation.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 25.04.2013</p>
 */
public class LocalisationHelper {
    private static ResourceBundle resourceBundle;
    
    /**
     * Returns localised text as result of lookup with <code>key</code>.
     * 
     * @param key key to look up localised text for
     * @return localised text
     */
    public static String getString(String key) {
        if (getBundle().containsKey(key)) {
            return getBundle().getString(key);
        }
        else {
            return "\"" + key + "\" not found";
        }
    }
    
    private static ResourceBundle getBundle() {
        if (resourceBundle == null) {
            resourceBundle = ResourceBundle.getBundle("com.tagtraum.perf.gcviewer.localStrings");
        }
        
        return resourceBundle;
    }
}
