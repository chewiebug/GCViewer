package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.exp.DataWriterType;
import com.tagtraum.perf.gcviewer.exp.impl.DataWriterFactory;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.ChartPanelView;
import com.tagtraum.perf.gcviewer.view.GCDocument;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.util.ImageHelper;

/**
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 2:01:07 PM
 */
public class Export extends AbstractAction {
    private GCViewerGui gcViewer;
    private JFileChooser saveDialog;

    public Export(final GCViewerGui gcViewer) {
        this.gcViewer = gcViewer;
        putValue(NAME, LocalisationHelper.getString("main_frame_menuitem_export"));
        putValue(MNEMONIC_KEY, new Integer(LocalisationHelper.getString("main_frame_menuitem_mnemonic_export").charAt(0)));
        putValue(SHORT_DESCRIPTION, LocalisationHelper.getString("main_frame_menuitem_hint_export"));
        putValue(ACTION_COMMAND_KEY, ActionCommands.EXPORT.toString());
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('E', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        putValue(SMALL_ICON, ImageHelper.loadImageIcon("save.png"));
        setEnabled(false);

        saveDialog = new JFileChooser();
        saveDialog.setDialogTitle(LocalisationHelper.getString("fileexport_dialog_title"));
        saveDialog.removeChoosableFileFilter(saveDialog.getAcceptAllFileFilter());
        saveDialog.addChoosableFileFilter(new ExtensionFileFilter(".csv", LocalisationHelper.getString("fileexport_dialog_csv"), DataWriterType.CSV));
        saveDialog.addChoosableFileFilter(new ExtensionFileFilter(".csv", LocalisationHelper.getString("fileexport_dialog_csv_ts"), DataWriterType.CSV_TS));
        saveDialog.addChoosableFileFilter(new ExtensionFileFilter(".txt", LocalisationHelper.getString("fileexport_dialog_txt"), DataWriterType.PLAIN));
        saveDialog.addChoosableFileFilter(new ExtensionFileFilter(".simple.log", LocalisationHelper.getString("fileexport_dialog_simplelog"), DataWriterType.SIMPLE));
        saveDialog.addChoosableFileFilter(new ExtensionFileFilter(".csv", LocalisationHelper.getString("fileexport_dialog_summarylog"), DataWriterType.SUMMARY));
        saveDialog.addChoosableFileFilter(new ExtensionFileFilter(".png", LocalisationHelper.getString("fileexport_dialog_png"), DataWriterType.PNG));
        }

    public void actionPerformed(final ActionEvent e) {
        final GCDocument gcDocument = gcViewer.getSelectedGCDocument();
        for (int i=0; i<gcDocument.getChartPanelViewCount(); i++) {
            final ChartPanelView chartPanelView = gcDocument.getChartPanelView(i);
            final File file = new File(chartPanelView.getGCResource().getResourceName());
            saveDialog.setCurrentDirectory(file.getParentFile());
            saveDialog.setSelectedFile(file);
            final int val = saveDialog.showSaveDialog(gcViewer);
            if (val == JFileChooser.APPROVE_OPTION) {
                ExtensionFileFilter fileFilter = (ExtensionFileFilter) saveDialog.getFileFilter();
                // On OS/X if you don't select one of the filters and just press "Save" the filter may be null. Use the CSV one then
                if (fileFilter==null) {
                    fileFilter = (ExtensionFileFilter) saveDialog.getChoosableFileFilters()[0];
                }
                exportFile(chartPanelView.getGCResource().getModel(),
                        saveDialog.getSelectedFile(), 
                        fileFilter.getExtension(),
                        fileFilter.getDataWriterType());
            }
            else if (val == JFileChooser.ERROR_OPTION) {
                JOptionPane.showMessageDialog(gcViewer, LocalisationHelper.getString("fileexport_dialog_error_occured"), LocalisationHelper.getString("fileexport_dialog_write_file_failed"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void exportFile(final GCModel model, File file, final String extension, final DataWriterType dataWriterType) {
        if (file.toString().indexOf('.') == -1) {
            file = new File(file.toString() + extension);
        }
        if (!file.exists()
                || JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(gcViewer,
                        LocalisationHelper.getString("fileexport_dialog_confirm_overwrite"),
                        LocalisationHelper.getString("fileexport_dialog_title"),
                        JOptionPane.YES_NO_OPTION)) {

            try (DataWriter writer = DataWriterFactory.getDataWriter(file, dataWriterType)) {
                writer.write(model);
            }
            catch (Exception ioe) {
                //ioe.printStackTrace();
                JOptionPane.showMessageDialog(gcViewer, ioe.getLocalizedMessage(), LocalisationHelper.getString("fileexport_dialog_write_file_failed"), JOptionPane.ERROR_MESSAGE);
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
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(super.toString());
            sb.append("\ntype=").append(dataWriterType);
            sb.append("; extension=").append(extension);
            sb.append("; description=").append(description);
            
            return sb.toString();
        }
    }
}
