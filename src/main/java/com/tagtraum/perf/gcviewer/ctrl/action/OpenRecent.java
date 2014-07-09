package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoaderController;
import com.tagtraum.perf.gcviewer.view.ActionCommands;

/**
 * Action to open an entry of the recent urls menu.
 * <p/>
 * Date: Sep 25, 2005
 * Time: 11:16:49 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class OpenRecent extends AbstractAction {

    private URL[] urls;
    private GCModelLoaderController controller;

    public OpenRecent(GCModelLoaderController controller, final URL[] urls) {
        this.urls = urls;
        this.controller = controller;
        
        putValue(ACTION_COMMAND_KEY, ActionCommands.OPEN_RECENT.toString());
        putValue(NAME, toString(urls));
    }

    public void actionPerformed(final ActionEvent e) {
        controller.open(urls);
    }

    public URL[] getURLs() {
        return urls;
    }

    private String toString(final URL[] urls) {
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
