package com.tagtraum.perf.gcviewer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.tagtraum.perf.gcviewer.math.DoubleData;
import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Lists detailed information about gc log.
 * 
 * @author <a href="mailto:jwu@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 05.12.2011</p>
 */
public class ModelDetailsPanel extends JPanel {
    private NumberFormat pauseFormatter;
    private NumberFormat percentFormatter;
    
    public ModelDetailsPanel() {
        super();
        
        this.setLayout(new GridBagLayout());
        
        pauseFormatter = NumberFormat.getInstance();
        pauseFormatter.setMaximumFractionDigits(5);
        pauseFormatter.setMinimumFractionDigits(5);
        
        percentFormatter = NumberFormat.getInstance();
        percentFormatter.setMaximumFractionDigits(1);
        percentFormatter.setMinimumFractionDigits(1);
    }
    
    private double addRowEntries(JPanel panel, 
            Set<Entry<String, DoubleData>> entrySet, 
            double runningTime, 
            GridBagConstraints constraints) {
        
        double totalSum = getTotalSum(entrySet);
        for (Entry<String, DoubleData> entry : entrySet) {
            constraints.gridy++;
            constraints.gridx = 0;
            constraints.weightx = 1.0;
            addRowEntry(panel, new JLabel(entry.getKey()), constraints);
            constraints.weightx = 0;
            addRowEntry(panel, new JLabel(entry.getValue().getN() + "", JLabel.RIGHT), constraints);
            addRowEntry(panel, new JLabel(pauseFormatter.format(entry.getValue().getMin()), JLabel.RIGHT), constraints);
            addRowEntry(panel, new JLabel(pauseFormatter.format(entry.getValue().getMax()), JLabel.RIGHT), constraints);
            addRowEntry(panel, new JLabel(pauseFormatter.format(entry.getValue().average()), JLabel.RIGHT), constraints);
            addRowEntry(panel, new JLabel(pauseFormatter.format(entry.getValue().standardDeviation()), JLabel.RIGHT), constraints);
            addRowEntry(panel, new JLabel(pauseFormatter.format(entry.getValue().getSum()), JLabel.RIGHT), constraints);
            addRowEntry(panel, new JLabel(percentFormatter.format(entry.getValue().getSum() / totalSum * 100), JLabel.RIGHT), constraints);
        }
        
        return totalSum;
    }

    private void addRowEntry(JPanel panel, JLabel label, GridBagConstraints constraints) {
        constraints.gridx++;
        
        panel.add(label, constraints);
    }
    
    private void addTitleRow(JPanel panel, GridBagConstraints constraints) {
        constraints.weightx = 1.0;
        addRowEntry(panel, new JLabel("name"), constraints);

        constraints.weightx = 0;
        addRowEntry(panel, new JLabel("n", JLabel.RIGHT), constraints);
        addRowEntry(panel, new JLabel("min (s)", JLabel.RIGHT), constraints);
        addRowEntry(panel, new JLabel("max (s)", JLabel.RIGHT), constraints);
        addRowEntry(panel, new JLabel("avg (s)", JLabel.RIGHT), constraints);
        addRowEntry(panel, new JLabel("stddev", JLabel.RIGHT), constraints);
        addRowEntry(panel, new JLabel("sum (s)", JLabel.RIGHT), constraints);
        addRowEntry(panel, new JLabel("sum (%)", JLabel.RIGHT), constraints);
    }
    
    private void addTotalRow(JPanel panel, double totalSum, double totalPause, GridBagConstraints constraints) {
        constraints.gridx = 1;
        constraints.gridy++;
        constraints.weightx = 1.0;
        panel.add(new JLabel("total", JLabel.RIGHT), constraints);

        constraints.weightx = 0;
        
        constraints.gridx = 7;
        panel.add(new JLabel(pauseFormatter.format(totalSum), JLabel.RIGHT), constraints);
        
        constraints.gridx = 8;
        panel.add(new JLabel(percentFormatter.format(totalSum / totalPause * 100), JLabel.RIGHT), constraints);
    }
    
    private JPanel createEntryGroupPanel(
            String groupTitle, 
            Set<Entry<String, DoubleData>> entrySet, 
            double totalPause,
            boolean showTotal) {
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(groupTitle),
                BorderFactory.createEmptyBorder(0,0,0,0)));
        
        GridBagConstraints constraints = createGridBagConstraints();
        
        addTitleRow(panel, constraints);
        double totalSum = addRowEntries(panel, entrySet, totalPause, constraints);
        
        if (showTotal) {
            addTotalRow(panel, totalSum, totalPause, constraints);
        }
        
        return panel;
    }
    
    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0;
        constraints.insets = new Insets(0, 3, 0, 3);
        constraints.gridy = 0;
        constraints.gridx = 0;
        
        return constraints;
    }
    
    private double getTotalSum(Set<Entry<String, DoubleData>> entrySet) {
        double totalSum = 0;
        for (Entry<String, DoubleData> entry : entrySet) {
            totalSum += entry.getValue().getSum();
        }
        
        return totalSum;
    }
    
    private void refreshContent(GCModel model) {
        JPanel gcEventPausesPanel = createEntryGroupPanel(
                "gc events", 
                model.getGcEventPauses().entrySet(), 
                model.getPause().getSum(),
                true);
        JPanel fullGcEventPausesPanel = createEntryGroupPanel(
                "full gc events", 
                model.getFullGcEventPauses().entrySet(), 
                model.getPause().getSum(),
                true);
        JPanel concurrentEventPausesPanel = createEntryGroupPanel(
                "concurrent gc events", 
                model.getConcurrentEventPauses().entrySet(), 
                model.getPause().getSum(),
                false);
        
        GridBagConstraints constraints = createGridBagConstraints();
        add(gcEventPausesPanel, constraints);
        constraints.gridy++;
        add(fullGcEventPausesPanel, constraints);
        constraints.gridy++;
        add(concurrentEventPausesPanel, constraints);
        
        // fill rest of panel
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridy++;
        constraints.weightx = 3;
        constraints.weighty = 3;
        add(new JPanel(), constraints);
    }
    
    public void setModel(GCModel model) {
        refreshContent(model);
        repaint();
    }
    
}
