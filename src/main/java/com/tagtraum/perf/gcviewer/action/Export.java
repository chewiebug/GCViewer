package com.tagtraum.perf.gcviewer.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import com.tagtraum.perf.gcviewer.ChartPanelView;
import com.tagtraum.perf.gcviewer.GCDocument;
import com.tagtraum.perf.gcviewer.GCViewerGui;
import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.exp.DataWriterType;
import com.tagtraum.perf.gcviewer.exp.impl.DataWriterFactory;
import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 2:01:07 PM
 *
 */
public class Export extends AbstractAction {
    private GCViewerGui gcViewer;
    private JFileChooser saveDialog;

    public Export(final GCViewerGui gcViewer) {
        this.gcViewer = gcViewer;
        putValue(NAME, GCViewerGui.localStrings.getString("main_frame_menuitem_export"));
        putValue(MNEMONIC_KEY, new Integer(GCViewerGui.localStrings.getString("main_frame_menuitem_mnemonic_export").charAt(0)));
        putValue(SHORT_DESCRIPTION, GCViewerGui.localStrings.getString("main_frame_menuitem_hint_export"));
        putValue(ACTION_COMMAND_KEY, "export");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('E', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        putValue(SMALL_ICON, new ImageIcon(Toolkit.getDefaultToolkit().getImage(gcViewer.getClass().getResource("images/save.png"))));
        setEnabled(false);
        
        saveDialog = new JFileChooser();
        saveDialog.setDialogTitle(GCViewerGui.localStrings.getString("fileexport_dialog_title"));
        saveDialog.removeChoosableFileFilter(saveDialog.getAcceptAllFileFilter());
        saveDialog.addChoosableFileFilter(new ExtensionFileFilter(".csv", GCViewerGui.localStrings.getString("fileexport_dialog_csv"), DataWriterType.CSV));
        saveDialog.addChoosableFileFilter(new ExtensionFileFilter(".txt", GCViewerGui.localStrings.getString("fileexport_dialog_txt"), DataWriterType.PLAIN));
        saveDialog.addChoosableFileFilter(new ExtensionFileFilter(".simple.log", GCViewerGui.localStrings.getString("fileexport_dialog_simplelog"), DataWriterType.SIMPLE));
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
                exportFile(chartPanelView.getModel(), saveDialog.getSelectedFile(), ((ExtensionFileFilter)saveDialog.getFileFilter()).getExtension(), ((ExtensionFileFilter)saveDialog.getFileFilter()).getDataWriterType());
            } else if (val == JFileChooser.ERROR_OPTION) {
                JOptionPane.showMessageDialog(gcViewer, GCViewerGui.localStrings.getString("fileexport_dialog_error_occured"), GCViewerGui.localStrings.getString("fileexport_dialog_write_file_failed"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void exportFile(final GCModel model, File file, final String extension, final DataWriterType dataWriterType) {
        DataWriter writer = null;
        try {
            if (file.toString().indexOf('.') == -1) file = new File(file.toString() + extension);
            if (!file.exists() || JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(gcViewer, GCViewerGui.localStrings.getString("fileexport_dialog_confirm_overwrite"), GCViewerGui.localStrings.getString("fileexport_dialog_title"), JOptionPane.YES_NO_OPTION)) {
                writer = DataWriterFactory.getDataWriter(file, dataWriterType);
                writer.write(model);
            }
        } catch (Exception ioe) {
            //ioe.printStackTrace();
            JOptionPane.showMessageDialog(gcViewer, ioe.getLocalizedMessage(), GCViewerGui.localStrings.getString("fileexport_dialog_write_file_failed"), JOptionPane.ERROR_MESSAGE);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static class ExtensionFileFilter extends FileFilter {
        private String extension;
        private String description;
        private DataWriterType dataWriterType;
        
        public ExtensionFileFilter(final String extension, final String description, final DataWriterType dataWriterType) {
            this.extension = extension.toLowerCase();
            this.description = description;
            this.dataWriterType = dataWriterType;
        }
        
        public boolean accept(final File file) {
        	// TODO refactor
        	try {
                return file.toString().toLowerCase().endsWith(extension);
        	}
        	catch (NullPointerException e) {
        		return false;
        	}
        }

        public String getExtension() {
            return extension;
        }

        public String getDescription() {
            return description;
        }
        
        public DataWriterType getDataWriterType() {
            return dataWriterType;
        }
    }
}
