package com.tagtraum.perf.gcviewer.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import com.tagtraum.perf.gcviewer.view.util.ImageLoader;

/**
 * DesktopPane is the "background" of the application after opening.
 * <p/>
 * Date: Sep 27, 2005
 * Time: 9:41:33 AM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class DesktopPane extends JDesktopPane {

    private ImageIcon logoIcon = ImageLoader.loadImageIcon("gcviewer_background.png");
    
    public DesktopPane(final GCViewerGui gcViewer) {
        // TODO refactor; looks very similar to GCDocument implementation
        gcViewer.setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, new DropTargetListener(){
            public void dragEnter(DropTargetDragEvent e) {
                if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    e.acceptDrag(DnDConstants.ACTION_COPY);
                }
                else {
                    e.rejectDrag();
                }
            }

            public void dragOver(DropTargetDragEvent dtde) {
            }

            public void dropActionChanged(DropTargetDragEvent dtde) {
            }

            public void dragExit(DropTargetEvent dte) {
            }

            public void drop(DropTargetDropEvent event) {
                try {
                    Transferable tr = event.getTransferable();
                    if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        event.acceptDrop(DnDConstants.ACTION_COPY);
                        @SuppressWarnings("unchecked")
                        List<File> list = (List<File>)tr.getTransferData(DataFlavor.javaFileListFlavor);
                        gcViewer.open((File[]) list.toArray());
                        event.dropComplete(true);
                    } 
                    else {
                        event.rejectDrop();
                    }
                } 
                catch (IOException | UnsupportedFlavorException e) {
                    event.rejectDrop();
                    e.printStackTrace();
                }
            }
        }));
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
