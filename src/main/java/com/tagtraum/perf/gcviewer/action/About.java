package com.tagtraum.perf.gcviewer.action;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import com.tagtraum.perf.gcviewer.AboutDialog;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;

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
        putValue(ACTION_COMMAND_KEY, "about");
        putValue(SMALL_ICON, new ImageIcon(Toolkit.getDefaultToolkit().getImage(parent.getClass().getResource("images/about.png"))));
    }

    public void actionPerformed(final ActionEvent e) {
        aboutDialog.setVisible(true);
    }

    // Used by OS X adaptations
    public void about() {
        actionPerformed(null);
    }
}
