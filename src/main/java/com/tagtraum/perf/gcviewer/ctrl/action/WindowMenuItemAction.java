package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;

/**
 * This action is used to control the selection of the items in the window menu.
 * 
 * Date: Jan 30, 2002
 * Time: 4:59:49 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class WindowMenuItemAction extends AbstractAction implements PropertyChangeListener {
    private JInternalFrame internalFrame;

    public WindowMenuItemAction(final InternalFrameEvent e) {
        this.internalFrame = e.getInternalFrame();
        putValue(Action.NAME, internalFrame.getTitle());
        this.internalFrame.addPropertyChangeListener("title", this);
    }

    public void actionPerformed(final ActionEvent ae) {
        try {
            internalFrame.setSelected(true);
        } 
        catch (PropertyVetoException e1) {
            e1.printStackTrace();
        }
    }

    public JInternalFrame getInternalFrame() {
        return internalFrame;
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        putValue(Action.NAME, internalFrame.getTitle());
    }
}