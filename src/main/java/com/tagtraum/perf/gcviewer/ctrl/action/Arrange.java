package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;

import javax.swing.AbstractAction;
import javax.swing.DesktopManager;
import javax.swing.JInternalFrame;
import javax.swing.KeyStroke;

import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 2:07:18 PM
 */
public class Arrange extends AbstractAction {
    private GCViewerGui gcViewer;

    public Arrange(final GCViewerGui gcViewer) {
        this.gcViewer = gcViewer;
        putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_arrange"));
        putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_arrange"));
        putValue(MNEMONIC_KEY, new Integer(LocalisationHelper.getString("main_frame_menuitem_mnemonic_arrange").charAt(0)));
        putValue(ACTION_COMMAND_KEY, ActionCommands.ARRANGE.toString());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('G', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        setEnabled(false);
    }

    public void actionPerformed(final ActionEvent e) {
        final JInternalFrame[] frames = gcViewer.getDesktopPane().getAllFrames();
        final DesktopManager desktopManager = gcViewer.getDesktopPane().getDesktopManager();
        for (int i=0; i<frames.length; i++) {
            final JInternalFrame frame = frames[i];
            desktopManager.deiconifyFrame(frame);
            try {
                frame.setMaximum(false);
            } 
            catch (PropertyVetoException e1) {
                e1.printStackTrace();
            }
            final int height = gcViewer.getDesktopPane().getHeight()/frames.length;
            desktopManager.setBoundsForFrame(frame, 0, height * i, gcViewer.getDesktopPane().getWidth(), height);
        }
    }
}
