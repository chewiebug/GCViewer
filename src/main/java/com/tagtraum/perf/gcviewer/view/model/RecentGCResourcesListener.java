package com.tagtraum.perf.gcviewer.view.model;

import java.util.EventListener;


/**
 * Listener interface for add / remove events from the {@link RecentGCResourcesModel}.
 * 
 * <p>Date: Oct 6, 2005</p>
 * <p>Time: 10:17:05 AM</p>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public interface RecentGCResourcesListener extends EventListener {
    
    /**
     * Invoked, when an item is removed from the model.
     * 
     * @param e details about the item, that is removed
     */
    public void remove(RecentGCResourcesEvent e);
    
    /**
     * Invoked, when an item is added to the model.
     * 
     * @param e details about the item, that is added
     */
    public void add(RecentGCResourcesEvent e);
}
