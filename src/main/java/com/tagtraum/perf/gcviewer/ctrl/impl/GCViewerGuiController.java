package com.tagtraum.perf.gcviewer.ctrl.impl;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoaderController;
import com.tagtraum.perf.gcviewer.ctrl.action.OpenFile;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.GCDocument;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.GCViewerGuiMenuBar;
import com.tagtraum.perf.gcviewer.view.model.GCPreferences;
import com.tagtraum.perf.gcviewer.view.model.GCResourceGroup;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Main controller class of GCViewer. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 11.02.2014</p>
 */
public class GCViewerGuiController extends WindowAdapter {
    
    void applyPreferences(GCViewerGui gui, GCPreferences preferences) {
        // default visibility to be able to access it from unittests
        gui.setPreferences(preferences);
        if (preferences.isPropertiesLoaded()) {
            for (Entry<String, JCheckBoxMenuItem> menuEntry : ((GCViewerGuiMenuBar)gui.getJMenuBar()).getViewMenuItems().entrySet()) {
                JCheckBoxMenuItem item = menuEntry.getValue();
                item.setState(preferences.getGcLineProperty(menuEntry.getKey()));
                
                // TODO necessary? state is set above; no GCDocument open at this moment
                //viewMenuActionListener.actionPerformed(new ActionEvent(item, 0, item.getActionCommand()));
            }
            gui.setBounds(preferences.getWindowX(),
                    preferences.getWindowY(),
                    preferences.getWindowWidth(),
                    preferences.getWindowHeight());
            String lastfile = preferences.getLastFile();
            if (lastfile != null) {
                ((OpenFile)gui.getActionMap().get(ActionCommands.OPEN_FILE.toString())).setSelectedFile(new File(lastfile));
            }
            // recent files (add in reverse order, so that the order in recentMenu is correct
            List<String> recentFiles = preferences.getRecentFiles();
            for (int i = recentFiles.size()-1; i >= 0; --i) {
                String filename = recentFiles.get(i);
                if (filename.length() > 0) {
                    ((GCViewerGuiMenuBar)gui.getJMenuBar()).getRecentGCResourcesModel().add(filename);
                }
            }
        }
        else {
            gui.setBounds(0, 0, 800, 600);
        }
    }

    private void closeAllButSelectedDocument(GCViewerGui gui) {
        if (gui.getSelectedGCDocument() != null) {
            GCDocument selected = gui.getSelectedGCDocument();
            for (int i = gui.getDesktopPane().getComponentCount()-1; i > 0; --i) {
                if (gui.getDesktopPane().getComponent(i) != selected) {
                    ((JInternalFrame)gui.getDesktopPane().getComponent(i)).dispose();
                }
            }

            gui.getSelectedGCDocument().doDefaultCloseAction();
        }
    }
    
    /**
     * Copies values that are stored in menu items into <code>GCPreferences</code> instance.
     * 
     * @param gui source to copy values from
     * @return <code>GCPreferences</code> with current values
     */
    private GCPreferences copyPreferencesFromGui(GCViewerGui gui) {
        GCPreferences preferences = gui.getPreferences();
        for (Entry<String, JCheckBoxMenuItem> menuEntry : ((GCViewerGuiMenuBar)gui.getJMenuBar()).getViewMenuItems().entrySet()) {
            JCheckBoxMenuItem item = menuEntry.getValue();
            preferences.setGcLineProperty(item.getActionCommand(), item.getState());
        }
        preferences.setWindowWidth(gui.getWidth());
        preferences.setWindowHeight(gui.getHeight());
        preferences.setWindowX(gui.getX());
        preferences.setWindowY(gui.getY());
        OpenFile openFileAction = (OpenFile)gui.getActionMap().get(ActionCommands.OPEN_FILE.toString());
        if (openFileAction.getLastSelectedFiles().length != 0) {
            preferences.setLastFile(openFileAction.getLastSelectedFiles()[0].getAbsolutePath());
        }
        
        // recent files
        List<String> recentFileList = new LinkedList<String>();
        for (GCResourceGroup urlSet : ((GCViewerGuiMenuBar) gui.getJMenuBar()).getRecentGCResourcesModel().getResourceNameGroups()) {    
            recentFileList.add(urlSet.getUrlGroupString());
        }
        preferences.setRecentFiles(recentFileList);
        
        return preferences;
    }

    /**
     * Start graphical user interface and load a log file (resourceName - if not <code>null</code>).
     * 
     * @param gcResource {@link GCResource} to be loaded at startup or <code>null</code>
     * @throws InvocationTargetException Some problem trying to start the gui
     * @throws InterruptedException Some problem trying to start the gui
     */
    public void startGui(final GCResource gcResource) throws InvocationTargetException, InterruptedException {
        final GCViewerGui gcViewerGui = new GCViewerGui();
        final GCModelLoaderController modelLoaderController = new GCModelLoaderControllerImpl(gcViewerGui);
        
        Runnable guiStarter = new Runnable() {

            @Override
            public void run() {
                new GCViewerGuiBuilder().initGCViewerGui(gcViewerGui, modelLoaderController);
                applyPreferences(gcViewerGui, new GCPreferences());
                gcViewerGui.addWindowListener(GCViewerGuiController.this);
                Thread.setDefaultUncaughtExceptionHandler(new GCViewerUncaughtExceptionHandler(gcViewerGui));
                gcViewerGui.setVisible(true);
            }
        };
        
        SwingUtilities.invokeAndWait(guiStarter);
        
        if (gcResource != null) {
            Runnable resourceLoader = new Runnable() {

                @Override
                public void run() {
                    modelLoaderController.open(gcResource);
                }
                
            };
            SwingUtilities.invokeLater(resourceLoader);
        }
    }

    /**
     * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
     */
    @Override
    public void windowClosing(WindowEvent e) {
        // TODO SWINGWORKER fix closing of main window with correct storing of preferences
        closeAllButSelectedDocument(((GCViewerGui)e.getWindow()));
        
        GCPreferences preferences = copyPreferencesFromGui(((GCViewerGui)e.getWindow()));
        preferences.store();
        e.getWindow().dispose();
    }


}
