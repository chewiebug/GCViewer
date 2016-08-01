package com.tagtraum.perf.gcviewer.ctrl.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JToggleButton;

/**
 * ActionListener to synchronize state between two buttons in Menu / on ToolBar. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 11.02.2014</p>
 */
public class WatchStateController implements ActionListener {

    private JCheckBoxMenuItem menuItemWatch;
    private JToggleButton watchToggle;
    
    public WatchStateController(JCheckBoxMenuItem menuItemWatch, JToggleButton watchToggle) {
        super();
        
        this.menuItemWatch = menuItemWatch;
        this.watchToggle = watchToggle;
    }
    
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == menuItemWatch) {
            watchToggle.setSelected(menuItemWatch.getState());
        }
        else {
            menuItemWatch.setState(watchToggle.isSelected());
        }
    }

}
