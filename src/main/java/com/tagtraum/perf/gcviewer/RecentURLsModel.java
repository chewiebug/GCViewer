package com.tagtraum.perf.gcviewer;

import java.net.URL;
import java.util.*;

/**
 * RecentFilesMenu.
 * <p/>
 * Date: Sep 25, 2005
 * Time: 10:54:45 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class RecentURLsModel {

    private int maxElements = 10;
    private List urlSetList;
    private List listeners;
    private Set allURLs;

    public RecentURLsModel() {
        this.urlSetList = new ArrayList();
        this.listeners = new ArrayList();
        this.allURLs = new HashSet();
    }

    public void addRecentURLsListener(RecentURLsListener recentURLsListener) {
        listeners.add(recentURLsListener);
    }

    protected void fireAddEvent(int position, URLSet urlSet) {
        for (int i=0; i<listeners.size(); i++) {
            ((RecentURLsListener)listeners.get(i)).add(new RecentURLEvent(this, position, urlSet));
        }
    }

    protected void fireRemoveEvent(int position) {
        for (int i=0; i<listeners.size(); i++) {
            ((RecentURLsListener)listeners.get(i)).remove(new RecentURLEvent(this, position));
        }
    }

    public List getURLsStartingWith(String start) {
        List result = new ArrayList();
        for (Iterator urls=allURLs.iterator(); urls.hasNext(); ) {
            String url = urls.next().toString();
            if (url.startsWith(start)) result.add(url);
        }
        Collections.sort(result);
        return result;
    }

    public void add(URL[] urls) {
        allURLs.addAll(Arrays.asList(urls));
        final URLSet urlSet = new URLSet(urls);
        if (!urlSetList.contains(urlSet)) {
            urlSetList.add(0, urlSet);
            fireAddEvent(0, urlSet);
            if (urlSetList.size() > maxElements) {
                urlSetList.remove(maxElements - 1);
                fireRemoveEvent(maxElements - 1);
            }
        }
        else {
            for (int i=0,max=urlSetList.size(); i<max; i++) {
                URLSet existingURLSet = (URLSet)urlSetList.get(i);
                if (urlSet.equals(existingURLSet)) {
                    urlSetList.remove(i);
                    fireRemoveEvent(i);
                    urlSetList.add(0, urlSet);
                    fireAddEvent(0, urlSet);
                    break;
                }
            }
        }
    }

    public class URLSet {
        private String[] urlStrings;
        private URL[] urls;

        public URLSet(URL[] urls) {
            this.urls = urls;
            this.urlStrings = createSortedFileStrings(urls);
        }

        private String[] createSortedFileStrings(URL[] urls) {
            String[] fileStrings = new String[urls.length];
            for (int i=0; i<urls.length; i++) {
                fileStrings[i] = urls[i].toString();
            }
            Arrays.sort(fileStrings);
            return fileStrings;
        }

        public URL[] getUrls() {
            return urls;
        }

        public int hashCode() {
            return urlStrings[0].hashCode();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof URLSet) || obj == null) return false;
            URLSet that = (URLSet)obj;
            if (that.urlStrings.length != this.urlStrings.length) return false;
            for (int i=0; i<that.urlStrings.length; i++) {
                if (!that.urlStrings[i].equals(this.urlStrings[i])) return false;
            }
            return true;
        }

    }

}
