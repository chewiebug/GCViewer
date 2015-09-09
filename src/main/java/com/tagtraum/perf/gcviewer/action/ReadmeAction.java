package com.tagtraum.perf.gcviewer.action;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.tagtraum.perf.gcviewer.TextFileViewer;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;

public class ReadmeAction extends AbstractAction {

    private TextFileViewer readmeDialog;

    public ReadmeAction(final Frame parent) {
        readmeDialog = new TextFileViewer(parent, "META-INF/README.md");
        putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_readme"));
        putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_readme"));
        putValue(MNEMONIC_KEY, new Integer(LocalisationHelper.getString("main_frame_menuitem_mnemonic_readme").charAt(0)));
        putValue(ACTION_COMMAND_KEY, "readme");
        // TODO icon?
        // putValue(SMALL_ICON, new ImageIcon(Toolkit.getDefaultToolkit().getImage(parent.getClass().getResource("images/readme.png"))));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        readmeDialog.setVisible(true);
    }

}
