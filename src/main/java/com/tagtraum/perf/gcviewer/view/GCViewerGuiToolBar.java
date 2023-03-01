package com.tagtraum.perf.gcviewer.view;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.tagtraum.perf.gcviewer.view.model.PropertyChangeEventConsts;

/**
 * Toolbar for {@link GCViewerGui}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 11.02.2014</p>
 */
public class GCViewerGuiToolBar extends JToolBar implements PropertyChangeListener {

    private JToggleButton watchToggle;
    private JComboBox<String> zoomComboBox;
    
    public void addWatchToggleButton(JToggleButton watchToggle) {
        this.watchToggle = watchToggle;
        add(watchToggle);
    }
    
    public void addZoomComboBox(JComboBox<String> zoomComboBox) {
        this.zoomComboBox = zoomComboBox;
        add(zoomComboBox);
    }
    
    public JToggleButton getWatchToggleButton() {
        return watchToggle;
    }
    
    public JComboBox<String> getZoomComboBox() {
        return zoomComboBox;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PropertyChangeEventConsts.MODELCHART_SCALEFACTOR_CHANGED)) {
            // don't fire change event to prevent updating twice
            updateWithoutFiringActionEvent(getZoomComboBox(), () -> getZoomComboBox().setSelectedItem(Math.round((double)evt.getNewValue() * 1000.0) + "%"));
        }
    }

    private void updateWithoutFiringActionEvent(final JComboBox<String> comboBox, final Runnable runnable) {
        final ActionListener[] actionListeners = comboBox.getActionListeners();
        for (final ActionListener listener : actionListeners)
            comboBox.removeActionListener(listener);
        try {
            runnable.run();
        } finally {
            for (final ActionListener listener : actionListeners)
                comboBox.addActionListener(listener);
        }
    }
}
