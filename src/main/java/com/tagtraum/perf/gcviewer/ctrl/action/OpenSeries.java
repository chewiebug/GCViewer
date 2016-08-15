package com.tagtraum.perf.gcviewer.ctrl.action;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoaderController;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.OpenFileView;
import com.tagtraum.perf.gcviewer.view.util.ImageHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Allows to open a series of log files, treating them as a consecutive log.
 *
 * @author martin.geldmacher
 */
public class OpenSeries extends AbstractAction {
    private static final Logger logger = Logger.getLogger(OpenSeries.class.getName());

    private GCModelLoaderController controller;
    private GCViewerGui gcViewer;
    private OpenFileView openFileView;

    public OpenSeries(GCModelLoaderController controller, final GCViewerGui gcViewer) {
        this.controller = controller;
        this.gcViewer = gcViewer;

        putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_open_series"));
        putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_open_series"));
        putValue(MNEMONIC_KEY, Integer.valueOf(LocalisationHelper.getString("main_frame_menuitem_mnemonic_open_series").charAt(0)));
        putValue(ACTION_COMMAND_KEY, ActionCommands.OPEN_SERIES.toString());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SMALL_ICON, ImageHelper.loadImageIcon("open.png"));

        openFileView = new OpenFileView();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final int val = openFileView.showOpenDialog(gcViewer);
        if (val == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = openFileView.getSelectedFiles();
            List<GCResource> resources = getResources(selectedFiles);
            controller.openAsSeries(resources);
        }
    }

    private List<GCResource> getResources(File[] selectedFiles) {
        if (selectedFiles == null || selectedFiles.length == 0)
            throw new IllegalArgumentException("At least one file must be selected!");

        java.util.List<GCResource> resources = new ArrayList<>();
        for (File file : selectedFiles) {
            resources.add(new GcResourceFile(file));
        }
        return resources;
    }
}
