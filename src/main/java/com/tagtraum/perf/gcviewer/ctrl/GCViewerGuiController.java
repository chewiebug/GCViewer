package com.tagtraum.perf.gcviewer.ctrl;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.SwingUtilities;

import com.tagtraum.perf.gcviewer.ctrl.action.OpenFile;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.GCViewerGuiMenuBar;
import com.tagtraum.perf.gcviewer.view.model.GCPreferences;

/**
 * Main controller class of GCViewer. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 11.02.2014</p>
 */
public class GCViewerGuiController extends WindowAdapter {
    private static final Logger LOGGER = Logger.getLogger(GCViewerGuiController.class.getName());
    
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
            final String lastfile = preferences.getLastFile();
            if (lastfile != null) {
                ((OpenFile)gui.getActionMap().get(ActionCommands.OPEN_FILE.toString())).setSelectedFile(new File(lastfile));
            }
            // recent files
            List<String> recentFiles = preferences.getRecentFiles();
            for (String filename : recentFiles) {
                final String[] tokens = filename.split(";");
                final List<URL> urls = new LinkedList<>();
                for (String token : tokens) {
                    try {
                        urls.add(new URL(token));
                    } 
                    catch (MalformedURLException e) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine("problem tokenizing recent file list: " + e.toString());
                        }
                    }
                }
                if (urls.size() > 0) {
                    // TODO SWINGWORKER set recentURLsMenu from properties
                    //recentURLsMenu.getRecentURLsModel().add(urls.toArray(new URL[0]));
                }
            }
        }
        else {
            gui.setBounds(0, 0, 800, 600);
        }
    }

    private GCPreferences retrievePreferences(GCViewerGui gui) {
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
        // TODO SWINGWORKER deal with recentURLsMenu to store in properties
        List<String> recentFileList = new LinkedList<String>();
//        for (Component recentMenuItem : recentURLsMenu.getMenuComponents()) {
//            final OpenRecent openRecent = (OpenRecent)((JMenuItem)recentMenuItem).getAction();
//            final URL[] urls = openRecent.getURLs();
//            final StringBuffer sb = new StringBuffer();
//            for (int j=0; j<urls.length; j++) {
//                sb.append(urls[j]);
//                sb.append(';');
//            }
//            recentFileList.add(sb.toString());
//        }
        preferences.setRecentFiles(recentFileList);
        
        return preferences;
    }

    /**
     * Start graphical user interface and load a log file (resourceName - if not <code>null</code>).
     * 
     * @param resourceName log file to be loaded at startup or <code>null</code>
     * @throws InvocationTargetException Some problem trying to start the gui
     * @throws InterruptedException Some problem trying to start the gui
     */
    public void startGui(final String resourceName) throws InvocationTargetException, InterruptedException {
        final GCViewerGui gcViewerGui = new GCViewerGui();
        final GCModelLoaderController modelLoaderController = new GCModelLoaderController(gcViewerGui);
        
        Runnable guiStarter = new Runnable() {
            
            @Override
            public void run() {
                new GCViewerGuiBuilder().initGCViewerGui(gcViewerGui, modelLoaderController);
                applyPreferences(gcViewerGui, new GCPreferences());
                Thread.setDefaultUncaughtExceptionHandler(new GCViewerUncaughtExceptionHandler(gcViewerGui));
                gcViewerGui.setVisible(true);
            }
        };
        
        SwingUtilities.invokeAndWait(guiStarter);
        
        if (resourceName != null) {
            Runnable resourceLoader = new Runnable() {

                @Override
                public void run() {
                    modelLoaderController.open(resourceName);
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
        GCPreferences preferences = retrievePreferences(((GCViewerGui)e.getSource()));
        preferences.store();
    }


}
