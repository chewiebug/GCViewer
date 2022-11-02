package com.tagtraum.perf.gcviewer.view;

import com.tagtraum.perf.gcviewer.view.model.GCPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is the main window of GCViewer.
 *
 * Date: Jan 30, 2002
 * Time: 4:59:49 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class GCViewerGui extends JFrame {

    private Map<String, Action> actionMap;
    private JDesktopPane desktopPane;
    private GCViewerGuiToolBar toolBar;
    private GCPreferences preferences;

    private static boolean isShiftPressed;
    private static boolean isControlPressed;
    private static boolean isAltPressed;

    public static boolean isShiftPressed() {
        return isShiftPressed;
    }

    public static boolean isControlPressed() {
        return isControlPressed;
    }

    public static boolean isAltPressed() {
        return isAltPressed;
    }
    private RecentGCResourcesMenu recentResourceNamesMenu;

    public GCViewerGui() {
        super("tagtraum industries incorporated - GCViewer");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {

                if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_SHIFT) { isShiftPressed = true; }
                else if (e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_SHIFT) { isShiftPressed = false; }
                else if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_CONTROL) { isControlPressed = true; }
                else if (e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_CONTROL) { isControlPressed = false; }
                else if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ALT) { isAltPressed = true; }
                else if (e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_ALT) { isAltPressed = false; }
                return false;
            }
        });
    }
    
    public void addDocument(GCDocument gcDocument) {
        desktopPane.add(gcDocument);
        gcDocument.setSize(450, 300);
        gcDocument.setVisible(true);
        repaint();

        try {
            gcDocument.setSelected(true);
            gcDocument.setMaximum(true);
        } 
        catch (PropertyVetoException e) {
            e.printStackTrace();
        }
    }
    
    public Map<String, Action> getActionMap() {
        return actionMap;
    }

    public JDesktopPane getDesktopPane() {
        return desktopPane;
    }

    public GCPreferences getPreferences() {
        return preferences;
    }

    public RecentGCResourcesMenu getRecentGCResourcesMenu() {
        return recentResourceNamesMenu;
    }

    public GCDocument getSelectedGCDocument() {
        return (GCDocument)desktopPane.getSelectedFrame();
    }

    public List<GCDocument> getAllGCDocuments() {
        List<GCDocument> documents = new ArrayList<>();
        for (JInternalFrame frame : desktopPane.getAllFrames()) {
            if (frame instanceof GCDocument) {
                GCDocument document = (GCDocument) frame;
                documents.add(document);
            }
        }
        return documents;
    }

    public GCViewerGuiToolBar getToolBar() {
        return toolBar;
    }
    
    public void setActionMap(Map<String, Action> actionMap) {
        this.actionMap = actionMap;
    }
    
    /**
     * Sets the desktopPane if this gui component - this method should be called exactly once.
     * 
     * @param desktopPane desktopPane to be set
     */
    public void setDesktopPane(JDesktopPane desktopPane) {
        this.desktopPane = desktopPane;
        getContentPane().add(desktopPane, BorderLayout.CENTER);
    }
    
    public void setPreferences(GCPreferences preferences) {
        this.preferences = preferences;
    }
    
    /**
     * Sets the toolbar of this gui component - this method should be called exactly once.
     * 
     * @param toolBar toolbar to be set
     */
    public void setToolBar(GCViewerGuiToolBar toolBar) {
        this.toolBar = toolBar;
        getContentPane().add(toolBar, BorderLayout.NORTH);
    }
}
