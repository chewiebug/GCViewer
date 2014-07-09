package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoaderController;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.OpenFileView;
import com.tagtraum.perf.gcviewer.view.util.ImageHelper;

/**
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 1:48:26 PM
 */
public class OpenFile extends AbstractAction {
    private GCModelLoaderController controller;
    private GCViewerGui gcViewer;
    private File[] lastSelectedFiles = new File[0];
    private OpenFileView openFileView;

    public OpenFile(GCModelLoaderController controller, final GCViewerGui gcViewer) {
        this.controller = controller;
        this.gcViewer = gcViewer;
        
        putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_open_file"));
        putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_open_file"));
        putValue(MNEMONIC_KEY, new Integer(LocalisationHelper.getString("main_frame_menuitem_mnemonic_open_file").charAt(0)));
        putValue(ACTION_COMMAND_KEY, ActionCommands.OPEN_FILE.toString());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        putValue(SMALL_ICON, ImageHelper.loadImageIcon("open.png"));
        
        openFileView = new OpenFileView();
    }

    public void actionPerformed(final ActionEvent e) {
        openFileView.setShowAddCheckBox(gcViewer.getSelectedGCDocument() != null);
        
        // TODO SWINGWORKER: open at last openposition (directory)
        final int val = openFileView.showOpenDialog(gcViewer);
        if (val == JFileChooser.APPROVE_OPTION) {
            lastSelectedFiles = openFileView.getSelectedFiles();
            if (openFileView.isAddCheckBoxSelected()) {
                controller.add(lastSelectedFiles);
            }
            else {
                controller.open(lastSelectedFiles);
            }
        }
    }

    public void setSelectedFile(final File file) {
        // TODO SWINGWORKER: should be part of some model
        openFileView.setCurrentDirectory(file.getParentFile());
        openFileView.setSelectedFile(file);
    }

    public File[] getLastSelectedFiles() {
        return lastSelectedFiles;
    }

}
