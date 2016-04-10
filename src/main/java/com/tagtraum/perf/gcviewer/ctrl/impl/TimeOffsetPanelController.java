package com.tagtraum.perf.gcviewer.ctrl.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.tagtraum.perf.gcviewer.view.GCDocument;
import com.tagtraum.perf.gcviewer.view.TimeOffsetPanel;
import com.tagtraum.perf.gcviewer.view.model.PropertyChangeEventConsts;

/**
 * Deal with events fired by {@link TimeOffsetPanel}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 25.07.2014</p>
 */
public class TimeOffsetPanelController implements PropertyChangeListener {

    private GCDocument gcDocument;

    public TimeOffsetPanelController(GCDocument gcDocument) {
        super();
        
        this.gcDocument = gcDocument;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyChangeEventConsts.TIMEOFFSETPANEL_STATE_CHANGED.equals(evt.getPropertyName())) {
            this.gcDocument.getModelChart().setShowDateStamp((Boolean)evt.getNewValue());
        }
    }
}
