/**
 * 
 */
package com.tagtraum.perf.gcviewer.exp.summary;

import java.io.*;
import java.text.NumberFormat;
import java.util.Date;

import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.*;

/**
 * @author sean
 * SR: adding feature to auto export summary data, when run from command line
 * 
 * 
 * HISTORY:
 * 
 * 04/04/2011 SR
 * - added unit test for the CSV output, for the Sun JDK
 * - removed embedded commas from the value field in the CSV output
 * - added another column for the units for each field
 * - lines that had extra value for std. deviation, or for %, have now been split into two lines
 * 
 * TODO:   datetime fields like 'totalTime' need to separate out their units.
 * 
 */
public class SummaryExporter {
	private ISummaryExportFormatter formatter;

	/*
	 * field formatters
	 * */
	private NumberFormat pauseFormatter;
	private MemoryFormat footprintSlopeFormatter;
	private NumberFormat percentFormatter;
	private NumberFormat gcTimeFormatter;
	private MemoryFormat footprintFormatter;
	private NumberFormat throughputFormatter;
	private TimeFormat totalTimeFormatter;
	private MemoryFormat freedMemoryPerMinFormatter;
	private MemoryFormat sigmaMemoryFormatter;

	public SummaryExporter()
	{
		//use the default csv formatter
		this.formatter = new CsvSummaryExportFormatter();
		initialiseFormatters();
	}

	public SummaryExporter(ISummaryExportFormatter formatter)
	{
		this.formatter = formatter;
		initialiseFormatters();
	}

	private void initialiseFormatters() {
		pauseFormatter = NumberFormat.getInstance();
		pauseFormatter.setMaximumFractionDigits(5);

		totalTimeFormatter = new TimeFormat();
		
		gcTimeFormatter = NumberFormat.getInstance();
		gcTimeFormatter.setMaximumFractionDigits(2);

		throughputFormatter = NumberFormat.getInstance();
		throughputFormatter.setMaximumFractionDigits(2);

		footprintFormatter = new MemoryFormat();

		sigmaMemoryFormatter = new MemoryFormat();

		footprintSlopeFormatter = new MemoryFormat();

		freedMemoryPerMinFormatter = new MemoryFormat();

		percentFormatter = NumberFormat.getInstance();
		percentFormatter.setMaximumFractionDigits(1);
		percentFormatter.setMinimumFractionDigits(1);
	}

	private void exportValue(PrintWriter writer, String tag, boolean bValue)
	{
		exportValue(writer, tag, bValue ? "true" : "false", "bool");
	}

	private void exportValue(PrintWriter writer, String tag, String value, String units)
	{
		String strFormatted = formatter.formatLine(tag, value, units);
		writer.println(strFormatted);
	}

	public void exportSummaryFromModel(GCModel model, String gcLogFileDesc, String filePath) throws IOException
	{
		FileWriter outFile = new FileWriter(filePath);
		PrintWriter out = new PrintWriter(outFile);

		exportValue(out, "gcLogFile", gcLogFileDesc, "-");
		
		exportMemorySummary(out, model);
		exportPauseSummary(out, model);
		exportOverallSummary(out, model);

		out.flush();
		out.close();
	}

	private void exportOverallSummary(PrintWriter out, GCModel model) {
		exportValue(out, "accumPause", gcTimeFormatter.format(model.getPause().getSum()), "s");
		
		Formatted formed = footprintFormatter.formatToFormatted(model.getFootprint());
		exportValue(out, "footprint", formed.getValue(), formed.getUnits());
		
		formed = footprintFormatter.formatToFormatted(model.getFreedMemory());		
		exportValue(out, "freedMemory", formed.getValue(), formed.getUnits());
		
		if (model.hasCorrectTimestamp()) {
			exportValue(out, "throughput", throughputFormatter.format(model.getThroughput()), "%");
			formed = totalTimeFormatter.formatToFormatted(new Date((long)model.getRunningTime()*1000l));
			exportValue(out, "totalTime", formed.getValue(), formed.getUnits());
			
			formed = freedMemoryPerMinFormatter.formatToFormatted(model.getFreedMemory()/model.getRunningTime()*60.0);
			exportValue(out, "freedMemoryPerMin", formed.getValue(), formed.getUnits() + "/min");
		} else {
			exportValue(out, "throughput", "n.a.", "%");
			exportValue(out, "totalTime", "n.a.", "s");
			exportValue(out, "freedMemoryPerMin", "n.a.", "M/min");
		}

		final boolean gcDataAvailable = model.getGCPause().getN() > 0;
		if (gcDataAvailable) {
			formed = footprintFormatter.formatToFormatted(model.getFreedMemoryByGC().getSum()/model.getGCPause().getSum());
			exportValue(out, "gcPerformance", formed.getValue(), formed.getUnits() + "/s");
		}
		else {
			exportValue(out, "gcPerformance", "n.a.", "M/s");
		}

		final boolean fullGCDataAvailable = model.getFullGCPause().getN() > 0;

		if (fullGCDataAvailable) {
			formed = footprintFormatter.formatToFormatted(model.getFreedMemoryByFullGC().getSum()/model.getFullGCPause().getSum());
			exportValue(out, "fullGCPerformance", formed.getValue(), formed.getUnits() + "/s");
		}
		else {
			exportValue(out, "fullGCPerformance", "n.a.", "M/s");
		}
	}

