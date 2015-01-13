package com.tagtraum.perf.gcviewer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.tagtraum.perf.gcviewer.action.OpenRecent;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;

/**
 * RecentFilesMenu.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class RecentURLsMenu extends JMenu {
    private RecentURLsModel model;

    public RecentURLsMenu(final GCViewerGui gcViewer) {
        super(LocalisationHelper.getString("main_frame_menuitem_recent_files"));
        this.model = new RecentURLsModel();
        this.model.addRecentURLsListener(new RecentURLsListener(){
            public void remove(RecentURLEvent e) {
                RecentURLsMenu.this.remove(e.getPosition());
            }

            public void add(RecentURLEvent e) {
                RecentURLsMenu.this.add(new JMenuItem(new OpenRecent(gcViewer, e.getURLSet().getUrls())), e.getPosition());
            }
        });
        setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_recent_files").charAt(0));
        setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_recent_files"));
    }

    public RecentURLsModel getRecentURLsModel() {
        return model;
    }

}
