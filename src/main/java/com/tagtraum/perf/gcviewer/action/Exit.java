package com.tagtraum.perf.gcviewer.action;

import com.tagtraum.perf.gcviewer.GCViewerGui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

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
        putValue(NAME, GCViewerGui.localStrings.getString("main_frame_menuitem_exit"));
        putValue(SHORT_DESCRIPTION, GCViewerGui.localStrings.getString("main_frame_menuitem_hint_exit"));
        putValue(MNEMONIC_KEY, new Integer(GCViewerGui.localStrings.getString("main_frame_menuitem_mnemonic_exit").charAt(0)));
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
