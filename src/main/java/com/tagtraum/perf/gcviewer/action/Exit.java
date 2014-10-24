package com.tagtraum.perf.gcviewer.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import com.tagtraum.perf.gcviewer.GCViewerGui;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 1:42:15 PM
 *
 */
public class Exit extends AbstractAction {
    private GCViewerGui gcViewer;

    public Exit(final GCViewerGui gcViewer) {
        this.gcViewer = gcViewer;
        putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_exit"));
        putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_exit"));
        putValue(MNEMONIC_KEY, new Integer(LocalisationHelper.getString("main_frame_menuitem_mnemonic_exit").charAt(0)));
        putValue(ACTION_COMMAND_KEY, "exit");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('X', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        putValue(SMALL_ICON, new ImageIcon(Toolkit.getDefaultToolkit().getImage(gcViewer.getClass().getResource("images/exit.png"))));
    }

    public void actionPerformed(final ActionEvent e) {
        gcViewer.exit();
    }

    // Used by OS X adaptations
    public void quit() {
        actionPerformed(null);
    }
}
