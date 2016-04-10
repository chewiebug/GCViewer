package com.tagtraum.perf.gcviewer.view.util;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.swing.JOptionPane;

/**
 * Helperclass to display urls in a browser on all platforms.
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 25.04.2013</p>
 *
 */
public class UrlDisplayHelper {
    /**
     * Returns <code>true</code> if the platform supports displaying of urls.
     *
     * @return <code>true</code> if displaying of urls is supported
     */
    public static boolean displayUrlIsSupported() {
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE);
    }

    /**
     * Display given <code>url</code> in the default browser. If this action is not supported
     * a message dialog with an error message is displayed.
     *
     * @param parent parent for error message dialog
     * @param url url to be displayed
     */
    public static void displayUrl(Component parent, String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        }
        catch (IOException | IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
            // TODO localize!
            JOptionPane.showMessageDialog(parent,
                    "oops - could not show url ('" + url + "'): " + e.toString(),
                    "oops",
                    JOptionPane.ERROR_MESSAGE | JOptionPane.OK_OPTION);
        }
    }

    /**
     * Convenience method to display an url.
     *
     * @param parent component to display in
     * @param url URL to display
     * @see #displayUrl(Component, String)
     */
    public static void displayUrl(Component parent, URL url) {
        displayUrl(parent, url.toString());
    }

}
