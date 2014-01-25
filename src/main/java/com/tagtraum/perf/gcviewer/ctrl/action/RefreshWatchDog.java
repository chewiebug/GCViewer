package com.tagtraum.perf.gcviewer.ctrl.action;

import java.util.TimerTask;

import com.tagtraum.perf.gcviewer.ctrl.GCViewerController;
import com.tagtraum.perf.gcviewer.view.GCDocument;

/**
 * Timer to trigger refresh of changed log files.
 *
 * <p>Date: May 26, 2005</p>
 * <p>Time: 2:04:38 PM</p>
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class RefreshWatchDog {
    private static final int RELOAD_DELAY = 1000;

    private GCViewerController controller;
    private GCDocument gcDocument;

    private java.util.Timer reloadTimer;

    public RefreshWatchDog(GCViewerController controller, GCDocument gcDocument) {
        this.controller = controller;
        this.gcDocument = gcDocument;
    }
    
    public void start() {
        reloadTimer = new java.util.Timer(true);
        reloadTimer.schedule(new ModelReloader(), 0, RELOAD_DELAY);
    }
    
    public void stop() {
        if (reloadTimer != null) {
            reloadTimer.cancel();
        }
    }

    private class ModelReloader extends TimerTask {
        
        public void run() {
            // TODO SWINGWORKER: next reload should not be started, if document takes too long to reload
            controller.reload(gcDocument);
        }
    }
}
