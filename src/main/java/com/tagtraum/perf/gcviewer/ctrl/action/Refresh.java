package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoaderController;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.util.ImageHelper;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 1:59:59 PM
 *
 */
public class Refresh extends AbstractAction {
    private GCModelLoaderController controller;
    private GCViewerGui gcViewer;

    public Refresh(GCModelLoaderController controller, GCViewerGui gcViewer) {
        this.controller = controller;
        this.gcViewer = gcViewer;
        
        putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_refresh"));
        putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_refresh"));
        putValue(MNEMONIC_KEY, new Integer(LocalisationHelper.getString("main_frame_menuitem_mnemonic_refresh").charAt(0)));
        putValue(ACTION_COMMAND_KEY, ActionCommands.REFRESH.toString());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        putValue(SMALL_ICON, ImageHelper.loadImageIcon("refresh.png"));
        
        setEnabled(false);
    }

    public void actionPerformed(final ActionEvent ae) {
        controller.reload(gcViewer.getSelectedGCDocument());
    }
}