	private void exportPauseSummary(PrintWriter out, GCModel model) {
		final boolean pauseDataAvailable = model.getPause().getN() != 0;
		final boolean gcDataAvailable = model.getGCPause().getN() > 0;
		final boolean fullGCDataAvailable = model.getFullGCPause().getN() > 0;

		if (pauseDataAvailable) {
			exportValue(out, "avgPauseIsSig", isSignificant(model.getPause().average(), model.getPause().standardDeviation()) );
			exportValue(out, "avgPause", pauseFormatter.format(model.getPause().average()), "s");
			exportValue(out, "avgPause\u03c3", pauseFormatter.format(model.getPause().standardDeviation()), "s");

			exportValue(out, "minPause", pauseFormatter.format(model.getPause().getMin()), "s");
			exportValue(out, "maxPause", pauseFormatter.format(model.getPause().getMax()), "s");

			if (gcDataAvailable) {
				exportValue(out, "avgGCPauseIsSig", isSignificant(model.getGCPause().average(), model.getGCPause().standardDeviation()) );
				exportValue(out, "avgGCPause", pauseFormatter.format(model.getGCPause().average()), "s");
				exportValue(out, "avgGCPause\u03c3", pauseFormatter.format(model.getGCPause().standardDeviation()), "s");
			}
			else {
				exportValue(out, "avgGCPause", "n.a.", "s");
			}

			if (fullGCDataAvailable) {
				exportValue(out, "avgFullGCPauseIsSig", isSignificant(model.getFullGCPause().average(), model.getPause().standardDeviation()));
				exportValue(out, "avgFullGCPause", pauseFormatter.format(model.getFullGCPause().average()), "s");
				exportValue(out, "avgFullGCPause\u03c3", pauseFormatter.format(model.getFullGCPause().standardDeviation()), "s");
			}
			else {
				exportValue(out, "avgFullGCPause", "n.a.", "s");
			}
		}
		else {
			exportValue(out, "avgPause", "n.a.", "s");
			exportValue(out, "minPause", "n.a.", "s");
			exportValue(out, "maxPause", "n.a.", "s");
			exportValue(out, "avgGCPause", "n.a.", "s");
			exportValue(out, "avgFullGCPause", "n.a.", "s");
		}
		exportValue(out, "accumPause", gcTimeFormatter.format(model.getPause().getSum()), "s");
		exportValue(out, "fullGCPause", gcTimeFormatter.format(model.getFullGCPause().getSum()), "s");
		exportValue(out, "fullGCPausePc", percentFormatter.format(model.getFullGCPause().getSum()*100.0/model.getPause().getSum()), "%");
		exportValue(out, "gcPause", gcTimeFormatter.format(model.getGCPause().getSum()), "s");
		exportValue(out, "gcPausePc", percentFormatter.format(model.getGCPause().getSum()*100.0/model.getPause().getSum()), "%");
	}

	private boolean isSignificant(final double average, final double standardDeviation) {
		// at least 68.3% of all points are within 0.75 to 1.25 times the average value
		// Note: this may or may not be a good measure, but it at least helps to mark some bad data as such
		return average-standardDeviation > 0.75 * average;
	}

