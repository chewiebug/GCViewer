package com.tagtraum.perf.gcviewer.action;

import com.tagtraum.perf.gcviewer.GCViewer;
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
    private GCViewer gcViewer;

    public Zoom(final GCViewer gcViewer) {
        this.gcViewer = gcViewer;
        putValue(NAME, GCViewer.localStrings.getString("action_zoom"));
        putValue(SHORT_DESCRIPTION, GCViewer.localStrings.getString("action_zoom_hint"));
        //putValue(Action.MNEMONIC_KEY, new Integer(localStrings.getString("main_frame_menuitem_mnemonic_export").charAt(0)));
        putValue(ACTION_COMMAND_KEY, "zoom");
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('E', java.awt.Event.CTRL_MASK ));
        setEnabled(false);
    }

    public void actionPerformed(final ActionEvent e) {
        final ItemSelectable is = (ItemSelectable)e.getSource();
        final Object[] o = is.getSelectedObjects();
        try {
            String item = (String)o[0];
            if (item.endsWith("%")) item = item.substring(0, item.length()-1);
            final int zoomFactor = NumberParser.parseInt(item.trim());
            if (zoomFactor > 0) gcViewer.getSelectedGCDocument().getModelChart().setScaleFactor(zoomFactor/1000.0);
        }
        catch (NumberFormatException nfe) {
            //nfe.printStackTrace();
        }
    }
}
