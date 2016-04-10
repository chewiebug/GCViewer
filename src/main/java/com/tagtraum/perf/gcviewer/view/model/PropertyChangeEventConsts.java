package com.tagtraum.perf.gcviewer.view.model;

/**
 * Constants for property change events.
 */
public interface PropertyChangeEventConsts {
    /** 
     * internal event: used to indicate changes in the "offset" property of the
     * <code>ModelChartImpl.Ruler</code> class
     *
     * <p>Parameters:
     * <p> oldValue: offset before (Double)
     * <p> newValue: offset after (Double)
     */
    String RULER_OFFSET_CHANGED = "rulerOffsetChanged";
    
    /** 
     * used to indicate that the format of the timestamp ruler has changed (seconds since beginning
     * vs. datestamps)
     *
     * <p>Parameters:
     * <p> oldValue: dateStamp was shown before (boolean)
     * <p> newValue: dateStamp is shown now (boolean)
     */
    String MODELCHART_TIMESTAMP_RULER_FORMAT_CHANGED = "modelchartTimestampRulerFormatChanged";
    
    /**
     * Used to indicate that the state of the date / checkbox in the
     * {@link com.tagtraum.perf.gcviewer.view.TimeOffsetPanel} has changed
     *
     * <p>Parameters
     * <p> oldValue: checkbox state before
     * <p> newValue: checkbox state now
     */
    String TIMEOFFSETPANEL_STATE_CHANGED = "timeoffsetpanelStateChanged";
}
