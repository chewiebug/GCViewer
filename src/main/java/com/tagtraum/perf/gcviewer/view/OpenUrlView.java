package com.tagtraum.perf.gcviewer.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.model.RecentGCResourcesModel;

/**
 * View class to display "open Url" dialog.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 04.01.2014</p>
 */
public class OpenUrlView {

    private JFrame parent;
    private JPanel panel;
    private AutoCompletionComboBox autoCompletionComboBox;
    private JCheckBox addUrlCheckBox;

    /**
     * Constructor taking a "parent" frame as parameter.
     * @param parent parent frame for this dialog
     */
    public OpenUrlView(JFrame parent) {
        super();
        
        this.parent = parent;
        autoCompletionComboBox = new AutoCompletionComboBox();
        
        panel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        panel.add(autoCompletionComboBox, gridBagConstraints);
        gridBagConstraints.gridy = 1;

        addUrlCheckBox = new JCheckBox(LocalisationHelper.getString("urlopen_dialog_add_checkbox"), false);
        addUrlCheckBox.setToolTipText(LocalisationHelper.getString("urlopen_dialog_hint_add_checkbox"));
        panel.add(addUrlCheckBox, gridBagConstraints);

    }
    
    /**
     * Get selected value from internal comboBox.
     * @return selected value
     */
    public String getSelectedItem() {
        return (String) autoCompletionComboBox.getSelectedItem();
    }

    public boolean isAddCheckBoxSelected() {
        return addUrlCheckBox.isSelected();
    }
    
    /**
     * OpenUrlView shows a checkbox to let the user choose "add selected URL to 
     * current document".
     * 
     * @param showCheckBox <code>true</code>: checkbox is shown, otherwise it is hidden.
     */
    public void setShowAddCheckBox(boolean showCheckBox) {
        
        addUrlCheckBox.setVisible(showCheckBox);
        addUrlCheckBox.setEnabled(showCheckBox);
        if (!showCheckBox) {
            // Checkbox must never be selected, when it is not visible.
            // Can happen if last file was added and whole document is closed afterwards.
            // -> State of checkbox still "selected" but not visible any more.
            addUrlCheckBox.setSelected(false);
        }
    }
    
    public void setRecentResourceNamesModel(RecentGCResourcesModel model) {
        autoCompletionComboBox.setRecentResourceNamesModel(model);
    }
    
    /**
     * Shows open Url dialog.
     * @return <code>true</code> if "OK" was pressed, <code>false</code> otherwise.
     */
    public boolean showDialog() {
        return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
                                            parent, 
                                            panel, 
                                            LocalisationHelper.getString("urlopen_dialog_title"), 
                                            JOptionPane.OK_CANCEL_OPTION, 
                                            JOptionPane.PLAIN_MESSAGE);
    }
}
