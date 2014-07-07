package com.tagtraum.perf.gcviewer.ctrl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.SwingUtilities;

import com.tagtraum.perf.gcviewer.view.GCViewerGui;

/**
 * Main controller class of GCViewer. Is responsible for control flow. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 15.12.2013</p>
 */
public class GCViewerController {

    public void startGui(final String resourceName) {
        final GCViewerGui viewer = new GCViewerGui();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (resourceName != null) {
                    if (resourceName.startsWith("http")) {
                        try {
                            viewer.open(new URL[]{new URL(resourceName)});
                        } 
                        catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        viewer.open(new File[] {new File(resourceName)});
                    }
                }
            }
        });

    }
}
