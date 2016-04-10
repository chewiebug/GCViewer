package com.tagtraum.perf.gcviewer.view.model;

import java.util.EventObject;

/**
 * Event, that is fired on changes in {@link RecentGCResourcesModel}.
 * 
 * <p>Date: Oct 6, 2005</p>
 * <p>Time: 10:18:15 AM</p>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class RecentGCResourcesEvent extends EventObject {
    private int position;
    private GCResourceGroup urlSet;

    public RecentGCResourcesEvent(Object source, int position, GCResourceGroup urlSet) {
        super(source);
        this.position = position;
        this.urlSet = urlSet;
    }

    public RecentGCResourcesEvent(Object source, int position) {
        this(source, position, null);
    }

    public int getPosition() {
        return position;
    }

    public GCResourceGroup getResourceNameGroup() {
        return urlSet;
    }
}
