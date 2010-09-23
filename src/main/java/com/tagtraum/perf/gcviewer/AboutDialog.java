package com.tagtraum.perf.gcviewer;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.net.URL;
import java.net.URLDecoder;
import java.net.MalformedURLException;
import java.io.IOException;


/**
 * About dialog.
 *
 * @author Hendrik Schreiber
 * @version $Id: $
 */
public class AboutDialog extends JDialog implements ActionListener {

    /**
     * Source-Version
     */
    public static String vcid = "$Id: AboutDialog.java,v 1.1.1.1 2002/01/15 19:48:45 hendriks73 Exp $";
    private static ResourceBundle localStrings = ResourceBundle.getBundle("com.tagtraum.perf.gcviewer.localStrings");
    private static final String GCVIEWER_HOMEPAGE = "http://www.tagtraum.com/";

    private Frame frame;

    public AboutDialog(Frame f) {
        super(f, localStrings.getString("about_dialog_title"), true);
        this.frame = f;
        setLocation(20, 20);
        Panel panel0 = new Panel();
        ImageIcon logoIcon = new ImageIcon(getClass().getResource(localStrings.getString("about_dialog_image")));
        JLabel la_icon = new JLabel(logoIcon);
        //la_icon.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        la_icon.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
        panel0.add(la_icon);
        Panel panel2 = new Panel();
        panel2.setLayout(new FlowLayout(FlowLayout.RIGHT));
        if (ExternalViewer.isSupported()) {
            try {
                JButton homepageButton = new JButton("Homepage");
                homepageButton.addActionListener(new ExternalViewer(new URL(GCVIEWER_HOMEPAGE)));
                panel2.add(homepageButton);
            } catch (MalformedURLException e) {
                // should never happen
                e.printStackTrace();
            }
        }

        JButton okButton = new JButton(localStrings.getString("button_ok"));
        okButton.setActionCommand("ok");
        okButton.addActionListener(this);
        panel2.add(okButton);
        getContentPane().add("North", panel0);
        // getContentPane().add("Center",panel1);
        getContentPane().add("South", panel2);
        pack();
        setResizable(false);
        setVisible(false);
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("ok")) {
            setVisible(false);
        }
    }

    public void setVisible(boolean visible) {
        if (visible) setLocation((int) frame.getLocation().getX() + (frame.getWidth()/2) - (getWidth()/2),
                (int) frame.getLocation().getY() + (frame.getHeight()/2) - (getHeight()/2));
        super.setVisible(visible);
    }

    private static class ExternalViewer implements ActionListener {

        private static final boolean WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("win");
        private static final boolean MAC = System.getProperty("os.name").toLowerCase().indexOf("mac") != -1;

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
         * Display a file in the system browser.  If you want to display a
         * file, you must include the absolute path name.
         *
         * @param url the file's url (the url must start with either "http://" or
         *            "file://").
         */
        public static void displayURL(URL url) throws IOException {
            String cmd = null;
            String urlString = url.toString();
            if (WINDOWS) {
                // cmd = 'rundll32 url.dll,FileProtocolHandler http://...'
                // make sure, file URLs start with file:// instead of just file:/
                if (urlString.startsWith("file:/")) {
                    if (!urlString.startsWith("file://")) {
                        urlString = "file://" + urlString.substring("file:/".length());
                    }
                }
                // replace %20 with spaces
                try {
                    urlString = URLDecoder.decode(urlString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cmd = WIN_PATH + " " + WIN_FLAG + " " + urlString;
                /*
                if (urlString.startsWith("file:")) {
                }
                else {
                    cmd = "rundll32.exe msconf.dll,CallToProtocolHandler " + urlString;
                }
                */
                Runtime.getRuntime().exec(cmd);
            } else if (MAC) {
                // TODO: fix this!
                // hack to display jsps in TextEdit
                if (urlString.endsWith(".jsp") || urlString.endsWith(".jspx")) {
                    cmd = MAC_PATH + " -e " + urlString.substring("file:".length());
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
