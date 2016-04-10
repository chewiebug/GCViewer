package com.tagtraum.perf.gcviewer.view;

import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * Toolbar for {@link GCViewerGui}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 11.02.2014</p>
 */
public class GCViewerGuiToolBar extends JToolBar {

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
}
