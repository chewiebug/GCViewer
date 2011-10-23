package com.tagtraum.perf.gcviewer.action;

import com.tagtraum.perf.gcviewer.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 2:01:07 PM
 *
 */
public class Export extends AbstractAction {
    private GCViewer gcViewer;
    private JFileChooser saveDialog;
    private DataWriterFactory writerFactory;

    public Export(final GCViewer gcViewer) {
        this.gcViewer = gcViewer;
        writerFactory = new DataWriterFactory();
        putValue(NAME, GCViewer.localStrings.getString("main_frame_menuitem_export"));
        putValue(MNEMONIC_KEY, new Integer(GCViewer.localStrings.getString("main_frame_menuitem_mnemonic_export").charAt(0)));
        putValue(SHORT_DESCRIPTION, GCViewer.localStrings.getString("main_frame_menuitem_hint_export"));
        putValue(ACTION_COMMAND_KEY, "export");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('E', Event.CTRL_MASK ));
        putValue(SMALL_ICON, new ImageIcon(Toolkit.getDefaultToolkit().getImage(gcViewer.getClass().getResource("images/save.png"))));
        setEnabled(false);
        saveDialog = new JFileChooser();
        saveDialog.setDialogTitle(GCViewer.localStrings.getString("fileexport_dialog_title"));
        saveDialog.removeChoosableFileFilter(saveDialog.getAcceptAllFileFilter());
        saveDialog.addChoosableFileFilter(new ExtensionFileFilter(".csv", GCViewer.localStrings.getString("fileexport_dialog_csv")));
        saveDialog.addChoosableFileFilter(new ExtensionFileFilter(".txt", GCViewer.localStrings.getString("fileexport_dialog_txt")));
    }

    public void actionPerformed(final ActionEvent e) {
        final GCDocument gcDocument = gcViewer.getSelectedGCDocument();
        for (int i=0; i<gcDocument.getChartPanelViewCount(); i++) {
            final ChartPanelView chartPanelView = gcDocument.getChartPanelView(i);
            final File file = new File(chartPanelView.getModel().getURL().getFile());
            saveDialog.setCurrentDirectory(file.getParentFile());
            saveDialog.setSelectedFile(file);
            final int val = saveDialog.showSaveDialog(gcViewer);
            if (val == JFileChooser.APPROVE_OPTION) {
                exportFile(chartPanelView.getModel(), saveDialog.getSelectedFile(), ((ExtensionFileFilter)saveDialog.getFileFilter()).getExtension());
            } else if (val == JFileChooser.ERROR_OPTION) {
                JOptionPane.showMessageDialog(gcViewer, GCViewer.localStrings.getString("fileexport_dialog_error_occured"), GCViewer.localStrings.getString("fileexport_dialog_write_file_failed"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    public void exportFile(final GCModel model, File file, final String extension) {
        DataWriter writer = null;
        try {
            if (file.toString().indexOf('.') == -1) file = new File(file.toString() + extension);
            if (!file.exists() || JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(gcViewer, GCViewer.localStrings.getString("fileexport_dialog_confirm_overwrite"), GCViewer.localStrings.getString("fileexport_dialog_title"), JOptionPane.YES_NO_OPTION)) {
                writer = writerFactory.getDataWriter(file, extension);
                writer.write(model);
            }
        } catch (Exception ioe) {
            //ioe.printStackTrace();
            JOptionPane.showMessageDialog(gcViewer, ioe.getLocalizedMessage(), GCViewer.localStrings.getString("fileexport_dialog_write_file_failed"), JOptionPane.ERROR_MESSAGE);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static class ExtensionFileFilter extends javax.swing.filechooser.FileFilter {
        private String extension;
        private String description;
        public ExtensionFileFilter(final String extension, final String description) {
            this.extension = extension.toLowerCase();
            this.description = description;

        }
        public boolean accept(final File file) {
        	// TODO refactor
        	if (file != null) {
                return file.toString().toLowerCase().endsWith(extension);
        	}
        	else {
        		return false;
        	}
        }

        public String getExtension() {
            return extension;
        }

        public String getDescription() {
            return description;
        }
    }
}
