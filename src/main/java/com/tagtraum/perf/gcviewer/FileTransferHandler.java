package com.tagtraum.perf.gcviewer;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.List;
import java.io.*;
import java.net.MalformedURLException;

/**
 * FileTransferHandler.
 * <p/>
 * Date: Sep 26, 2005
 * Time: 6:07:35 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class FileTransferHandler extends TransferHandler {

    public boolean importData(final JComponent jComponent, Transferable transferable) {
        if (canImport(jComponent, transferable.getTransferDataFlavors())) {
            try {
                List list = (List)transferable.getTransferData(DataFlavor.javaFileListFlavor);
                for (int i=0; i<list.size(); i++) {
                    System.out.println(list.get(i));
                }
            } catch (UnsupportedFlavorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    public boolean canImport(JComponent jComponent, DataFlavor[] dataFlavors) {
        return isFileListType(dataFlavors);
    }


    /**
     * Checks whether the provided dataflavors are exclusively file lists.
     *
     * @param dataFlavors
     * @return
     */
    private static boolean isFileListType(DataFlavor[] dataFlavors) {
        for (int i=0; i<dataFlavors.length; i++) {
            DataFlavor dataFlavor = dataFlavors[i];
            if (!dataFlavor.isFlavorJavaFileListType()) return false;
        }
        return true;
    }

}
