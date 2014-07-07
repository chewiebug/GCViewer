package com.tagtraum.perf.gcviewer.ctrl;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;

/**
 * Main controller class of GCViewer. Is responsible for control flow. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 15.12.2013</p>
 */
public class GCViewerController {
    private GCViewerGui gcViewerGui;
    
    protected GCViewerController() {
        super();
    }
    
    public void add(String resourceName) {
        addModel(resourceName);
    }
    
    public void add(File[] files) {
        for (File file : files) {
            add(file.getAbsolutePath());
        }
    }
    
    public void add(String[] resourceNames) {
        
    }
    
    private void addModel(String resourceName) {
        GCModelLoader loader = new GCModelLoader(new GCResource(resourceName));
        gcViewerGui.addModel(loader);
        
        loader.execute();
    }
    
    protected GCViewerGui getGCViewerGui() {
        return this.gcViewerGui;
    }
    
    private void openModel(String resourceName) {
        GCModelLoader loader = new GCModelLoader(new GCResource(resourceName));
        gcViewerGui.openModel(loader);
        
        loader.execute();
    }
    
    public void open(String[] resourceNames) {
        // TODO SWINGWORKER: introduce GCResourceGroup to accommodate "add"? -> Where is it held?
        String nameGroup = Arrays.asList(resourceNames).toString();
        nameGroup = nameGroup.substring(1, nameGroup.length() - 1);
        int i = 0; 
        for (String name : resourceNames) {
            if (i == 0) {
                openModel(name);
            }
            else {
                addModel(name);
            }
            ++i;
        }
    }
    
    public void open(File[] files) {
        List<String> fileNames = new LinkedList<String>();
        for (File file : files) {
            fileNames.add(file.getAbsolutePath());
        }
        open(fileNames.toArray(new String[fileNames.size()]));
    }
    
    /**
     * Open a gc log resource from a filename or URL.
     * 
     * @param fileOrUrl filename or URL name.
     */
    public void open(String fileOrUrl) {
        open(new String[]{ fileOrUrl });
    }
    
    /**
     * Open a new log file.
     * @param file filename
     */
    public void open(File file) {
        open(file.getAbsolutePath());
    }
        
    /**
     * Set GCViewerGui (for test purposes).
     * @param gui gui to be set
     */
    protected void setGCViewerGui(GCViewerGui gui) {
        this.gcViewerGui = gui;
    }
    
    /**
     * Start graphical user interface and load a log file (resourceName - if not <code>null</code>).
     * 
     * @param resourceName log file to be loaded at startup or <code>null</code>
     * @throws InvocationTargetException Some problem trying to start the gui
     * @throws InterruptedException Some problem trying to start the gui
     */
    public void startGui(final String resourceName) {
        Runnable guiStarter = new Runnable() {
            
            @Override
            public void run() {
                // TODO SWINGWORKER: Not parameter but register actionListener!!
                gcViewerGui = new GCViewerGui(GCViewerController.this);
                gcViewerGui.setVisible(true);
                if (resourceName != null) {
                    open(resourceName);
                }
            }
        };
        
        SwingUtilities.invokeLater(guiStarter);
    }

}
