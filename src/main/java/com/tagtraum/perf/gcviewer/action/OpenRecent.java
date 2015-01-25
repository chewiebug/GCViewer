package com.tagtraum.perf.gcviewer.action;

import com.tagtraum.perf.gcviewer.GCViewerGui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * OpenRecent.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class OpenRecent extends AbstractAction {

    private URL[] urls;
    private GCViewerGui gcViewer;

    public OpenRecent(final GCViewerGui gcViewer, final URL[] urls) {
        this.urls = urls;
        this.gcViewer = gcViewer;
        putValue(NAME, toString(urls));
    }

    public void actionPerformed(final ActionEvent e) {
        gcViewer.open(urls);
    }

    public URL[] getURLs() {
        return urls;
    }

    private static String toString(final URL[] urls) {
        if (urls.length == 1) return urls[0].toString();
        final StringBuffer sb = new StringBuffer();
        for (int i=0; i<urls.length; i++) {
            final URL url = urls[i];
            if (url.getProtocol().startsWith("file")) {
                sb.append(url.getPath().substring(url.getPath().lastIndexOf('/')+1));
            }
            else {
                sb.append(url);
            }
            if (i+1<urls.length) sb.append(", ");
        }
        return sb.toString();
    }

}
