package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.util.ImageHelper;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 1:42:15 PM
 */
public class Exit extends AbstractAction {
    private Frame frame;

    public Exit(Frame frame) {
        this.frame = frame;
        
        putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_exit"));
        putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_exit"));
        putValue(MNEMONIC_KEY, new Integer(LocalisationHelper.getString("main_frame_menuitem_mnemonic_exit").charAt(0)));
        putValue(ACTION_COMMAND_KEY, ActionCommands.EXIT.toString());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('X', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SMALL_ICON, ImageHelper.loadImageIcon("exit.png"));
    }

    public void actionPerformed(final ActionEvent e) {
        // TODO SWINGWORKER does this really close and dispose the window?
        WindowEvent windowClosing = new WindowEvent(frame, WindowEvent.WINDOW_CLOSING);
        frame.dispatchEvent(windowClosing);
    }

    // Used by OS X adaptations
    public boolean quit() {
        actionPerformed(null);
        return true;
    }
}
