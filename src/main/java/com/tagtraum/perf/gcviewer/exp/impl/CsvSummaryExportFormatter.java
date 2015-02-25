/**
 *
 */
package com.tagtraum.perf.gcviewer.exp.impl;

/**
 * Write Summary Information in CSV format.
 * <p>
 * Write each summary metric on its own line with {@literal "tag; value; unit"} format.
 *
 * @author sean
 *
 */
public class CsvSummaryExportFormatter implements ISummaryExportFormatter {

    private String separator = "; ";

    /**
     * @see ISummaryExportFormatter#formatLine(String, String, String)
     */
    @Override
    public String formatLine(String tag, String value, String units) {
        return tag + separator + value + separator + units;
    }
}
