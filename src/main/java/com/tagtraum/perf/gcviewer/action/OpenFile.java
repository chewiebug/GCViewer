package com.tagtraum.perf.gcviewer.action;

import com.tagtraum.perf.gcviewer.GCViewer;
import com.tagtraum.perf.gcviewer.util.ExtensionFileFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 1:48:26 PM
 *
 */
public class OpenFile extends AbstractAction {
    private GCViewer gcViewer;
    private JFileChooser openDialog;
    private File[] lastSelectedFiles = new File[0];
    private JCheckBox addURLCheckBox;


    public OpenFile(final GCViewer gcViewer) {
        this.gcViewer = gcViewer;
        putValue(NAME, GCViewer.localStrings.getString("main_frame_menuitem_open_file"));
        putValue(SHORT_DESCRIPTION, GCViewer.localStrings.getString("main_frame_menuitem_hint_open_file"));
        putValue(MNEMONIC_KEY, new Integer(GCViewer.localStrings.getString("main_frame_menuitem_mnemonic_open_file").charAt(0)));
        putValue(ACTION_COMMAND_KEY, "open");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('O', Event.CTRL_MASK ));
        putValue(SMALL_ICON, new ImageIcon(Toolkit.getDefaultToolkit().getImage(gcViewer.getClass().getResource("images/open.png"))));
        openDialog = new JFileChooser();
        openDialog.setDialogTitle(GCViewer.localStrings.getString("fileopen_dialog_title"));
        openDialog.setMultiSelectionEnabled(true);
        openDialog.addChoosableFileFilter(ExtensionFileFilter.GcExtensionFilter);
        openDialog.addChoosableFileFilter(ExtensionFileFilter.TxtExtensionFilter);
        openDialog.addChoosableFileFilter(ExtensionFileFilter.LogExtensionFilter);
        addURLCheckBox = new JCheckBox(GCViewer.localStrings.getString("fileopen_dialog_add_checkbox"), false);
        addURLCheckBox.setVerticalTextPosition(SwingConstants.TOP);
        addURLCheckBox.setToolTipText(GCViewer.localStrings.getString("fileopen_dialog_hint_add_checkbox"));

        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 2;
        panel.add(addURLCheckBox, gridBagConstraints);
        openDialog.setAccessory(panel);
    }

    public void actionPerformed(final ActionEvent e) {
        final boolean aDocumentIsAlreadyOpen = gcViewer.getSelectedGCDocument() != null;
        addURLCheckBox.setVisible(aDocumentIsAlreadyOpen);
        addURLCheckBox.setEnabled(aDocumentIsAlreadyOpen);
        final int val = openDialog.showOpenDialog(gcViewer);
        if (val == JFileChooser.APPROVE_OPTION) {
            lastSelectedFiles = openDialog.getSelectedFiles();
            if (addURLCheckBox.isSelected()) {
                gcViewer.add(lastSelectedFiles);
            }
            else {
                gcViewer.open(lastSelectedFiles);
            }
        }
    }

    public void setSelectedFile(final File file) {
        openDialog.setCurrentDirectory(file.getParentFile());
        openDialog.setSelectedFile(file);
    }

    public File[] getLastSelectedFiles() {
        return lastSelectedFiles;
    }

}
