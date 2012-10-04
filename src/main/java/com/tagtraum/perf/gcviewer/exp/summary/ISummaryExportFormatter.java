/**
 * 
 */
package com.tagtraum.perf.gcviewer.exp.summary;

/**
 * @author sean
 *
 */
public interface ISummaryExportFormatter {
    String formatLine(String tag, String value, String units);
}
