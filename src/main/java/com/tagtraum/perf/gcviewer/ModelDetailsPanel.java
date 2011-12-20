package com.tagtraum.perf.gcviewer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.tagtraum.perf.gcviewer.math.DoubleData;
import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Lists detailed information about gc events.
 * 
 * @author <a href="mailto:jwu@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 05.12.2011</p>
 */
public class ModelDetailsPanel extends JPanel {
    private static NumberFormat pauseFormatter;
    private static NumberFormat percentFormatter;
    
    private DoubleDataMapModel gcEventModel;
    private DoubleDataMapModel fullGcEventModel;
    private DoubleDataMapModel concurrentGcEventModel;
    
    public ModelDetailsPanel() {
        super();
        
        this.setLayout(new GridBagLayout());
        
        pauseFormatter = NumberFormat.getInstance();
        pauseFormatter.setMaximumFractionDigits(5);
        pauseFormatter.setMinimumFractionDigits(5);
        
        percentFormatter = NumberFormat.getInstance();
        percentFormatter.setMaximumFractionDigits(1);
        percentFormatter.setMinimumFractionDigits(1);
        
        gcEventModel = new DoubleDataMapModel();
        fullGcEventModel = new DoubleDataMapModel();
        concurrentGcEventModel = new DoubleDataMapModel();
        
        // TODO i18n
        DoubleDataMapTable gcTable = new DoubleDataMapTable("gc events", gcEventModel);
        DoubleDataMapTable fullGcTable = new DoubleDataMapTable("full gc events", fullGcEventModel);
        DoubleDataMapTable concurrentGcTable = new DoubleDataMapTable("concurrent gc events", concurrentGcEventModel);
        
        GridBagConstraints constraints = createGridBagConstraints();
        add(gcTable, constraints);
        
        constraints.gridy++;
        add(fullGcTable, constraints);
        
        constraints.gridy++;
        add(concurrentGcTable, constraints);
        
        // empty panel to fill the rest of the space
        constraints.gridy++;
        constraints.weighty = 1.0;
        add(new JPanel(), constraints);
    }
    
    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0;
        constraints.insets = new Insets(0, 3, 0, 3);
        constraints.gridy = 0;
        constraints.gridx = 0;
        
