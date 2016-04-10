package com.tagtraum.perf.gcviewer.ctrl.impl;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * UncaughtExceptionHandler for GCViewer - makes sure, no exception goes completely unnoticed. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 15.01.2014</p>
 */
public class GCViewerUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Component parent;

    public GCViewerUncaughtExceptionHandler(Component parent) {
        super();
        
        this.parent = parent;
    }
    
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(parent, t.getName() + ": " + e.toString());
    }

}
