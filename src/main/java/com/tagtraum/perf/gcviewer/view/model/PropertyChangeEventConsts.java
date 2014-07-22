package com.tagtraum.perf.gcviewer.view.model;

/**
 * Constants for property change events.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 16.07.2014</p>
 */
public interface PropertyChangeEventConsts {
    /** 
     * internal event: used to indicate changes in the "offset" property of the {@link Ruler} class
     * <br/>Parameters:
     * <br/> oldValue: offset before (Double)
     * <br/> newValue: offset after (Double)
     */
    String RULER_OFFSET_CHANGED = "rulerOffsetChanged";
    
    /** 
     * used to indicate that the format of the timestamp ruler has changed (seconds since beginning
     * vs. datestamps) 
     * <br/>Parameters:
     * <br/> oldValue: dateStamp was shown before (boolean)
     * <br/> newValue: dateStamp is shown now (boolean)
     */
    String MODELCHART_TIMESTAMP_RULER_FORMAT_CHANGED = "modelchartTimestampRulerFormatChanged";
    
    /**
     * Used to indicate that the state of the date / checkbox in the {@TImeOffsetPanel) has changed
     * <br/>Parameters
     * <br/> oldValue: checkbox state before
     * <br/> newValue: checkbox state now
     */
    String TIMEOFFSETPANEL_STATE_CHANGED = "timeoffsetpanelStateChanged";
}
