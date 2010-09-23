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
 * Time: 1:51:06 PM
 *
 */
public class AddFile extends AbstractAction {
    private GCViewer gcViewer;
    private JFileChooser addDialog;
    private File[] lastSelectedFiles = new File[0];

    public AddFile(final GCViewer gcViewer) {
        this.gcViewer = gcViewer;
        putValue(NAME, GCViewer.localStrings.getString("main_frame_menuitem_add_file"));
        putValue(SHORT_DESCRIPTION, GCViewer.localStrings.getString("main_frame_menuitem_hint_add_file"));
        putValue(MNEMONIC_KEY, new Integer(GCViewer.localStrings.getString("main_frame_menuitem_mnemonic_add_file").charAt(0)));
        putValue(ACTION_COMMAND_KEY, "add");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('A', Event.CTRL_MASK ));
        putValue(SMALL_ICON, new ImageIcon(Toolkit.getDefaultToolkit().getImage(gcViewer.getClass().getResource("images/add.gif"))));
        addDialog = new JFileChooser();
        // todo: change localstring to add file
        addDialog.setDialogTitle(GCViewer.localStrings.getString("fileopen_dialog_title"));
        addDialog.setMultiSelectionEnabled(true);
        addDialog.addChoosableFileFilter(ExtensionFileFilter.GcExtensionFilter);
        addDialog.addChoosableFileFilter(ExtensionFileFilter.TxtExtensionFilter);
        addDialog.addChoosableFileFilter(ExtensionFileFilter.LogExtensionFilter);
        setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
        final int val = addDialog.showOpenDialog(gcViewer);
        if (val == JFileChooser.APPROVE_OPTION) {
            lastSelectedFiles = addDialog.getSelectedFiles();
            gcViewer.add(lastSelectedFiles);
        }
    }

    public void setSelectedFile(final File file) {
        addDialog.setCurrentDirectory(file.getParentFile());
        addDialog.setSelectedFile(file);
    }

    public File[] getLastSelectedFiles() {
        return lastSelectedFiles;
    }

}
