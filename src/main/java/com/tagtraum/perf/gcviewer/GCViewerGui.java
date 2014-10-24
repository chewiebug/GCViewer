package com.tagtraum.perf.gcviewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.tagtraum.perf.gcviewer.action.About;
import com.tagtraum.perf.gcviewer.action.Arrange;
import com.tagtraum.perf.gcviewer.action.Exit;
import com.tagtraum.perf.gcviewer.action.Export;
import com.tagtraum.perf.gcviewer.action.LicenseAction;
import com.tagtraum.perf.gcviewer.action.OSXFullScreen;
import com.tagtraum.perf.gcviewer.action.OpenFile;
import com.tagtraum.perf.gcviewer.action.OpenRecent;
import com.tagtraum.perf.gcviewer.action.OpenURL;
import com.tagtraum.perf.gcviewer.action.ReadmeAction;
import com.tagtraum.perf.gcviewer.action.Refresh;
import com.tagtraum.perf.gcviewer.action.Watch;
import com.tagtraum.perf.gcviewer.action.Zoom;
import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.renderer.ConcurrentGcBegionEndRenderer;
import com.tagtraum.perf.gcviewer.renderer.FullGCLineRenderer;
import com.tagtraum.perf.gcviewer.renderer.GCRectanglesRenderer;
import com.tagtraum.perf.gcviewer.renderer.GCTimesRenderer;
import com.tagtraum.perf.gcviewer.renderer.IncLineRenderer;
import com.tagtraum.perf.gcviewer.renderer.InitialMarkLevelRenderer;
import com.tagtraum.perf.gcviewer.renderer.TotalHeapRenderer;
import com.tagtraum.perf.gcviewer.renderer.TotalTenuredRenderer;
import com.tagtraum.perf.gcviewer.renderer.TotalYoungRenderer;
import com.tagtraum.perf.gcviewer.renderer.UsedHeapRenderer;
import com.tagtraum.perf.gcviewer.renderer.UsedTenuredRenderer;
import com.tagtraum.perf.gcviewer.renderer.UsedYoungRenderer;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.util.LoggerHelper;
import com.tagtraum.perf.gcviewer.util.OSXSupport;

