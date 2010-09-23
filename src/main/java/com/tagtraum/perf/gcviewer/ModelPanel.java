package com.tagtraum.perf.gcviewer;

import com.tagtraum.perf.gcviewer.util.TimeFormat;
import com.tagtraum.perf.gcviewer.util.MemoryFormat;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.text.DateFormat;
import java.util.ResourceBundle;
import java.util.Date;

/**
 * Panel that contains characteristic data about about the gc file.
 *
 * Date: Feb 3, 2002
 * Time: 9:58:00 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class ModelPanel extends JTabbedPane {

    private static ResourceBundle localStrings = ResourceBundle.getBundle("com.tagtraum.perf.gcviewer.localStrings");

    private NumberFormat pauseFormatter;
    private DateFormat totalTimeFormatter;
    private NumberFormat throughputFormatter;
    private NumberFormat footprintFormatter;
    private NumberFormat footprintSlopeFormatter;
    private NumberFormat freedMemoryPerMinFormatter;
    private NumberFormat sigmaFormatter;
    private NumberFormat percentFormatter;
    private NumberFormat gcTimeFormatter;
    private MemoryFormat sigmaMemoryFormatter;
    private SummaryTab summaryTab;
    private MemoryTab memoryTab;
    private PauseTab pauseTab;

    public ModelPanel() {
        //setBorder(new TitledBorder(localStrings.getString("data_panel_title")));
        pauseFormatter = NumberFormat.getInstance();
        pauseFormatter.setMaximumFractionDigits(5);

        totalTimeFormatter = new TimeFormat();

        gcTimeFormatter = NumberFormat.getInstance();
        gcTimeFormatter.setMaximumFractionDigits(2);

        throughputFormatter = NumberFormat.getInstance();
        throughputFormatter.setMaximumFractionDigits(2);

        footprintFormatter = new MemoryFormat();
        //footprintFormatter.setMaximumFractionDigits(0);

        sigmaFormatter = NumberFormat.getInstance();
        sigmaFormatter.setMaximumFractionDigits(0);

        sigmaMemoryFormatter = new MemoryFormat();

        footprintSlopeFormatter = new MemoryFormat();

        freedMemoryPerMinFormatter = new MemoryFormat();

        percentFormatter = NumberFormat.getInstance();
        percentFormatter.setMaximumFractionDigits(1);
        percentFormatter.setMinimumFractionDigits(1);

        summaryTab = new SummaryTab();
        addTab(localStrings.getString("data_panel_tab_summary"), summaryTab);
        memoryTab = new MemoryTab();
        addTab(localStrings.getString("data_panel_tab_memory"), memoryTab);
        pauseTab = new PauseTab();
        addTab(localStrings.getString("data_panel_tab_pause"), pauseTab);
    }

    private String sigmaMemoryFormat(double value) {
        if (Double.isNaN(value)) return "NaN";
        return sigmaMemoryFormatter.format(value);
    }

    public void setModel(GCModel model) {
        memoryTab.setModel(model);
        pauseTab.setModel(model);
        summaryTab.setModel(model);
        repaint();
    }

    private boolean isSignificant(final double average, final double standardDeviation) {
        // at least 68.3% of all points are within 0.75 to 1.25 times the average value
        // Note: this may or may not be a good measure, but it at least helps to mark some bad data as such
        return average-standardDeviation > 0.75 * average;
    }


    private class MemoryTab extends JPanel {
        private JLabel footprintAfterFullGCValue;
        private JLabel footprintAfterGCValue;
        private JLabel slopeAfterFullGCValue;
        private JLabel slopeAfterGCValue;
        private JLabel freedMemoryByFullGCValue;
        private JLabel avgFreedMemoryByFullGCValue;
        private JLabel freedMemoryByGCValue;
        private JLabel avgFreedMemoryByGCValue;
        private JLabel avgRelativePostGCIncValue;
        private JLabel avgRelativePostFullGCIncValue;
        private JLabel footprintValue;
        private JLabel freedMemoryValue;

        public MemoryTab() {
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.WEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            constraints.insets = new Insets(0, 3, 0, 3);
            constraints.gridy = -1;
            setLayout(layout);

            // footprint
            JLabel footprintLabel = new JLabel(localStrings.getString("data_panel_footprint"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(footprintLabel, constraints);
            add(footprintLabel);
            footprintValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(footprintValue, constraints);
            add(footprintValue);

            // footprint after full gc
            JLabel footprintAfterFullGCLabel = new JLabel(localStrings.getString("data_panel_footprintafterfullgc"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(footprintAfterFullGCLabel, constraints);
            add(footprintAfterFullGCLabel);
            footprintAfterFullGCValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(footprintAfterFullGCValue, constraints);
            add(footprintAfterFullGCValue);

            // footprint after (small) gc
            JLabel footprintAfterGCLabel = new JLabel(localStrings.getString("data_panel_footprintaftergc"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(footprintAfterGCLabel, constraints);
            add(footprintAfterGCLabel);
            footprintAfterGCValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(footprintAfterGCValue, constraints);
            add(footprintAfterGCValue);

            // freed memory
            JLabel freedMemoryLabel = new JLabel(localStrings.getString("data_panel_freedmemory"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(freedMemoryLabel, constraints);
            add(freedMemoryLabel);
            freedMemoryValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(freedMemoryValue, constraints);
            add(freedMemoryValue);

            // memory freed by full gc
            JLabel freedMemoryByFullGCLabel = new JLabel(localStrings.getString("data_panel_freedmemorybyfullgc"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(freedMemoryByFullGCLabel, constraints);
            add(freedMemoryByFullGCLabel);
            freedMemoryByFullGCValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(freedMemoryByFullGCValue, constraints);
            add(freedMemoryByFullGCValue);

            // memory freed by gc
            JLabel freedMemoryByGCLabel = new JLabel(localStrings.getString("data_panel_freedmemorybygc"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(freedMemoryByGCLabel, constraints);
            add(freedMemoryByGCLabel);
            freedMemoryByGCValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(freedMemoryByGCValue, constraints);
            add(freedMemoryByGCValue);

            // avg memory freed by full gc
            JLabel avgFreedMemoryByFullGCLabel = new JLabel(localStrings.getString("data_panel_avgfreedmemorybyfullgc"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(avgFreedMemoryByFullGCLabel, constraints);
            add(avgFreedMemoryByFullGCLabel);
            avgFreedMemoryByFullGCValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(avgFreedMemoryByFullGCValue, constraints);
            add(avgFreedMemoryByFullGCValue);

            // avg memory freed by gc
            JLabel avgFreedMemoryByGCLabel = new JLabel(localStrings.getString("data_panel_avgfreedmemorybygc"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(avgFreedMemoryByGCLabel, constraints);
            add(avgFreedMemoryByGCLabel);
            avgFreedMemoryByGCValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(avgFreedMemoryByGCValue, constraints);
            add(avgFreedMemoryByGCValue);

            // avg increase in memory consumption in comparison to foorprint after the last full GC
            JLabel avgRelativePostFullGCIncLabel = new JLabel(localStrings.getString("data_panel_avgrelativepostfullgcincrease"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(avgRelativePostFullGCIncLabel, constraints);
            add(avgRelativePostFullGCIncLabel);
            avgRelativePostFullGCIncValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(avgRelativePostFullGCIncValue, constraints);
            add(avgRelativePostFullGCIncValue);

            // avg increase in memory consumption in comparison to foorprint after the last GC
            JLabel avgRelativepostGCIncLabel = new JLabel(localStrings.getString("data_panel_avgrelativepostgcincrease"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(avgRelativepostGCIncLabel, constraints);
            add(avgRelativepostGCIncLabel);
            avgRelativePostGCIncValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(avgRelativePostGCIncValue, constraints);
            add(avgRelativePostGCIncValue);

            // slope of footprint after full gc
            JLabel slopeAfterFullGCLabel = new JLabel(localStrings.getString("data_panel_slopeafterfullgc"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(slopeAfterFullGCLabel, constraints);
            add(slopeAfterFullGCLabel);
            slopeAfterFullGCValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(slopeAfterFullGCValue, constraints);
            add(slopeAfterFullGCValue);

            // weighted slope of footprint after (small) gc
            JLabel slopeAfterGCLabel = new JLabel(localStrings.getString("data_panel_slopeaftergc"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(slopeAfterGCLabel, constraints);
            add(slopeAfterGCLabel);
            slopeAfterGCValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(slopeAfterGCValue, constraints);
            add(slopeAfterGCValue);

        }

        public void setModel(GCModel model) {
            footprintValue.setText(footprintFormatter.format(model.getFootprint()));
            // check whether we have full gc data at all
            final boolean fullGCDataVailable = model.getFootprintAfterFullGC().getN() != 0;
            final boolean fullGCSlopeDataVailable = model.getFootprintAfterFullGC().getN() > 1;
            footprintAfterFullGCValue.setEnabled(fullGCDataVailable);
            slopeAfterFullGCValue.setEnabled(fullGCDataVailable);
            freedMemoryByFullGCValue.setEnabled(fullGCDataVailable);
            avgFreedMemoryByFullGCValue.setEnabled(fullGCDataVailable);
            avgRelativePostFullGCIncValue.setEnabled(fullGCDataVailable);
            if (!fullGCDataVailable) {
                footprintAfterFullGCValue.setText("n.a.");
                slopeAfterFullGCValue.setText("n.a.");
                freedMemoryByFullGCValue.setText("n.a.");
                avgFreedMemoryByFullGCValue.setText("n.a.");
            }
            else {
                footprintAfterFullGCValue.setText(footprintFormatter.format(model.getFootprintAfterFullGC().average())
                        + " (\u03c3=" + sigmaMemoryFormat(model.getFootprintAfterFullGC().standardDeviation()) +")");
                footprintAfterFullGCValue.setEnabled(isSignificant(model.getFootprintAfterFullGC().average(),
                        model.getFootprintAfterFullGC().standardDeviation()));
                freedMemoryByFullGCValue.setText(footprintFormatter.format(model.getFreedMemoryByFullGC().getSum())
                        + " (" + percentFormatter.format(model.getFreedMemoryByFullGC().getSum()*100.0/model.getFreedMemory()) + "%)");
                avgFreedMemoryByFullGCValue.setText(footprintFormatter.format(model.getFreedMemoryByFullGC().average())
                        + "/coll (\u03c3=" + sigmaMemoryFormat(model.getFreedMemoryByFullGC().standardDeviation()) + ")");
                avgFreedMemoryByFullGCValue.setEnabled(isSignificant(model.getFreedMemoryByFullGC().average(),
                        model.getFreedMemoryByFullGC().standardDeviation()));
                if (fullGCSlopeDataVailable) {
                    slopeAfterFullGCValue.setText(footprintSlopeFormatter.format(model.getPostFullGCSlope().slope()) + "/s");
                    avgRelativePostFullGCIncValue.setText(footprintSlopeFormatter.format(model.getRelativePostFullGCIncrease().slope()) + "/coll");
                }
                else {
                    slopeAfterFullGCValue.setText("n.a.");
                    avgRelativePostFullGCIncValue.setText("n.a.");
                }
            }
            // check whether we have gc data at all (or only full gc)
            final boolean gcDataAvailable = model.getFootprintAfterGC().getN() != 0;
            footprintAfterGCValue.setEnabled(gcDataAvailable);
            slopeAfterGCValue.setEnabled(gcDataAvailable && fullGCDataVailable);
            freedMemoryByGCValue.setEnabled(gcDataAvailable);
            avgFreedMemoryByGCValue.setEnabled(gcDataAvailable);
            avgRelativePostGCIncValue.setEnabled(gcDataAvailable && fullGCDataVailable);
            if (!gcDataAvailable) {
                footprintAfterGCValue.setText("n.a.");
                slopeAfterGCValue.setText("n.a.");
                freedMemoryByGCValue.setText("n.a.");
                avgFreedMemoryByGCValue.setText("n.a.");
                avgRelativePostGCIncValue.setText("n.a.");
            }
            else {
                footprintAfterGCValue.setText(footprintFormatter.format(model.getFootprintAfterGC().average())
                    + " (\u03c3=" + sigmaMemoryFormat(model.getFootprintAfterGC().standardDeviation()) + ")");
                footprintAfterGCValue.setEnabled(isSignificant(model.getFootprintAfterGC().average(),
                        model.getFootprintAfterGC().standardDeviation()));
                if (fullGCDataVailable && model.getRelativePostGCIncrease().getN() != 0) {
                    slopeAfterGCValue.setText(footprintSlopeFormatter.format(model.getPostGCSlope()) + "/s");
                    avgRelativePostGCIncValue.setText(footprintSlopeFormatter.format(model.getRelativePostGCIncrease().average()) + "/coll");
                }
                else {
                    slopeAfterGCValue.setText("n.a.");
                    avgRelativePostGCIncValue.setText("n.a.");
                    slopeAfterGCValue.setEnabled(false);
                    avgRelativePostGCIncValue.setEnabled(false);
                }
                freedMemoryByGCValue.setText(footprintFormatter.format(model.getFreedMemoryByGC().getSum())
                        + " (" + percentFormatter.format(model.getFreedMemoryByGC().getSum()*100.0/model.getFreedMemory()) + "%)");
                avgFreedMemoryByGCValue.setText(footprintFormatter.format(model.getFreedMemoryByGC().average())
                        + "/coll (\u03c3=" + sigmaMemoryFormat(model.getFreedMemoryByGC().standardDeviation()) + ")");
                avgFreedMemoryByGCValue.setEnabled(isSignificant(model.getFreedMemoryByGC().average(),
                        model.getFreedMemoryByGC().standardDeviation()));
            }
            freedMemoryValue.setText(footprintFormatter.format(model.getFreedMemory()));
        }

    }

    private class PauseTab extends JPanel {
        private JLabel fullGCPauseValue;
        private JLabel gcPauseValue;
        private JLabel avgFullGCPauseValue;
        private JLabel avgGCPauseValue;
        private JLabel avgPauseValue;
        private JLabel minPauseValue;
        private JLabel maxPauseValue;
        private JLabel accumPauseValue;


        public PauseTab() {
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.WEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            constraints.insets = new Insets(0, 3, 0, 3);
            constraints.gridy = -1;
            setLayout(layout);

            JLabel accumPauseLabel = new JLabel(localStrings.getString("data_panel_acc_pauses"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(accumPauseLabel, constraints);
            add(accumPauseLabel);
            accumPauseValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(accumPauseValue, constraints);
            add(accumPauseValue);

            // full gc pauses
            JLabel fullGCPauseLabel = new JLabel(localStrings.getString("data_panel_acc_fullgcpauses"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(fullGCPauseLabel, constraints);
            add(fullGCPauseLabel);
            fullGCPauseValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(fullGCPauseValue, constraints);
            add(fullGCPauseValue);

            // gc pauses
            JLabel gcPauseLabel = new JLabel(localStrings.getString("data_panel_acc_gcpauses"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(gcPauseLabel, constraints);
            add(gcPauseLabel);
            gcPauseValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(gcPauseValue, constraints);
            add(gcPauseValue);

            JLabel minPauseLabel = new JLabel(localStrings.getString("data_panel_min_pause"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(minPauseLabel, constraints);
            add(minPauseLabel);
            minPauseValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(minPauseValue, constraints);
            add(minPauseValue);

            JLabel maxPauseLabel = new JLabel(localStrings.getString("data_panel_max_pause"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(maxPauseLabel, constraints);
            add(maxPauseLabel);
            maxPauseValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(maxPauseValue, constraints);
            add(maxPauseValue);

            JLabel avgPauseLabel = new JLabel(localStrings.getString("data_panel_avg_pause"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(avgPauseLabel, constraints);
            add(avgPauseLabel);
            avgPauseValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(avgPauseValue, constraints);
            add(avgPauseValue);

            JLabel avgFullGCPauseLabel = new JLabel(localStrings.getString("data_panel_avg_fullgcpause"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(avgFullGCPauseLabel, constraints);
            add(avgFullGCPauseLabel);
            avgFullGCPauseValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(avgFullGCPauseValue, constraints);
            add(avgFullGCPauseValue);

            JLabel avgGCPauseLabel = new JLabel(localStrings.getString("data_panel_avg_gcpause"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(avgGCPauseLabel, constraints);
            add(avgGCPauseLabel);
            avgGCPauseValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(avgGCPauseValue, constraints);
            add(avgGCPauseValue);

        }

        public void setModel(GCModel model) {
            final boolean pauseDataAvailable = model.getPause().getN() != 0;
            final boolean gcDataAvailable = model.getGCPause().getN() > 0;
            final boolean fullGCDataAvailable = model.getFullGCPause().getN() > 0;
            avgPauseValue.setEnabled(pauseDataAvailable);
            minPauseValue.setEnabled(pauseDataAvailable);
            maxPauseValue.setEnabled(pauseDataAvailable);
            avgGCPauseValue.setEnabled(pauseDataAvailable);
            avgFullGCPauseValue.setEnabled(pauseDataAvailable);
            if (pauseDataAvailable) {
                avgPauseValue.setEnabled(isSignificant(model.getPause().average(), model.getPause().standardDeviation()));
                avgPauseValue.setText(pauseFormatter.format(model.getPause().average()) + "s (\u03c3=" + pauseFormatter.format(model.getPause().standardDeviation()) +")");
                minPauseValue.setText(pauseFormatter.format(model.getPause().getMin()) + "s");
                maxPauseValue.setText(pauseFormatter.format(model.getPause().getMax()) + "s");

                avgGCPauseValue.setEnabled(gcDataAvailable);
                if (gcDataAvailable) {
                    avgGCPauseValue.setEnabled(isSignificant(model.getGCPause().average(), model.getGCPause().standardDeviation()));
                    avgGCPauseValue.setText(pauseFormatter.format(model.getGCPause().average()) + "s (\u03c3=" + pauseFormatter.format(model.getGCPause().standardDeviation()) +")");
                }
                else {
                    avgGCPauseValue.setText("n.a.");
                }
                avgFullGCPauseValue.setEnabled(fullGCDataAvailable);
                if (fullGCDataAvailable) {
                    avgFullGCPauseValue.setEnabled(isSignificant(model.getFullGCPause().average(), model.getPause().standardDeviation()));
                    avgFullGCPauseValue.setText(pauseFormatter.format(model.getFullGCPause().average()) + "s (\u03c3=" + pauseFormatter.format(model.getFullGCPause().standardDeviation()) +")");
                }
                else {
                    avgFullGCPauseValue.setText("n.a.");
                }
            }
            else {
                avgPauseValue.setText("n.a.");
                minPauseValue.setText("n.a.");
                maxPauseValue.setText("n.a.");
                avgGCPauseValue.setText("n.a.");
                avgFullGCPauseValue.setText("n.a.");
            }
            accumPauseValue.setText(gcTimeFormatter.format(model.getPause().getSum()) + "s");
            fullGCPauseValue.setText(gcTimeFormatter.format(model.getFullGCPause().getSum())+ "s (" + percentFormatter.format(model.getFullGCPause().getSum()*100.0/model.getPause().getSum()) + "%)");
            gcPauseValue.setText(gcTimeFormatter.format(model.getGCPause().getSum())+ "s (" + percentFormatter.format(model.getGCPause().getSum()*100.0/model.getPause().getSum()) + "%)");
        }
    }

    private class SummaryTab extends JPanel {
        private JLabel footprintValue;
        private JLabel accumPauseValue;
        private JLabel throughputValue;
        private JLabel totalTimeValue;
        private JLabel freedMemoryPerMinValue;
        private JLabel freedMemoryValue;
        private JLabel fullGCPerformanceValue;
        private JLabel gcPerformanceValue;


        public SummaryTab() {
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.WEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            constraints.insets = new Insets(0, 3, 0, 3);
            constraints.gridy = -1;
            setLayout(layout);

            // footprint
            JLabel footprintLabel = new JLabel(localStrings.getString("data_panel_footprint"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(footprintLabel, constraints);
            add(footprintLabel);
            footprintValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(footprintValue, constraints);
            add(footprintValue);

            // freed memory
            JLabel freedMemoryLabel = new JLabel(localStrings.getString("data_panel_freedmemory"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(freedMemoryLabel, constraints);
            add(freedMemoryLabel);
            freedMemoryValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(freedMemoryValue, constraints);
            add(freedMemoryValue);

            JLabel freedMemoryPerMinLabel = new JLabel(localStrings.getString("data_panel_freedmemorypermin"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(freedMemoryPerMinLabel, constraints);
            add(freedMemoryPerMinLabel);
            freedMemoryPerMinValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(freedMemoryPerMinValue, constraints);
            add(freedMemoryPerMinValue);

            JLabel totalTimeLabel = new JLabel(localStrings.getString("data_panel_total_time"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(totalTimeLabel, constraints);
            add(totalTimeLabel);
            totalTimeValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(totalTimeValue, constraints);
            add(totalTimeValue);

            JLabel accumPauseLabel = new JLabel(localStrings.getString("data_panel_acc_pauses"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(accumPauseLabel, constraints);
            add(accumPauseLabel);
            accumPauseValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(accumPauseValue, constraints);
            add(accumPauseValue);

            JLabel throughputLabel = new JLabel(localStrings.getString("data_panel_throughput"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(throughputLabel, constraints);
            add(throughputLabel);
            throughputValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(throughputValue, constraints);
            add(throughputValue);

            // fullgc performance
            JLabel fullGCPerformanceLabel = new JLabel(localStrings.getString("data_panel_performance_fullgc"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(fullGCPerformanceLabel, constraints);
            add(fullGCPerformanceLabel);
            fullGCPerformanceValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(fullGCPerformanceValue, constraints);
            add(fullGCPerformanceValue);

            // gc performance
            JLabel gcPerformanceLabel = new JLabel(localStrings.getString("data_panel_performance_gc"));
            constraints.gridy++;
            constraints.gridx = 0;
            layout.setConstraints(gcPerformanceLabel, constraints);
            add(gcPerformanceLabel);
            gcPerformanceValue = new JLabel("", JLabel.RIGHT);
            constraints.gridx = 1;
            layout.setConstraints(gcPerformanceValue, constraints);
            add(gcPerformanceValue);
        }

        public void setModel(GCModel model) {
            accumPauseValue.setText(gcTimeFormatter.format(model.getPause().getSum()) + "s");
            footprintValue.setText(footprintFormatter.format(model.getFootprint()));
            freedMemoryValue.setText(footprintFormatter.format(model.getFreedMemory()));
            if (model.hasCorrectTimestamp()) {
                throughputValue.setText(throughputFormatter.format(model.getThroughput()) + "%");
                totalTimeValue.setText(totalTimeFormatter.format(new Date((long)model.getRunningTime()*1000l)));
                freedMemoryPerMinValue.setText(freedMemoryPerMinFormatter.format(model.getFreedMemory()/model.getRunningTime()*60.0) + "/min");
            } else {
                throughputValue.setText("n.a.");
                totalTimeValue.setText("n.a.");
                freedMemoryPerMinValue.setText("n.a.");
            }
            final boolean gcDataAvailable = model.getGCPause().getN() > 0;
            gcPerformanceValue.setEnabled(gcDataAvailable);
            if (gcDataAvailable) {
                gcPerformanceValue.setText(footprintFormatter.format(model.getFreedMemoryByGC().getSum()/model.getGCPause().getSum()) + "/s");
            }
            else {
                gcPerformanceValue.setText("n.a.");
            }
            final boolean fullGCDataAvailable = model.getFullGCPause().getN() > 0;
            fullGCPerformanceValue.setEnabled(fullGCDataAvailable);
            if (fullGCDataAvailable) {
                fullGCPerformanceValue.setText(footprintFormatter.format(model.getFreedMemoryByFullGC().getSum()/model.getFullGCPause().getSum()) + "/s");
            }
            else {
                fullGCPerformanceValue.setText("n.a.");
            }
        }

    }
}
