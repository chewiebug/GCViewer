package com.tagtraum.perf.gcviewer.action;

import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import com.tagtraum.perf.gcviewer.GCViewerGui;
import com.tagtraum.perf.gcviewer.util.ExtensionFileFilter;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.util.OSXSupport;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 1:48:26 PM
 *
 */
public class OpenFile extends AbstractAction {
    private GCViewerGui gcViewer;
    private JFileChooser openDialog;
    private File[] lastSelectedFiles = new File[0];
    private JCheckBox addURLCheckBox;


    public OpenFile(final GCViewerGui gcViewer) {
        this.gcViewer = gcViewer;
        putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_open_file"));
        putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_open_file"));
        putValue(MNEMONIC_KEY, new Integer(LocalisationHelper.getString("main_frame_menuitem_mnemonic_open_file").charAt(0)));
        putValue(ACTION_COMMAND_KEY, "open");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        putValue(SMALL_ICON, new ImageIcon(Toolkit.getDefaultToolkit().getImage(gcViewer.getClass().getResource("images/open.png"))));
        openDialog = new JFileChooser();
        openDialog.setDialogTitle(LocalisationHelper.getString("fileopen_dialog_title"));
        openDialog.setMultiSelectionEnabled(true);
        for (ExtensionFileFilter filter:ExtensionFileFilter.EXT_FILE_FILTERS) {
        	openDialog.addChoosableFileFilter(filter);
        }
        addURLCheckBox = new JCheckBox(LocalisationHelper.getString("fileopen_dialog_add_checkbox"), false);
        addURLCheckBox.setVerticalTextPosition(SwingConstants.TOP);
        addURLCheckBox.setToolTipText(LocalisationHelper.getString("fileopen_dialog_hint_add_checkbox"));

        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 2;
        panel.add(addURLCheckBox, gridBagConstraints);
        openDialog.setAccessory(panel);
    }

    public void actionPerformed(final ActionEvent e) {
        if(OSXSupport.isOSX()) {
            // there is no way to show a checkbox on the native dialog so
            // open a new window instead
            FileDialog dialog = new FileDialog(gcViewer, LocalisationHelper.getString("fileopen_dialog_title"), FileDialog.LOAD);
            dialog.setMultipleMode(true);
            dialog.setVisible(true);
            // dialog.setFilenameFilter doesn't do much on OSX
            openFiles(dialog.getFiles(), false);
            dialog.dispose();
            return;
        }
        
        final boolean aDocumentIsAlreadyOpen = gcViewer.getSelectedGCDocument() != null;
        addURLCheckBox.setVisible(aDocumentIsAlreadyOpen);
        addURLCheckBox.setEnabled(aDocumentIsAlreadyOpen);
        if (!aDocumentIsAlreadyOpen) {
            // checkbox must never be selected, when no document is opened
            // can happen if last file was added and whole document is closed afterwards
            // -> state of checkbox still "selected" but not visible any more
            addURLCheckBox.setSelected(false);
        }
        final int val = openDialog.showOpenDialog(gcViewer);
        if (val == JFileChooser.APPROVE_OPTION) {
            openFiles(openDialog.getSelectedFiles(), addURLCheckBox.isSelected());
        }
    }

    private void openFiles(File[] files, boolean shouldAdd) {
        if (files == null || files.length == 0) {
            return;
        }
        lastSelectedFiles = files;
        if (shouldAdd) {
            gcViewer.add(lastSelectedFiles);
        }
        else {
            gcViewer.open(lastSelectedFiles);
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
