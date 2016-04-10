package com.tagtraum.perf.gcviewer.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.util.ExtensionFileFilter;

/**
 * View class to display "open file" gui element.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 04.01.2014</p>
 */
public class OpenFileView extends JFileChooser {

    private JCheckBox addFileCheckBox;

    public OpenFileView() {
        setDialogTitle(LocalisationHelper.getString("fileopen_dialog_title"));
        setMultiSelectionEnabled(true);
        for (ExtensionFileFilter filter: ExtensionFileFilter.EXT_FILE_FILTERS) {
            addChoosableFileFilter(filter);
        }
        
        addFileCheckBox = new JCheckBox(LocalisationHelper.getString("fileopen_dialog_add_checkbox"), false);
        addFileCheckBox.setVerticalTextPosition(SwingConstants.TOP);
        addFileCheckBox.setToolTipText(LocalisationHelper.getString("fileopen_dialog_hint_add_checkbox"));

        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 2;
        panel.add(addFileCheckBox, gridBagConstraints);
        
        setAccessory(panel);
    }
    
    public boolean isAddCheckBoxSelected() {
        return addFileCheckBox.isSelected();
    }
    
    /**
     * OpenFileView shows an accessory checkbox to let the user choose "add selected file(s) to 
     * current document".
     * 
     * @param showCheckBox <code>true</code>: checkbox is shown, otherwise it is hidden.
     */
    public void setShowAddCheckBox(boolean showCheckBox) {
        addFileCheckBox.setVisible(showCheckBox);
        addFileCheckBox.setEnabled(showCheckBox);
        if (!showCheckBox) {
            // Checkbox must never be selected, when it is not visible.
            // Can happen if last file was added and whole document is closed afterwards.
            // -> State of checkbox still "selected" but not visible any more.
            addFileCheckBox.setSelected(false);
        }
    }
}
