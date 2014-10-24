package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.TextFileViewer;

public class LicenseAction extends AbstractAction {

    private TextFileViewer readmeDialog;

    public LicenseAction(final Frame parent) {
        readmeDialog = new TextFileViewer(parent, "META-INF/LICENSE.txt");
        putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_license"));
        putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_license"));
        putValue(MNEMONIC_KEY, new Integer(LocalisationHelper.getString("main_frame_menuitem_mnemonic_license").charAt(0)));
        putValue(ACTION_COMMAND_KEY, ActionCommands.SHOW_LICENSE.toString());
        // TODO icon?
        //putValue(SMALL_ICON, new ImageIcon(Toolkit.getDefaultToolkit().getImage(parent.getClass().getResource("license.png"))));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        readmeDialog.setVisible(true);
    }

}
