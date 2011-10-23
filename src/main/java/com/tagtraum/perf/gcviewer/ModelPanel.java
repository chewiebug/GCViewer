package com.tagtraum.perf.gcviewer;

import java.awt.GridLayout;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.tagtraum.perf.gcviewer.util.MemoryFormat;
import com.tagtraum.perf.gcviewer.util.TimeFormat;

/**
 * Panel that contains characteristic data about the gc file.
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

    private class ValuesTab extends JPanel {
    	private int rowCount = 0;
    	private JPanel currentPanel;
    	private GridLayout currentLayout;
    	
    	public ValuesTab() {
    		super();
    		
    		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    		
    	}
    	
    	public void newGroup(String name, boolean withBorder) {
    		currentPanel = new JPanel();
    		if (withBorder) {
	    		currentPanel.setBorder(BorderFactory.createCompoundBorder(
	                    BorderFactory.createTitledBorder(name),
	                    BorderFactory.createEmptyBorder(0,0,0,0)));
    		}
    		
    		currentLayout = new GridLayout(0, 2, 0, 0);
    		
    		currentPanel.setLayout(currentLayout);
    		
    		add(currentPanel);
    		
    		rowCount = 0;
    	}
    	
    	public void addValue(String name, String value, boolean enabled) {
    		if (currentPanel == null) {
    			newGroup("", false);
    		}
    		
    		currentLayout.setRows(++rowCount);
    		
    		currentPanel.add(new JLabel(name));
    		
    		JLabel valueLabel = new JLabel(value, JLabel.RIGHT);
    		valueLabel.setEnabled(enabled);
    		currentPanel.add(valueLabel);
    	}
    	
    }
    
    private class MemoryTab extends ValuesTab {

        public void setModel(GCModel model) {
            final boolean fullGCDataVailable = model.getFootprintAfterFullGC().getN() != 0;
            final boolean fullGCSlopeDataAvailable = model.getFootprintAfterFullGC().getN() > 1;
            final boolean gcDataAvailable = model.getFootprintAfterGC().getN() != 0;
            final boolean gcSlopeDataAvailable = model.getRelativePostGCIncrease().getN() != 0;

            addValue(localStrings.getString("data_panel_footprint"),
        			footprintFormatter.format(model.getFootprint()), 
        			true);
        	addValue(localStrings.getString("data_panel_footprintafterfullgc"),
        			fullGCDataVailable ? footprintFormatter.format(model.getFootprintAfterFullGC().average())
                            + " (\u03c3=" + sigmaMemoryFormat(model.getFootprintAfterFullGC().standardDeviation()) +")" : "n/a",
                    fullGCDataVailable && isSignificant(model.getFootprintAfterFullGC().average(),
                            model.getFootprintAfterFullGC().standardDeviation()));
        	addValue(localStrings.getString("data_panel_footprintaftergc"),
        			gcDataAvailable ? footprintFormatter.format(model.getFootprintAfterGC().average())
                            + " (\u03c3=" + sigmaMemoryFormat(model.getFootprintAfterGC().standardDeviation()) + ")" : "n/a",
                    gcDataAvailable && isSignificant(model.getFootprintAfterGC().average(),
                                    model.getFootprintAfterGC().standardDeviation()));
        	
        	addValue(localStrings.getString("data_panel_freedmemorybygc"),
        			footprintFormatter.format(model.getFreedMemory()),
        			true);
        	addValue(localStrings.getString("data_panel_freedmemorybyfullgc"),
        			fullGCDataVailable ? footprintFormatter.format(model.getFreedMemoryByFullGC().getSum())
                            + " (" + percentFormatter.format(model.getFreedMemoryByFullGC().getSum()*100.0/model.getFreedMemory()) + "%)" : "n/a",
                    fullGCDataVailable);
        	addValue(localStrings.getString("data_panel_freedmemorybygc"),
        			gcDataAvailable ? footprintFormatter.format(model.getFreedMemoryByGC().getSum())
                            + " (" + percentFormatter.format(model.getFreedMemoryByGC().getSum()*100.0/model.getFreedMemory()) + "%)" : "n/a",
                    gcDataAvailable);
        	
        	addValue(localStrings.getString("data_panel_avgfreedmemorybyfullgc"),
        			fullGCDataVailable ? footprintFormatter.format(model.getFreedMemoryByFullGC().average())
                            + "/coll (\u03c3=" + sigmaMemoryFormat(model.getFreedMemoryByFullGC().standardDeviation()) + ")" : "n/a",
                    fullGCDataVailable && isSignificant(model.getFreedMemoryByFullGC().average(),
                            model.getFreedMemoryByFullGC().standardDeviation()));
        	addValue(localStrings.getString("data_panel_avgfreedmemorybygc"),
        			gcDataAvailable ? footprintFormatter.format(model.getFreedMemoryByGC().average())
                            + "/coll (\u03c3=" + sigmaMemoryFormat(model.getFreedMemoryByGC().standardDeviation()) + ")" : "n/a",
                    gcDataAvailable && isSignificant(model.getFreedMemoryByGC().average(),
                            model.getFreedMemoryByGC().standardDeviation()));
        	
        	addValue(localStrings.getString("data_panel_avgrelativepostfullgcincrease"),
        			fullGCSlopeDataAvailable ? footprintSlopeFormatter.format(model.getRelativePostFullGCIncrease().slope()) + "/coll" : "n/a",
        			fullGCSlopeDataAvailable);
        	addValue(localStrings.getString("data_panel_avgrelativepostgcincrease"),
        			gcSlopeDataAvailable ? footprintSlopeFormatter.format(model.getRelativePostGCIncrease().average()) + "/coll" : "n/a",
        			gcSlopeDataAvailable);
        	
        	addValue(localStrings.getString("data_panel_slopeafterfullgc"),
        			fullGCSlopeDataAvailable ? footprintSlopeFormatter.format(model.getPostFullGCSlope().slope()) + "/s" : "n/a",
        			fullGCSlopeDataAvailable);
        	addValue(localStrings.getString("data_panel_slopeaftergc"),
        			gcSlopeDataAvailable ? footprintSlopeFormatter.format(model.getPostGCSlope()) + "/s" : "n/a",
        			gcSlopeDataAvailable);
        }
    }

    private class PauseTab extends ValuesTab {

        public void setModel(GCModel model) {
            final boolean pauseDataAvailable = model.getPause().getN() != 0;
            final boolean gcDataAvailable = model.getGCPause().getN() > 0;
            final boolean fullGCDataAvailable = model.getFullGCPause().getN() > 0;
            
            newGroup(localStrings.getString("data_panel_group_total_pause"), true);
            addValue(localStrings.getString("data_panel_acc_pauses"), 
            		gcTimeFormatter.format(model.getPause().getSum()) + "s", 
            		true);
            addValue(localStrings.getString("data_panel_count_pauses"), 
            		Integer.toString(model.getPause().getN()), 
            		true);
            addValue(localStrings.getString("data_panel_avg_pause"), 
            		pauseDataAvailable ? pauseFormatter.format(model.getPause().average()) + "s (\u03c3=" + pauseFormatter.format(model.getPause().standardDeviation()) +")" : "n/a", 
            		pauseDataAvailable ? isSignificant(model.getPause().average(), model.getPause().standardDeviation()) : false);
            addValue(localStrings.getString("data_panel_min_pause"), 
            		pauseDataAvailable ? pauseFormatter.format(model.getPause().getMin()) + "s" : "n/a", 
            		pauseDataAvailable);
            addValue(localStrings.getString("data_panel_max_pause"), 
            		pauseDataAvailable ? pauseFormatter.format(model.getPause().getMax()) + "s" : "n/a", 
            		pauseDataAvailable);

            newGroup(localStrings.getString("data_panel_group_full_gc_pauses"), true);
            addValue(localStrings.getString("data_panel_acc_fullgcpauses"), 
            		gcTimeFormatter.format(model.getFullGCPause().getSum())+ "s (" + percentFormatter.format(model.getFullGCPause().getSum()*100.0/model.getPause().getSum()) + "%)", 
            		true);
            addValue(localStrings.getString("data_panel_count_full_gc_pauses"), 
            		Integer.toString(model.getFullGCPause().getN()), 
            		true);
            addValue(localStrings.getString("data_panel_avg_fullgcpause"), 
            		fullGCDataAvailable ? pauseFormatter.format(model.getFullGCPause().average()) + "s (\u03c3=" + pauseFormatter.format(model.getFullGCPause().standardDeviation()) +")" : "n/a", 
            		fullGCDataAvailable ? isSignificant(model.getFullGCPause().average(), model.getPause().standardDeviation()) : false);
            addValue(localStrings.getString("data_panel_min_full_gc_pause"), 
            		fullGCDataAvailable ? pauseFormatter.format(model.getFullGCPause().getMin()) + "s" : "n/a", 
            		fullGCDataAvailable);
            addValue(localStrings.getString("data_panel_max_full_gc_pause"), 
            		fullGCDataAvailable ? pauseFormatter.format(model.getFullGCPause().getMax()) + "s" : "n/a", 
            		fullGCDataAvailable);

            newGroup(localStrings.getString("data_panel_group_gc_pauses"), true);
            addValue(localStrings.getString("data_panel_acc_gcpauses"), 
            		gcTimeFormatter.format(model.getGCPause().getSum())+ "s (" + percentFormatter.format(model.getGCPause().getSum()*100.0/model.getPause().getSum()) + "%)", 
            		true);
            addValue(localStrings.getString("data_panel_count_gc_pauses"), 
            		Integer.toString(model.getGCPause().getN()), 
            		true);
            addValue(localStrings.getString("data_panel_avg_gcpause"), 
            		gcDataAvailable ? pauseFormatter.format(model.getGCPause().average()) + "s (\u03c3=" + pauseFormatter.format(model.getGCPause().standardDeviation()) +")" : "n/a", 
            		gcDataAvailable ? isSignificant(model.getGCPause().average(), model.getGCPause().standardDeviation()) : false);
            addValue(localStrings.getString("data_panel_min_gc_pause"), 
            		gcDataAvailable ? pauseFormatter.format(model.getGCPause().getMin()) + "s" : "n/a", 
            		gcDataAvailable);
            addValue(localStrings.getString("data_panel_max_gc_pause"), 
            		gcDataAvailable ? pauseFormatter.format(model.getGCPause().getMax()) + "s" : "n/a", 
            		gcDataAvailable);
            
        }
    }

    private class SummaryTab extends ValuesTab {

        public void setModel(GCModel model) {
        	addValue(localStrings.getString("data_panel_footprint"),
        			footprintFormatter.format(model.getFootprint()), 
        			true);
        	addValue(localStrings.getString("data_panel_freedmemory"),
        			footprintFormatter.format(model.getFreedMemory()),
        			true);
        	addValue(localStrings.getString("data_panel_freedmemorypermin"),
        			freedMemoryPerMinFormatter.format(model.getFreedMemory()/model.getRunningTime()*60.0) + "/min",
        			true);
        	addValue(localStrings.getString("data_panel_total_time"),
        			model.hasCorrectTimestamp() ? totalTimeFormatter.format(new Date((long)model.getRunningTime()*1000l)) : "n/a",
        			model.hasCorrectTimestamp());
        	addValue(localStrings.getString("data_panel_acc_pauses"),
        			gcTimeFormatter.format(model.getPause().getSum()) + "s",
        			true);
        	addValue(localStrings.getString("data_panel_throughput"),
        			model.hasCorrectTimestamp() ? throughputFormatter.format(model.getThroughput()) + "%" : "n/a",
        			model.hasCorrectTimestamp());
        	addValue(localStrings.getString("data_panel_performance_fullgc"),
        			model.getFullGCPause().getN() > 0  
        					? footprintFormatter.format(model.getFreedMemoryByFullGC().getSum()/model.getFullGCPause().getSum()) + "/s"
        					: "n/a",
        			model.getFullGCPause().getN() > 0);
        	addValue(localStrings.getString("data_panel_performance_gc"),
        			model.getGCPause().getN() > 0 
        				? footprintFormatter.format(model.getFreedMemoryByGC().getSum()/model.getGCPause().getSum()) + "/s"
        				: "n/a",
        			model.getGCPause().getN() > 0);
        }

    }
}
