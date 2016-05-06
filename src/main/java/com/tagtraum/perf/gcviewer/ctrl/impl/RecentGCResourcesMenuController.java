package com.tagtraum.perf.gcviewer.ctrl.impl;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoaderController;
import com.tagtraum.perf.gcviewer.ctrl.action.OpenRecent;
import com.tagtraum.perf.gcviewer.view.RecentGCResourcesMenu;
import com.tagtraum.perf.gcviewer.view.model.RecentGCResourcesEvent;
import com.tagtraum.perf.gcviewer.view.model.RecentGCResourcesListener;
import com.tagtraum.perf.gcviewer.view.model.RecentGCResourcesModel;

import javax.swing.*;

/**
 * Controller for the {@link RecentGCResourcesMenu} keeping it in sync with the {@link RecentGCResourcesModel}. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 20.02.2014</p>
 */
public class RecentGCResourcesMenuController implements RecentGCResourcesListener {

    private GCModelLoaderController controller;
    private JMenu menu;
    
    public RecentGCResourcesMenuController(GCModelLoaderController controller, JMenu menu) {
        super();
        
        this.controller = controller;
        this.menu = menu;
    }
    
    /**
     * @see com.tagtraum.perf.gcviewer.view.model.RecentGCResourcesListener#remove(com.tagtraum.perf.gcviewer.view.model.RecentGCResourcesEvent)
     */
    @Override
    public void remove(RecentGCResourcesEvent e) {
        menu.remove(e.getPosition());
    }

    /**
     * @see com.tagtraum.perf.gcviewer.view.model.RecentGCResourcesListener#add(com.tagtraum.perf.gcviewer.view.model.RecentGCResourcesEvent)
     */
    @Override
    public void add(RecentGCResourcesEvent e) {
        menu.add(new JMenuItem(new OpenRecent(controller,
                e.getResourceNameGroup())), 
                e.getPosition());
    }

}
