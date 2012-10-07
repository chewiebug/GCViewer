/**
 * 
 */
package com.tagtraum.perf.gcviewer.exp.summary;

/**
 * @author sean
 *
 */
public interface ISummaryExportFormatter {
    static final String NAME = "summaryExportFormatter"; 
    String formatLine(String tag, String value, String units);
}
