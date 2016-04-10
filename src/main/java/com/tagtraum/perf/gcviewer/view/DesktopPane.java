package com.tagtraum.perf.gcviewer.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;

import com.tagtraum.perf.gcviewer.util.BuildInfoReader;
import com.tagtraum.perf.gcviewer.view.util.ImageHelper;

/**
 * DesktopPane is the "background" of the application after opening.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class DesktopPane extends JDesktopPane {

    private ImageIcon logoIcon = ImageHelper.loadImageIcon("gcviewer_background.png");
    
    public DesktopPane() {
        super();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        setBackground(Color.WHITE);
        
        // draw logo
        g.drawImage(logoIcon.getImage(),
                getWidth()/2 - logoIcon.getIconWidth()/2,
                getHeight()/2 - logoIcon.getIconHeight()/2,
                logoIcon.getIconWidth(),
                logoIcon.getIconHeight(),
                logoIcon.getImageObserver());

        drawVersionString(g, logoIcon);
    }

    /**
     * Adds version string below <code>logoImage</code>.
     *
     * @param g
     */
    private void drawVersionString(Graphics g, ImageIcon logoImage) {
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Serif", Font.BOLD, 12));

        // use anti aliasing to draw string
        Graphics2D g2d = (Graphics2D)g;
        Object oldAAHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        String versionString = "version: " + BuildInfoReader.getVersion() + " (" + BuildInfoReader.getBuildDate() + ")";
        g.drawString(versionString,
                getWidth()/2 - g.getFontMetrics().stringWidth(versionString)/2,
                getHeight()/2 + logoImage.getIconHeight()/2 + 25);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAAHint);
    }

    public boolean isOpaque() {
        return true;
    }
}
