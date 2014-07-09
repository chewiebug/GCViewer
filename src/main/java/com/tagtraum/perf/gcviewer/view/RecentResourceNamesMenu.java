package com.tagtraum.perf.gcviewer.view;

import javax.swing.JMenu;

import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.model.RecentResourceNamesListener;
import com.tagtraum.perf.gcviewer.view.model.RecentResourceNamesModel;

/**
 * Special menu item dealing with recent URLs.
 * 
 * <p>Date: Sep 25, 2005</p>
 * <p>Time: 10:54:45 PM</p>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class RecentResourceNamesMenu extends JMenu {
    private RecentResourceNamesModel model;

    public RecentResourceNamesMenu() {
        super(LocalisationHelper.getString("main_frame_menuitem_recent_files"));
        
        this.model = new RecentResourceNamesModel();
        setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_recent_files").charAt(0));
        setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_recent_files"));
    }

    public RecentResourceNamesModel getRecentResourceNamesModel() {
        return model;
    }

}
