package com.tagtraum.perf.gcviewer.exp.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tagtraum.perf.gcviewer.exp.AbstractDataWriter;
import com.tagtraum.perf.gcviewer.math.DoubleData;
import com.tagtraum.perf.gcviewer.math.DoubleDataPercentile;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.FormattedValue;
import com.tagtraum.perf.gcviewer.util.MemoryFormat;
import com.tagtraum.perf.gcviewer.util.TimeFormat;

/**
 * SummaryDataWriter writes a csv-file of quite a few parameters of the {@link GCModel} class.
 * <p>
 * It is intended to be used from a command line version of GCViewer.
 * <p>
 * TODO:   datetime fields like 'totalTime' need to separate out their units.
 *
 * @author sean
 */
public class SummaryDataWriter extends AbstractDataWriter {

    private ISummaryExportFormatter formatter;

    /*
     * field formatters
     */
    private NumberFormat pauseFormatter;
    private MemoryFormat footprintSlopeFormatter;
    private NumberFormat percentFormatter;
    private NumberFormat gcTimeFormatter;
    private MemoryFormat footprintFormatter;
    private NumberFormat throughputFormatter;
    private TimeFormat totalTimeFormatter;
    private MemoryFormat freedMemoryPerMinFormatter;
    private MemoryFormat sigmaMemoryFormatter;

    public SummaryDataWriter(OutputStream out) {
        //use the default csv formatter
        this(out, null);
    }

    /**
     * Constructor for SummaryDatWriter with additional <code>configuration</code> parameter.
     *
     * @param out OutputStream, where the output should be written to
     * @param configuration Configuration for this SummaryDataWriter; expected contents of the parameter:
     * <ul>
     * <li>String: <code>ISummaryExportFormatter.NAME</code></li>
     * <li>Object: instance of class implementing ISummaryExportFormatter
     * </ul>
     */
    public SummaryDataWriter(OutputStream out, Map<String, Object> configuration) {
        super(out, configuration);

        // don't use "configuration" parameter directly - it may be null
        this.formatter = (ISummaryExportFormatter) getConfiguration().get(ISummaryExportFormatter.NAME);
        if (this.formatter == null) {
            this.formatter = new CsvSummaryExportFormatter();
        }

        initialiseFormatters();
    }

