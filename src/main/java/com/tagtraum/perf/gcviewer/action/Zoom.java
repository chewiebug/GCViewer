package com.tagtraum.perf.gcviewer.action;

import com.tagtraum.perf.gcviewer.GCViewerGui;
import com.tagtraum.perf.gcviewer.util.NumberParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 2:06:04 PM
 *
 */
public class Zoom extends AbstractAction {
    private GCViewerGui gcViewer;

    public Zoom(final GCViewerGui gcViewer) {
        this.gcViewer = gcViewer;
        putValue(NAME, GCViewerGui.localStrings.getString("action_zoom"));
        putValue(SHORT_DESCRIPTION, GCViewerGui.localStrings.getString("action_zoom_hint"));
        //putValue(Action.MNEMONIC_KEY, new Integer(localStrings.getString("main_frame_menuitem_mnemonic_export").charAt(0)));
        putValue(ACTION_COMMAND_KEY, "zoom");
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('E', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        setEnabled(false);
    }

    public void actionPerformed(final ActionEvent e) {
        final ItemSelectable is = (ItemSelectable)e.getSource();
        final Object[] o = is.getSelectedObjects();
        try {
            String item = (String)o[0];
            if (item.endsWith("%")) item = item.substring(0, item.length()-1);
            final double zoomFactor = Double.parseDouble(item.trim());
            if (zoomFactor > 0) gcViewer.getSelectedGCDocument().getModelChart().setScaleFactor(zoomFactor/1000.0);
        }
        catch (NumberFormatException nfe) {
            //nfe.printStackTrace();
        }
    }
}