/**
 * Main class.
 *
 * Date: Jan 30, 2002
 * Time: 4:59:49 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class GCViewerGui extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(GCViewerGui.class.getName());

    private JToolBar toolBar;
    private ActionListener viewMenuActionListener;
    private JMenu fileMenu;
    private JMenu windowMenu;
    private JMenuItem exportMenuItem;
    private ButtonGroup windowCheckBoxGroup = new ButtonGroup();
    private JDesktopPane desktopPane;
    private JComboBox<String> zoomComboBox;
    private Image iconImage;
    // actions
    private Exit exitAction = new Exit(this);
    private About aboutAction = new About(this);
    private ReadmeAction readmeAction = new ReadmeAction(this);
    private LicenseAction licenseAction = new LicenseAction(this);
    private OpenFile openFileAction = new OpenFile(this);
    private OpenURL openURLAction = new OpenURL(this);
    private Refresh refreshAction = new Refresh(this);
    private Export exportAction = new Export(this);
    private Zoom zoomAction = new Zoom(this);
    private Arrange arrangeAction = new Arrange(this);
    private Watch watchAction = new Watch(this);
    private OSXFullScreen osxFullScreenAction;
    private JCheckBoxMenuItem menuItemShowDataPanel;
    private JCheckBoxMenuItem menuItemShowDateStamp;
    private JCheckBoxMenuItem menuItemFullGCLines;
    private JCheckBoxMenuItem menuItemIncGCLines;
    private JCheckBoxMenuItem menuItemGcTimesLine;
    private JCheckBoxMenuItem menuItemGcTimesRectangle;
    private JCheckBoxMenuItem menuItemUsedMemory;
    private JCheckBoxMenuItem menuItemUsedTenuredMemory;
    private JCheckBoxMenuItem menuItemUsedYoungMemory;
    private JCheckBoxMenuItem menuItemTotalMemory;
    private JCheckBoxMenuItem menuItemTenuredMemory;
    private JCheckBoxMenuItem menuItemYoungMemory;
    private JCheckBoxMenuItem menuItemInitialMarkLevel;
    private JCheckBoxMenuItem menuItemConcurrentGcBeginEnd;
    private JCheckBoxMenuItem menuItemWatch;
    private JCheckBoxMenuItem menuItemAntiAlias;
    private Map<String, JCheckBoxMenuItem> gcLineMenuItems;
    private JToggleButton watchToggle;

    private RecentURLsMenu recentURLsMenu;
    
    private GCPreferences preferences;

    public GCViewerGui() {
        super("tagtraum industries incorporated - GCViewer");

        iconImage = loadImage("gcviewericon.gif");
        setIconImage(iconImage);
        
        desktopPane = new DesktopPane(this);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                exit();
            }
        });
        viewMenuActionListener = new ViewMenuActionListener();
        recentURLsMenu = new RecentURLsMenu(this);
        openURLAction.setRecentURLsModel(recentURLsMenu.getRecentURLsModel());

        if (OSXSupport.isOSX()) {
            OSXSupport.initializeMacOSX(aboutAction, exitAction, null, iconImage, this);
            if (OSXSupport.hasOSXFullScreenSupport()) {
                osxFullScreenAction = new OSXFullScreen(this);
            }
        }

        setJMenuBar(initMenuBar());
        toolBar = initToolBar();

        // cross reference the two toggle buttons
        menuItemWatch.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                watchToggle.setSelected(menuItemWatch.getState());
            }
        });
        watchToggle.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                menuItemWatch.setState(watchToggle.isSelected());
            }
        });

        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(desktopPane, BorderLayout.CENTER);

        preferences = new GCPreferences();
        loadPreferences(preferences);
        setVisible(true);
    }

    /**
     * Loads an image if <code>imageName</code> can be found. If not, a warning is logged.
     * 
     * @param imageName name of the image to be found in this classes classpath.
     * @return loaded image or <code>null</code> if it could not be loaded.
     */
    private Image loadImage(String imageName) {
        URL imageUrl = null;
        Image image = null;
        try {
            imageUrl = getClass().getResource(imageName);
            image = ImageIO.read(imageUrl);
        } 
        catch (IOException e) {
            LoggerHelper.logException(LOGGER, Level.WARNING, "could not load icon (imageName='" 
                    + imageName + "'; url='" + imageUrl + "')", e);
        } 
        catch (IllegalArgumentException e) {
            LoggerHelper.logException(LOGGER, Level.WARNING, "could not load icon (imageName='" 
                    + imageName + "'; url='" + imageUrl + "')", e);
        }
        
        return image;
    }

    public RecentURLsMenu getRecentFilesMenu() {
        return recentURLsMenu;
    }

    public JDesktopPane getDesktopPane() {
        return desktopPane;
    }

    private InternalFrameListener gcDocumentListener = new InternalFrameListener() {
        public void internalFrameOpened(final InternalFrameEvent e) {
            final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(new WindowMenuItemAction(e));
            windowMenu.add(menuItem);
            windowCheckBoxGroup.add(menuItem);
        }

        public void internalFrameClosing(final InternalFrameEvent e) {
            if (getDesktopPane().getComponentCount() == 1) {
                // only needs to be done, when the last window is closed
                // otherwise an internalFrameActivated event will follow immediately after the
                // close for the next active window
                updateMenuItemState();
            }
        }

        public void internalFrameClosed(final InternalFrameEvent e) {
            if (desktopPane.getAllFrames().length == 0) arrangeAction.setEnabled(false);
            ((GCDocument)e.getInternalFrame()).getRefreshWatchDog().stop();
            // remove menuitem from menu and from button group
            for (int i=2; i<windowMenu.getItemCount(); i++) {
                final JMenuItem item = windowMenu.getItem(i);
                if (((WindowMenuItemAction)item.getAction()).getInternalFrame() == e.getInternalFrame()) {
                    windowMenu.remove(item);
                    windowCheckBoxGroup.remove(item);
                    break;
                }
            }
            
        }

        public void internalFrameIconified(final InternalFrameEvent e) {
        }

        public void internalFrameDeiconified(final InternalFrameEvent e) {
        }

        public void internalFrameActivated(final InternalFrameEvent e) {
            for (int i=2; i<windowMenu.getItemCount(); i++) {
                final JMenuItem item = windowMenu.getItem(i);
                if (((WindowMenuItemAction)item.getAction()).getInternalFrame() == e.getInternalFrame()) {
                    item.setSelected(true);
                    break;
                }
            }
            menuItemWatch.setSelected(getSelectedGCDocument().isWatched());
            getSelectedGCDocument().getRefreshWatchDog().setAction(watchAction);
            watchToggle.setSelected(getSelectedGCDocument().isWatched());
            exportAction.setEnabled(true);
            refreshAction.setEnabled(true);
            watchAction.setEnabled(true);
            zoomAction.setEnabled(true);
            arrangeAction.setEnabled(true);
            
            updateMenuItemState();
        }

        public void internalFrameDeactivated(final InternalFrameEvent e) {
            exportAction.setEnabled(false);
            refreshAction.setEnabled(false);
            watchAction.setEnabled(false);
            zoomAction.setEnabled(false);
            watchToggle.setSelected(false);
            menuItemWatch.setSelected(false);
            ((GCDocument)e.getInternalFrame()).getRefreshWatchDog().setAction(null);
        }
        
        private void updateMenuItemState() {
            zoomComboBox.setSelectedItem((int) (getSelectedGCDocument().getModelChart().getScaleFactor() * 1000.0) + "%");
            menuItemShowDataPanel.setState(getSelectedGCDocument().isShowModelPanel());
            menuItemShowDateStamp.setState(getSelectedGCDocument().getModelChart().isShowDateStamp());
            menuItemAntiAlias.setSelected(getSelectedGCDocument().getModelChart().isAntiAlias());
            menuItemFullGCLines.setState(getSelectedGCDocument().getModelChart().isShowFullGCLines());
            menuItemIncGCLines.setState(getSelectedGCDocument().getModelChart().isShowIncGCLines());
            menuItemGcTimesLine.setState(getSelectedGCDocument().getModelChart().isShowGCTimesLine());
            menuItemGcTimesRectangle.setState(getSelectedGCDocument().getModelChart().isShowGCTimesRectangles());
            menuItemTotalMemory.setState(getSelectedGCDocument().getModelChart().isShowTotalMemoryLine());
            menuItemTenuredMemory.setState(getSelectedGCDocument().getModelChart().isShowTenured());
            menuItemYoungMemory.setState(getSelectedGCDocument().getModelChart().isShowYoung());
            menuItemUsedMemory.setState(getSelectedGCDocument().getModelChart().isShowUsedMemoryLine());
            menuItemUsedTenuredMemory.setState(getSelectedGCDocument().getModelChart().isShowUsedTenuredMemoryLine());
            menuItemUsedYoungMemory.setState(getSelectedGCDocument().getModelChart().isShowUsedYoungMemoryLine());
            menuItemInitialMarkLevel.setState(getSelectedGCDocument().getModelChart().isShowInitialMarkLevel());
            menuItemConcurrentGcBeginEnd.setState(getSelectedGCDocument().getModelChart().isShowConcurrentCollectionBeginEnd());
        }
    };

    public GCDocument getSelectedGCDocument() {
        return (GCDocument)desktopPane.getSelectedFrame();
    }

    private URL[] convertFilesToURLs(final File[] files) throws MalformedURLException {
        final URL[] urls = new URL[files.length];
        for (int i=0; i<files.length; i++) {
            urls[i] = files[i].getAbsoluteFile().toURI().toURL();
        }
        return urls;
    }

    public GCPreferences getPreferences() {
        return preferences;
    }
    
    public void open(final File[] files) {
        // delegate to open(...)
        try {
            final URL[] urls = convertFilesToURLs(files);
            open(urls);
        } catch (RuntimeException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(GCViewerGui.this, e.toString() + " " + e.getLocalizedMessage(), LocalisationHelper.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            //e.printStackTrace();
            JOptionPane.showMessageDialog(GCViewerGui.this, e.getLocalizedMessage(), LocalisationHelper.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void open(final URL[] urls) {
        try {
            if (urls.length >= 1) {
                final URL url = urls[0];
                final GCDocument gcDocument = new GCDocument(this, url.toString());
                gcDocument.add(url);
                gcDocument.addInternalFrameListener(gcDocumentListener);
                desktopPane.add(gcDocument);
                gcDocument.setSelected(true);
                gcDocument.setSize(450, 300);
                gcDocument.setMaximum(true);
                //addAction.setSelectedFile(url);
                gcDocument.setVisible(true);
            }
            if (urls.length>1) {
                final URL[] addURLs = new URL[urls.length-1];
                System.arraycopy(urls, 1, addURLs, 0, addURLs.length);
                add(addURLs);
            }
            recentURLsMenu.getRecentURLsModel().add(urls);
            repaint();
        } catch (DataReaderException e) {
            e.printStackTrace();
        } catch (PropertyVetoException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(GCViewerGui.this, e.toString() + " " + e.getLocalizedMessage(), LocalisationHelper.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void add(final File[] files) {
        try {
            if (files.length >= 0) openFileAction.setSelectedFile(files[0]);
            final URL[] urls = convertFilesToURLs(files);
            add(urls);
        } catch (RuntimeException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(GCViewerGui.this, e.toString() + " " + e.getLocalizedMessage(), LocalisationHelper.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            //e.printStackTrace();
            JOptionPane.showMessageDialog(GCViewerGui.this, e.getLocalizedMessage(), LocalisationHelper.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void add(final URL[] urls) {
        try {
            for (int i=0; i<urls.length; i++) {
                final URL url = urls[i];
                getSelectedGCDocument().add(url);
            }
            recentURLsMenu.getRecentURLsModel().add(urls);
            repaint();
        }
        catch (DataReaderException e) {
            e.printStackTrace();
        }
    }

    private JToolBar initToolBar() {
        final JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(openFileAction);
        toolBar.add(openURLAction);
        toolBar.add(exportAction);
        toolBar.add(refreshAction);
        watchToggle = new JToggleButton();
        watchToggle.setAction(watchAction);
        watchToggle.setText("");
        toolBar.add(watchToggle);
        toolBar.addSeparator();
        zoomComboBox = new JComboBox<String>(new String[] {"1%", "5%", "10%", "50%", "100%", "200%", "300%", "500%", "1000%", "5000%"});
        zoomComboBox.setSelectedIndex(2);
        zoomComboBox.setAction(zoomAction);
        zoomComboBox.setEditable(true);
        zoomComboBox.setMaximumSize(zoomComboBox.getPreferredSize());
        toolBar.add(zoomComboBox);
        toolBar.addSeparator();
        toolBar.add(aboutAction);
        return toolBar;
    }

    /**
     * Creates the horizontal rectangle used in the menus.
     *
     * @param paint paint of the rectangle
     * @param width
     * @param height
     * @return icon
     */
    private static ImageIcon createMonoColoredImageIcon(final Paint paint, final int width, final int height) {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
        final Graphics2D g = image.createGraphics();
        g.setPaint(paint);
        final int lineHeight = 4;
        g.fill3DRect(0, height / 2 - lineHeight / 2, width, lineHeight, false);
        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Creates empty image.
     *
     * @param width
     * @param height
     * @return icon
     */
    private static ImageIcon createEmptyImageIcon(final int width, final int height) {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
        final Graphics2D g = image.createGraphics();
        g.dispose();
        return new ImageIcon(image);
    }

    private JMenuBar initMenuBar() {
        final JMenuBar menuBar = new JMenuBar();

        fileMenu = new JMenu(LocalisationHelper.getString("main_frame_menu_file"));
        fileMenu.setMnemonic(LocalisationHelper.getString("main_frame_menu_mnemonic_file").charAt(0));
        menuBar.add(fileMenu);

        JMenuItem menuItem = new JMenuItem(openFileAction);
        fileMenu.add(menuItem);

        menuItem = new JMenuItem(openURLAction);
        fileMenu.add(menuItem);

        recentURLsMenu.setIcon(createEmptyImageIcon(20, 20));
        fileMenu.add(recentURLsMenu);

        exportMenuItem = new JMenuItem(exportAction);
        fileMenu.add(exportMenuItem);

        menuItem = new JMenuItem(refreshAction);
        fileMenu.add(menuItem);

        menuItemWatch = new JCheckBoxMenuItem(watchAction);
        fileMenu.add(menuItemWatch);

        if ( ! OSXSupport.isOSX()) {
            menuItem = new JMenuItem(exitAction);
            fileMenu.add(menuItem);
        }

        final JMenu viewMenu = new JMenu(LocalisationHelper.getString("main_frame_menu_view"));

        viewMenu.setMnemonic(LocalisationHelper.getString("main_frame_menu_mnemonic_view").charAt(0));
        menuBar.add(viewMenu);

        gcLineMenuItems = new HashMap<String, JCheckBoxMenuItem>();

        menuItemShowDataPanel = new JCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_show_data_panel"), true);
        menuItemShowDataPanel.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_show_data_panel").charAt(0));
        menuItemShowDataPanel.setIcon(createEmptyImageIcon(20, 20));
        menuItemShowDataPanel.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_show_data_panel"));
        menuItemShowDataPanel.setActionCommand(GCPreferences.SHOW_DATA_PANEL);
        menuItemShowDataPanel.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e) {
                final GCDocument gcDocument = getSelectedGCDocument();
                if (gcDocument != null) {
                    gcDocument.setShowModelPanel(menuItemShowDataPanel.getState());
                }
            }
        });
        viewMenu.add(menuItemShowDataPanel);
        viewMenu.addSeparator();
        gcLineMenuItems.put(GCPreferences.SHOW_DATA_PANEL, menuItemShowDataPanel);

        menuItemShowDateStamp = new JCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_show_data_panel"), true);
        menuItemShowDateStamp.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_show_data_panel").charAt(0));
        menuItemShowDateStamp.setIcon(createEmptyImageIcon(20, 20));
        menuItemShowDateStamp.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_show_data_panel"));
        menuItemShowDateStamp.setActionCommand(GCPreferences.SHOW_DATE_STAMP);
        menuItemShowDateStamp.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e) {
                final GCDocument gcDocument = getSelectedGCDocument();
                if (gcDocument != null) {
                    gcDocument.setShowModelPanel(menuItemShowDateStamp.getState());
                }
            }
        });
        // TODO fix menu item "showdatestamp"
        //viewMenu.add(menuItemShowDateStamp);
        //viewMenu.addSeparator();
        gcLineMenuItems.put(GCPreferences.SHOW_DATE_STAMP, menuItemShowDateStamp);

        menuItemAntiAlias = new JCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_antialias"), true);
        menuItemAntiAlias.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_antialias").charAt(0));
        menuItemAntiAlias.setIcon(createEmptyImageIcon(20, 20));
        menuItemAntiAlias.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_antialias"));
        menuItemAntiAlias.setActionCommand(GCPreferences.ANTI_ALIAS);
        menuItemAntiAlias.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e) {
                final GCDocument gcDocument = getSelectedGCDocument();
                if (gcDocument != null) {
                    gcDocument.getModelChart().setAntiAlias(menuItemAntiAlias.getState());
                    gcDocument.relayout();
                }
            }
        });
        menuItemAntiAlias.setSelected(false);
        viewMenu.add(menuItemAntiAlias);
        viewMenu.addSeparator();
        gcLineMenuItems.put(GCPreferences.ANTI_ALIAS, menuItemAntiAlias);

        menuItemFullGCLines = new JCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_full_gc_lines"), true);
        menuItemFullGCLines.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_full_gc_lines").charAt(0));
        menuItemFullGCLines.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_full_gc_lines"));
        menuItemFullGCLines.setIcon(createMonoColoredImageIcon(FullGCLineRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemFullGCLines.setActionCommand(GCPreferences.FULL_GC_LINES);
        menuItemFullGCLines.addActionListener(viewMenuActionListener);
        viewMenu.add(menuItemFullGCLines);
        gcLineMenuItems.put(GCPreferences.FULL_GC_LINES, menuItemFullGCLines);

        menuItemIncGCLines = new JCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_inc_gc_lines"), true);
        menuItemIncGCLines.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_inc_gc_lines").charAt(0));
        menuItemIncGCLines.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_inc_gc_lines"));
        menuItemIncGCLines.setIcon(createMonoColoredImageIcon(IncLineRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemIncGCLines.setActionCommand(GCPreferences.INC_GC_LINES);
        menuItemIncGCLines.addActionListener(viewMenuActionListener);
        viewMenu.add(menuItemIncGCLines);
        gcLineMenuItems.put(GCPreferences.INC_GC_LINES, menuItemIncGCLines);

        menuItemGcTimesLine = new JCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_gc_times_line"), true);
        menuItemGcTimesLine.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_gc_times_line").charAt(0));
        menuItemGcTimesLine.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_gc_times_line"));
        menuItemGcTimesLine.setIcon(createMonoColoredImageIcon(GCTimesRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemGcTimesLine.setActionCommand(GCPreferences.GC_LINES_LINE);
        menuItemGcTimesLine.addActionListener(viewMenuActionListener);
        viewMenu.add(menuItemGcTimesLine);
        gcLineMenuItems.put(GCPreferences.GC_LINES_LINE, menuItemGcTimesLine);

        menuItemGcTimesRectangle = new JCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_gc_times_rectangles"), true);
        menuItemGcTimesRectangle.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_gc_times_rectangles").charAt(0));
        menuItemGcTimesRectangle.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_gc_times_rectangles"));
        menuItemGcTimesRectangle.setIcon(createMonoColoredImageIcon(GCRectanglesRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemGcTimesRectangle.setActionCommand(GCPreferences.GC_TIMES_RECTANGLES);
        menuItemGcTimesRectangle.addActionListener(viewMenuActionListener);
        viewMenu.add(menuItemGcTimesRectangle);
        gcLineMenuItems.put(GCPreferences.GC_TIMES_RECTANGLES, menuItemGcTimesRectangle);

        menuItemTotalMemory = new JCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_total_memory"), true);
        menuItemTotalMemory.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_total_memory").charAt(0));
        menuItemTotalMemory.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_total_memory"));
        menuItemTotalMemory.setIcon(createMonoColoredImageIcon(TotalHeapRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemTotalMemory.setActionCommand(GCPreferences.TOTAL_MEMORY);
        menuItemTotalMemory.addActionListener(viewMenuActionListener);
        viewMenu.add(menuItemTotalMemory);
        gcLineMenuItems.put(GCPreferences.TOTAL_MEMORY, menuItemTotalMemory);

        menuItemTenuredMemory = new JCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_tenured_memory"), true);
        menuItemTenuredMemory.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_tenured_memory").charAt(0));
        menuItemTenuredMemory.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_tenured_memory"));
        menuItemTenuredMemory.setIcon(createMonoColoredImageIcon(TotalTenuredRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemTenuredMemory.setActionCommand(GCPreferences.TENURED_MEMORY);
        menuItemTenuredMemory.addActionListener(viewMenuActionListener);
        viewMenu.add(menuItemTenuredMemory);
        gcLineMenuItems.put(GCPreferences.TENURED_MEMORY, menuItemTenuredMemory);

        menuItemYoungMemory = new JCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_young_memory"), true);
        menuItemYoungMemory.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_young_memory").charAt(0));
        menuItemYoungMemory.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_young_memory"));
        menuItemYoungMemory.setIcon(createMonoColoredImageIcon(TotalYoungRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemYoungMemory.setActionCommand(GCPreferences.YOUNG_MEMORY);
        menuItemYoungMemory.addActionListener(viewMenuActionListener);
        viewMenu.add(menuItemYoungMemory);
        gcLineMenuItems.put(GCPreferences.YOUNG_MEMORY, menuItemYoungMemory);

        menuItemUsedMemory = new JCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_used_memory"), true);
        menuItemUsedMemory.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_used_memory").charAt(0));
        menuItemUsedMemory.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_used_memory"));
        menuItemUsedMemory.setIcon(createMonoColoredImageIcon(UsedHeapRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemUsedMemory.setActionCommand(GCPreferences.USED_MEMORY);
        menuItemUsedMemory.addActionListener(viewMenuActionListener);
        viewMenu.add(menuItemUsedMemory);
        gcLineMenuItems.put(GCPreferences.USED_MEMORY, menuItemUsedMemory);

        menuItemUsedTenuredMemory = new JCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_used_tenured_memory"), true);
        menuItemUsedTenuredMemory.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_used_tenured_memory").charAt(0));
        menuItemUsedTenuredMemory.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_used_tenured_memory"));
        menuItemUsedTenuredMemory.setIcon(createMonoColoredImageIcon(UsedTenuredRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemUsedTenuredMemory.setActionCommand(GCPreferences.USED_TENURED_MEMORY);
        menuItemUsedTenuredMemory.addActionListener(viewMenuActionListener);
        viewMenu.add(menuItemUsedTenuredMemory);
        gcLineMenuItems.put(GCPreferences.USED_TENURED_MEMORY, menuItemUsedTenuredMemory);

        menuItemUsedYoungMemory = new JCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_used_young_memory"), true);
        menuItemUsedYoungMemory.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_used_young_memory").charAt(0));
        menuItemUsedYoungMemory.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_used_young_memory"));
        menuItemUsedYoungMemory.setIcon(createMonoColoredImageIcon(UsedYoungRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemUsedYoungMemory.setActionCommand(GCPreferences.USED_YOUNG_MEMORY);
        menuItemUsedYoungMemory.addActionListener(viewMenuActionListener);
        viewMenu.add(menuItemUsedYoungMemory);
        gcLineMenuItems.put(GCPreferences.USED_YOUNG_MEMORY, menuItemUsedYoungMemory);

        menuItemInitialMarkLevel = new JCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_initial_mark_level"), true);
        menuItemInitialMarkLevel.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_initial_mark_level").charAt(0));
        menuItemInitialMarkLevel.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_initial_mark_level"));
        menuItemInitialMarkLevel.setIcon(createMonoColoredImageIcon(InitialMarkLevelRenderer.DEFAULT_LINEPAINT, 20, 20));
        menuItemInitialMarkLevel.setActionCommand(GCPreferences.INITIAL_MARK_LEVEL);
        menuItemInitialMarkLevel.addActionListener(viewMenuActionListener);
        viewMenu.add(menuItemInitialMarkLevel);
        gcLineMenuItems.put(GCPreferences.INITIAL_MARK_LEVEL, menuItemInitialMarkLevel);

        menuItemConcurrentGcBeginEnd = new JCheckBoxMenuItem(LocalisationHelper.getString("main_frame_menuitem_concurrent_collection_begin_end"), true);
        menuItemConcurrentGcBeginEnd.setMnemonic(LocalisationHelper.getString("main_frame_menuitem_mnemonic_concurrent_collection_begin_end").charAt(0));
        menuItemConcurrentGcBeginEnd.setToolTipText(LocalisationHelper.getString("main_frame_menuitem_hint_concurrent_collection_begin_end"));
        menuItemConcurrentGcBeginEnd.setIcon(createMonoColoredImageIcon(ConcurrentGcBegionEndRenderer.CONCURRENT_COLLECTION_BEGIN, 20, 20));
        menuItemConcurrentGcBeginEnd.setActionCommand(GCPreferences.CONCURRENT_COLLECTION_BEGIN_END);
        menuItemConcurrentGcBeginEnd.addActionListener(viewMenuActionListener);
        viewMenu.add(menuItemConcurrentGcBeginEnd);
        gcLineMenuItems.put(GCPreferences.CONCURRENT_COLLECTION_BEGIN_END, menuItemConcurrentGcBeginEnd);

        if (OSXSupport.hasOSXFullScreenSupport()) {
            viewMenu.addSeparator();
            viewMenu.add(new JMenuItem(osxFullScreenAction));
        }
        
        windowMenu = new JMenu(LocalisationHelper.getString("main_frame_menu_window"));
        windowMenu.setMnemonic(LocalisationHelper.getString("main_frame_menu_mnemonic_window").charAt(0));
        menuBar.add(windowMenu);

        menuItem = new JMenuItem(arrangeAction);
        windowMenu.add(menuItem);
        windowMenu.addSeparator();

        if ( ! OSXSupport.isOSX()) {

            final JMenu helpMenu = new JMenu(LocalisationHelper.getString("main_frame_menu_help"));
            helpMenu.setMnemonic(LocalisationHelper.getString("main_frame_menu_mnemonic_help").charAt(0));
            menuBar.add(helpMenu);

            menuItem = new JMenuItem(readmeAction);
            helpMenu.add(menuItem);

            menuItem = new JMenuItem(licenseAction);
            helpMenu.add(menuItem);

            menuItem = new JMenuItem(aboutAction);
            helpMenu.add(menuItem);
        }

        return menuBar;
    }

    private class ViewMenuActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            if (getSelectedGCDocument() == null) return;
            if (e.getActionCommand() == GCPreferences.FULL_GC_LINES) {
                getSelectedGCDocument().getModelChart().setShowFullGCLines(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == GCPreferences.INC_GC_LINES) {
                getSelectedGCDocument().getModelChart().setShowIncGCLines(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == GCPreferences.GC_LINES_LINE) {
                getSelectedGCDocument().getModelChart().setShowGCTimesLine(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == GCPreferences.GC_TIMES_RECTANGLES) {
                getSelectedGCDocument().getModelChart().setShowGCTimesRectangles(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == GCPreferences.TOTAL_MEMORY) {
                getSelectedGCDocument().getModelChart().setShowTotalMemoryLine(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == GCPreferences.USED_MEMORY) {
                getSelectedGCDocument().getModelChart().setShowUsedMemoryLine(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == GCPreferences.USED_TENURED_MEMORY) {
                getSelectedGCDocument().getModelChart().setShowUsedTenuredMemoryLine(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == GCPreferences.USED_YOUNG_MEMORY) {
                getSelectedGCDocument().getModelChart().setShowUsedYoungMemoryLine(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == GCPreferences.TENURED_MEMORY) {
                getSelectedGCDocument().getModelChart().setShowTenured(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == GCPreferences.YOUNG_MEMORY) {
                getSelectedGCDocument().getModelChart().setShowYoung(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == GCPreferences.INITIAL_MARK_LEVEL) {
                getSelectedGCDocument().getModelChart().setShowInitialMarkLevel(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == GCPreferences.CONCURRENT_COLLECTION_BEGIN_END) {
                getSelectedGCDocument().getModelChart().setShowConcurrentCollectionBeginEnd(((JCheckBoxMenuItem) e.getSource()).getState());
            }
        }
    }

    public void exit() {
        if (getDesktopPane().getComponentCount() > 0) {
            // close all GCDocuments, other than the selected
            GCDocument selected = getSelectedGCDocument();
            for (int i = getDesktopPane().getComponentCount()-1; i > 0; --i) {
                if (getDesktopPane().getComponent(i) != selected) {
                    ((GCDocument)getDesktopPane().getComponent(i)).dispose();
                }
            }
            // close current internal frame, because only then menu item state is updated
            getSelectedGCDocument().doDefaultCloseAction();
        }
        
        // store current state of menu items
        storePreferences(preferences);
        dispose();
        System.exit(0);
    }

    private void loadPreferences(GCPreferences preferences) {
        if (preferences.isPropertiesLoaded()) {
            for (Entry<String, JCheckBoxMenuItem> menuEntry : gcLineMenuItems.entrySet()) {
                JCheckBoxMenuItem item = menuEntry.getValue();
                item.setState(preferences.getGcLineProperty(menuEntry.getKey()));
                viewMenuActionListener.actionPerformed(new ActionEvent(item, 0, item.getActionCommand()));
            }
            setBounds(preferences.getWindowX(),
                    preferences.getWindowY(),
                    preferences.getWindowWidth(),
                    preferences.getWindowHeight());
            final String lastfile = preferences.getLastFile();
            if (lastfile != null) {
                openFileAction.setSelectedFile(new File(lastfile));
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
                    recentURLsMenu.getRecentURLsModel().add(urls.toArray(new URL[0]));
                }
            }
        }
        else {
            setBounds(0, 0, 800, 600);
        }
    }

    private void storePreferences(GCPreferences preferences) {
        for (Entry<String, JCheckBoxMenuItem> menuEntry : gcLineMenuItems.entrySet()) {
            JCheckBoxMenuItem item = menuEntry.getValue();
            preferences.setGcLineProperty(item.getActionCommand(), item.getState());
        }
        preferences.setWindowWidth(getWidth());
        preferences.setWindowHeight(getHeight());
        preferences.setWindowX(getX());
        preferences.setWindowY(getY());
        if (openFileAction.getLastSelectedFiles().length != 0) {
            preferences.setLastFile(openFileAction.getLastSelectedFiles()[0].getAbsolutePath());
        }
        
        // recent files
        List<String> recentFileList = new LinkedList<String>();
        for (Component recentMenuItem : recentURLsMenu.getMenuComponents()) {
            final OpenRecent openRecent = (OpenRecent)((JMenuItem)recentMenuItem).getAction();
            final URL[] urls = openRecent.getURLs();
            final StringBuffer sb = new StringBuffer();
            for (int j=0; j<urls.length; j++) {
                sb.append(urls[j]);
                sb.append(';');
            }
            recentFileList.add(sb.toString());
        }
        preferences.setRecentFiles(recentFileList);
        
        preferences.store();
    }

    public static void start(final String arg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final GCViewerGui viewer = new GCViewerGui();
                if (arg != null) {
                    if (arg.startsWith("http")) {
                        try {
                            viewer.open(new URL[]{new URL(arg)});
                        } 
                        catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        viewer.open(new File[] {new File(arg)});
                    }
                }
            }
        });
    }

    private static class WindowMenuItemAction extends AbstractAction implements PropertyChangeListener {
        private JInternalFrame internalFrame;

        public WindowMenuItemAction(final InternalFrameEvent e) {
            this.internalFrame = e.getInternalFrame();
            putValue(Action.NAME, internalFrame.getTitle());
            this.internalFrame.addPropertyChangeListener("title", this);
        }

        public void actionPerformed(final ActionEvent ae) {
            try {
                internalFrame.setSelected(true);
            } catch (PropertyVetoException e1) {
                e1.printStackTrace();
            }
        }

        public JInternalFrame getInternalFrame() {
            return internalFrame;
        }

        public void propertyChange(final PropertyChangeEvent evt) {
            putValue(Action.NAME, internalFrame.getTitle());
        }
    }

}
