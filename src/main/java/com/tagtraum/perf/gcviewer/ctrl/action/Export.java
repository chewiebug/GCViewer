package com.tagtraum.perf.gcviewer.ctrl.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Map;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.exp.DataWriterType;
import com.tagtraum.perf.gcviewer.exp.impl.DataWriterFactory;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.view.ActionCommands;
import com.tagtraum.perf.gcviewer.view.ChartPanelView;
import com.tagtraum.perf.gcviewer.view.GCDocument;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.util.ExtensionFileFilter;
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
        for (ExportExtensionFileFilter filter : ExportExtensionFileFilter.EXT_FILE_FILTERS) {
            saveDialog.addChoosableFileFilter(filter);
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final GCDocument gcDocument = gcViewer.getSelectedGCDocument();
        for (int i=0; i<gcDocument.getChartPanelViewCount(); i++) {
            final ChartPanelView chartPanelView = gcDocument.getChartPanelView(i);
            final File file = new File(chartPanelView.getGCResource().getResourceName());
            saveDialog.setCurrentDirectory(file.getParentFile());
            saveDialog.setSelectedFile(file);
            final int val = saveDialog.showSaveDialog(gcViewer);
            if (val == JFileChooser.APPROVE_OPTION) {
                ExportExtensionFileFilter fileFilter = (ExportExtensionFileFilter) saveDialog.getFileFilter();
                // On OS/X if you don't select one of the filters and just press "Save" the filter may be null. Use the CSV one then
                if (fileFilter==null) {
                    fileFilter = (ExportExtensionFileFilter) saveDialog.getChoosableFileFilters()[0];
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

            Map<String, Object> configuration = new HashMap<>();
            configuration.put(DataWriterFactory.GC_PREFERENCES, gcViewer.getSelectedGCDocument().getPreferences());
            try (DataWriter writer = DataWriterFactory.getDataWriter(file, dataWriterType, configuration)) {
                writer.write(model);
            }
            catch (Exception ioe) {
                //ioe.printStackTrace();
                JOptionPane.showMessageDialog(gcViewer, ioe.getLocalizedMessage(), LocalisationHelper.getString("fileexport_dialog_write_file_failed"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class ExportExtensionFileFilter extends ExtensionFileFilter {

        public static final ExportExtensionFileFilter[] EXT_FILE_FILTERS = {
            new ExportExtensionFileFilter("csv", LocalisationHelper.getString("fileexport_dialog_csv"), DataWriterType.CSV),
            new ExportExtensionFileFilter("csv", LocalisationHelper.getString("fileexport_dialog_csv_ts"), DataWriterType.CSV_TS),
            new ExportExtensionFileFilter("txt", LocalisationHelper.getString("fileexport_dialog_txt"), DataWriterType.PLAIN),
            new ExportExtensionFileFilter("simple.log", LocalisationHelper.getString("fileexport_dialog_simplelog"), DataWriterType.SIMPLE),
            new ExportExtensionFileFilter("csv", LocalisationHelper.getString("fileexport_dialog_summarylog"), DataWriterType.SUMMARY),
            new ExportExtensionFileFilter("png", LocalisationHelper.getString("fileexport_dialog_png"), DataWriterType.PNG)        
        };

        private final String description;
        private final DataWriterType dataWriterType;

        public ExportExtensionFileFilter(final String extension, final String description, final DataWriterType dataWriterType) {
            super(extension.toLowerCase());
            this.description = description;
            this.dataWriterType = dataWriterType;
        }

        public String getExtension() {
            return super.extension;
        }

        @Override
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
