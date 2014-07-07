package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.GCDocument;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.util.ImageLoader;

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
        putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_refresh"));
        putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_refresh"));
        putValue(MNEMONIC_KEY, new Integer(LocalisationHelper.getString("main_frame_menuitem_mnemonic_refresh").charAt(0)));
        putValue(ACTION_COMMAND_KEY, "refresh");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        putValue(SMALL_ICON, ImageLoader.loadImageIcon("refresh.png"));
        setEnabled(false);
    }

    public void actionPerformed(final ActionEvent ae) {
        try {
            final GCDocument gcDocument = gcViewer.getSelectedGCDocument();
            if (gcDocument != null) gcDocument.reloadModels(false);
        } catch (RuntimeException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(gcViewer, e.toString() + " " + e.getLocalizedMessage(), LocalisationHelper.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            //e.printStackTrace();
            JOptionPane.showMessageDialog(gcViewer, e.getLocalizedMessage(), LocalisationHelper.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
        }
    }
}
