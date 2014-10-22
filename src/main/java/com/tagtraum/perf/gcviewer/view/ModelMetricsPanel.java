package com.tagtraum.perf.gcviewer.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.util.MemoryFormat;
import com.tagtraum.perf.gcviewer.util.TimeFormat;

/**
 * Panel that contains characteristic data about the gc file.
 *
 * Date: Feb 3, 2002
 * Time: 9:58:00 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ModelMetricsPanel extends JTabbedPane {

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
    private MemoryFormat promotionFormatter;
    
    private SummaryTab summaryTab;
    private MemoryTab memoryTab;
    private PauseTab pauseTab;


    public ModelMetricsPanel() {
        //setBorder(new TitledBorder(LocalisationHelper.getString("data_panel_title")));
        pauseFormatter = NumberFormat.getInstance();
        pauseFormatter.setMaximumFractionDigits(5);

        totalTimeFormatter = new TimeFormat();

        gcTimeFormatter = NumberFormat.getInstance();
        gcTimeFormatter.setMaximumFractionDigits(2);

        throughputFormatter = NumberFormat.getInstance();
        throughputFormatter.setMaximumFractionDigits(2);

        footprintFormatter = new MemoryFormat();
        footprintFormatter.setMaximumFractionDigits(1);

        sigmaFormatter = NumberFormat.getInstance();
        sigmaFormatter.setMaximumFractionDigits(0);

        sigmaMemoryFormatter = new MemoryFormat();

        footprintSlopeFormatter = new MemoryFormat();

        freedMemoryPerMinFormatter = new MemoryFormat();
        
        promotionFormatter = new MemoryFormat();

        percentFormatter = NumberFormat.getInstance();
        percentFormatter.setMaximumFractionDigits(1);
        percentFormatter.setMinimumFractionDigits(1);

        summaryTab = new SummaryTab();
        addTab(LocalisationHelper.getString("data_panel_tab_summary"), summaryTab);
        memoryTab = new MemoryTab();
        addTab(LocalisationHelper.getString("data_panel_tab_memory"), memoryTab);
        pauseTab = new PauseTab();
        addTab(LocalisationHelper.getString("data_panel_tab_pause"), pauseTab);
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

    /**
     * A simple container class to hold two labels ("name" and "value"). 
     * 
     * @author <a href="mailto:jwu@gmx.ch">Joerg Wuethrich</a>
     * <p>created on: 10.11.2011</p>
     */
    private class TwoLabelsContainer {
        private JLabel nameLabel;
        private JLabel valueLabel;
        
        public TwoLabelsContainer(JLabel nameLabel, JLabel valueLabel) {
            super();
            
            this.nameLabel = nameLabel;
            this.valueLabel = valueLabel;
        }
        
        public void updateValue(String value) {
            valueLabel.setText(value);
        }
        
        public void setEnabled(boolean enabled) {
            nameLabel.setEnabled(enabled);
            valueLabel.setEnabled(enabled);
        }
    }
    
    private class ValuesTab extends JPanel {
    	private JPanel currentPanel;
    	private GridBagLayout currentLayout;
    	private GridBagConstraints currentConstraints;
    	private Map<String, TwoLabelsContainer> labelMap;
    	
    	public ValuesTab() {
    		super();
    		
    		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    		labelMap = new HashMap<String, TwoLabelsContainer>();
    	}
    	
    	public void newGroup(String name, boolean withBorder) {
    		currentPanel = new JPanel();
    		if (withBorder) {
	    		currentPanel.setBorder(BorderFactory.createCompoundBorder(
	                    BorderFactory.createTitledBorder(name),
	                    BorderFactory.createEmptyBorder(0,0,0,0)));
    		}
    		
    		// using GridBagLayout because it allows for 2 columns with different widths
    		currentLayout = new GridBagLayout();
    		
            currentConstraints = new GridBagConstraints();
            currentConstraints.anchor = GridBagConstraints.WEST;
            currentConstraints.fill = GridBagConstraints.HORIZONTAL;
            currentConstraints.weightx = 1.0;
            currentConstraints.weighty = 1.0;
            currentConstraints.insets = new Insets(0, 3, 0, 3);
            currentConstraints.gridy = -1;
    		
    		currentPanel.setLayout(currentLayout);
    		
    		add(currentPanel);
    	}

    	public void addEntry(String name) {
    		if (currentPanel == null) {
    			newGroup("", false);
    		}
    		
            JLabel nameLabel = new JLabel(name);

            currentConstraints.gridy++;
            currentConstraints.gridx = 0;
            currentLayout.setConstraints(nameLabel, currentConstraints);
            currentPanel.add(nameLabel);
            
            JLabel valueLabel = new JLabel("", JLabel.RIGHT);

            currentConstraints.gridx = 1;
            currentLayout.setConstraints(valueLabel, currentConstraints);
            currentPanel.add(valueLabel);
            
            labelMap.put(name, new TwoLabelsContainer(nameLabel, valueLabel));
    	}
    	
    	public void removeEntry(String name) {
    	    TwoLabelsContainer labelContainer = labelMap.get(name);
    	    assert labelContainer != null : "labelContainer for '" + name + "' is null -> was it registered?";
    	    assert labelContainer.nameLabel != null : "Name label for '" + name + "' is null.";
    	    assert labelContainer.nameLabel.getParent() != null : "Parent for name label for '" + name + "' is null.";
    	    labelContainer.nameLabel.getParent().remove(labelContainer.nameLabel);
    	    assert labelContainer.valueLabel != null : "Value label for '" + name + "' is null.";
    	    assert labelContainer.valueLabel.getParent() != null : "Parent for value label for '" + name + "' is null.";
    	    labelContainer.valueLabel.getParent().remove(labelContainer.valueLabel);
    	}
    	
    	public void updateValue(String name, String value, boolean enabled) {
    	    TwoLabelsContainer labelContainer = labelMap.get(name);
            assert labelContainer != null : "labelContainer for '" + name + "' is null -> was it registered?";
	        labelContainer.updateValue(value);
	        labelContainer.setEnabled(enabled);
    	}
    	
    }
    
    private class MemoryTab extends ValuesTab {

        public MemoryTab() {
            super();
            
            addEntry(LocalisationHelper.getString("data_panel_memory_heap_usage"));
            addEntry(LocalisationHelper.getString("data_panel_memory_tenured_heap_usage"));
            addEntry(LocalisationHelper.getString("data_panel_memory_young_heap_usage"));
            addEntry(LocalisationHelper.getString("data_panel_memory_perm_heap_usage"));
            addEntry(LocalisationHelper.getString("data_panel_tenuredafterconcgc_max"));
            addEntry(LocalisationHelper.getString("data_panel_tenuredafterconcgc_avg"));
            addEntry(LocalisationHelper.getString("data_panel_footprintafterconcgc_max"));
            addEntry(LocalisationHelper.getString("data_panel_footprintafterconcgc_avg"));
            addEntry(LocalisationHelper.getString("data_panel_footprintafterfullgc_max"));
            addEntry(LocalisationHelper.getString("data_panel_footprintafterfullgc_avg"));
            addEntry(LocalisationHelper.getString("data_panel_footprintaftergc_avg"));
            
            addEntry(LocalisationHelper.getString("data_panel_freedmemorybyfullgc"));
            addEntry(LocalisationHelper.getString("data_panel_freedmemorybygc"));
            
            addEntry(LocalisationHelper.getString("data_panel_avgfreedmemorybyfullgc"));
            addEntry(LocalisationHelper.getString("data_panel_avgfreedmemorybygc"));
            
            addEntry(LocalisationHelper.getString("data_panel_avgrelativepostfullgcincrease"));
            addEntry(LocalisationHelper.getString("data_panel_avgrelativepostgcincrease"));
            
            addEntry(LocalisationHelper.getString("data_panel_slopeafterfullgc"));
            addEntry(LocalisationHelper.getString("data_panel_slopeaftergc"));
            
            addEntry(LocalisationHelper.getString("data_panel_memory_initiatingoccupancyfraction"));
            
            addEntry(LocalisationHelper.getString("data_panel_memory_promotion_avg"));
            addEntry(LocalisationHelper.getString("data_panel_memory_promotion_total"));
       }
        
        public void setModel(GCModel model) {
            boolean fullGcDataAvailable = model.getFootprintAfterFullGC().getN() != 0;
            boolean fullGcSlopeDataAvailable = model.getFootprintAfterFullGC().getN() > 1;
            boolean gcDataAvailable = model.getFootprintAfterGC().getN() != 0;
            boolean gcSlopeDataAvailable = model.getRelativePostGCIncrease().getN() != 0;
            boolean initiatingOccFractionAvailable = model.getCmsInitiatingOccupancyFraction().getN() > 0;
            boolean promotionAvailable = model.getPromotion().getN() > 0;
            boolean postConcurrentUsedSizeAvailable = model.getPostConcurrentCycleHeapUsedSizes().getN() > 0;
            boolean postConcurrentUsedTenuredSizeAvailable = model.getPostConcurrentCycleTenuredUsedSizes().getN() > 0;

            updateValue(LocalisationHelper.getString("data_panel_memory_heap_usage"),
                    footprintFormatter.format(model.getHeapUsedSizes().getMax()) 
                        + " (" + percentFormatter.format(model.getHeapUsedSizes().getMax() / (double)model.getHeapAllocatedSizes().getMax() * 100) + "%)"
                        + " / " + footprintFormatter.format(model.getHeapAllocatedSizes().getMax()),
                    true);
            updateValue(LocalisationHelper.getString("data_panel_memory_tenured_heap_usage"),
                    model.getTenuredAllocatedSizes().getN() > 0 ? footprintFormatter.format(model.getTenuredUsedSizes().getMax()) 
                        + " (" + percentFormatter.format(model.getTenuredUsedSizes().getMax() / (double)model.getTenuredAllocatedSizes().getMax() * 100) + "%)"
                        + " / " + footprintFormatter.format(model.getTenuredAllocatedSizes().getMax()) : "n/a",
                    model.getTenuredAllocatedSizes().getN() > 0);
            updateValue(LocalisationHelper.getString("data_panel_memory_young_heap_usage"),
                    model.getYoungUsedSizes().getN() > 0 ? footprintFormatter.format(model.getYoungUsedSizes().getMax()) 
                        + " (" + percentFormatter.format(model.getYoungUsedSizes().getMax() / (double)model.getYoungAllocatedSizes().getMax() * 100) + "%)"
                        + " / " + footprintFormatter.format(model.getYoungAllocatedSizes().getMax()) : "n/a",
                    model.getYoungAllocatedSizes().getN() > 0);
            updateValue(LocalisationHelper.getString("data_panel_memory_perm_heap_usage"),
                    model.getPermUsedSizes().getN() > 0 ? footprintFormatter.format(model.getPermUsedSizes().getMax()) 
                            + " (" + percentFormatter.format(model.getPermUsedSizes().getMax() / (double)model.getPermAllocatedSizes().getMax() * 100) + "%)"
                            + " / " + footprintFormatter.format(model.getPermAllocatedSizes().getMax()) : "n/a",
                    model.getPermAllocatedSizes().getN() > 0);
            updateValue(LocalisationHelper.getString("data_panel_footprintafterconcgc_max"),
                    postConcurrentUsedSizeAvailable ? footprintFormatter.format(model.getPostConcurrentCycleHeapUsedSizes().getMax())
                            + " (" + percentFormatter.format(model.getPostConcurrentCycleHeapUsedSizes().getMax() / (double)model.getFootprint() * 100) + "%)": "n/a",
                    postConcurrentUsedSizeAvailable);
            updateValue(LocalisationHelper.getString("data_panel_footprintafterconcgc_avg"),
                    postConcurrentUsedSizeAvailable ? footprintFormatter.format(model.getPostConcurrentCycleHeapUsedSizes().average())
                            + " (\u03c3=" + sigmaMemoryFormat(model.getPostConcurrentCycleHeapUsedSizes().standardDeviation()) +")" : "n/a",
                            postConcurrentUsedSizeAvailable && isSignificant(model.getPostConcurrentCycleHeapUsedSizes().average(),
                            model.getPostConcurrentCycleHeapUsedSizes().standardDeviation()));
            updateValue(LocalisationHelper.getString("data_panel_tenuredafterconcgc_max"),
                    postConcurrentUsedTenuredSizeAvailable ? footprintFormatter.format(model.getPostConcurrentCycleTenuredUsedSizes().getMax())
                            + " (" + percentFormatter.format(model.getPostConcurrentCycleTenuredUsedSizes().getMax() / (double)model.getTenuredAllocatedSizes().getMax() * 100) + "% / "
                                   + percentFormatter.format(model.getPostConcurrentCycleTenuredUsedSizes().getMax() / (double)model.getFootprint() * 100) + "%)": "n/a",
                    postConcurrentUsedTenuredSizeAvailable);
            updateValue(LocalisationHelper.getString("data_panel_tenuredafterconcgc_avg"),
                    postConcurrentUsedTenuredSizeAvailable ? footprintFormatter.format(model.getPostConcurrentCycleTenuredUsedSizes().average())
                            + " (\u03c3=" + sigmaMemoryFormat(model.getPostConcurrentCycleTenuredUsedSizes().standardDeviation()) +")" : "n/a",
                            postConcurrentUsedTenuredSizeAvailable && isSignificant(model.getPostConcurrentCycleTenuredUsedSizes().average(),
                            model.getPostConcurrentCycleTenuredUsedSizes().standardDeviation()));
            updateValue(LocalisationHelper.getString("data_panel_footprintafterfullgc_max"),
                    fullGcDataAvailable ? footprintFormatter.format(model.getFootprintAfterFullGC().getMax())
                            + " (" + percentFormatter.format(model.getFootprintAfterFullGC().getMax() / (double)model.getFootprint() * 100) + "%)": "n/a",
                    fullGcDataAvailable);
            updateValue(LocalisationHelper.getString("data_panel_footprintafterfullgc_avg"),
        			fullGcDataAvailable ? footprintFormatter.format(model.getFootprintAfterFullGC().average())
                            + " (\u03c3=" + sigmaMemoryFormat(model.getFootprintAfterFullGC().standardDeviation()) +")" : "n/a",
                    fullGcDataAvailable && isSignificant(model.getFootprintAfterFullGC().average(),
                            model.getFootprintAfterFullGC().standardDeviation()));
            updateValue(LocalisationHelper.getString("data_panel_footprintaftergc_avg"),
        			gcDataAvailable ? footprintFormatter.format(model.getFootprintAfterGC().average())
                            + " (\u03c3=" + sigmaMemoryFormat(model.getFootprintAfterGC().standardDeviation()) + ")" : "n/a",
                    gcDataAvailable && isSignificant(model.getFootprintAfterGC().average(),
                                    model.getFootprintAfterGC().standardDeviation()));
        	
            updateValue(LocalisationHelper.getString("data_panel_freedmemorybyfullgc"),
        			fullGcDataAvailable ? footprintFormatter.format(model.getFreedMemoryByFullGC().getSum())
                            + " (" + percentFormatter.format(model.getFreedMemoryByFullGC().getSum()*100.0/model.getFreedMemory()) + "%)" : "n/a",
                    fullGcDataAvailable);
            updateValue(LocalisationHelper.getString("data_panel_freedmemorybygc"),
        			gcDataAvailable ? footprintFormatter.format(model.getFreedMemoryByGC().getSum())
                            + " (" + percentFormatter.format(model.getFreedMemoryByGC().getSum()*100.0/model.getFreedMemory()) + "%)" : "n/a",
                    gcDataAvailable);
        	
            updateValue(LocalisationHelper.getString("data_panel_avgfreedmemorybyfullgc"),
        			fullGcDataAvailable ? footprintFormatter.format(model.getFreedMemoryByFullGC().average())
                            + "/coll (\u03c3=" + sigmaMemoryFormat(model.getFreedMemoryByFullGC().standardDeviation()) + ")" : "n/a",
                    fullGcDataAvailable && isSignificant(model.getFreedMemoryByFullGC().average(),
                            model.getFreedMemoryByFullGC().standardDeviation()));
            updateValue(LocalisationHelper.getString("data_panel_avgfreedmemorybygc"),
        			gcDataAvailable ? footprintFormatter.format(model.getFreedMemoryByGC().average())
                            + "/coll (\u03c3=" + sigmaMemoryFormat(model.getFreedMemoryByGC().standardDeviation()) + ")" : "n/a",
                    gcDataAvailable && isSignificant(model.getFreedMemoryByGC().average(),
                            model.getFreedMemoryByGC().standardDeviation()));
        	
            updateValue(LocalisationHelper.getString("data_panel_avgrelativepostfullgcincrease"),
        			fullGcSlopeDataAvailable ? footprintSlopeFormatter.format(model.getRelativePostFullGCIncrease().slope()) + "/coll" : "n/a",
        			fullGcSlopeDataAvailable);
            updateValue(LocalisationHelper.getString("data_panel_avgrelativepostgcincrease"),
        			gcSlopeDataAvailable ? footprintSlopeFormatter.format(model.getRelativePostGCIncrease().average()) + "/coll" : "n/a",
        			gcSlopeDataAvailable);
        	
            updateValue(LocalisationHelper.getString("data_panel_slopeafterfullgc"),
        			fullGcSlopeDataAvailable ? footprintSlopeFormatter.format(model.getPostFullGCSlope().slope()) + "/s" : "n/a",
        			fullGcSlopeDataAvailable);
            updateValue(LocalisationHelper.getString("data_panel_slopeaftergc"),
        			gcSlopeDataAvailable ? footprintSlopeFormatter.format(model.getPostGCSlope()) + "/s" : "n/a",
        			gcSlopeDataAvailable);
        	
            updateValue(LocalisationHelper.getString("data_panel_memory_initiatingoccupancyfraction"),
        	        initiatingOccFractionAvailable ? 
        	                percentFormatter.format(model.getCmsInitiatingOccupancyFraction().average()*100) + "%"
        	                + " / " + percentFormatter.format(model.getCmsInitiatingOccupancyFraction().getMax()*100) + "%": "n/a",
        	        initiatingOccFractionAvailable);

            updateValue(LocalisationHelper.getString("data_panel_memory_promotion_avg"),
                    promotionAvailable ? promotionFormatter.format(model.getPromotion().average()) + "/coll"+
                            " (\u03c3=" + sigmaMemoryFormat(model.getPromotion().standardDeviation()) + ")" : "n/a",
                    promotionAvailable && isSignificant(model.getPromotion().average(),
                            model.getPromotion().standardDeviation()));
            updateValue(LocalisationHelper.getString("data_panel_memory_promotion_total"),
                    promotionAvailable ? promotionFormatter.format(model.getPromotion().getSum()) : "n/a",
                    promotionAvailable);
        }
    }

    private class PauseTab extends ValuesTab {
        private boolean hasOverheadEntry;
        public PauseTab() {
            super();
            
            newGroup(LocalisationHelper.getString("data_panel_group_total_pause"), true);
            addEntry(LocalisationHelper.getString("data_panel_acc_pauses"));
            addEntry(LocalisationHelper.getString("data_panel_count_pauses"));
            addEntry(LocalisationHelper.getString("data_panel_avg_pause"));
            addEntry(LocalisationHelper.getString("data_panel_min_max_pause"));
            addEntry(LocalisationHelper.getString("data_panel_avg_pause_interval"));
            addEntry(LocalisationHelper.getString("data_panel_min_max_pause_interval"));
            addEntry(LocalisationHelper.getString("data_panel_vm_op_overhead"));
            hasOverheadEntry = true;
            
            newGroup(LocalisationHelper.getString("data_panel_group_full_gc_pauses"), true);
            addEntry(LocalisationHelper.getString("data_panel_acc_fullgcpauses"));
            addEntry(LocalisationHelper.getString("data_panel_count_full_gc_pauses"));
            addEntry(LocalisationHelper.getString("data_panel_avg_fullgcpause"));
            addEntry(LocalisationHelper.getString("data_panel_min_max_full_gc_pause"));

            newGroup(LocalisationHelper.getString("data_panel_group_gc_pauses"), true);
            addEntry(LocalisationHelper.getString("data_panel_acc_gcpauses"));
            addEntry(LocalisationHelper.getString("data_panel_count_gc_pauses"));
            addEntry(LocalisationHelper.getString("data_panel_avg_gcpause"));
            addEntry(LocalisationHelper.getString("data_panel_min_max_gc_pause"));
        }
        
        public void setModel(GCModel model) {
            boolean pauseDataAvailable = model.getPause().getN() > 0;
            boolean gcDataAvailable = model.getGCPause().getN() > 0;
            boolean fullGCDataAvailable = model.getFullGCPause().getN() > 0;
            boolean pauseIntervalDataAvailable = model.getPauseInterval().getN() > 0; // fix for "Exception if only one GC log line in file"
            boolean vmOperationsAvailable = model.getVmOperationPause().getN() > 0;
            
            updateValue(LocalisationHelper.getString("data_panel_acc_pauses"), 
            		gcTimeFormatter.format(model.getPause().getSum()) + "s", 
            		true);
            updateValue(LocalisationHelper.getString("data_panel_count_pauses"), 
            		Integer.toString(model.getPause().getN()), 
            		true);
            updateValue(LocalisationHelper.getString("data_panel_avg_pause"), 
            		pauseDataAvailable ? pauseFormatter.format(model.getPause().average()) + "s (\u03c3=" + pauseFormatter.format(model.getPause().standardDeviation()) +")" : "n/a", 
            		pauseDataAvailable ? isSignificant(model.getPause().average(), model.getPause().standardDeviation()) : false);
            updateValue(LocalisationHelper.getString("data_panel_min_max_pause"), 
            		pauseDataAvailable ? pauseFormatter.format(model.getPause().getMin()) + "s / " +pauseFormatter.format(model.getPause().getMax()) + "s" : "n/a", 
            		pauseDataAvailable);
            updateValue(LocalisationHelper.getString("data_panel_avg_pause_interval"), 
                    pauseIntervalDataAvailable ? pauseFormatter.format(model.getPauseInterval().average()) + "s (\u03c3=" + pauseFormatter.format(model.getPauseInterval().standardDeviation()) +")" : "n/a", 
                    pauseIntervalDataAvailable ? isSignificant(model.getPauseInterval().average(), model.getPauseInterval().standardDeviation()) : false);
            updateValue(LocalisationHelper.getString("data_panel_min_max_pause_interval"), 
                    pauseIntervalDataAvailable ? pauseFormatter.format(model.getPauseInterval().getMin()) + "s / " +pauseFormatter.format(model.getPauseInterval().getMax()) + "s" : "n/a", 
                    pauseIntervalDataAvailable);
            if (vmOperationsAvailable) {
                if (!hasOverheadEntry) {
                    addEntry(LocalisationHelper.getString("data_panel_vm_op_overhead"));
                    hasOverheadEntry = true;
                }
                updateValue(LocalisationHelper.getString("data_panel_vm_op_overhead"), 
                        gcTimeFormatter.format(model.getVmOperationPause().getSum())+ "s (" + percentFormatter.format(model.getVmOperationPause().getSum()*100.0 / model.getPause().getSum()) + "%)",
                        true);
            }
            else if (hasOverheadEntry) {
                removeEntry(LocalisationHelper.getString("data_panel_vm_op_overhead"));
                hasOverheadEntry = false;
            }

            updateValue(LocalisationHelper.getString("data_panel_acc_fullgcpauses"), 
            		gcTimeFormatter.format(model.getFullGCPause().getSum())+ "s (" + percentFormatter.format(model.getFullGCPause().getSum()*100.0/model.getPause().getSum()) + "%)", 
            		true);
            updateValue(LocalisationHelper.getString("data_panel_count_full_gc_pauses"), 
            		Integer.toString(model.getFullGCPause().getN()), 
            		true);
            updateValue(LocalisationHelper.getString("data_panel_avg_fullgcpause"), 
            		fullGCDataAvailable ? pauseFormatter.format(model.getFullGCPause().average()) + "s (\u03c3=" + pauseFormatter.format(model.getFullGCPause().standardDeviation()) +")" : "n/a", 
            		fullGCDataAvailable ? isSignificant(model.getFullGCPause().average(), model.getFullGCPause().standardDeviation()) : false);
            updateValue(LocalisationHelper.getString("data_panel_min_max_full_gc_pause"), 
            		fullGCDataAvailable ? pauseFormatter.format(model.getFullGCPause().getMin()) + "s / " + pauseFormatter.format(model.getFullGCPause().getMax()) + "s" : "n/a", 
            		fullGCDataAvailable);

            updateValue(LocalisationHelper.getString("data_panel_acc_gcpauses"), 
            		gcTimeFormatter.format(model.getGCPause().getSum())+ "s (" + percentFormatter.format(model.getGCPause().getSum()*100.0/model.getPause().getSum()) + "%)", 
            		true);
            updateValue(LocalisationHelper.getString("data_panel_count_gc_pauses"), 
            		Integer.toString(model.getGCPause().getN()), 
            		true);
            updateValue(LocalisationHelper.getString("data_panel_avg_gcpause"), 
            		gcDataAvailable ? pauseFormatter.format(model.getGCPause().average()) + "s (\u03c3=" + pauseFormatter.format(model.getGCPause().standardDeviation()) +")" : "n/a", 
            		gcDataAvailable ? isSignificant(model.getGCPause().average(), model.getGCPause().standardDeviation()) : false);
            updateValue(LocalisationHelper.getString("data_panel_min_max_gc_pause"), 
            		gcDataAvailable ? pauseFormatter.format(model.getGCPause().getMin()) + "s / " + pauseFormatter.format(model.getGCPause().getMax()) + "s": "n/a", 
            		gcDataAvailable);
            
        }
    }

    private class SummaryTab extends ValuesTab {

        public SummaryTab() {
            super();
            
            addEntry(LocalisationHelper.getString("data_panel_memory_heap_usage"));
            addEntry(LocalisationHelper.getString("data_panel_footprintafterconcgc_max"));
            addEntry(LocalisationHelper.getString("data_panel_tenuredafterconcgc_max"));
            addEntry(LocalisationHelper.getString("data_panel_footprintafterfullgc_max"));
            addEntry(LocalisationHelper.getString("data_panel_freedmemory"));
            addEntry(LocalisationHelper.getString("data_panel_freedmemorypermin"));
            addEntry(LocalisationHelper.getString("data_panel_total_time"));
            addEntry(LocalisationHelper.getString("data_panel_acc_pauses"));
            addEntry(LocalisationHelper.getString("data_panel_throughput"));
            addEntry(LocalisationHelper.getString("data_panel_count_full_gc_pauses"));
            addEntry(LocalisationHelper.getString("data_panel_performance_fullgc"));
            addEntry(LocalisationHelper.getString("data_panel_count_gc_pauses"));
            addEntry(LocalisationHelper.getString("data_panel_performance_gc"));
        }
        
        public void setModel(GCModel model) {
            boolean fullGcDataAvailable = model.getFootprintAfterFullGC().getN() > 0;
            boolean postConcurrentUsedSizeAvailable = model.getPostConcurrentCycleHeapUsedSizes().getN() > 0;
            boolean postConcurrentUsedTenuredSizeAvailable = model.getPostConcurrentCycleTenuredUsedSizes().getN() > 0;
                    
            updateValue(LocalisationHelper.getString("data_panel_memory_heap_usage"),
                    footprintFormatter.format(model.getHeapUsedSizes().getMax()) 
                        + " (" + percentFormatter.format(model.getHeapUsedSizes().getMax() / (double)model.getHeapAllocatedSizes().getMax() * 100) + "%)"
                        + " / " + footprintFormatter.format(model.getHeapAllocatedSizes().getMax()),
                    true);
            updateValue(LocalisationHelper.getString("data_panel_footprintafterconcgc_max"),
                    postConcurrentUsedSizeAvailable ? footprintFormatter.format(model.getPostConcurrentCycleHeapUsedSizes().getMax())
                            + " (" + percentFormatter.format(model.getPostConcurrentCycleHeapUsedSizes().getMax() / (double)model.getFootprint() * 100) + "%)": "n/a",
                    postConcurrentUsedSizeAvailable);
            updateValue(LocalisationHelper.getString("data_panel_tenuredafterconcgc_max"),
                    postConcurrentUsedTenuredSizeAvailable ? footprintFormatter.format(model.getPostConcurrentCycleTenuredUsedSizes().getMax())
                            + " (" + percentFormatter.format(model.getPostConcurrentCycleTenuredUsedSizes().getMax() / (double)model.getTenuredAllocatedSizes().getMax() * 100) + "% / "
                                   + percentFormatter.format(model.getPostConcurrentCycleTenuredUsedSizes().getMax() / (double)model.getFootprint() * 100) + "%)": "n/a",
                    postConcurrentUsedTenuredSizeAvailable);
            updateValue(LocalisationHelper.getString("data_panel_footprintafterfullgc_max"),
                    fullGcDataAvailable ? footprintFormatter.format(model.getFootprintAfterFullGC().getMax())
                            + " (" + percentFormatter.format(model.getFootprintAfterFullGC().getMax() / (double)model.getFootprint() * 100) + "%)": "n/a",
                    fullGcDataAvailable);
        	updateValue(LocalisationHelper.getString("data_panel_freedmemory"),
        			footprintFormatter.format(model.getFreedMemory()),
        			true);
        	updateValue(LocalisationHelper.getString("data_panel_freedmemorypermin"),
        			freedMemoryPerMinFormatter.format(model.getFreedMemory()/model.getRunningTime()*60.0) + "/min",
        			true);
        	updateValue(LocalisationHelper.getString("data_panel_total_time"),
        			model.hasCorrectTimestamp() ? totalTimeFormatter.format(new Date((long)model.getRunningTime()*1000l)) : "n/a",
        			model.hasCorrectTimestamp());
        	updateValue(LocalisationHelper.getString("data_panel_acc_pauses"),
        			gcTimeFormatter.format(model.getPause().getSum()) + "s",
        			true);
        	updateValue(LocalisationHelper.getString("data_panel_throughput"),
        			model.hasCorrectTimestamp() ? throughputFormatter.format(model.getThroughput()) + "%" : "n/a",
        			model.hasCorrectTimestamp());
            updateValue(LocalisationHelper.getString("data_panel_count_full_gc_pauses"), 
                    Integer.toString(model.getFullGCPause().getN()), 
                    true);
        	updateValue(LocalisationHelper.getString("data_panel_performance_fullgc"),
        			model.getFullGCPause().getN() > 0  
        					? footprintFormatter.format(model.getFreedMemoryByFullGC().getSum()/model.getFullGCPause().getSum()) + "/s"
        					: "n/a",
        			model.getFullGCPause().getN() > 0);
            updateValue(LocalisationHelper.getString("data_panel_count_gc_pauses"), 
                    Integer.toString(model.getGCPause().getN()), 
                    true);
        	updateValue(LocalisationHelper.getString("data_panel_performance_gc"),
        			model.getGCPause().getN() > 0 
        				? footprintFormatter.format(model.getFreedMemoryByGC().getSum()/model.getGCPause().getSum()) + "/s"
        				: "n/a",
        			model.getGCPause().getN() > 0);
        }

    }
}