        return constraints;
    }
    
    /**
     * Sets the model to be displayed.
     */
    public void setModel(GCModel model) {
        gcEventModel.setModel(model.getGcEventPauses(), model.getPause().getSum());
        fullGcEventModel.setModel(model.getFullGcEventPauses(), model.getPause().getSum());
        concurrentGcEventModel.setModel(model.getConcurrentEventPauses(), model.getPause().getSum());
        
        repaint();
    }
    
    /**
     * Displays a {@link DoubleDataMapModel} as a table.
     * 
     * @author <a href="mailto:jwu@gmx.ch">Joerg Wuethrich</a>
     * <p>created on: 20.12.2011</p>
     */
    private static class DoubleDataMapTable extends JPanel implements TableModelListener {

        private String title;
        private List<List<JLabel>> labelGrid = new ArrayList<List<JLabel>>();
        private List<JLabel> titleRow = new ArrayList<JLabel>();
        private DoubleDataMapModel model;
        
        public DoubleDataMapTable(String title, DoubleDataMapModel model) {
            super();
            
            this.title = title;
            this.setModel(model);
            this.setLayout(new GridBagLayout());
            this.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(title),
                    BorderFactory.createEmptyBorder(0,0,0,0)));
        }
        
        /**
         * Sets the new model and makes sure, that its data will be displayed.
         * @param model data to be displayed
         */
        public void setModel(DoubleDataMapModel model) {
            if (model == null) {
                throw new IllegalArgumentException("Cannot set a null TableModel");
            }
            
            if (this.model != model) {
                TableModel old = this.model;
                if (old != null) {
                    old.removeTableModelListener(this);
                }
                this.model = model;
                model.addTableModelListener(this);

            }

            checkStructure(model);
            updateValues(model);
        }
        
        /**
         * Makes sure that size of model fits to size of currently displayed data. If it doesn't
         * fit, display is rebuilt.
         * @param model model to be displayed.
         */
        private void checkStructure(DoubleDataMapModel model) {
            if (labelGrid.size() != model.getRowCount()) {
                this.removeAll();
                
                titleRow = createTitleRow(model);
                addTitleRowToPanel(titleRow);
                
                labelGrid = createLabelGrid(model);
                addLabelGridToPanel(labelGrid);
            }
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
        
        /**
         * Adds all titleRow labels to the panel.
         */
        private void addTitleRowToPanel(List<JLabel> titleLabels) {
            GridBagConstraints constraints = createGridBagConstraints();
            constraints.weightx = 1.0;
            for (JLabel label : titleLabels) {
                add(label, constraints);
                constraints.weightx = 0;
                constraints.gridx++;
            }
        }
        
        /**
         * Creates the <code>titleRow</code>-labels.
         * @param model data to be displayed
         */
        private List<JLabel> createTitleRow(DoubleDataMapModel model) {
            List<JLabel> titleRow = new ArrayList<JLabel>();
            for (int i = 0; i < model.getColumnCount(); ++i) {
                titleRow.add(new JLabel(model.getColumnName(i), JLabel.RIGHT));
            }
            
            return titleRow;
        }

        /**
         * Adds all labels of the grid to the panel.
         */
        private void addLabelGridToPanel(List<List<JLabel>> labelGrid) {
            GridBagConstraints constraints = createGridBagConstraints();
            for (List<JLabel> labelRow : labelGrid) {
                constraints.gridy++;
                constraints.gridx = 0;
                // first label size is flexible
                constraints.weightx = 1.0;
                for (JLabel label : labelRow) {
                    add(label, constraints);
                    // the other columns not
                    constraints.weightx = 0;
                    constraints.gridx++;
                }
            }
        }
        
        /**
         * Updates all values in the <code>labelGrid</code>.
         * @param model source for the values
         */
        private void updateValues(DoubleDataMapModel model) {
            for (int rowIndex = 0; rowIndex < model.getRowCount(); ++rowIndex) {
                List<JLabel> labelRow = labelGrid.get(rowIndex);
                for (int columnIndex = 0; columnIndex < model.getColumnCount(); ++columnIndex) {
                    labelRow.get(columnIndex).setText((String)model.getValueAt(rowIndex, columnIndex));
                }
            }
        }

        /**
         * creates the <code>labelGrid</code>-labels.
         * @param model data to be displayed
         */
        private List<List<JLabel>> createLabelGrid(DoubleDataMapModel model) {
            List<List<JLabel>> labelGrid = new ArrayList<List<JLabel>>();
            for (int rowIndex = 0; rowIndex < model.getRowCount(); ++rowIndex) {
                List<JLabel> labelRow = new ArrayList<JLabel>();
                labelGrid.add(labelRow);
                for (int columnIndex = 0; columnIndex < model.getColumnCount(); ++columnIndex) {
                    labelRow.add(new JLabel((String)model.getValueAt(rowIndex, columnIndex), JLabel.RIGHT));
                }
            }
            
            return labelGrid;
        }

        /**
         * Updates the display to reflect changes in the model.
         * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
         */
        @Override
        public void tableChanged(TableModelEvent e) {
            if (e.getSource() instanceof DoubleDataMapModel) {
                setModel((DoubleDataMapModel)e.getSource());
            }
        }
        
    }
    
    /**
     * This is a model holding the following structure for display:
     * <code>Map&lt;String, List&lt;{@link DoubleData}&gt;&gt;</code>.
     * 
     * @author <a href="mailto:jwu@gmx.ch">Joerg Wuethrich</a>
     * <p>created on: 20.12.2011</p>
     */
    private static class DoubleDataMapModel extends AbstractTableModel {

        private List<String> columnNames;
        private List<List<String>> data = new ArrayList<List<String>>();
        private double totalPause;
        
        public DoubleDataMapModel() {
            super();
            
            this.columnNames = createColumnNamesList();
        }
        
        private List<String> createColumnNamesList() {
            List<String> columnNames = new ArrayList<String>();
            
            // TODO i18n
            columnNames.add("name");
            columnNames.add("n");
            columnNames.add("min (s)");
            columnNames.add("max (s)");
            columnNames.add("avg (s)");
            columnNames.add("stddev");
            columnNames.add("sum (s)");
            columnNames.add("sum (%)");
            
            return columnNames;
        }
        
        public void setModel(Map<String, DoubleData> model, double totalPause) {
            this.data = createDataList(model, totalPause);
            this.totalPause = totalPause;
            fireTableDataChanged();
        }
        
        private double getTotalSum(Set<Entry<String, DoubleData>> entrySet) {
            double totalSum = 0;
            for (Entry<String, DoubleData> entry : entrySet) {
                totalSum += entry.getValue().getSum();
            }
            
            return totalSum;
        }
        
        private List<List<String>> createDataList(Map<String, DoubleData> model, double totalPause) {
            double totalSum = getTotalSum(model.entrySet());
            int totalNumber = 0;
            
            List<List<String>> dataList = new ArrayList<List<String>>();
            for (Entry<String, DoubleData> entry : model.entrySet()) {
                List<String> entryList = new ArrayList<String>();
                entryList.add(entry.getKey());
                entryList.add(entry.getValue().getN() + "");
                entryList.add(pauseFormatter.format(entry.getValue().getMin()));
                entryList.add(pauseFormatter.format(entry.getValue().getMax()));
                entryList.add(pauseFormatter.format(entry.getValue().average()));
                entryList.add(pauseFormatter.format(entry.getValue().standardDeviation()));
                entryList.add(pauseFormatter.format(entry.getValue().getSum()));
                entryList.add(percentFormatter.format(entry.getValue().getSum() / totalSum * 100));
                
                dataList.add(entryList);
                
                totalNumber += entry.getValue().getN();
            }
            
            List<String> totalList = new ArrayList<String>();
            totalList.add("total");
            totalList.add(totalNumber + "");
            totalList.add("");
            totalList.add("");
            totalList.add("");
            totalList.add("");
            totalList.add(pauseFormatter.format(totalSum));
            totalList.add(percentFormatter.format(totalSum / totalPause * 100));
            
            dataList.add(totalList);
            
            return dataList;
        }
        
        /**
         * @see javax.swing.table.TableModel#getRowCount()
         */
        @Override
        public int getRowCount() {
            return data.size();
        }

        /**
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        @Override
        public int getColumnCount() {
            return columnNames.size();
        }
        
        /**
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Number.class;
        }
        
        /**
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column) {
            String name = null; 
            if (column < columnNames.size() && (column >= 0)) {  
                name = columnNames.get(column); 
            }
            
            return (name == null) ? super.getColumnName(column) : name;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data.get(rowIndex).get(columnIndex);
        }
        
    }
}
