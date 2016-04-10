package com.tagtraum.perf.gcviewer.ctrl.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.JCheckBoxMenuItem;

import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.GCViewerGuiMenuBar;
import com.tagtraum.perf.gcviewer.view.model.GCPreferences;
import com.tagtraum.perf.gcviewer.view.model.PropertyChangeEventConsts;

/**
 * Deals with all actions for the "view".
 *
 * Date: Jan 30, 2002
 * Time: 4:59:49 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ViewMenuController implements ActionListener, PropertyChangeListener {
    private static final Logger LOGGER = Logger.getLogger(ViewMenuController.class.getName());
    private GCViewerGui gui;

    /**
     * @param gui
     */
    public ViewMenuController(GCViewerGui gui) {
        this.gui = gui;
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if (gui.getSelectedGCDocument() == null) {
            // TODO SWINGWORKER: nothing to do here?
            return;
        }

        boolean state = ((JCheckBoxMenuItem)e.getSource()).getState();
        if (GCPreferences.SHOW_MODEL_METRICS_PANEL.equals(e.getActionCommand())) {
            gui.getSelectedGCDocument().setShowModelMetricsPanel(state);
        }
        else if (GCPreferences.SHOW_DATE_STAMP.equals(e.getActionCommand())) {
            LOGGER.fine("setShowDateStamp(" + state + ")");
            gui.getSelectedGCDocument().getModelChart().setShowDateStamp(state);
        }
        else if (GCPreferences.FULL_GC_LINES.equals(e.getActionCommand())) {
            gui.getSelectedGCDocument().getModelChart().setShowFullGCLines(state);
        }
        else if (GCPreferences.INC_GC_LINES.equals(e.getActionCommand())) {
            gui.getSelectedGCDocument().getModelChart().setShowIncGCLines(state);
        }
        else if (GCPreferences.GC_TIMES_LINE.equals(e.getActionCommand())) {
            gui.getSelectedGCDocument().getModelChart().setShowGCTimesLine(state);
        }
        else if (GCPreferences.GC_TIMES_RECTANGLES.equals(e.getActionCommand())) {
            gui.getSelectedGCDocument().getModelChart().setShowGCTimesRectangles(state);
        }
        else if (GCPreferences.TOTAL_MEMORY.equals(e.getActionCommand())) {
            gui.getSelectedGCDocument().getModelChart().setShowTotalMemoryLine(state);
        }
        else if (GCPreferences.USED_MEMORY.equals(e.getActionCommand())) {
            gui.getSelectedGCDocument().getModelChart().setShowUsedMemoryLine(state);
        }
        else if (GCPreferences.USED_TENURED_MEMORY.equals(e.getActionCommand())) {
            gui.getSelectedGCDocument().getModelChart().setShowUsedTenuredMemoryLine(state);
        }
        else if (GCPreferences.USED_YOUNG_MEMORY.equals(e.getActionCommand())) {
            gui.getSelectedGCDocument().getModelChart().setShowUsedYoungMemoryLine(state);
        }
        else if (GCPreferences.TENURED_MEMORY.equals(e.getActionCommand())) {
            gui.getSelectedGCDocument().getModelChart().setShowTenured(state);
        }
        else if (GCPreferences.YOUNG_MEMORY.equals(e.getActionCommand())) {
            gui.getSelectedGCDocument().getModelChart().setShowYoung(state);
        }
        else if (GCPreferences.INITIAL_MARK_LEVEL.equals(e.getActionCommand())) {
            gui.getSelectedGCDocument().getModelChart().setShowInitialMarkLevel(state);
        }
        else if (GCPreferences.CONCURRENT_COLLECTION_BEGIN_END.equals(e.getActionCommand())) {
            gui.getSelectedGCDocument().getModelChart().setShowConcurrentCollectionBeginEnd(state);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyChangeEventConsts.MODELCHART_TIMESTAMP_RULER_FORMAT_CHANGED.equals(evt.getPropertyName())) {
            if (gui.getSelectedGCDocument() != null) {
            gui.getSelectedGCDocument().getModelChart().setShowDateStamp((Boolean)evt.getNewValue());
            ((GCViewerGuiMenuBar) gui.getJMenuBar()).getViewMenuItems()
                .get(GCPreferences.SHOW_DATE_STAMP).setSelected((Boolean)evt.getNewValue());
        }
    }
}
}