package com.tagtraum.perf.gcviewer.ctrl.impl;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoaderController;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * DragAndDrop Listener implementation to support add / open of files using drag and drop.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 12.01.2014</p>
 */
public class FileDropTargetListener extends DropTargetAdapter {

    private GCModelLoaderController controller;
    private DropFlavor flavor;

    /**
     * The {@link FileDropTargetListener} can be used in two flavors:
     * <ul>
     * <li>OPEN a new log file and</li>
     * <li>ADD a log file to the current selected document.</li>
     * </ul> 
     * 
     * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
     * <p>created on: 19.01.2014</p>
     */
    public enum DropFlavor {
        OPEN, ADD
    }
    
    /**
     * Create new instance with controller as target for files and flavor if listener.
     * 
     * @param controller target for add / open files
     * @param flavor add or open
     */
    public FileDropTargetListener(GCModelLoaderController controller, DropFlavor flavor) {
        super();
        
        this.controller = controller;
        this.flavor = flavor;
    }
    
    @Override
    public void dragEnter(DropTargetDragEvent event) {
        if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            event.acceptDrag(DnDConstants.ACTION_COPY);
        }
        else {
            event.rejectDrag();
        }
    }

    @Override
    public void drop(DropTargetDropEvent event) {
        try {
            Transferable tr = event.getTransferable();
            if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                event.acceptDrop(DnDConstants.ACTION_COPY);
                @SuppressWarnings("unchecked")
                List<File> list = (List<File>)tr.getTransferData(DataFlavor.javaFileListFlavor);
                File[] fileArray = list.toArray(new File[list.size()]);
                if (flavor.equals(DropFlavor.ADD)) {
                    controller.add(fileArray);
                }
                else {
                    controller.open(fileArray);
                }
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

}
