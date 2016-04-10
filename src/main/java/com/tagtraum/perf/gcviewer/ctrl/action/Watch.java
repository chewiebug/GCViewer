package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoaderController;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.GCDocument;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.util.ImageHelper;

/**
 * (De)activate automatic reloading of all {@link GCResource} within one {@link GCDocument}.
 * 
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 1:59:59 PM
 */
public class Watch extends AbstractAction {
    private Logger logger = Logger.getLogger(Watch.class.getCanonicalName());
    
    private static final ImageIcon WATCH_ICON = ImageHelper.loadImageIcon("watch.png");
    private static final ImageIcon CLOCK_ICON = ImageHelper.loadImageIcon("clock.png");

    private GCModelLoaderController controller;
    private GCViewerGui gcViewer;
    
    private Map<GCDocument, TimerInfo> timerMap;

    public Watch(GCModelLoaderController controller, GCViewerGui gcViewer) {
        this.controller = controller;
        this.gcViewer = gcViewer;
        this.timerMap = new HashMap<GCDocument, TimerInfo>();
        
        putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_watch"));
        putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_watch"));
        putValue(MNEMONIC_KEY, new Integer(LocalisationHelper.getString("main_frame_menuitem_mnemonic_watch").charAt(0)));
        putValue(ACTION_COMMAND_KEY, ActionCommands.WATCH.toString());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        putValue(SMALL_ICON, WATCH_ICON);
        
        setEnabled(false);
    }

    public void actionPerformed(final ActionEvent ae) {
        final AbstractButton source = (AbstractButton)ae.getSource();
        if (source.isSelected()) {
            start();
        }
        else {
            stop(gcViewer.getSelectedGCDocument());
        }
    }

    private void start() {
        GCDocument gcDocument = gcViewer.getSelectedGCDocument();
        TimerInfo timerInfo = new TimerInfo(
                new RefreshWatchDog(controller, gcDocument),
                new GCDocumentCloseListener(gcDocument));

        gcDocument.setWatched(true);
        gcDocument.addInternalFrameListener(timerInfo.closeListener);
        timerMap.put(gcDocument, timerInfo);
        
        timerInfo.refreshWatchDog.start();
        putValue(SMALL_ICON, CLOCK_ICON);
    }
    
    private void stop(GCDocument gcDocument) {
        gcDocument.setWatched(false);
        TimerInfo timerInfo = timerMap.get(gcDocument);
        timerInfo.refreshWatchDog.stop();
        
        gcDocument.removeInternalFrameListener(timerInfo.closeListener);
        
        putValue(SMALL_ICON, WATCH_ICON);
    }
    
    public void setEnabled(final boolean newValue) {
        super.setEnabled(newValue);
        if (!newValue) {
            putValue(SMALL_ICON, WATCH_ICON);
        }
    }
    
    private class TimerInfo {
        RefreshWatchDog refreshWatchDog;
        GCDocumentCloseListener closeListener;
        
        public TimerInfo(RefreshWatchDog refreshWatchDog, GCDocumentCloseListener closeListener) {
            this.refreshWatchDog = refreshWatchDog;
            this.closeListener = closeListener;
        }
    }
    
    private class GCDocumentCloseListener extends InternalFrameAdapter {
        private GCDocument gcDocument;

        public GCDocumentCloseListener(GCDocument gcDocument) {
            this.gcDocument = gcDocument;
        }

        @Override
        public void internalFrameActivated(InternalFrameEvent e) {
            putValue(SMALL_ICON, CLOCK_ICON);
        }

        @Override
        public void internalFrameClosing(InternalFrameEvent e) {
            stop(this.gcDocument);
        }
        
        @Override
        public void internalFrameDeactivated(InternalFrameEvent e) {
            putValue(SMALL_ICON, WATCH_ICON);
        }

    }
}
