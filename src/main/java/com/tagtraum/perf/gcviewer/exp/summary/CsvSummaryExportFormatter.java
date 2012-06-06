/**
 * 
 */
package com.tagtraum.perf.gcviewer.exp.summary;

/**
 * @author sean
 *
 */
public class CsvSummaryExportFormatter implements ISummaryExportFormatter {

	private String separator = ", ";
	
	/* (non-Javadoc)
	 * @see com.tagtraum.perf.gcviewer.exp.ISummaryExportFormatter#exportValue(java.lang.String, java.lang.String)
	 */
	//@Override
	public String formatLine(String tag, String value, String units) {
        return tag + separator + value + separator + units;
	}
}
