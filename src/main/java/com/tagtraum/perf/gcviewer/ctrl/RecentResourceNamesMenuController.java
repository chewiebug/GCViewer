package com.tagtraum.perf.gcviewer.ctrl;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.tagtraum.perf.gcviewer.ctrl.action.OpenRecent;
import com.tagtraum.perf.gcviewer.view.RecentResourceNamesMenu;
import com.tagtraum.perf.gcviewer.view.model.RecentResourceNamesEvent;
import com.tagtraum.perf.gcviewer.view.model.RecentResourceNamesListener;
import com.tagtraum.perf.gcviewer.view.model.RecentResourceNamesModel;

/**
 * Controller for the {@link RecentResourceNamesMenu} keeping it in sync with the {@link RecentResourceNamesModel}. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 20.02.2014</p>
 */
public class RecentResourceNamesMenuController implements RecentResourceNamesListener {

    private GCModelLoaderController controller;
    private JMenu menu;
    
    public RecentResourceNamesMenuController(GCModelLoaderController controller, JMenu menu) {
        super();
        
        this.controller = controller;
        this.menu = menu;
    }
    
    /**
     * @see com.tagtraum.perf.gcviewer.view.model.RecentResourceNamesListener#remove(com.tagtraum.perf.gcviewer.view.model.RecentResourceNamesEvent)
     */
    @Override
    public void remove(RecentResourceNamesEvent e) {
        menu.remove(e.getPosition());
    }

    /**
     * @see com.tagtraum.perf.gcviewer.view.model.RecentResourceNamesListener#add(com.tagtraum.perf.gcviewer.view.model.RecentResourceNamesEvent)
     */
    @Override
    public void add(RecentResourceNamesEvent e) {
        menu.add(new JMenuItem(new OpenRecent(controller, 
                e.getResourceNameGroup())), 
                e.getPosition());
    }

}
