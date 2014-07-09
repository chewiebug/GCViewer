package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoaderController;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.OpenUrlView;
import com.tagtraum.perf.gcviewer.view.model.RecentResourceNamesModel;
import com.tagtraum.perf.gcviewer.view.util.ImageHelper;

/**
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 1:48:26 PM
 */
public class OpenURL extends AbstractAction {
    private GCModelLoaderController controller;
    private OpenUrlView view;

    public OpenURL(GCModelLoaderController controller, final JFrame parent) {
        this.controller = controller;
        this.view = new OpenUrlView(parent);
        
        putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_open_url"));
        putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_open_url"));
        putValue(MNEMONIC_KEY, new Integer(LocalisationHelper.getString("main_frame_menuitem_mnemonic_open_url").charAt(0)));
        putValue(ACTION_COMMAND_KEY, ActionCommands.OPEN_URL.toString());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('U', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        putValue(SMALL_ICON, ImageHelper.loadImageIcon("open_url.png"));
    }

    public void setRecentResourceNamesModel(final RecentResourceNamesModel recentResourceNamesModel) {
        this.view.setRecentResourceNamesModel(recentResourceNamesModel);
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
