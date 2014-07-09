package com.tagtraum.perf.gcviewer.view;

import java.util.EventObject;

import com.tagtraum.perf.gcviewer.view.model.RecentResourceNamesModel;

/**
 * RecentURLEvent.
 * <p/>
 * Date: Oct 6, 2005
 * Time: 10:18:15 AM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class RecentResourceNamesEvent extends EventObject {
    private int position;
    private RecentResourceNamesModel.URLSet urlSet;

    public RecentResourceNamesEvent(Object source, int position, RecentResourceNamesModel.URLSet urlSet) {
        super(source);
        this.position = position;
        this.urlSet = urlSet;
    }

    public RecentResourceNamesEvent(Object source, int position) {
        this(source, position, null);
    }

    public int getPosition() {
        return position;
    }

    public RecentResourceNamesModel.URLSet getURLSet() {
        return urlSet;
    }
}
