package com.tagtraum.perf.gcviewer.view;

/**
 * RecentURLsListener.
 * <p/>
 * Date: Oct 6, 2005
 * Time: 10:17:05 AM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public interface RecentURLsListener {
    public void remove(RecentURLEvent e);
    public void add(RecentURLEvent e);
}
