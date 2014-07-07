package com.tagtraum.perf.gcviewer.ctrl.action;

import java.util.TimerTask;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import com.tagtraum.perf.gcviewer.view.GCDocument;
import com.tagtraum.perf.gcviewer.view.util.ImageLoader;

/**
 * Timer to trigger refresh of changed log files.
 *
 * <p>Date: May 26, 2005</p>
 * <p>Time: 2:04:38 PM</p>
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class RefreshWatchDog {
    private GCDocument gcDocument;
    private java.util.Timer reloadTimer;
    private Action action;
    private boolean isRunning;
    public static final ImageIcon WATCH_ICON = ImageLoader.loadImageIcon("watch.png");
    private static final ImageIcon CLOCK_ICON = ImageLoader.loadImageIcon("clock.png");
    private static final int RELOAD_DELAY = 1000;

    /**
     * Get the {@link GCDocument} this watchdog is used for.
     * @return GCDocument for this watchdog
     */
    public GCDocument getGcDocument() {
        return gcDocument;
    }

    /**
     * Set the {@link GCDocument} this watchdog should trigger refreshes for.
     * @param gcDocument to be refreshed
     */
    public void setGcDocument(GCDocument gcDocument) {
        this.gcDocument = gcDocument;
    }

    /**
     * Get action that is triggered by the watchdog.
     * @return Action to be triggered
     */
    public Action getAction() {
        return action;
    }

    /**
     * Set action to be triggered by this watchdog.
     * @param action Action to be triggered
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * Start the watchdog.
     */
    public void start() {
        reloadTimer = new java.util.Timer(true);
        reloadTimer.schedule(new ModelReloader(), 0, RELOAD_DELAY);
        action.putValue(Action.SMALL_ICON, CLOCK_ICON);
        isRunning = true;
    }

    /**
     * Stop the watchdog.
     */
    public void stop() {
        if (reloadTimer != null) reloadTimer.cancel();
        if (action != null) action.putValue(Action.SMALL_ICON, WATCH_ICON);
        isRunning = false;
    }
    
    /**
     * Is the watchdog timer currently running and triggering refreshes of the logs?
     * @return <code>true</code> if it is running, <code>false</code> otherwise.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Implementation of the task to reload the model.
     * 
     * <p>Date: May 26, 2005</p>
     * <p>Time: 2:04:38 PM</p>
     * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
     */
    private class ModelReloader extends TimerTask {
        public void run() {
            try {
                if (gcDocument.reloadModels(true)) {
                    //invoke and wait so that we don't reload before we actually displayed the new model
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            gcDocument.relayout();
                        }
                    });
                }
            } 
            catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

}
