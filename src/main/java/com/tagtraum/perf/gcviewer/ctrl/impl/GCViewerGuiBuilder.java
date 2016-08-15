package com.tagtraum.perf.gcviewer.ctrl.impl;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoaderController;
import com.tagtraum.perf.gcviewer.ctrl.action.*;
import com.tagtraum.perf.gcviewer.ctrl.impl.FileDropTargetListener.DropFlavor;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.*;
import com.tagtraum.perf.gcviewer.view.model.GCPreferences;
import com.tagtraum.perf.gcviewer.view.renderer.*;
import com.tagtraum.perf.gcviewer.view.util.ImageHelper;
import com.tagtraum.perf.gcviewer.view.util.OSXSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class is responsible for construction of {@link GCViewerGui}, whose construction is
 * quite complex. It is a simple implementation of the Builder pattern described in
 * "Design Patterns: Elements of Reusable Object-Oriented Software". 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 07.02.2014</p>
 */
public class GCViewerGuiBuilder {
    
    public void initGCViewerGui(GCViewerGui gui, GCModelLoaderController controller) {
        Image iconImage = ImageHelper.loadImage("gcviewericon.gif");

        gui.setIconImage(iconImage);
        Map<String, Action> actionMap = initActionMap(controller, gui, iconImage);

        GCViewerGuiMenuBar menuBar = initMenuBar(actionMap, gui, controller);
        GCViewerGuiToolBar toolBar = initToolBar(actionMap);
        JDesktopPane desktopPane = initDesktopPane(gui, controller);
        
        WatchStateController wsListener = new WatchStateController(menuBar.getWatchMenuItem(), toolBar.getWatchToggleButton());
        menuBar.getWatchMenuItem().addActionListener(wsListener);
        toolBar.getWatchToggleButton().addActionListener(wsListener);
        
        gui.setActionMap(actionMap);
        gui.setJMenuBar(menuBar);
        gui.setToolBar(toolBar);
        gui.setDesktopPane(desktopPane);
    }

    private Map<String, Action> initActionMap(GCModelLoaderController controller, GCViewerGui gui, Image icon) {
        Map<String, Action> actions = new TreeMap<String, Action>();
        
        actions.put(ActionCommands.EXIT.toString(), new Exit(gui));
        actions.put(ActionCommands.ABOUT.toString(), new About(gui));
        actions.put(ActionCommands.SHOW_README.toString(), new ReadmeAction(gui));
        actions.put(ActionCommands.SHOW_LICENSE.toString(), new LicenseAction(gui));
        actions.put(ActionCommands.OPEN_FILE.toString(), new OpenFile(controller, gui));
        actions.put(ActionCommands.OPEN_SERIES.toString(), new OpenSeries(controller, gui));
        actions.put(ActionCommands.OPEN_URL.toString(), new OpenURL(controller, gui));
        actions.put(ActionCommands.REFRESH.toString(), new Refresh(controller, gui));
        actions.put(ActionCommands.EXPORT.toString(), new Export(gui));
        actions.put(ActionCommands.ZOOM.toString(), new Zoom(gui));
        actions.put(ActionCommands.ARRANGE.toString(), new Arrange(gui));
        actions.put(ActionCommands.WATCH.toString(), new Watch(controller, gui));
        

        if (OSXSupport.isOSX()) {
            OSXSupport.initializeMacOSX(actions.get(ActionCommands.ABOUT.toString()), 
                    actions.get(ActionCommands.EXIT.toString()), 
                    null, 
                    icon, 
                    gui);
            
            if (OSXSupport.hasOSXFullScreenSupport()) {
                actions.put(ActionCommands.OSX_FULLSCREEN.toString(), new OSXFullScreen(gui));
            }
        }
        
        return actions;
    }

    private JDesktopPane initDesktopPane(GCViewerGui gui, GCModelLoaderController controller) {
        JDesktopPane desktopPane = new DesktopPane();
        desktopPane.setDropTarget(new DropTarget(gui, 
                DnDConstants.ACTION_COPY, 
                new FileDropTargetListener(controller, DropFlavor.OPEN)));

        return desktopPane;
    }
    
