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
public class Refresh extends AbstractAction {
    private GCViewerGui gcViewer;

    public Refresh(final GCViewerGui gcViewer) {
        this.gcViewer = gcViewer;
        putValue(NAME, GCViewerGui.localStrings.getString("main_frame_menuitem_refresh"));
        putValue(SHORT_DESCRIPTION, GCViewerGui.localStrings.getString("main_frame_menuitem_hint_refresh"));
        putValue(MNEMONIC_KEY, new Integer(GCViewerGui.localStrings.getString("main_frame_menuitem_mnemonic_refresh").charAt(0)));
        putValue(ACTION_COMMAND_KEY, "refresh");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        putValue(SMALL_ICON, new ImageIcon(Toolkit.getDefaultToolkit().getImage(gcViewer.getClass().getResource("images/refresh.png"))));
        setEnabled(false);
    }

    public void actionPerformed(final ActionEvent ae) {
        try {
            final GCDocument gcDocument = gcViewer.getSelectedGCDocument();
            if (gcDocument != null) gcDocument.reloadModels(false);
        } catch (RuntimeException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(gcViewer, e.toString() + " " + e.getLocalizedMessage(), GCViewerGui.localStrings.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            //e.printStackTrace();
            JOptionPane.showMessageDialog(gcViewer, e.getLocalizedMessage(), GCViewerGui.localStrings.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
        }
    }
}
