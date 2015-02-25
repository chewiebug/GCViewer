package com.tagtraum.perf.gcviewer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;

import com.tagtraum.perf.gcviewer.util.BuildInfoReader;

/**
 * DesktopPane is the "background" of the application after opening.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class DesktopPane extends JDesktopPane {

    public DesktopPane(final GCViewerGui gcViewer) {
        // TODO refactor; looks very similar to GCDocument implementation
        gcViewer.setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, new DropTargetListener(){
            public void dragEnter(DropTargetDragEvent e) {
                if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    e.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    e.rejectDrag();
                }
            }

            public void dragOver(DropTargetDragEvent dtde) {
            }

            public void dropActionChanged(DropTargetDragEvent dtde) {
            }

            public void dragExit(DropTargetEvent dte) {
            }

            public void drop(DropTargetDropEvent e) {
                try {
                    Transferable tr = e.getTransferable();
                    if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        e.acceptDrop(DnDConstants.ACTION_COPY);
                        List list = (List)tr.getTransferData(DataFlavor.javaFileListFlavor);
                        File[] files = (File[])list.toArray(new File[list.size()]);
                        gcViewer.open(files);
                        e.dropComplete(true);
                    } else {
                        e.rejectDrop();
                    }
                } catch (IOException ioe) {
                    e.rejectDrop();
                    ioe.printStackTrace();
                } catch (UnsupportedFlavorException ufe) {
                    e.rejectDrop();
                    ufe.printStackTrace();
                }
            }
        }));
    }

    private ImageIcon logoIcon = new ImageIcon(GCViewerGui.class.getResource("gcviewer_background.png"));

    public void paint(Graphics g) {
        fillBackground(g);

        // draw logo
        g.drawImage(logoIcon.getImage(),
                getWidth()/2 - logoIcon.getIconWidth()/2,
                getHeight()/2 - logoIcon.getIconHeight()/2,
                logoIcon.getIconWidth(),
                logoIcon.getIconHeight(),
                logoIcon.getImageObserver());

        drawVersionString(g, logoIcon);

        super.paint(g);
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

    private void fillBackground(Graphics g) {
        Rectangle r = g.getClipBounds();
        g.setColor(Color.WHITE);
        if (r != null) {
            g.fillRect(r.x, r.y, r.width, r.height);
        }
        else {
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    public boolean isOpaque() {
        return false;
    }
}