    private GCViewerGuiMenuBar initMenuBar(Map<String, Action> actions, 
            GCViewerGui gui,
            GCModelLoaderController controller) {
        
        ViewMenuController viewMenuController = new ViewMenuController(gui);
        GCViewerGuiMenuBar menuBar = new GCViewerGuiMenuBar();

        // file menu
        menuBar.addToFileMenu(actions.get(ActionCommands.OPEN_FILE.toString()));
        menuBar.addToFileMenu(actions.get(ActionCommands.OPEN_SERIES.toString()));
        menuBar.addToFileMenu(actions.get(ActionCommands.OPEN_URL.toString()));
        RecentGCResourcesMenu recentResourceNamesMenu = new RecentGCResourcesMenu();
        recentResourceNamesMenu.setIcon(ImageHelper.createEmptyImageIcon(20, 20));
        menuBar.addToFileMenu(recentResourceNamesMenu);
        menuBar.addToFileMenu(actions.get(ActionCommands.EXPORT.toString()));
        menuBar.addToFileMenu(actions.get(ActionCommands.REFRESH.toString()));
        menuBar.addToFileMenu(new JCheckBoxMenuItem(actions.get(ActionCommands.WATCH.toString())));
        if ( ! OSXSupport.isOSX()) {
            menuBar.addToFileMenu(actions.get(ActionCommands.EXIT.toString()));
        }

        RecentGCResourcesMenuController recentResourceNamesMenuController 
            = new RecentGCResourcesMenuController(controller, recentResourceNamesMenu);
        recentResourceNamesMenu.getRecentResourceNamesModel().addRecentResourceNamesListener(recentResourceNamesMenuController);

        // view menu
        StayOpenCheckBoxMenuItem menuItemShowModelMetricsPanel = new StayOpenCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_show_data_panel"), true);
        menuItemShowModelMetricsPanel.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_show_data_panel").charAt(0));
        menuItemShowModelMetricsPanel.setIcon(ImageHelper.createEmptyImageIcon(20, 20));
        menuItemShowModelMetricsPanel.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_show_data_panel"));
        menuItemShowModelMetricsPanel.setActionCommand(GCPreferences.SHOW_MODEL_METRICS_PANEL);
        menuItemShowModelMetricsPanel.addActionListener(viewMenuController);
        menuBar.addToViewMenu(GCPreferences.SHOW_MODEL_METRICS_PANEL, menuItemShowModelMetricsPanel);
        menuBar.addSeparatorToViewMenu();

        StayOpenCheckBoxMenuItem menuItemShowDateStamp = new StayOpenCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_show_date_stamp"), true);
        menuItemShowDateStamp.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_show_date_stamp").charAt(0));
        menuItemShowDateStamp.setIcon(ImageHelper.createEmptyImageIcon(20, 20));
        menuItemShowDateStamp.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_show_date_stamp"));
        menuItemShowDateStamp.setActionCommand(GCPreferences.SHOW_DATE_STAMP);
        menuItemShowDateStamp.addActionListener(viewMenuController);
        menuBar.addToViewMenu(GCPreferences.SHOW_DATE_STAMP, menuItemShowDateStamp);
        menuBar.addSeparatorToViewMenu();

        StayOpenCheckBoxMenuItem menuItemAntiAlias = new StayOpenCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_antialias"), true);
        menuItemAntiAlias.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_antialias").charAt(0));
        menuItemAntiAlias.setIcon(ImageHelper.createEmptyImageIcon(20, 20));
        menuItemAntiAlias.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_antialias"));
        menuItemAntiAlias.setActionCommand(GCPreferences.ANTI_ALIAS);
        menuItemAntiAlias.addActionListener(viewMenuController);
        menuBar.addToViewMenu(GCPreferences.ANTI_ALIAS, menuItemAntiAlias);
        menuBar.addSeparatorToViewMenu();

        StayOpenCheckBoxMenuItem menuItemFullGCLines = new StayOpenCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_full_gc_lines"), true);
        menuItemFullGCLines.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_full_gc_lines").charAt(0));
        menuItemFullGCLines.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_full_gc_lines"));
        menuItemFullGCLines.setIcon(ImageHelper.createMonoColoredImageIcon(FullGCLineRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemFullGCLines.setActionCommand(GCPreferences.FULL_GC_LINES);
        menuItemFullGCLines.addActionListener(viewMenuController);
        menuBar.addToViewMenu(GCPreferences.FULL_GC_LINES, menuItemFullGCLines);

        StayOpenCheckBoxMenuItem menuItemIncGCLines = new StayOpenCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_inc_gc_lines"), true);
        menuItemIncGCLines.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_inc_gc_lines").charAt(0));
        menuItemIncGCLines.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_inc_gc_lines"));
        menuItemIncGCLines.setIcon(ImageHelper.createMonoColoredImageIcon(IncLineRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemIncGCLines.setActionCommand(GCPreferences.INC_GC_LINES);
        menuItemIncGCLines.addActionListener(viewMenuController);
        menuBar.addToViewMenu(GCPreferences.INC_GC_LINES, menuItemIncGCLines);

        StayOpenCheckBoxMenuItem menuItemGcTimesLine = new StayOpenCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_gc_times_line"), true);
        menuItemGcTimesLine.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_gc_times_line").charAt(0));
        menuItemGcTimesLine.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_gc_times_line"));
        menuItemGcTimesLine.setIcon(ImageHelper.createMonoColoredImageIcon(GCTimesRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemGcTimesLine.setActionCommand(GCPreferences.GC_TIMES_LINE);
        menuItemGcTimesLine.addActionListener(viewMenuController);
        menuBar.addToViewMenu(GCPreferences.GC_TIMES_LINE, menuItemGcTimesLine);

        StayOpenCheckBoxMenuItem menuItemGcTimesRectangle = new StayOpenCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_gc_times_rectangles"), true);
        menuItemGcTimesRectangle.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_gc_times_rectangles").charAt(0));
        menuItemGcTimesRectangle.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_gc_times_rectangles"));
        menuItemGcTimesRectangle.setIcon(ImageHelper.createMonoColoredImageIcon(GCRectanglesRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemGcTimesRectangle.setActionCommand(GCPreferences.GC_TIMES_RECTANGLES);
        menuItemGcTimesRectangle.addActionListener(viewMenuController);
        menuBar.addToViewMenu(GCPreferences.GC_TIMES_RECTANGLES, menuItemGcTimesRectangle);

        StayOpenCheckBoxMenuItem menuItemTotalMemory = new StayOpenCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_total_memory"), true);
        menuItemTotalMemory.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_total_memory").charAt(0));
        menuItemTotalMemory.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_total_memory"));
        menuItemTotalMemory.setIcon(ImageHelper.createMonoColoredImageIcon(TotalHeapRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemTotalMemory.setActionCommand(GCPreferences.TOTAL_MEMORY);
        menuItemTotalMemory.addActionListener(viewMenuController);
        menuBar.addToViewMenu(GCPreferences.TOTAL_MEMORY, menuItemTotalMemory);

        StayOpenCheckBoxMenuItem menuItemTenuredMemory = new StayOpenCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_tenured_memory"), true);
        menuItemTenuredMemory.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_tenured_memory").charAt(0));
        menuItemTenuredMemory.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_tenured_memory"));
        menuItemTenuredMemory.setIcon(ImageHelper.createMonoColoredImageIcon(TotalTenuredRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemTenuredMemory.setActionCommand(GCPreferences.TENURED_MEMORY);
        menuItemTenuredMemory.addActionListener(viewMenuController);
        menuBar.addToViewMenu(GCPreferences.TENURED_MEMORY, menuItemTenuredMemory);

        StayOpenCheckBoxMenuItem menuItemYoungMemory = new StayOpenCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_young_memory"), true);
        menuItemYoungMemory.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_young_memory").charAt(0));
        menuItemYoungMemory.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_young_memory"));
        menuItemYoungMemory.setIcon(ImageHelper.createMonoColoredImageIcon(TotalYoungRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemYoungMemory.setActionCommand(GCPreferences.YOUNG_MEMORY);
        menuItemYoungMemory.addActionListener(viewMenuController);
        menuBar.addToViewMenu(GCPreferences.YOUNG_MEMORY, menuItemYoungMemory);

        StayOpenCheckBoxMenuItem menuItemUsedMemory = new StayOpenCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_used_memory"), true);
        menuItemUsedMemory.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_used_memory").charAt(0));
        menuItemUsedMemory.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_used_memory"));
        menuItemUsedMemory.setIcon(ImageHelper.createMonoColoredImageIcon(UsedHeapRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemUsedMemory.setActionCommand(GCPreferences.USED_MEMORY);
        menuItemUsedMemory.addActionListener(viewMenuController);
        menuBar.addToViewMenu(GCPreferences.USED_MEMORY, menuItemUsedMemory);

        StayOpenCheckBoxMenuItem menuItemUsedTenuredMemory = new StayOpenCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_used_tenured_memory"), true);
        menuItemUsedTenuredMemory.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_used_tenured_memory").charAt(0));
        menuItemUsedTenuredMemory.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_used_tenured_memory"));
        menuItemUsedTenuredMemory.setIcon(ImageHelper.createMonoColoredImageIcon(UsedTenuredRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemUsedTenuredMemory.setActionCommand(GCPreferences.USED_TENURED_MEMORY);
        menuItemUsedTenuredMemory.addActionListener(viewMenuController);
        menuBar.addToViewMenu(GCPreferences.USED_TENURED_MEMORY, menuItemUsedTenuredMemory);

        StayOpenCheckBoxMenuItem menuItemUsedYoungMemory = new StayOpenCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_used_young_memory"), true);
        menuItemUsedYoungMemory.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_used_young_memory").charAt(0));
        menuItemUsedYoungMemory.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_used_young_memory"));
        menuItemUsedYoungMemory.setIcon(ImageHelper.createMonoColoredImageIcon(UsedYoungRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemUsedYoungMemory.setActionCommand(GCPreferences.USED_YOUNG_MEMORY);
        menuItemUsedYoungMemory.addActionListener(viewMenuController);
        menuBar.addToViewMenu(GCPreferences.USED_YOUNG_MEMORY, menuItemUsedYoungMemory);

        StayOpenCheckBoxMenuItem menuItemInitialMarkLevel = new StayOpenCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_initial_mark_level"), true);
        menuItemInitialMarkLevel.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_initial_mark_level").charAt(0));
        menuItemInitialMarkLevel.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_initial_mark_level"));
        menuItemInitialMarkLevel.setIcon(ImageHelper.createMonoColoredImageIcon(InitialMarkLevelRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemInitialMarkLevel.setActionCommand(GCPreferences.INITIAL_MARK_LEVEL);
        menuItemInitialMarkLevel.addActionListener(viewMenuController);
        menuBar.addToViewMenu(GCPreferences.INITIAL_MARK_LEVEL, menuItemInitialMarkLevel);

        StayOpenCheckBoxMenuItem menuItemConcurrentGcBeginEnd = new StayOpenCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_concurrent_collection_begin_end"), true);
        menuItemConcurrentGcBeginEnd.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_concurrent_collection_begin_end").charAt(0));
        menuItemConcurrentGcBeginEnd.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_concurrent_collection_begin_end"));
        menuItemConcurrentGcBeginEnd.setIcon(ImageHelper.createMonoColoredImageIcon(ConcurrentGcBegionEndRenderer.CONCURRENT_COLLECTION_BEGIN, 20, 20));
        menuItemConcurrentGcBeginEnd.setActionCommand(GCPreferences.CONCURRENT_COLLECTION_BEGIN_END);
        menuItemConcurrentGcBeginEnd.addActionListener(viewMenuController);
        menuBar.addToViewMenu(GCPreferences.CONCURRENT_COLLECTION_BEGIN_END, menuItemConcurrentGcBeginEnd);

        if (OSXSupport.hasOSXFullScreenSupport()) {
            // TODO No saving in properties?
            menuBar.addSeparatorToViewMenu();
            menuBar.addToViewMenu(actions.get(ActionCommands.OSX_FULLSCREEN.toString()));
        }
        
        menuBar.addToWindowMenu(actions.get(ActionCommands.ARRANGE.toString()));
        menuBar.addSeparatorToWindowMenu();
        
        if ( ! OSXSupport.isOSX()) {
            // TODO why is the help menu not shown in OSX? -> is there never one?
            menuBar.addToHelpMenu(actions.get(ActionCommands.SHOW_README.toString()));
            menuBar.addToHelpMenu(actions.get(ActionCommands.SHOW_LICENSE.toString()));
            menuBar.addToHelpMenu(actions.get(ActionCommands.ABOUT.toString()));
        }

        return menuBar;
    }

    private GCViewerGuiToolBar initToolBar(Map<String, Action> actions) {
        GCViewerGuiToolBar toolBar = new GCViewerGuiToolBar();
        toolBar.setFloatable(false);
        
        toolBar.add(actions.get(ActionCommands.OPEN_FILE.toString()));
        toolBar.add(actions.get(ActionCommands.OPEN_SERIES.toString()));
        toolBar.add(actions.get(ActionCommands.OPEN_URL.toString()));
        toolBar.add(actions.get(ActionCommands.EXPORT.toString()));
        toolBar.add(actions.get(ActionCommands.REFRESH.toString()));
        
        JToggleButton watchToggle = new JToggleButton();
        watchToggle.setAction(actions.get(ActionCommands.WATCH.toString()));
        watchToggle.setText("");
        toolBar.addWatchToggleButton(watchToggle);
        
        toolBar.addSeparator();
        
        JComboBox<String> zoomComboBox = new JComboBox<String>(new String[] {"1%", "5%", "10%", "50%", "100%", "200%", "300%", "500%", "1000%", "5000%"});
        zoomComboBox.setSelectedIndex(2);
        zoomComboBox.setAction(actions.get(ActionCommands.ZOOM.toString()));
        zoomComboBox.setEditable(true);
        zoomComboBox.setMaximumSize(zoomComboBox.getPreferredSize());
        toolBar.addZoomComboBox(zoomComboBox);
        
        toolBar.addSeparator();
        
        toolBar.add(actions.get(ActionCommands.ABOUT.toString()));
        
        return toolBar;
    }

}
