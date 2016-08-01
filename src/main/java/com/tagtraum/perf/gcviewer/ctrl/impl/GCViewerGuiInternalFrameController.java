package com.tagtraum.perf.gcviewer.ctrl.impl;

import java.util.Map;
import java.util.Map.Entry;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.tagtraum.perf.gcviewer.ctrl.action.WindowMenuItemAction;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.GCDocument;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.GCViewerGuiMenuBar;
import com.tagtraum.perf.gcviewer.view.GCViewerGuiToolBar;
import com.tagtraum.perf.gcviewer.view.model.GCPreferences;

/**
 * Controller class for internal frames of {@link GCViewerGui}. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 11.02.2014</p>
 */
public class GCViewerGuiInternalFrameController extends InternalFrameAdapter {

    @Override
    public void internalFrameOpened(InternalFrameEvent e) {
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(new WindowMenuItemAction(e));
        
        getMenuBar(e).addToWindowMenuGroup(menuItem);
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {
        JInternalFrame internalFrame = e.getInternalFrame();
        internalFrame.removeInternalFrameListener(this);
        internalFrame.getRootPane().remove(internalFrame);

        if (internalFrame.getRootPane().getComponentCount() == 0) {
            getActionMap(e).get(ActionCommands.ARRANGE.toString()).setEnabled(false);
        }
        
        // remove menuitem from menu and from button group
        JMenu windowMenu = getMenuBar(e).getWindowMenu();
        for (int i = 2; i < windowMenu.getItemCount(); i++) {
            JMenuItem item = windowMenu.getItem(i);
            if (((WindowMenuItemAction) item.getAction()).getInternalFrame() == internalFrame) {
                getMenuBar(e).removeFromWindowMenuGroup(item);
                break;
            }
        }

        // if this internalFrame is the last to be open, update the menu state
        // -> otherwise any settings done by the user are lost
        if (getGCViewerGui(e).getDesktopPane().getComponentCount() == 1) {
            updateMenuItemState(e);

            // set same menustate, when the last is closed as is set for deactivated
            internalFrameDeactivated(e);
        }

        // if some thread is still loading, it should stop now
        getSelectedGCDocument(e).getGCResources().stream().forEach(gcResource -> gcResource.setIsReadCancelled(true));
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {
        for (int i=2; i < getMenuBar(e).getWindowMenu().getItemCount(); i++) {
            final JMenuItem item = getMenuBar(e).getWindowMenu().getItem(i);
            if (((WindowMenuItemAction)item.getAction()).getInternalFrame() == e.getInternalFrame()) {
                item.setSelected(true);
                break;
            }
        }
        
        getActionMap(e).get(ActionCommands.EXPORT.toString()).setEnabled(true);
        getActionMap(e).get(ActionCommands.REFRESH.toString()).setEnabled(true);
        getActionMap(e).get(ActionCommands.WATCH.toString()).setEnabled(true);
        getActionMap(e).get(ActionCommands.ZOOM.toString()).setEnabled(true);
        getActionMap(e).get(ActionCommands.ARRANGE.toString()).setEnabled(true);

        // setSelected() does not fire ActionEvent -> both buttons have to be changed
        getMenuBar(e).getWatchMenuItem().setSelected(getSelectedGCDocument(e).isWatched());
        getToolBar(e).getWatchToggleButton().setSelected(getSelectedGCDocument(e).isWatched());
        
        updateMenuItemState(e);
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {
        getActionMap(e).get(ActionCommands.EXPORT.toString()).setEnabled(false);
        getActionMap(e).get(ActionCommands.REFRESH.toString()).setEnabled(false);
        getActionMap(e).get(ActionCommands.WATCH.toString()).setEnabled(false);
        getActionMap(e).get(ActionCommands.ZOOM.toString()).setEnabled(false);

        // setSelected() does not fire ActionEvent -> both buttons have to be changed
        getMenuBar(e).getWatchMenuItem().setSelected(false);
        getToolBar(e).getWatchToggleButton().setSelected(false);
    }
    
    private Map<String, Action> getActionMap(InternalFrameEvent e) {
        return getGCViewerGui(e).getActionMap();
    }
    
    private GCViewerGui getGCViewerGui(InternalFrameEvent e) {
        return (GCViewerGui) e.getInternalFrame().getDesktopPane().getTopLevelAncestor();
    }
    
    private GCViewerGuiMenuBar getMenuBar(InternalFrameEvent e) {
        return (GCViewerGuiMenuBar) getGCViewerGui(e).getJMenuBar();
    }
    
    private GCDocument getSelectedGCDocument(InternalFrameEvent e) {
        return getGCViewerGui(e).getSelectedGCDocument();
    }
    
    private GCViewerGuiToolBar getToolBar(InternalFrameEvent e) {
        return (GCViewerGuiToolBar) getGCViewerGui(e).getToolBar();
    }
    
    private void updateMenuItemState(InternalFrameEvent e) {
        getToolBar(e).getZoomComboBox().setSelectedItem(
                (int) (getSelectedGCDocument(e).getModelChart().getScaleFactor() * 1000.0) + "%");
        GCPreferences preferences = getSelectedGCDocument(e).getPreferences();
        for (Entry<String, JCheckBoxMenuItem> menuEntry : getMenuBar(e).getViewMenuItems().entrySet()) {
            JCheckBoxMenuItem item = menuEntry.getValue();
            item.setState(preferences.getGcLineProperty(menuEntry.getKey()));
        }
    }

}
