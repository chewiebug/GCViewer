package com.tagtraum.perf.gcviewer.view;

import java.awt.Component;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.model.RecentGCResourcesModel;
import com.tagtraum.perf.gcviewer.view.model.StayOpenCheckBoxMenuItem;
import com.tagtraum.perf.gcviewer.view.util.OSXSupport;

/**
 * MenuBar for GCViewerGui. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 07.02.2014</p>
 */
public class GCViewerGuiMenuBar extends JMenuBar {
    private Map<String, StayOpenCheckBoxMenuItem> viewMenuItemMap;
    private JMenu fileMenu;
    private RecentGCResourcesModel recentResourceNamesModel;
    private JMenu viewMenu;
    private JMenu windowMenu;
    /** control that only one checkbox menuitem inside the window menu can be active at a time */
    private ButtonGroup windowMenuCheckBoxGroup;
    private JMenu helpMenu;
    
    public GCViewerGuiMenuBar() {
        super();
        
        viewMenuItemMap = new HashMap<String, StayOpenCheckBoxMenuItem>();
        
        fileMenu = new JMenu(LocalisationHelper.getString("main_frame_menu_file"));
        fileMenu.setMnemonic(LocalisationHelper.getString("main_frame_menu_mnemonic_file").charAt(0));
        add(fileMenu);
        
        viewMenu = new JMenu(LocalisationHelper.getString("main_frame_menu_view"));
        viewMenu.setMnemonic(LocalisationHelper.getString("main_frame_menu_mnemonic_view").charAt(0));
        add(viewMenu);
        
        windowMenu = new JMenu(LocalisationHelper.getString("main_frame_menu_window"));
        windowMenu.setMnemonic(LocalisationHelper.getString("main_frame_menu_mnemonic_window").charAt(0));
        add(windowMenu);
        windowMenuCheckBoxGroup = new ButtonGroup();

        helpMenu = new JMenu(LocalisationHelper.getString("main_frame_menu_help"));
        helpMenu.setMnemonic(LocalisationHelper.getString("main_frame_menu_mnemonic_help").charAt(0));
        if ( ! OSXSupport.isOSX()) {
            add(helpMenu);
        }
    }
    
    public void addSeparatorToViewMenu() {
        viewMenu.addSeparator();
    }
    
    public void addSeparatorToWindowMenu() {
        windowMenu.addSeparator();
    }

    public void addToFileMenu(JMenuItem menuItem) {
        fileMenu.add(menuItem);
    }
    
    public void addToFileMenu(RecentGCResourcesMenu recentResourceNamesMenu) {
        addToFileMenu((JMenuItem) recentResourceNamesMenu);
        this.recentResourceNamesModel = recentResourceNamesMenu.getRecentResourceNamesModel();
    }
    
    public void addToFileMenu(Action action) {
        fileMenu.add(action);
    }
    
    public void addToHelpMenu(Action action) {
        helpMenu.add(action);
    }
    
    public void addToViewMenu(String key, StayOpenCheckBoxMenuItem menuItem) {
        viewMenu.add(menuItem);
        viewMenuItemMap.put(key, menuItem);
    }
    
    public void addToViewMenu(Action action) {
        viewMenu.add(action);
    }
    
    /**
     * Adds <code>menuItem</code> to the internal list of view menu items, but doesn't display
     * this item.
     * 
     * @param key unique key for storage of this <code>menuItem</code> in the internal map
     * @param menuItem item to be stored in the map
     */
    public void addToViewMenuInvisible(String key, StayOpenCheckBoxMenuItem menuItem) {
        viewMenuItemMap.put(key, menuItem);
    }
    
    /**
     * Add action to "window" menu.
     * @param action action to be added
     */
    public void addToWindowMenu(Action action) {
        windowMenu.add(action);
    }
    
    /**
     * Add checkbox item to window menu group (only one of these items can be active at a time).
     * @param menuItem checkbox item to be added
     */
    public void addToWindowMenuGroup(StayOpenCheckBoxMenuItem menuItem) {
        windowMenu.add(menuItem);
        windowMenuCheckBoxGroup.add(menuItem);
    }
    
    /**
     * Remove a menuItem from the window menu including removal of the group. 
     * @param menuItem
     */
    public void removeFromWindowMenuGroup(JMenuItem menuItem) {
        windowMenu.remove(menuItem);
        windowMenuCheckBoxGroup.remove(menuItem);
    }
    
    public JMenu getFileMenu() {
        return fileMenu;
    }
    
    public RecentGCResourcesModel getRecentGCResourcesModel() {
        assert recentResourceNamesModel != null : "recentResourceNamesModel is not initialized";
        return recentResourceNamesModel;
    }
    
    public JMenu getViewMenu() {
        return viewMenu;
    }
    
    public Map<String, StayOpenCheckBoxMenuItem> getViewMenuItems() {
        return Collections.unmodifiableMap(viewMenuItemMap);
    }
    
    public JMenu getWindowMenu() {
        return windowMenu;
    }

    /**
     * Returns StayOpenCheckBoxMenuItem, which is associated with "WATCH" action command.
     * 
     * @return "WATCH" menu item
     */
    public StayOpenCheckBoxMenuItem getWatchMenuItem() {
        for (Component component : fileMenu.getMenuComponents()) {
            if (component instanceof StayOpenCheckBoxMenuItem
                && ((StayOpenCheckBoxMenuItem) component).getActionCommand().equals(ActionCommands.WATCH.toString())) {
                
                return (StayOpenCheckBoxMenuItem) component;
            }
        }
        
        throw new IllegalStateException("StayOpenCheckBoxMenuItem with actionCommand '" + ActionCommands.WATCH + "' not found");
    }
}
