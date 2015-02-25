package com.tagtraum.perf.gcviewer;

/**
 * RecentURLsListener.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public interface RecentURLsListener {
    public void remove(RecentURLEvent e);
    public void add(RecentURLEvent e);
}