    private void initialiseFormatters() {
        pauseFormatter = NumberFormat.getInstance();
        pauseFormatter.setMaximumFractionDigits(6);

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

    private void exportValue(PrintWriter writer, String tag, boolean bValue) {
        exportValue(writer, tag, Boolean.toString(bValue), "bool");
    }

    private void exportValue(PrintWriter writer, String tag, String value, String units) {
        String strFormatted = formatter.formatLine(tag, value, units);
        writer.println(strFormatted);
    }

    @Override
    public void write(GCModel model) throws IOException {
        int lastIndexOfSlash = model.getURL().getFile().lastIndexOf('/');
        exportValue(out,
                "gcLogFile",
                model.getURL().getFile().substring(lastIndexOfSlash >= 0 ? lastIndexOfSlash + 1 : 0),
                "-");

        exportMemorySummary(out, model);
        exportPauseSummary(out, model);
        exportOverallSummary(out, model);

        out.flush();
    }

    public void exportSummaryFromModel(GCModel model, String filePath) throws IOException {
        FileWriter outFile = new FileWriter(filePath);
        PrintWriter out = new PrintWriter(outFile);

        int lastIndexOfSlash = model.getURL().getFile().lastIndexOf('/');
        exportValue(out,
                "gcLogFile",
                model.getURL().getFile().substring(lastIndexOfSlash >= 0 ? lastIndexOfSlash + 1 : 0),
                "-");

        exportMemorySummary(out, model);
        exportPauseSummary(out, model);
        exportOverallSummary(out, model);

        out.flush();
        out.close();
    }

    private void exportOverallSummary(PrintWriter out, GCModel model) {
        FormattedValue formed = footprintFormatter.formatToFormatted(model.getFootprint());
        exportValue(out, "footprint", formed.getValue(), formed.getUnits());

        formed = footprintFormatter.formatToFormatted(model.getFreedMemory());
        exportValue(out, "freedMemory", formed.getValue(), formed.getUnits());

        if (model.hasCorrectTimestamp()) {
            exportValue(out, "throughput", throughputFormatter.format(model.getThroughput()), "%");
            formed = totalTimeFormatter.formatToFormatted(new Date((long)model.getRunningTime()*1000l));
            exportValue(out, "totalTime", formed.getValue(), formed.getUnits());

            formed = freedMemoryPerMinFormatter.formatToFormatted(model.getFreedMemory()/model.getRunningTime()*60.0);
            exportValue(out, "freedMemoryPerMin", formed.getValue(), formed.getUnits() + "/min");
        }
        else {
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
            exportValue(out, "pauseCount", "" + model.getPause().getN(), "-");

            exportValue(out, "avgPauseIsSig", isSignificant(model.getPause().average(), model.getPause().standardDeviation()) );
            exportValue(out, "avgPause", pauseFormatter.format(model.getPause().average()), "s");
            exportValue(out, "avgPause\u03c3", pauseFormatter.format(model.getPause().standardDeviation()), "s");

            exportValue(out, "minPause", pauseFormatter.format(model.getPause().getMin()), "s");
            exportValue(out, "maxPause", pauseFormatter.format(model.getPause().getMax()), "s");

            if (gcDataAvailable) {
                exportValue(out, "gcPauseCount", "" + model.getGCPause().getN(), "-");

                exportValue(out, "avgGCPauseIsSig", isSignificant(model.getGCPause().average(), model.getGCPause().standardDeviation()) );
                exportValue(out, "avgGCPause", pauseFormatter.format(model.getGCPause().average()), "s");
                exportValue(out, "avgGCPause\u03c3", pauseFormatter.format(model.getGCPause().standardDeviation()), "s");
            }
            else {
                exportValue(out, "avgGCPause", "n.a.", "s");
            }

            if (fullGCDataAvailable) {
                exportValue(out, "fullGcPauseCount", "" + model.getFullGCPause().getN(), "-");

                exportValue(out, "avgFullGCPauseIsSig", isSignificant(model.getFullGCPause().average(), model.getFullGCPause().standardDeviation()));
                exportValue(out, "avgFullGCPause", pauseFormatter.format(model.getFullGCPause().average()), "s");
                exportValue(out, "avgFullGCPause\u03c3", pauseFormatter.format(model.getFullGCPause().standardDeviation()), "s");

                exportValue(out, "minFullGCPause", pauseFormatter.format(model.getFullGCPause().getMin()), "s");
                exportValue(out, "maxFullGCPause", pauseFormatter.format(model.getFullGCPause().getMax()), "s");
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

        // Add extra statistical data: sum, count, min, max, average, standardDeviation, median, 75th percentile, 95, 99, 99.5, 99.9
        // All Pause stats
        if (pauseDataAvailable) {
            exportValue(out, "pauseSum", pauseFormatter.format(model.getPause().getSum()), "s");
            // exportValue(out, "pauseCount", "" + model.getPause().getN(), "-");
            exportValue(out, "pauseMin", pauseFormatter.format(model.getPause().getMin()), "s");
            exportValue(out, "pauseMax", pauseFormatter.format(model.getPause().getMax()), "s");
            exportValue(out, "pauseAverage", pauseFormatter.format(model.getPause().average()), "s");
            exportValue(out, "pauseStandardDeviation", pauseFormatter.format(model.getPause().standardDeviation()), "s");
            exportValue(out, "pauseMedian", pauseFormatter.format(((DoubleDataPercentile)model.getPause()).getPercentile(50)), "s");
            exportValue(out, "pausePercentile75th", pauseFormatter.format(((DoubleDataPercentile)model.getPause()).getPercentile(50)), "s");
            exportValue(out, "pausePercentile95th", pauseFormatter.format(((DoubleDataPercentile)model.getPause()).getPercentile(75)), "s");
            exportValue(out, "pausePercentile99th", pauseFormatter.format(((DoubleDataPercentile)model.getPause()).getPercentile(95)), "s");
            exportValue(out, "pausePercentile99.5th", pauseFormatter.format(((DoubleDataPercentile)model.getPause()).getPercentile(99)), "s");
            exportValue(out, "pausePercentile99.9th", pauseFormatter.format(((DoubleDataPercentile)model.getPause()).getPercentile(99.9)), "s");
        }
        // GC Pause stats
        if (gcDataAvailable) {
            exportValue(out, "gcPauseSum", pauseFormatter.format(model.getGCPause().getSum()), "s");
            // exportValue(out, "gcPauseCount", "" + model.getGCPause().getN(), "-");
            exportValue(out, "gcPauseMin", pauseFormatter.format(model.getGCPause().getMin()), "s");
            exportValue(out, "gcPauseMax", pauseFormatter.format(model.getGCPause().getMax()), "s");
            exportValue(out, "gcPauseAverage", pauseFormatter.format(model.getGCPause().average()), "s");
            exportValue(out, "gcPauseStandardDeviation", pauseFormatter.format(model.getGCPause().standardDeviation()), "s");
            exportValue(out, "gcPauseMedian", pauseFormatter.format(((DoubleDataPercentile)model.getGCPause()).getPercentile(50)), "s");
            exportValue(out, "gcPausePercentile75th", pauseFormatter.format(((DoubleDataPercentile)model.getGCPause()).getPercentile(50)), "s");
            exportValue(out, "gcPausePercentile95th", pauseFormatter.format(((DoubleDataPercentile)model.getGCPause()).getPercentile(75)), "s");
            exportValue(out, "gcPausePercentile99th", pauseFormatter.format(((DoubleDataPercentile)model.getGCPause()).getPercentile(95)), "s");
            exportValue(out, "gcPausePercentile99.5th", pauseFormatter.format(((DoubleDataPercentile)model.getGCPause()).getPercentile(99)), "s");
            exportValue(out, "gcPausePercentile99.9th", pauseFormatter.format(((DoubleDataPercentile)model.getGCPause()).getPercentile(99.9)), "s");
        }
        // Full GC Pause stats
        if (fullGCDataAvailable) {
            exportValue(out, "fullGCPauseSum", pauseFormatter.format(model.getFullGCPause().getSum()), "s");
            // exportValue(out, "fullGCPauseCount", "" + model.getFullGCPause().getN(), "-");
            exportValue(out, "fullGCPauseMin", pauseFormatter.format(model.getFullGCPause().getMin()), "s");
            exportValue(out, "fullGCPauseMax", pauseFormatter.format(model.getFullGCPause().getMax()), "s");
            exportValue(out, "fullGCPauseAverage", pauseFormatter.format(model.getFullGCPause().average()), "s");
            exportValue(out, "fullGCPauseStandardDeviation", pauseFormatter.format(model.getFullGCPause().standardDeviation()), "s");
            exportValue(out, "fullGCPauseMedian", pauseFormatter.format(((DoubleDataPercentile)model.getFullGCPause()).getPercentile(50)), "s");
            exportValue(out, "fullGCPausePercentile75th", pauseFormatter.format(((DoubleDataPercentile)model.getFullGCPause()).getPercentile(50)), "s");
            exportValue(out, "fullGCPausePercentile95th", pauseFormatter.format(((DoubleDataPercentile)model.getFullGCPause()).getPercentile(75)), "s");
            exportValue(out, "fullGCPausePercentile99th", pauseFormatter.format(((DoubleDataPercentile)model.getFullGCPause()).getPercentile(95)), "s");
            exportValue(out, "fullGCPausePercentile99.5th", pauseFormatter.format(((DoubleDataPercentile)model.getFullGCPause()).getPercentile(99)), "s");
            exportValue(out, "fullGCPausePercentile99.9th", pauseFormatter.format(((DoubleDataPercentile)model.getFullGCPause()).getPercentile(99.9)), "s");
        }
        // ZGC stats: [gc,phases]
        if (model.size() > 1 && model.getGcEventPhases().size() > 0) {
            DoubleData gcPhases = new DoubleDataPercentile();
            for (Entry<String, DoubleData> entry : model.getGcEventPhases().entrySet()) {
                List<Double> phaseList = ((DoubleDataPercentile)entry.getValue()).getDoubleData();
                for (Double d : phaseList)
                    gcPhases.add(d);
            }
            exportValue(out, "gcPhaseSum", pauseFormatter.format(gcPhases.getSum()), "s");
            exportValue(out, "gcPhaseCount", "" + gcPhases.getN(), "-");
            exportValue(out, "gcPhaseMin", pauseFormatter.format(gcPhases.getMin()), "s");
            exportValue(out, "gcPhaseMax", pauseFormatter.format(gcPhases.getMax()), "s");
            exportValue(out, "gcPhaseAverage", pauseFormatter.format(gcPhases.average()), "s");
            exportValue(out, "gcPhaseStandardDeviation", pauseFormatter.format(gcPhases.standardDeviation()), "s");
            exportValue(out, "gcPhaseMedian", pauseFormatter.format(((DoubleDataPercentile)gcPhases).getPercentile(50)), "s");
            exportValue(out, "gcPhasePercentile75th", pauseFormatter.format(((DoubleDataPercentile)gcPhases).getPercentile(50)), "s");
            exportValue(out, "gcPhasePercentile95th", pauseFormatter.format(((DoubleDataPercentile)gcPhases).getPercentile(75)), "s");
            exportValue(out, "gcPhasePercentile99th", pauseFormatter.format(((DoubleDataPercentile)gcPhases).getPercentile(95)), "s");
            exportValue(out, "gcPhasePercentile99.5th", pauseFormatter.format(((DoubleDataPercentile)gcPhases).getPercentile(99)), "s");
            exportValue(out, "gcPhasePercentile99.9th", pauseFormatter.format(((DoubleDataPercentile)gcPhases).getPercentile(99.9)), "s");
        }
    }

    private boolean isSignificant(final double average, final double standardDeviation) {
        // at least 68.3% of all points are within 0.75 to 1.25 times the average value
        // Note: this may or may not be a good measure, but it at least helps to mark some bad data as such
        return average-standardDeviation > 0.75 * average;
    }

    private void exportMemorySummary(PrintWriter out, GCModel model) {
        FormattedValue formed = footprintFormatter.formatToFormatted(model.getHeapAllocatedSizes().getMax());
        exportValue(out, "totalHeapAllocMax", formed.getValue(), formed.getUnits());
        formed = footprintFormatter.formatToFormatted(model.getHeapUsedSizes().getMax());
        exportValue(out, "totalHeapUsedMax", formed.getValue(), formed.getUnits());
        exportValue(out, "totalHeapUsedMaxpc", percentFormatter.format(model.getHeapUsedSizes().getMax() * 100.0 / model.getHeapAllocatedSizes().getMax()), "%");

        if (model.getTenuredAllocatedSizes().getN() == 0) {
            exportValue(out, "totalTenuredAllocMax", "n/a", "M");
            exportValue(out, "totalTenuredUsedMax", "n/a", "M");
            exportValue(out, "totalTenuredUsedMaxpc", "n/a", "%");
        } else {
            formed = footprintFormatter.formatToFormatted(model.getTenuredAllocatedSizes().getMax());
            exportValue(out, "totalTenuredAllocMax", formed.getValue(), formed.getUnits());
            formed = footprintFormatter.formatToFormatted(model.getTenuredUsedSizes().getMax());
            exportValue(out, "totalTenuredUsedMax", formed.getValue(), formed.getUnits());
            exportValue(out, "totalTenuredUsedMaxpc", percentFormatter.format(model.getTenuredUsedSizes().getMax() * 100.0 / model.getTenuredAllocatedSizes().getMax()), "%");
        }

        if (model.getYoungAllocatedSizes().getN() == 0) {
            exportValue(out, "totalYoungAllocMax", "n/a", "M");
            exportValue(out, "totalYoungUsedMax", "n/a", "M");
            exportValue(out, "totalYoungUsedMaxpc", "n/a", "%");
        } else {
            formed = footprintFormatter.formatToFormatted(model.getYoungAllocatedSizes().getMax());
            exportValue(out, "totalYoungAllocMax", formed.getValue(), formed.getUnits());
            formed = footprintFormatter.formatToFormatted(model.getYoungUsedSizes().getMax());
            exportValue(out, "totalYoungUsedMax", formed.getValue(), formed.getUnits());
            exportValue(out, "totalYoungUsedMaxpc", percentFormatter.format(model.getYoungUsedSizes().getMax() * 100.0 / model.getYoungAllocatedSizes().getMax()), "%");
        }

        if (model.getPermAllocatedSizes().getN() == 0) {
            exportValue(out, "totalPermAllocMax", "n/a", "M");
            exportValue(out, "totalPermUsedMax", "n/a", "M");
            exportValue(out, "totalPermUsedMaxpc", "n/a", "%");
        } else {
            formed = footprintFormatter.formatToFormatted(model.getPermAllocatedSizes().getMax());
            exportValue(out, "totalPermAllocMax", formed.getValue(), formed.getUnits());
            formed = footprintFormatter.formatToFormatted(model.getPermUsedSizes().getMax());
            exportValue(out, "totalPermUsedMax", formed.getValue(), formed.getUnits());
            exportValue(out, "totalPermUsedMaxpc", percentFormatter.format(model.getPermUsedSizes().getMax() * 100.0 / model.getPermAllocatedSizes().getMax()), "%");
        }

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

        final boolean promotionDataAvailable = model.getPromotion().getN() != 0;

        if (!promotionDataAvailable) {
            exportValue(out, "avgPromotion", "n.a.", "M");
            exportValue(out, "promotionTotal", "n.a.", "M");
        }
        else {
            formed = footprintFormatter.formatToFormatted(model.getPromotion().average());
            exportValue(out, "avgPromotion", formed.getValue(), formed.getUnits());
            formed = footprintFormatter.formatToFormatted(model.getPromotion().getSum());
            exportValue(out, "promotionTotal", formed.getValue(), formed.getUnits());
        }
    }

    private FormattedValue sigmaMemoryFormat(double value) {
        if (Double.isNaN(value)) {
            StringBuffer buffer = new StringBuffer("NaN");
            return new FormattedValue(buffer, ' ');
        }
        return sigmaMemoryFormatter.formatToFormatted(value);
    }

}

