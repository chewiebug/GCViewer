package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.AboutDialog;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.util.ImageHelper;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 1:45:11 PM
 *
 */
public class About extends AbstractAction {
    private AboutDialog aboutDialog;

    public About(final Frame parent) {
        aboutDialog = new AboutDialog(parent);
        
        putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_about"));
        putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_about"));
        putValue(MNEMONIC_KEY, new Integer(LocalisationHelper.getString("main_frame_menuitem_mnemonic_about").charAt(0)));
        putValue(ACTION_COMMAND_KEY, ActionCommands.ABOUT.toString());
        putValue(SMALL_ICON, ImageHelper.loadImageIcon("about.png"));
    }

    public void actionPerformed(final ActionEvent e) {
        aboutDialog.setVisible(true);
    }

    // Used by OS X adaptations
    public void about() {
        actionPerformed(null);
    }
}