	private void exportMemorySummary(PrintWriter out, GCModel model) {
		Formatted formed = footprintFormatter.formatToFormatted(model.getFootprint());
		exportValue(out, "footprint", formed.getValue(), formed.getUnits());
		
		// check whether we have full gc data at all
		final boolean fullGCDataVailable = model.getFootprintAfterFullGC().getN() != 0;
		final boolean fullGCSlopeDataVailable = model.getFootprintAfterFullGC().getN() > 1;

		if (!fullGCDataVailable) {
			exportValue(out, "footprintAfterFullGC", "n.a.", "M");
			exportValue(out, "slopeAfterFullGC", "n.a.", "M/s");
			exportValue(out, "freedMemoryByFullGC", "n.a.", "M");
			exportValue(out, "avgFreedMemoryByFullGC", "n.a.", "M");
			exportValue(out, "avgFreedMemoryByFullGC\u03c3", "n.a.", "M");
			exportValue(out, "avgFreedMemoryByFullGCisSig", "n.a.", "bool");
		}
		else {
			formed = footprintFormatter.formatToFormatted(model.getFootprintAfterFullGC().average());
			exportValue(out, "avgfootprintAfterFullGC", formed.getValue(), formed.getUnits());
			formed = sigmaMemoryFormat(model.getFootprintAfterFullGC().standardDeviation());
			exportValue(out, "avgfootprintAfterFullGC\u03c3", formed.getValue(), formed.getUnits());
			exportValue(out, "avgfootprintAfterFullGCisSig", isSignificant(model.getFootprintAfterFullGC().average(),
					model.getFootprintAfterFullGC().standardDeviation()));
			formed = footprintFormatter.formatToFormatted(model.getFreedMemoryByFullGC().getSum());
			exportValue(out, "freedMemoryByFullGC", formed.getValue(), formed.getUnits());
			exportValue(out, "freedMemoryByFullGCpc", percentFormatter.format(model.getFreedMemoryByFullGC().getSum()*100.0/model.getFreedMemory()), "%");
			
			formed = footprintFormatter.formatToFormatted(model.getFreedMemoryByFullGC().average());
			exportValue(out, "avgFreedMemoryByFullGC", formed.getValue(), formed.getUnits() + "/coll");
			formed = sigmaMemoryFormat(model.getFreedMemoryByFullGC().standardDeviation());
			exportValue(out, "avgFreedMemoryByFullGC\u03c3", formed.getValue(), formed.getUnits() + "/coll");
			exportValue(out, "avgFreedMemoryByFullGCisSig", isSignificant(model.getFreedMemoryByFullGC().average(),
					model.getFreedMemoryByFullGC().standardDeviation()));
			if (fullGCSlopeDataVailable) {
				formed = footprintSlopeFormatter.formatToFormatted(model.getPostFullGCSlope().slope());
				exportValue(out, "slopeAfterFullGC", formed.getValue(), formed.getUnits() + "/s");
				
				formed = footprintSlopeFormatter.formatToFormatted(model.getRelativePostFullGCIncrease().slope());
				exportValue(out, "avgRelativePostFullGCInc", formed.getValue(), formed.getUnits() + "/coll");
			}
			else {
				exportValue(out, "slopeAfterFullGC", "n.a.", "M/s");
				exportValue(out, "avgRelativePostFullGCInc", "n.a.", "M/coll");
			}
		}
		// check whether we have gc data at all (or only full gc)
		final boolean gcDataAvailable = model.getFootprintAfterGC().getN() != 0;

		if (!gcDataAvailable) {
			exportValue(out, "footprintAfterGC", "n.a.", "M");
			exportValue(out, "slopeAfterGC", "n.a.", "M/s");
			exportValue(out, "freedMemoryByGC", "n.a.", "M");
			exportValue(out, "avgFreedMemoryByGC", "n.a.", "M/coll");
			exportValue(out, "avgRelativePostGCInc", "n.a.", "M/coll");
		}
		else {
			formed = footprintFormatter.formatToFormatted(model.getFootprintAfterGC().average());
			exportValue(out, "avgfootprintAfterGC", formed.getValue(), formed.getUnits());
			
			formed = sigmaMemoryFormat(model.getFootprintAfterGC().standardDeviation());
			exportValue(out, "avgfootprintAfterGC\u03c3", formed.getValue(), formed.getUnits());
			exportValue(out, "avgfootprintAfterGCisSig", isSignificant(model.getFootprintAfterGC().average(),
					model.getFootprintAfterGC().standardDeviation()));
			if (fullGCDataVailable && model.getRelativePostGCIncrease().getN() != 0) {
				formed = footprintSlopeFormatter.formatToFormatted(model.getPostGCSlope());
				exportValue(out, "slopeAfterGC", formed.getValue(), formed.getUnits() + "/s");
				
				formed = footprintSlopeFormatter.formatToFormatted(model.getRelativePostGCIncrease().average());
				exportValue(out, "avgRelativePostGCInc", formed.getValue(), formed.getUnits() + "/coll");
			}
			else {
				exportValue(out, "slopeAfterGC", "n.a.", "M/s");
				exportValue(out, "avgRelativePostGCInc", "n.a.", "M/coll");
			}
			
			formed = footprintFormatter.formatToFormatted(model.getFreedMemoryByGC().getSum());
			exportValue(out, "freedMemoryByGC", formed.getValue(), formed.getUnits());
			
			exportValue(out, "freedMemoryByGCpc", percentFormatter.format(model.getFreedMemoryByGC().getSum()*100.0/model.getFreedMemory()), "%");
			
			formed = footprintFormatter.formatToFormatted(model.getFreedMemoryByGC().average());
			exportValue(out, "avgFreedMemoryByGC", formed.getValue(), formed.getUnits() + "/coll");
			formed = sigmaMemoryFormat(model.getFreedMemoryByGC().standardDeviation());
			exportValue(out, "avgFreedMemoryByGC\u03c3", formed.getValue(), formed.getUnits() + "/coll");
			exportValue(out, "avgFreedMemoryByGCisSig", isSignificant(model.getFreedMemoryByGC().average(),
					model.getFreedMemoryByGC().standardDeviation()));
		}
		formed = footprintFormatter.formatToFormatted(model.getFreedMemory());
		exportValue(out, "freedMemory", formed.getValue(), formed.getUnits());
	}

    private Formatted sigmaMemoryFormat(double value) {
        if (Double.isNaN(value))
        {
        	StringBuffer buffer = new StringBuffer("NaN");
        	return new Formatted(buffer, ' '); 
        }
        return sigmaMemoryFormatter.formatToFormatted(value);
    }
}

