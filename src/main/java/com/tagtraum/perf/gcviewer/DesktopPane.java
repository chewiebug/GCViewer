package com.tagtraum.perf.gcviewer;

import javax.swing.*;
import java.awt.dnd.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.*;
import java.util.List;
import java.io.File;
import java.io.IOException;

/**
 * DesktopPane.
 * <p/>
 * Date: Sep 27, 2005
 * Time: 9:41:33 AM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class DesktopPane extends JDesktopPane {

    public DesktopPane(final GCViewer gcViewer) {
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

    private ImageIcon logoIcon = new ImageIcon(GCViewer.class.getResource("gcviewer_background.png"));
    
    public void paint(Graphics g) {
        Rectangle r = g.getClipBounds();
        g.setColor(Color.WHITE);
        if (r != null) {
            g.fillRect(r.x, r.y, r.width, r.height);
        }
        else {
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        g.drawImage(logoIcon.getImage(), getWidth()/2 - logoIcon.getIconWidth()/2, getHeight()/2 - logoIcon.getIconHeight()/2, logoIcon.getIconWidth(), logoIcon.getIconHeight(), logoIcon.getImageObserver());
        super.paint(g);
    }

    public boolean isOpaque() {
        return false;
    }
}
