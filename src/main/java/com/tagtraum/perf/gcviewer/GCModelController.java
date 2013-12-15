package com.tagtraum.perf.gcviewer;

import java.awt.Component;
import java.beans.PropertyVetoException;
import java.io.File;
import java.net.URL;
import java.util.Arrays;

import javax.swing.JOptionPane;

import com.tagtraum.perf.gcviewer.util.LocalisationHelper;

/**
 * Controller class implementing all functionality needed to load GCModels.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 04.12.2013</p>
 */
public class GCModelController {
    private Component parent;
    
    public GCModelController(Component parent) {
        this.parent = parent;
    }
    
    public void open(File[] files) {
        
    }
    
    public void open(URL[] urls) {
        final int nUrls = urls.length; 
        try {
            if (nUrls >= 1) {               
                final String title = Arrays.asList(urls).toString();
                final GCDocument gcDocument = new GCDocument(this, title.substring(1, title.length() - 1));
                gcDocument.addInternalFrameListener(gcDocumentListener);
                desktopPane.add(gcDocument);
                gcDocument.setSelected(true);
                gcDocument.setSize(450, 300);
                gcDocument.setMaximum(true);
                //addAction.setSelectedFile(url);
                gcDocument.setVisible(true);
            }
            add(urls);
            recentURLsMenu.getRecentURLsModel().add(urls);
            repaint();
        } catch (PropertyVetoException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(GCViewerGui.this, e.toString() + " " + e.getLocalizedMessage(), LocalisationHelper.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadModel(String documentTitle, URL url) {
        GCModelLoader loader = new GCModelLoader(documentTitle, url);
        loader.execute();
    }
    
    public void add(File[] files) {
        
    }
    
    public void add(URL[] urls) {
        
    }
        
}
