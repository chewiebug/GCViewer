package com.tagtraum.perf.gcviewer.ctrl;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tagtraum.perf.gcviewer.ctrl.FileDropTargetListener.DropFlavor;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.view.GCDocument;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.GCViewerGuiMenuBar;
import com.tagtraum.perf.gcviewer.view.model.RecentGCResourcesModel;

/**
 * Controller class for {@link GCModelLoader}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 15.12.2013</p>
 */
public class GCModelLoaderController {
    private GCViewerGui gcViewerGui;
    
    /**
     * Constructor is package protected, because this controller should only be instantiated in 
     * this package.
     */
    GCModelLoaderController(GCViewerGui gcViewerGui) {
        super();
        
        this.gcViewerGui = gcViewerGui;
    }
    
    public void add(File[] files) {
        List<GCResource> gcResourceList = new ArrayList<GCResource>();
        for (File file : files) {
            GCResource gcResource = new GCResource(file.getAbsolutePath());
            gcResourceList.add(gcResource);
            
            addGCResource(gcResource);
        }

        getRecentGCResourcesModel().add(gcResourceList);
    }
    
    public void add(GCResource gcResource) {
        add(Arrays.asList(new GCResource[] { gcResource }));
    }

    public void add(List<GCResource> gcResourceList) {
        for (GCResource gcResource : gcResourceList) {
            addGCResource(gcResource);
        }
        
        getRecentGCResourcesModel().add(gcResourceList);
    }
    
    private void addGCResource(GCResource gcResource) {
        GCModelLoader loader = new GCModelLoader(gcResource);
        GCDocumentController docController = getDocumentController(gcViewerGui.getSelectedGCDocument());
        docController.addGCResource(loader);
        
        loader.execute();
    }
    
    private GCDocumentController getDocumentController(GCDocument document) {
        GCDocumentController controller = null;
        for (PropertyChangeListener listener : document.getPropertyChangeListeners()) {
            if (listener instanceof GCDocumentController) {
                controller = (GCDocumentController) listener;
            }
        }
        
        assert controller != null : "instance of GCDocumentController should have been found!";
        
        return controller;
    }
    
    protected GCViewerGui getGCViewerGui() {
        return this.gcViewerGui;
    }
    
    private RecentGCResourcesModel getRecentGCResourcesModel() {
        return ((GCViewerGuiMenuBar) this.gcViewerGui.getJMenuBar()).getRecentGCResourcesModel();
    }
    
    private void openGCResource(GCResource gcResource) {
        GCModelLoader loader = new GCModelLoader(gcResource);
        GCDocument document = new GCDocument(gcViewerGui.getPreferences(), gcResource.getResourceName());
        document.setDropTarget(
                new DropTarget(document, 
                        DnDConstants.ACTION_COPY, 
                        new FileDropTargetListener(this, DropFlavor.ADD))
                );
        document.addInternalFrameListener(new GCViewerGuiInternalFrameController());
        
        gcViewerGui.addDocument(document);
        
        GCDocumentController docController = new GCDocumentController(document);
        docController.addGCResource(loader);
        
        loader.execute();
    }
    
    public void open(File[] files) {
        List<GCResource> gcResourceList = new ArrayList<GCResource>();
        for (File file : files) {
            GCResource gcResource = new GCResource(file.getAbsolutePath());
            gcResourceList.add(gcResource);
        }
        
        open(gcResourceList);
    }
    
    /**
     * Open a gc log resource from a filename or URL.
     * 
     * @param gcResource filename or URL name.
     */
    public void open(GCResource gcResource) {
        open(Arrays.asList(new GCResource[] { gcResource }));
    }
    
    public void open(List<GCResource> gcResourceList) {
        for (int i = 0; i < gcResourceList.size(); ++i) {
            GCResource gcResource = gcResourceList.get(i);
            
            if (i == 0) {
                openGCResource(gcResource);
            }
            else {
                addGCResource(gcResource);
            }
        }
        
        getRecentGCResourcesModel().add(gcResourceList);
    }
    
    /**
     * Reload all models of <code>gcDocument</code> and provide tracker. The tracker will
     * fire a propertyChangeEvent, as soon as all GCModelLoaders have finished loading.
     * 
     * @param gcDocument document of which models should be reloaded
     * @return tracker to track finish state of all models being loaded
     */
    public GCModelLoaderGroupTracker reload(GCDocument gcDocument) {
        GCModelLoaderGroupTracker tracker = new GCModelLoaderGroupTracker();
        for (GCResource gcResource : gcDocument.getGCResources()) {
            gcResource.setIsReload(true);
            GCModelLoader loader = new GCModelLoader(gcResource);
            GCDocumentController docController = getDocumentController(gcDocument);
            docController.reloadGCResource(loader);
            
            tracker.addGcModelLoader(loader);
        }
        
        tracker.execute();
        
        return tracker;
    }
    
    /**
     * Set GCViewerGui (for test purposes).
     * @param gui gui to be set
     */
    protected void setGCViewerGui(GCViewerGui gui) {
        this.gcViewerGui = gui;
    }
    
}
