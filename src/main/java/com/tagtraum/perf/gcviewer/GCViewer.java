package com.tagtraum.perf.gcviewer;

import com.tagtraum.perf.gcviewer.action.*;
import com.tagtraum.perf.gcviewer.renderer.*;
import com.tagtraum.perf.gcviewer.util.NumberParser;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Main class.
 *
 * Date: Jan 30, 2002
 * Time: 4:59:49 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class GCViewer extends JFrame {

    public static ResourceBundle localStrings = ResourceBundle.getBundle("com.tagtraum.perf.gcviewer.localStrings");
    private static final String PREFERENCE_VERSION = "1.1";
    private static final String FULL_GC_LINES = "fullgclines";
    private static final String INC_GC_LINES = "incgclines";
    private static final String GC_LINES_LINE = "gctimesline";
    private static final String GC_TIMES_RECTANGLES = "gctimesrectangles";
    private static final String TOTAL_MEMORY = "totalmemory";
    private static final String USED_MEMORY = "usedmemory";
    private static final String TENURED_MEMORY = "tenuredmemory";
    private static final String YOUNG_MEMORY = "youngmemory";

    private JToolBar toolBar;
    private ActionListener viewActionListener;
    private JMenu fileMenu;
    private JMenu windowMenu;
    private JMenuItem exportMenuItem;
    private ButtonGroup windowCheckBoxGroup = new ButtonGroup();
    private JDesktopPane desktopPane;
    private JComboBox zoomComboBox;
    // actions
    private Exit exitAction = new Exit(this);
    private About aboutAction = new About(this);
    private OpenFile openFileAction = new OpenFile(this);
    private OpenURL openURLAction = new OpenURL(this);
    //private AddFile addFileAction = new AddFile(this);
    private Refresh refreshAction = new Refresh(this);
    private Export exportAction = new Export(this);
    private Zoom zoomAction = new Zoom(this);
    private Arrange arrangeAction = new Arrange(this);
    private Watch watchAction = new Watch(this);
    private JCheckBoxMenuItem showDataPanel;
    private JCheckBoxMenuItem fullGCLines;
    private JCheckBoxMenuItem incGCLines;
    private JCheckBoxMenuItem gcTimesLine;
    private JCheckBoxMenuItem gcTimesRectangle;
    private JCheckBoxMenuItem usedMemory;
    private JCheckBoxMenuItem totalMemory;
    private JCheckBoxMenuItem tenuredMemory;
    private JCheckBoxMenuItem youngMemory;
    private JCheckBoxMenuItem watchCheckBoxMenuItem;
    private JCheckBoxMenuItem antiAlias;
    private Map lines;
    private JToggleButton watchToggle;

    private RecentURLsMenu recentURLsMenu;

    public GCViewer() {
        super("tagtraum industries incorporated - GCViewer");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("gcviewericon.gif")));
        desktopPane = new DesktopPane(this);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                exit();
            }
        });
        viewActionListener = new ViewMenuActionListener();
        recentURLsMenu = new RecentURLsMenu(this);
        openURLAction.setRecentURLsModel(recentURLsMenu.getRecentURLsModel());

        setJMenuBar(initMenuBar());
        toolBar = initToolBar();

        // cross reference the two toggle buttons
        watchCheckBoxMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                watchToggle.setSelected(watchCheckBoxMenuItem.getState());
            }
        });
        watchToggle.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                watchCheckBoxMenuItem.setState(watchToggle.isSelected());
            }
        });

        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(desktopPane, BorderLayout.CENTER);

        loadPreferences();
        setVisible(true);
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
        }

        public void internalFrameClosed(final InternalFrameEvent e) {
            if (desktopPane.getAllFrames().length == 0) arrangeAction.setEnabled(false);
            ((GCDocument)e.getInternalFrame()).getRefreshWatchDog().stop();
            // remove menuitem from menu and from button group
            for (int i=2; i<windowMenu.getItemCount(); i++) {
                final JMenuItem item = windowMenu.getItem(i);
                if (((WindowMenuItemAction)item.getAction()).getGcDocument() == e.getInternalFrame()) {
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
                if (((WindowMenuItemAction)item.getAction()).getGcDocument() == e.getInternalFrame()) {
                    item.setSelected(true);
                    break;
                }
            }
            zoomComboBox.setSelectedItem((int) (getSelectedGCDocument().getModelChart().getScaleFactor() * 1000.0) + "%");
            exportAction.setEnabled(true);
            refreshAction.setEnabled(true);
            watchAction.setEnabled(true);
            watchCheckBoxMenuItem.setSelected(getSelectedGCDocument().isWatched());
            getSelectedGCDocument().getRefreshWatchDog().setAction(watchAction);
            watchToggle.setSelected(getSelectedGCDocument().isWatched());
            //addFileAction.setEnabled(true);
            zoomAction.setEnabled(true);
            arrangeAction.setEnabled(true);
            fullGCLines.setState(getSelectedGCDocument().getModelChart().isShowFullGCLines());
            incGCLines.setState(getSelectedGCDocument().getModelChart().isShowIncGCLines());
            gcTimesLine.setState(getSelectedGCDocument().getModelChart().isShowGCTimesLine());
            gcTimesRectangle.setState(getSelectedGCDocument().getModelChart().isShowGCTimesRectangles());
            totalMemory.setState(getSelectedGCDocument().getModelChart().isShowTotalMemoryLine());
            usedMemory.setState(getSelectedGCDocument().getModelChart().isShowUsedMemoryLine());
            tenuredMemory.setState(getSelectedGCDocument().getModelChart().isShowTenured());
            youngMemory.setState(getSelectedGCDocument().getModelChart().isShowYoung());
            showDataPanel.setState(getSelectedGCDocument().isShowModelPanel());
            antiAlias.setSelected(getSelectedGCDocument().getModelChart().isAntiAlias());
        }

        public void internalFrameDeactivated(final InternalFrameEvent e) {
            exportAction.setEnabled(false);
            refreshAction.setEnabled(false);
            watchAction.setEnabled(false);
            //addFileAction.setEnabled(false);
            zoomAction.setEnabled(false);
            watchToggle.setSelected(false);
            watchCheckBoxMenuItem.setSelected(false);
            ((GCDocument)e.getInternalFrame()).getRefreshWatchDog().setAction(null);
        }
    };

    public GCDocument getSelectedGCDocument() {
        return (GCDocument)desktopPane.getSelectedFrame();
    }

    private static URL[] convertFilesToURLs(final File[] files) throws MalformedURLException {
        final URL[] urls = new URL[files.length];
        for (int i=0; i<files.length; i++) {
            urls[i] = files[i].getAbsoluteFile().toURL();
        }
        return urls;
    }

    public void open(final File[] files) {
        // delegate to open(...)
        try {
            final URL[] urls = convertFilesToURLs(files);
            if (files.length >= 1) {
                //addFileAction.setSelectedFile(files[0]);
            }
            open(urls);
        } catch (RuntimeException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(GCViewer.this, e.toString() + " " + e.getLocalizedMessage(), localStrings.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            //e.printStackTrace();
            JOptionPane.showMessageDialog(GCViewer.this, e.getLocalizedMessage(), localStrings.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
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
        } catch (RuntimeException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(GCViewer.this, e.toString() + " " + e.getLocalizedMessage(), localStrings.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            //e.printStackTrace();
            JOptionPane.showMessageDialog(GCViewer.this, e.getLocalizedMessage(), localStrings.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void add(final File[] files) {
        try {
            if (files.length >= 0) openFileAction.setSelectedFile(files[0]);
            final URL[] urls = convertFilesToURLs(files);
            add(urls);
        } catch (RuntimeException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(GCViewer.this, e.toString() + " " + e.getLocalizedMessage(), localStrings.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            //e.printStackTrace();
            JOptionPane.showMessageDialog(GCViewer.this, e.getLocalizedMessage(), localStrings.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
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
        } catch (RuntimeException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(GCViewer.this, e.toString() + " " + e.getLocalizedMessage(), localStrings.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            //e.printStackTrace();
            JOptionPane.showMessageDialog(GCViewer.this, e.getLocalizedMessage(), localStrings.getString("fileopen_dialog_read_file_failed"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private JToolBar initToolBar() {
        final JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(openFileAction);
        toolBar.add(openURLAction);
        //toolBar.add(addFileAction);
        toolBar.add(exportAction);
        toolBar.add(refreshAction);
        watchToggle = new JToggleButton();
        watchToggle.setAction(watchAction);
        watchToggle.setText("");
        toolBar.add(watchToggle);
        toolBar.addSeparator();
        zoomComboBox = new JComboBox(new String[] {"1%", "5%", "10%", "50%", "100%", "200%", "300%", "500%", "1000%", "5000%"});
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

        fileMenu = new JMenu(localStrings.getString("main_frame_menu_file"));
        fileMenu.setMnemonic(localStrings.getString("main_frame_menu_mnemonic_file").charAt(0));
        menuBar.add(fileMenu);

        JMenuItem menuItem = new JMenuItem(openFileAction);
        fileMenu.add(menuItem);

        menuItem = new JMenuItem(openURLAction);
        fileMenu.add(menuItem);

        recentURLsMenu.setIcon(createEmptyImageIcon(20, 20));
        fileMenu.add(recentURLsMenu);

        /*
        menuItem = new JMenuItem(addFileAction);
        fileMenu.add(menuItem);
        */

        exportMenuItem = new JMenuItem(exportAction);
        fileMenu.add(exportMenuItem);

        menuItem = new JMenuItem(refreshAction);
        fileMenu.add(menuItem);

        watchCheckBoxMenuItem = new JCheckBoxMenuItem(watchAction);
        fileMenu.add(watchCheckBoxMenuItem);

        menuItem = new JMenuItem(exitAction);
        fileMenu.add(menuItem);

        final JMenu viewMenu = new JMenu(localStrings.getString("main_frame_menu_view"));

        viewMenu.setMnemonic(localStrings.getString("main_frame_menu_mnemonic_view").charAt(0));
        menuBar.add(viewMenu);

        showDataPanel = new JCheckBoxMenuItem(localStrings.getString("main_frame_menuitem_show_data_panel"), true);
        showDataPanel.setMnemonic(localStrings.getString("main_frame_menuitem_mnemonic_show_data_panel").charAt(0));
        showDataPanel.setIcon(createEmptyImageIcon(20, 20));
        showDataPanel.setToolTipText(localStrings.getString("main_frame_menuitem_hint_show_data_panel"));
        showDataPanel.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e) {
                final GCDocument gcDocument = getSelectedGCDocument();
                if (gcDocument != null) {
                    gcDocument.setShowModelPanel(showDataPanel.getState());
                }
            }
        });
        viewMenu.add(showDataPanel);
        viewMenu.addSeparator();

        antiAlias = new JCheckBoxMenuItem(localStrings.getString("main_frame_menuitem_antialias"), true);
        antiAlias.setMnemonic(localStrings.getString("main_frame_menuitem_mnemonic_antialias").charAt(0));
        antiAlias.setIcon(createEmptyImageIcon(20, 20));
        antiAlias.setToolTipText(localStrings.getString("main_frame_menuitem_hint_antialias"));
        antiAlias.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e) {
                final GCDocument gcDocument = getSelectedGCDocument();
                if (gcDocument != null) {
                    gcDocument.getModelChart().setAntiAlias(antiAlias.getState());
                    gcDocument.relayout();
                }
            }
        });
        antiAlias.setSelected(false);
        viewMenu.add(antiAlias);
        viewMenu.addSeparator();

        lines = new HashMap();

        fullGCLines = new JCheckBoxMenuItem(localStrings.getString("main_frame_menuitem_full_gc_lines"), true);
        fullGCLines.setMnemonic(localStrings.getString("main_frame_menuitem_mnemonic_full_gc_lines").charAt(0));
        fullGCLines.setToolTipText(localStrings.getString("main_frame_menuitem_hint_full_gc_lines"));
        fullGCLines.setIcon(createMonoColoredImageIcon(FullGCLineRenderer.DEFAULT_LINEPAINT, 20, 20));
        fullGCLines.setActionCommand(FULL_GC_LINES);
        fullGCLines.addActionListener(viewActionListener);
        viewMenu.add(fullGCLines);
        lines.put(FULL_GC_LINES, fullGCLines);

        incGCLines = new JCheckBoxMenuItem(localStrings.getString("main_frame_menuitem_inc_gc_lines"), true);
        incGCLines.setMnemonic(localStrings.getString("main_frame_menuitem_mnemonic_inc_gc_lines").charAt(0));
        incGCLines.setToolTipText(localStrings.getString("main_frame_menuitem_hint_inc_gc_lines"));
        incGCLines.setIcon(createMonoColoredImageIcon(IncLineRenderer.DEFAULT_LINEPAINT, 20, 20));
        incGCLines.setActionCommand(INC_GC_LINES);
        incGCLines.addActionListener(viewActionListener);
        viewMenu.add(incGCLines);
        lines.put(INC_GC_LINES, incGCLines);

        gcTimesLine = new JCheckBoxMenuItem(localStrings.getString("main_frame_menuitem_gc_times_line"), true);
        gcTimesLine.setMnemonic(localStrings.getString("main_frame_menuitem_mnemonic_gc_times_line").charAt(0));
        gcTimesLine.setToolTipText(localStrings.getString("main_frame_menuitem_hint_gc_times_line"));
        gcTimesLine.setIcon(createMonoColoredImageIcon(GCTimesRenderer.DEFAULT_LINEPAINT, 20, 20));
        gcTimesLine.setActionCommand(GC_LINES_LINE);
        gcTimesLine.addActionListener(viewActionListener);
        viewMenu.add(gcTimesLine);
        lines.put(GC_LINES_LINE, gcTimesLine);

        gcTimesRectangle = new JCheckBoxMenuItem(localStrings.getString("main_frame_menuitem_gc_times_rectangles"), true);
        gcTimesRectangle.setMnemonic(localStrings.getString("main_frame_menuitem_mnemonic_gc_times_rectangles").charAt(0));
        gcTimesRectangle.setToolTipText(localStrings.getString("main_frame_menuitem_hint_gc_times_rectangles"));
        gcTimesRectangle.setIcon(createMonoColoredImageIcon(GCRectanglesRenderer.DEFAULT_LINEPAINT, 20, 20));
        gcTimesRectangle.setActionCommand(GC_TIMES_RECTANGLES);
        gcTimesRectangle.addActionListener(viewActionListener);
        viewMenu.add(gcTimesRectangle);
        lines.put(GC_TIMES_RECTANGLES, gcTimesRectangle);

        totalMemory = new JCheckBoxMenuItem(localStrings.getString("main_frame_menuitem_total_memory"), true);
        totalMemory.setMnemonic(localStrings.getString("main_frame_menuitem_mnemonic_total_memory").charAt(0));
        totalMemory.setToolTipText(localStrings.getString("main_frame_menuitem_hint_total_memory"));
        totalMemory.setIcon(createMonoColoredImageIcon(TotalHeapRenderer.DEFAULT_LINEPAINT, 20, 20));
        totalMemory.setActionCommand(TOTAL_MEMORY);
        totalMemory.addActionListener(viewActionListener);
        viewMenu.add(totalMemory);
        lines.put(TOTAL_MEMORY, totalMemory);

        tenuredMemory = new JCheckBoxMenuItem(localStrings.getString("main_frame_menuitem_tenured_memory"), true);
        tenuredMemory.setMnemonic(localStrings.getString("main_frame_menuitem_mnemonic_tenured_memory").charAt(0));
        tenuredMemory.setToolTipText(localStrings.getString("main_frame_menuitem_hint_tenured_memory"));
        tenuredMemory.setIcon(createMonoColoredImageIcon(TotalTenuredRenderer.DEFAULT_LINEPAINT, 20, 20));
        tenuredMemory.setActionCommand(TENURED_MEMORY);
        tenuredMemory.addActionListener(viewActionListener);
        viewMenu.add(tenuredMemory);
        lines.put(TENURED_MEMORY, tenuredMemory);

        youngMemory = new JCheckBoxMenuItem(localStrings.getString("main_frame_menuitem_young_memory"), true);
        youngMemory.setMnemonic(localStrings.getString("main_frame_menuitem_mnemonic_young_memory").charAt(0));
        youngMemory.setToolTipText(localStrings.getString("main_frame_menuitem_hint_young_memory"));
        youngMemory.setIcon(createMonoColoredImageIcon(TotalYoungRenderer.DEFAULT_LINEPAINT, 20, 20));
        youngMemory.setActionCommand(YOUNG_MEMORY);
        youngMemory.addActionListener(viewActionListener);
        viewMenu.add(youngMemory);
        lines.put(YOUNG_MEMORY, youngMemory);

        usedMemory = new JCheckBoxMenuItem(localStrings.getString("main_frame_menuitem_used_memory"), true);
        usedMemory.setMnemonic(localStrings.getString("main_frame_menuitem_mnemonic_used_memory").charAt(0));
        usedMemory.setToolTipText(localStrings.getString("main_frame_menuitem_hint_used_memory"));
        usedMemory.setIcon(createMonoColoredImageIcon(UsedHeapRenderer.DEFAULT_LINEPAINT, 20, 20));
        usedMemory.setActionCommand(USED_MEMORY);
        usedMemory.addActionListener(viewActionListener);
        viewMenu.add(usedMemory);
        lines.put(USED_MEMORY, usedMemory);

        windowMenu = new JMenu(localStrings.getString("main_frame_menu_window"));
        windowMenu.setMnemonic(localStrings.getString("main_frame_menu_mnemonic_window").charAt(0));
        menuBar.add(windowMenu);

        menuItem = new JMenuItem(arrangeAction);
        windowMenu.add(menuItem);
        windowMenu.addSeparator();

        final JMenu helpMenu = new JMenu(localStrings.getString("main_frame_menu_help"));
        helpMenu.setMnemonic(localStrings.getString("main_frame_menu_mnemonic_help").charAt(0));
        menuBar.add(helpMenu);

        menuItem = new JMenuItem(aboutAction);
        helpMenu.add(menuItem);

        return menuBar;
    }

    private class ViewMenuActionListener implements ActionListener {
        public void actionPerformed(final ActionEvent e) {
            if (getSelectedGCDocument() == null) return;
            if (e.getActionCommand() == FULL_GC_LINES) {
                getSelectedGCDocument().getModelChart().setShowFullGCLines(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == INC_GC_LINES) {
                getSelectedGCDocument().getModelChart().setShowIncGCLines(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == GC_LINES_LINE) {
                getSelectedGCDocument().getModelChart().setShowGCTimesLine(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == GC_TIMES_RECTANGLES) {
                getSelectedGCDocument().getModelChart().setShowGCTimesRectangles(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == TOTAL_MEMORY) {
                getSelectedGCDocument().getModelChart().setShowTotalMemoryLine(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == USED_MEMORY) {
                getSelectedGCDocument().getModelChart().setShowUsedMemoryLine(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == TENURED_MEMORY) {
                getSelectedGCDocument().getModelChart().setShowTenured(((JCheckBoxMenuItem) e.getSource()).getState());
            } else if (e.getActionCommand() == YOUNG_MEMORY) {
                getSelectedGCDocument().getModelChart().setShowYoung(((JCheckBoxMenuItem) e.getSource()).getState());
            }
        }
    }

    public void exit() {
        storePreferences();
        dispose();
        System.exit(0);
    }

    private void loadPreferences() {
        final File preferences = getPreferencesFile();
        if (preferences.exists()) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(preferences);
                final Properties properties = new Properties();
                properties.load(in);
                if (PREFERENCE_VERSION.equals(properties.getProperty("preferences.version"))) {
                    for (Iterator line=lines.keySet().iterator(); line.hasNext(); ) {
                        final String name = (String)line.next();
                        final JCheckBoxMenuItem item = (JCheckBoxMenuItem)lines.get(name);
                        item.setState("true".equals(properties.getProperty("view." + name, "true")));
                        viewActionListener.actionPerformed(new ActionEvent(item, 0, item.getActionCommand()));
                    }
                    //modelChart.setScaleFactor(getDoubleProperty(properties.getProperty("view.zoom"), 100.0d));
                    final int width = getIntegerProperty(properties.getProperty("window.width"), 800);
                    final int height = getIntegerProperty(properties.getProperty("window.height"), 600);
                    final int x = getIntegerProperty(properties.getProperty("window.x"), 0);
                    final int y = getIntegerProperty(properties.getProperty("window.y"), 0);
                    setBounds(x, y, width, height);
                    final String lastfile = properties.getProperty("lastfile");
                    if (lastfile != null) {
                        openFileAction.setSelectedFile(new File(lastfile));
                    }
                    // recent files
                    for (int i=0;; i++) {
                        final String recentFiles = properties.getProperty("recent." + i);
                        if (recentFiles != null) {
                            final StringTokenizer st = new StringTokenizer(recentFiles, ";");
                            final URL[] urls = new URL[st.countTokens()];
                            for (int j=0; st.hasMoreTokens(); j++) {
                                urls[j] = new URL(st.nextToken());
                            }
                            recentURLsMenu.getRecentURLsModel().add(urls);
                        }
                        else {
                            break;
                        }
                    }
                }
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
            }
            finally {
                if (in != null) try { in.close(); } catch (IOException ioe) {}
            }
        }
        else {
            setBounds(0, 0, 800, 600);
        }
    }

    private void storePreferences() {
        final File preferences = getPreferencesFile();
        if (true) {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(preferences);
                final Properties properties = new Properties();
                for (Iterator line=lines.keySet().iterator(); line.hasNext(); ) {
                    final String name = (String)line.next();
                    final JCheckBoxMenuItem item = (JCheckBoxMenuItem)lines.get(name);
                    properties.setProperty("view."+item.getActionCommand(), item.getState()?Boolean.TRUE.toString():Boolean.FALSE.toString());
                }
                //properties.setProperty("view.zoom", Double.toString(modelChart.getScaleFactor()));
                properties.setProperty("window.width", Integer.toString(getWidth()));
                properties.setProperty("window.height", Integer.toString(getHeight()));
                properties.setProperty("window.x", Integer.toString(getX()));
                properties.setProperty("window.y", Integer.toString(getY()));
                properties.setProperty("preferences.version", PREFERENCE_VERSION);
                if (openFileAction.getLastSelectedFiles().length != 0) {
                    properties.setProperty("lastfile", openFileAction.getLastSelectedFiles()[0].getAbsolutePath());
                }
                // recent files
                final Component[] recentMenuItems = recentURLsMenu.getMenuComponents();
                for (int i=0; i<recentMenuItems.length; i++) {
                    final OpenRecent openRecent = (OpenRecent)((JMenuItem)recentMenuItems[i]).getAction();
                    final URL[] urls = openRecent.getURLs();
                    final StringBuffer sb = new StringBuffer();
                    for (int j=0; j<urls.length; j++) {
                        sb.append(urls[j]);
                        sb.append(';');
                    }
                    properties.setProperty("recent."+(recentMenuItems.length - i - 1), sb.toString());
                }
                properties.store(out, "GCViewer preferences " + new Date());
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
            }
            finally {
                if (out != null) try { out.close(); } catch (IOException ioe) {}
            }
        }
    }

    private int getIntegerProperty(final String stringValue, final int defaultValue) {
        int value = defaultValue;
        try {
            if (stringValue != null) value = NumberParser.parseInt(stringValue);
        }
        catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
        return value;
    }

    // currently not used
    /*
    private double getDoubleProperty(String stringValue, double defaultValue) {
        double value = defaultValue;
        try {
            if (stringValue != null) value = Double.parseDouble(stringValue);
        }
        catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
        return value;
    }
    */

    private File getPreferencesFile() {
        return new File(System.getProperty("user.home")+"/gcviewer.properties");
    }

    public static void main(final String[] args) {
        if (args.length > 1)
            usage();
        else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    final GCViewer viewer = new GCViewer();
                    if (args.length == 1) {
                        viewer.open(new File[] {new File(args[0])});
                    }
                }
            });
        }
    }

    private static void usage() {
        System.out.println("GCViewer");
        System.out.println("java com.tagtraum.perf.gcviewer.GCViewer [<gc-log-file>]");
    }

    private static class WindowMenuItemAction extends AbstractAction implements PropertyChangeListener {
        private GCDocument gcDocument;

        public WindowMenuItemAction(final InternalFrameEvent e) {
            this.gcDocument = (GCDocument)e.getInternalFrame();
            putValue(Action.NAME, gcDocument.getTitle());
            this.gcDocument.addPropertyChangeListener("title", this);
        }

        public void actionPerformed(final ActionEvent ae) {
            try {
                gcDocument.setSelected(true);
            } catch (PropertyVetoException e1) {
                e1.printStackTrace();
            }
        }

        public GCDocument getGcDocument() {
            return gcDocument;
        }

        public void propertyChange(final PropertyChangeEvent evt) {
            putValue(Action.NAME, gcDocument.getTitle());
        }
    }

}
