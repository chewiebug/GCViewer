package com.tagtraum.perf.gcviewer.view;

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
import com.tagtraum.perf.gcviewer.math.DoubleDataPercentile;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;

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
    private DoubleDataMapModel vmOperationEventModel;
    private DoubleDataMapModel concurrentGcEventModel;

    private DoubleDataMapTable vmOperationTable;
    
    private DoubleDataMapModel gcPhasesModel;
    private DoubleDataMapTable gcPhasesTable;

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
        vmOperationEventModel = new DoubleDataMapModel();
        concurrentGcEventModel = new DoubleDataMapModel();
        gcPhasesModel = new DoubleDataMapModel();

        DoubleDataMapTable gcTable = new DoubleDataMapTable(LocalisationHelper.getString("data_panel_group_gc_pauses"), gcEventModel);
        DoubleDataMapTable fullGcTable = new DoubleDataMapTable(LocalisationHelper.getString("data_panel_group_full_gc_pauses"), fullGcEventModel);
        vmOperationTable = new DoubleDataMapTable(LocalisationHelper.getString("data_panel_vm_op_overhead"), vmOperationEventModel);
        DoubleDataMapTable concurrentGcTable = new DoubleDataMapTable(LocalisationHelper.getString("data_panel_group_concurrent_gc_events"), concurrentGcEventModel);
        gcPhasesTable = new DoubleDataMapTable(LocalisationHelper.getString("data_panel_group_gc_phases"), gcPhasesModel);

        GridBagConstraints constraints = createGridBagConstraints();
        add(gcTable, constraints);
        
        constraints.gridy++;
        add(gcPhasesTable, constraints);

        constraints.gridy++;
        add(fullGcTable, constraints);

        constraints.gridy++;
        add(vmOperationTable, constraints);

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
     * @param model The model to set.
     */
    public void setModel(GCModel model) {
        double totalPause = model.getPause().getSum();
        gcEventModel.setModel(model.getGcEventPauses(), totalPause, true);
        fullGcEventModel.setModel(model.getFullGcEventPauses(), totalPause, true);
        if (model.size() > 1 && model.getVmOperationPause().getN() == 0) {
            remove(vmOperationTable);
        }
        else {
            vmOperationEventModel.setModel(model.getVmOperationEventPauses(), totalPause, true);
        }
        concurrentGcEventModel.setModel(model.getConcurrentEventPauses(), totalPause, false);

        if (model.size() > 1 && model.getGcEventPhases().size() == 0) {
            remove(gcPhasesTable);
        }
        else {
            gcPhasesModel.setModel(model.getGcEventPhases(), totalPause, false);
        }

        repaint();
    }

    /**
     * Displays a {@link DoubleDataMapModel} as a table.
     *
     * @author <a href="mailto:jwu@gmx.ch">Joerg Wuethrich</a>
     * <p>created on: 20.12.2011</p>
     */
    private static class DoubleDataMapTable extends JPanel implements TableModelListener {

        private List<List<JLabel>> labelGrid = new ArrayList<List<JLabel>>();
        private List<JLabel> titleRow = new ArrayList<JLabel>();
        private DoubleDataMapModel model;

        /**
         * Create a table with the <code>title</code> and the <code>model</code> to display.
         *
         * @param title Title to be used for this table.
         * @param model data to be displayed
         */
        public DoubleDataMapTable(String title, DoubleDataMapModel model) {
            super();

            this.setModel(model);
            this.setLayout(new GridBagLayout());
            this.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(title),
                    BorderFactory.createEmptyBorder(0,0,0,0)));
        }

        /**
         * Sets the new model and makes sure, that its data will be displayed. This table
         * listens to data changes in the model and updates if necessary.
         *
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

        /**
         * Create a standard set of GridBagConstraints.
         */
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
         * Creates the <code>titleRow</code>-labels (but doesn't add them to the table).
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
         * Adds all labels of the grid to the panel (builds the table).
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

        /**
         * Create the model.
         */
        public DoubleDataMapModel() {
            super();

            this.columnNames = createColumnNamesList();
        }

        /**
         * Creates a list of names for the columns.
         */
        private List<String> createColumnNamesList() {
            List<String> columnNames = new ArrayList<String>();

            columnNames.add(LocalisationHelper.getString("data_panel_details_name"));
            columnNames.add(LocalisationHelper.getString("data_panel_details_count"));
            columnNames.add(LocalisationHelper.getString("data_panel_details_min"));
            columnNames.add(LocalisationHelper.getString("data_panel_details_max"));
            columnNames.add(LocalisationHelper.getString("data_panel_details_avg"));
            columnNames.add(LocalisationHelper.getString("data_panel_details_stddev"));
            columnNames.add("median");
            columnNames.add("75th");
            columnNames.add("95th");
            columnNames.add("99th");
            columnNames.add("99.5th");
            columnNames.add("99.9th");
            columnNames.add(LocalisationHelper.getString("data_panel_details_sum"));
            columnNames.add(LocalisationHelper.getString("data_panel_details_sum_percent"));

            return columnNames;
        }

        /**
         * Sets the data for the model.
         *
         * @param model data of the model formatted as map containing DoubleData-Objects identified with strings.
         * @param totalPause total pause that occurred in this run
         * @param showPercentOfTotalPause flag to indicate if the total pause in the DoubleData Object
         * should be added as percentage of <code>totalPause</code> in the total line.
         */
        public void setModel(Map<String, DoubleData> model, double totalPause, boolean showPercentOfTotalPause) {
            this.data = createDataList(model, totalPause, showPercentOfTotalPause);
            fireTableDataChanged();
        }

        /**
         * Calculates the total sum of the <code>DoubleData</code> objects in the set.
         * @param entrySet data
         * @return total sum of all <code>DoubleData</code> objects.
         */
        private double getTotalSum(Set<Entry<String, DoubleData>> entrySet) {
            double totalSum = 0;
            for (Entry<String, DoubleData> entry : entrySet) {
                totalSum += entry.getValue().getSum();
            }

            return totalSum;
        }

        /**
         * Creates the data list (sets a string in each "cell" of the grid).
         *
         * @param model data to be displayed
         * @param totalPause total pause of this gc file
         * @param showPercentOfTotalPause flag to indicate if the total pause in the DoubleData Object
         * should be added as percentage of <code>totalPause</code> in the total line.
         * @return Datastructure containing all values as a "grid".
         *
         * @see #setModel(Map, double, boolean)
         */
        private List<List<String>> createDataList(Map<String, DoubleData> model, double totalPause, boolean showPercentOfTotalPause) {
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
                entryList.add(pauseFormatter.format(((DoubleDataPercentile)entry.getValue()).getPercentile(50)));
                entryList.add(pauseFormatter.format(((DoubleDataPercentile)entry.getValue()).getPercentile(75)));
                entryList.add(pauseFormatter.format(((DoubleDataPercentile)entry.getValue()).getPercentile(95)));
                entryList.add(pauseFormatter.format(((DoubleDataPercentile)entry.getValue()).getPercentile(99)));
                entryList.add(pauseFormatter.format(((DoubleDataPercentile)entry.getValue()).getPercentile(99.5)));
                entryList.add(pauseFormatter.format(((DoubleDataPercentile)entry.getValue()).getPercentile(99.9)));
                entryList.add(pauseFormatter.format(entry.getValue().getSum()));
                entryList.add(percentFormatter.format(entry.getValue().getSum() / totalSum * 100));

                dataList.add(entryList);

                totalNumber += entry.getValue().getN();
            }

            List<String> totalList = new ArrayList<String>();
            totalList.add(LocalisationHelper.getString("data_panel_details_total"));
            totalList.add(totalNumber + "");
            totalList.add("");
            totalList.add("");
            totalList.add("");
            totalList.add("");
            totalList.add("");
            totalList.add("");
            totalList.add("");
            totalList.add("");
            totalList.add("");
            totalList.add("");
            totalList.add(pauseFormatter.format(totalSum));
            totalList.add(showPercentOfTotalPause ? percentFormatter.format(totalSum / totalPause * 100) : "");

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

        /**
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data.get(rowIndex).get(columnIndex);
        }

    }
}
