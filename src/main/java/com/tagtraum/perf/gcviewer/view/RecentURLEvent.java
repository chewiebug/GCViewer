package com.tagtraum.perf.gcviewer.view;

import java.util.EventObject;

import com.tagtraum.perf.gcviewer.view.model.RecentURLsModel;

/**
 * RecentURLEvent.
 * <p/>
 * Date: Oct 6, 2005
 * Time: 10:18:15 AM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class RecentURLEvent extends EventObject {
    private int position;
    private RecentURLsModel.URLSet urlSet;

    public RecentURLEvent(Object source, int position, RecentURLsModel.URLSet urlSet) {
        super(source);
        this.position = position;
        this.urlSet = urlSet;
    }

    public RecentURLEvent(Object source, int position) {
        this(source, position, null);
    }

    public int getPosition() {
        return position;
    }

    public RecentURLsModel.URLSet getURLSet() {
        return urlSet;
    }
}
