package com.tagtraum.perf.gcviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimerTask;

/**
 *
 * Date: May 26, 2005
 * Time: 2:04:38 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class RefreshWatchDog {
    private GCDocument gcDocument;
    private javax.swing.Timer animationTimer;
    private java.util.Timer reloadTimer;
    private Action action;
    public static final ImageIcon WATCH_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(GCViewerGui.class.getResource("images/watch.png")));
    private static final ImageIcon CLOCK_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(GCViewerGui.class.getResource("images/clock.png")));
    /*
    private static final ImageIcon CLOCK_N_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(GCViewer.class.getResource("images/clock_n.gif")));
    private static final ImageIcon CLOCK_NE_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(GCViewer.class.getResource("images/clock_ne.gif")));
    private static final ImageIcon CLOCK_E_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(GCViewer.class.getResource("images/clock_e.gif")));
    private static final ImageIcon CLOCK_SE_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(GCViewer.class.getResource("images/clock_se.gif")));
    private static final ImageIcon CLOCK_S_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(GCViewer.class.getResource("images/clock_s.gif")));
    private static final ImageIcon CLOCK_SW_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(GCViewer.class.getResource("images/clock_sw.gif")));
    private static final ImageIcon CLOCK_W_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(GCViewer.class.getResource("images/clock_w.gif")));
    private static final ImageIcon CLOCK_NW_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(GCViewer.class.getResource("images/clock_nw.gif")));
    private static final ImageIcon[] CLOCK_ICONS = new ImageIcon[] {CLOCK_N_ICON, CLOCK_NE_ICON, CLOCK_E_ICON, CLOCK_SE_ICON, CLOCK_S_ICON, CLOCK_SW_ICON, CLOCK_W_ICON, CLOCK_NW_ICON};
    */
    private static final int ANIMATION_DELAY = 250;
    private static final int RELOAD_DELAY = 1000;

    public GCDocument getGcDocument() {
        return gcDocument;
    }

    public void setGcDocument(GCDocument gcDocument) {
        this.gcDocument = gcDocument;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void start() {
        animationTimer = new javax.swing.Timer(ANIMATION_DELAY, new ClockAnimation());
        reloadTimer = new java.util.Timer(true);
        reloadTimer.schedule(new ModelReloader(), 0, RELOAD_DELAY);
        action.putValue(Action.SMALL_ICON, CLOCK_ICON);
        animationTimer.start();
    }

    public void stop() {
        if (reloadTimer != null) reloadTimer.cancel();
        if (animationTimer != null && animationTimer.isRunning()) animationTimer.stop();
        if (action != null) action.putValue(Action.SMALL_ICON, WATCH_ICON);
    }

    private class ClockAnimation implements ActionListener {
        private int clockIndex;
        public void actionPerformed(final ActionEvent e) {
            if (action != null) {
                //clockIndex = (clockIndex+1) % CLOCK_ICONS.length;
                //action.putValue(Action.SMALL_ICON, CLOCK_ICONS[clockIndex]);
            }
        }
    }

    private class ModelReloader extends TimerTask {
        public void run() {
            try {
                if (gcDocument.reloadModels(true)) {
                    //invoke and wait so that we don't reload before we actually displayed the new model
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            gcDocument.relayout();
                            //gcDocument.scrollToRightEdge();
                            //gcDocument.repaint();
                        }
                    });
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

}
