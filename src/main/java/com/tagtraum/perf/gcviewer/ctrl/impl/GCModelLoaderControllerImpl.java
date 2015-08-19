package com.tagtraum.perf.gcviewer.ctrl.impl;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoader;
import com.tagtraum.perf.gcviewer.ctrl.GCModelLoaderController;
import com.tagtraum.perf.gcviewer.ctrl.GCModelLoaderGroupTracker;
import com.tagtraum.perf.gcviewer.ctrl.impl.FileDropTargetListener.DropFlavor;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.view.GCDocument;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.GCViewerGuiMenuBar;
import com.tagtraum.perf.gcviewer.view.model.RecentGCResourcesModel;

import javax.swing.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Controller class for {@link GCModelLoader}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 15.12.2013</p>
 */
public class GCModelLoaderControllerImpl implements GCModelLoaderController {
    private GCViewerGui gcViewerGui;
    
    /**
     * Constructor is package protected, because this controller should only be instantiated in 
     * this package.
     */
    GCModelLoaderControllerImpl(GCViewerGui gcViewerGui) {
        super();
        
        this.gcViewerGui = gcViewerGui;
    }
    
    @Override
    public void add(File[] files) {
        List<GCResource> gcResourceList = new ArrayList<>();
        for (File file : files) {
            GCResource gcResource = new GCResource(file.getAbsolutePath());
            gcResourceList.add(gcResource);
            
            addGCResource(gcResource);
        }

        getRecentGCResourcesModel().add(gcResourceList);
    }
    
    @Override
    public void add(GCResource gcResource) {
        add(Arrays.asList(new GCResource[]{gcResource}));
    }

    @Override
    public void add(List<GCResource> gcResourceList) {
        for (GCResource gcResource : gcResourceList) {
            gcResource.reset();
            addGCResource(gcResource);
        }
        
        getRecentGCResourcesModel().add(gcResourceList);
    }
    
    private void addGCResource(GCResource gcResource) {
        GCModelLoader loader = new GCModelLoaderImpl(gcResource);
        GCDocumentController docController = getDocumentController(gcViewerGui.getSelectedGCDocument());
        docController.addGCResource(loader, getViewMenuController());
        
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
    
    private ViewMenuController getViewMenuController() {
        Map<String, JCheckBoxMenuItem> viewMenuItems 
            = ((GCViewerGuiMenuBar) this.gcViewerGui.getJMenuBar()).getViewMenuItems();

        assert viewMenuItems.size() > 0 : "viewMenuItems is not initialised!!";
        
        JCheckBoxMenuItem menuItem = viewMenuItems.values().iterator().next();
        for (ActionListener actionListener : menuItem.getActionListeners()) {
            if (actionListener instanceof ViewMenuController) {
                return (ViewMenuController) actionListener;
            }
        }
        
        throw new IllegalStateException("no ActionListener of type 'ViewMenuController' found");
    }
    
    private void openGCResource(GCResource gcResource) {
        GCModelLoader loader = new GCModelLoaderImpl(gcResource);
        GCDocument document = new GCDocument(gcViewerGui.getPreferences(), gcResource.getResourceName());
        document.setDropTarget(
                new DropTarget(document, 
                        DnDConstants.ACTION_COPY, 
                        new FileDropTargetListener(this, DropFlavor.ADD))
                );
        document.addInternalFrameListener(new GCViewerGuiInternalFrameController());
        
        gcViewerGui.addDocument(document);
        
        GCDocumentController docController = new GCDocumentController(document);
        docController.addGCResource(loader, getViewMenuController());
        
        loader.execute();
    }
    
    @Override
    public void open(File[] files) {
        List<GCResource> gcResourceList = new ArrayList<GCResource>();
        for (File file : files) {
            GCResource gcResource = new GCResource(file.getAbsolutePath());
            gcResourceList.add(gcResource);
        }
        
        open(gcResourceList);
    }
    
    @Override
    public void open(GCResource gcResource) {
        gcResource.reset();
        open(Arrays.asList(new GCResource[]{gcResource}));
    }
    
    @Override
    public void open(List<GCResource> gcResourceList) {
        for (int i = 0; i < gcResourceList.size(); ++i) {
            GCResource gcResource = new GCResource(gcResourceList.get(i).getResourceName());

            if (i == 0) {
                openGCResource(gcResource);
            }
            else {
                addGCResource(gcResource);
            }
        }
        
        getRecentGCResourcesModel().add(gcResourceList);
    }
    
    @Override
    public GCModelLoaderGroupTracker reload(GCDocument gcDocument) {
        GCModelLoaderGroupTracker tracker = new GCModelLoaderGroupTrackerImpl();
        for (GCResource gcResource : gcDocument.getGCResources()) {
            if (gcResource.hasUnderlyingResourceChanged()) {
                gcResource.reset();
                gcResource.setIsReload(true);
                GCModelLoader loader = new GCModelLoaderImpl(gcResource);
                GCDocumentController docController = getDocumentController(gcDocument);
                docController.reloadGCResource(loader);

                tracker.addGcModelLoader(loader);
            }
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
