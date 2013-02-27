package com.tagtraum.perf.gcviewer;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.border.SoftBevelBorder;

import com.tagtraum.perf.gcviewer.util.BuildInfoReader;

/**
 * About dialog.
 * 
 * @author Hendrik Schreiber
 * @version $Id: $
 */
public class AboutDialog extends JDialog implements ActionListener {

    public static String vcid = "$Id: AboutDialog.java,v 1.1.1.1 2002/01/15 19:48:45 hendriks73 Exp $";
    private static ResourceBundle localStrings = ResourceBundle.getBundle("com.tagtraum.perf.gcviewer.localStrings");
    private static final String GCVIEWER_HOMEPAGE = "https://github.com/chewiebug/gcviewer/wiki";

    private Frame frame;

    public AboutDialog(Frame f) {
        super(f, localStrings.getString("about_dialog_title"), true);
        this.frame = f;
        setLocation(20, 20);
        Panel logoPanel = new Panel();
        ImageIcon logoIcon = new ImageIcon(getClass().getResource(localStrings.getString("about_dialog_image")));
        JLabel la_icon = new JLabel(logoIcon);
        la_icon.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
        logoPanel.add(la_icon);

        JPanel versionPanel = new JPanel();
        versionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        versionPanel.setLayout(new GridBagLayout());
        
        JLabel copyright = new JLabel("\u00A9" + " 2011-2012: Joerg Wuethrich", JLabel.CENTER);
        
        JLabel contributorsLabel = new JLabel("contributors (alphabetically ordered):", JLabel.CENTER);
        contributorsLabel.setForeground(Color.GRAY);
        JLabel contributors = new JLabel(
                "<html><center>Peter Bilstein | Cka3o4Huk | Bernd Eckenfels <br>" +
        		"Neil Gentleman | Johan Kaving | Carl Meyer <br>" +
        		"Rupesh Ramachandran | Serafín Sedano<br>" +
        		"Andrey Skripalschikov</center><html>",
        		JLabel.CENTER);
        
        JLabel version = new JLabel("<html><font color=\"gray\">version:</font> " + BuildInfoReader.getVersion() + "</html>", JLabel.CENTER);
        JLabel buildDate = new JLabel("<html><font color=\"gray\">build date:</font> " + BuildInfoReader.getBuildDate() + "</html>", JLabel.CENTER);
        
        JLabel spacer1 = new JLabel("spacer");
        spacer1.setForeground(getBackground());
        JLabel spacer2 = new JLabel("spacer");
        spacer2.setForeground(getBackground());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints.gridx = 0;

        versionPanel.add(copyright, gridBagConstraints);
        gridBagConstraints.gridy = 1;
        versionPanel.add(spacer1, gridBagConstraints);
        gridBagConstraints.gridy = 2;
        versionPanel.add(contributorsLabel, gridBagConstraints);
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        versionPanel.add(contributors, gridBagConstraints);
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        versionPanel.add(spacer2, gridBagConstraints);
        gridBagConstraints.gridy = 5;
        versionPanel.add(version, gridBagConstraints);
        gridBagConstraints.gridy = 6;
        versionPanel.add(buildDate, gridBagConstraints);
        
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        if (ExternalViewer.isSupported()) {
            try {
                JButton homepageButton = new JButton("Homepage");
                homepageButton.addActionListener(new ExternalViewer(new URL(
                        GCVIEWER_HOMEPAGE)));
                buttonPanel.add(homepageButton);
            } catch (MalformedURLException e) {
                // should never happen
                e.printStackTrace();
            }
        }

        JButton okButton = new JButton(localStrings.getString("button_ok"));
        okButton.setActionCommand("ok");
        okButton.addActionListener(this);
        buttonPanel.add(okButton);
        getContentPane().add("North", logoPanel);
        getContentPane().add("Center", versionPanel);
        getContentPane().add("South", buttonPanel);
        pack();
        setResizable(false);
        setVisible(false);
    }

    protected JRootPane createRootPane() {
        KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        KeyStroke enterStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        
        JRootPane rootPane = new JRootPane();
        rootPane.registerKeyboardAction(this, escapeStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        rootPane.registerKeyboardAction(this, enterStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        return rootPane;
    }
    
    public void actionPerformed(ActionEvent e) {
        setVisible(false);
    }

    public void setVisible(boolean visible) {
        if (visible) {
            setLocation((int) frame.getLocation().getX() + (frame.getWidth() / 2) - (getWidth() / 2), 
                    (int) frame.getLocation().getY() + (frame.getHeight() / 2) - (getHeight() / 2));
        }
        
        super.setVisible(visible);
    }

    private static class ExternalViewer implements ActionListener {

        private static final boolean WINDOWS = System.getProperty("os.name")
                .toLowerCase().startsWith("win");
        private static final boolean MAC = System.getProperty("os.name")
                .toLowerCase().indexOf("mac") != -1;

        // The default system browser under Mac OS X.
        private static final String MAC_PATH = "open";
        // The default system browser under WINDOWS.
        private static final String WIN_PATH = "rundll32";
        // The flag to display a url.
        private static final String WIN_FLAG = "url.dll,FileProtocolHandler";

        private URL url;

        public ExternalViewer(URL url) {
            this.url = url;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                displayURL(url);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        /**
         * Display a file in the system browser. If you want to display a file,
         * you must include the absolute path name.
         * 
         * @param url
         *            the file's url (the url must start with either "http://"
         *            or "file://").
         */
        public static void displayURL(URL url) throws IOException {
            String cmd = null;
            String urlString = url.toString();
            if (WINDOWS) {
                // cmd = 'rundll32 url.dll,FileProtocolHandler http://...'
                // make sure, file URLs start with file:// instead of just
                // file:/
                if (urlString.startsWith("file:/")) {
                    if (!urlString.startsWith("file://")) {
                        urlString = "file://"
                                + urlString.substring("file:/".length());
                    }
                }
                // replace %20 with spaces
                try {
                    urlString = URLDecoder.decode(urlString, "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cmd = WIN_PATH + " " + WIN_FLAG + " " + urlString;
                /*
                 * if (urlString.startsWith("file:")) { } else { cmd =
                 * "rundll32.exe msconf.dll,CallToProtocolHandler " + urlString;
                 * }
                 */
                Runtime.getRuntime().exec(cmd);
            } else if (MAC) {
                // TODO: fix this!
                // hack to display jsps in TextEdit
                if (urlString.endsWith(".jsp") || urlString.endsWith(".jspx")) {
                    cmd = MAC_PATH + " -e "
                            + urlString.substring("file:".length());
                } else {
                    cmd = MAC_PATH + " " + url;
                }
                Runtime.getRuntime().exec(cmd);
            }
        }

        public static boolean isSupported() {
            return WINDOWS || MAC;
        }
    }

}
