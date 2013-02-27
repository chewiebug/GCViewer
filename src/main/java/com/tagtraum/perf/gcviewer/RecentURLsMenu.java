package com.tagtraum.perf.gcviewer;

import com.tagtraum.perf.gcviewer.action.OpenRecent;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * RecentFilesMenu.
 * <p/>
 * Date: Sep 25, 2005
 * Time: 10:54:45 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class RecentURLsMenu extends JMenu {
    public static ResourceBundle localStrings = ResourceBundle.getBundle("com.tagtraum.perf.gcviewer.localStrings");

    private RecentURLsModel model;

    public RecentURLsMenu(final GCViewerGui gcViewer) {
        super(localStrings.getString("main_frame_menuitem_recent_files"));
        this.model = new RecentURLsModel();
        this.model.addRecentURLsListener(new RecentURLsListener(){
            public void remove(RecentURLEvent e) {
                RecentURLsMenu.this.remove(e.getPosition());
            }

            public void add(RecentURLEvent e) {
                RecentURLsMenu.this.add(new JMenuItem(new OpenRecent(gcViewer, e.getURLSet().getUrls())), e.getPosition());
            }
        });
        setMnemonic(localStrings.getString("main_frame_menuitem_mnemonic_recent_files").charAt(0));
        setToolTipText(localStrings.getString("main_frame_menuitem_hint_recent_files"));
    }

    public RecentURLsModel getRecentURLsModel() {
        return model;
    }

}
