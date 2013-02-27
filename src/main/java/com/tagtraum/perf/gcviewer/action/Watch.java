package com.tagtraum.perf.gcviewer.action;

import com.tagtraum.perf.gcviewer.GCDocument;
import com.tagtraum.perf.gcviewer.GCViewerGui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 1:59:59 PM
 *
 */
public class Watch extends AbstractAction {
    private GCViewerGui gcViewer;
    private static final ImageIcon WATCH_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(GCViewerGui.class.getResource("images/watch.png")));

    public Watch(final GCViewerGui gcViewer) {
        this.gcViewer = gcViewer;
        putValue(NAME, GCViewerGui.localStrings.getString("main_frame_menuitem_watch"));
        putValue(SHORT_DESCRIPTION, GCViewerGui.localStrings.getString("main_frame_menuitem_hint_watch"));
        putValue(MNEMONIC_KEY, new Integer(GCViewerGui.localStrings.getString("main_frame_menuitem_mnemonic_watch").charAt(0)));
        putValue(ACTION_COMMAND_KEY, "watch");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        putValue(SMALL_ICON, WATCH_ICON);
        setEnabled(false);
    }

    public void actionPerformed(final ActionEvent ae) {
        final AbstractButton source = (AbstractButton)ae.getSource();
        if (source.isSelected()) {
            final GCDocument selectedGCDocument = gcViewer.getSelectedGCDocument();
            selectedGCDocument.setWatched(true);
            selectedGCDocument.getRefreshWatchDog().setAction(this);
            selectedGCDocument.getRefreshWatchDog().start();
        }
        else {
            final GCDocument selectedGCDocument = gcViewer.getSelectedGCDocument();
            selectedGCDocument.setWatched(false);
            selectedGCDocument.getRefreshWatchDog().stop();
        }
    }

    public void setEnabled(final boolean newValue) {
        super.setEnabled(newValue);
        if (!newValue) {
            putValue(SMALL_ICON, WATCH_ICON);
        }
    }
}
