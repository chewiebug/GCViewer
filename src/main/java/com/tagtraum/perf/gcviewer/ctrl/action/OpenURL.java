package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import com.tagtraum.perf.gcviewer.ctrl.GCViewerController;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.OpenUrlView;
import com.tagtraum.perf.gcviewer.view.model.RecentURLsModel;
import com.tagtraum.perf.gcviewer.view.util.ImageLoader;

/**
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 1:48:26 PM
 */
public class OpenURL extends AbstractAction {
    private GCViewerController controller;
    private GCViewerGui gcViewer;
    private OpenUrlView view;

    public OpenURL(GCViewerController controller, final GCViewerGui gcViewer) {
        this.controller = controller;
        this.gcViewer = gcViewer;
        this.view = new OpenUrlView(gcViewer);
        
        putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_open_url"));
        putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_open_url"));
        putValue(MNEMONIC_KEY, new Integer(LocalisationHelper.getString("main_frame_menuitem_mnemonic_open_url").charAt(0)));
        putValue(ACTION_COMMAND_KEY, "open_url");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('U', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        putValue(SMALL_ICON, ImageLoader.loadImageIcon("open_url.png"));
    }

    public void setRecentURLsModel(final RecentURLsModel recentURLsModel) {
        this.view.setRecentUrlsModel(recentURLsModel);
    }

    public void actionPerformed(final ActionEvent e) {
        if (view.showDialog()) {
            if (view.isAddCheckBoxSelected()) {
                controller.add(view.getSelectedItem());
            }
            else {
                controller.open(view.getSelectedItem());
            }
        }
    }

}
