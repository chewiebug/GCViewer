package com.tagtraum.perf.gcviewer.action;

import com.tagtraum.perf.gcviewer.GCViewer;
import com.tagtraum.perf.gcviewer.RecentURLsModel;
import com.tagtraum.perf.gcviewer.util.AutoCompletionComboBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.net.MalformedURLException;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 1:48:26 PM
 *
 */
public class OpenURL extends AbstractAction {
    private GCViewer gcViewer;
    private AutoCompletionComboBox autoCompletionComboBox;


    public OpenURL(final GCViewer gcViewer) {
        this.gcViewer = gcViewer;
        this.autoCompletionComboBox = new AutoCompletionComboBox();
        putValue(NAME, GCViewer.localStrings.getString("main_frame_menuitem_open_url"));
        putValue(SHORT_DESCRIPTION, GCViewer.localStrings.getString("main_frame_menuitem_hint_open_url"));
        putValue(MNEMONIC_KEY, new Integer(GCViewer.localStrings.getString("main_frame_menuitem_mnemonic_open_url").charAt(0)));
        putValue(ACTION_COMMAND_KEY, "open_url");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('U', Event.CTRL_MASK ));
        putValue(SMALL_ICON, new ImageIcon(Toolkit.getDefaultToolkit().getImage(gcViewer.getClass().getResource("images/open_url.png"))));
    }

    public void setRecentURLsModel(final RecentURLsModel recentURLsModel) {
        this.autoCompletionComboBox.setRecentURLsModel(recentURLsModel);
    }

    public void actionPerformed(final ActionEvent e) {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        panel.add(autoCompletionComboBox, gridBagConstraints);
        gridBagConstraints.gridy = 1;

        final JCheckBox addURLCheckBox = new JCheckBox(GCViewer.localStrings.getString("urlopen_dialog_add_checkbox"), false);
        final boolean aDocumentIsAlreadyOpen = gcViewer.getSelectedGCDocument() != null;
        addURLCheckBox.setVisible(aDocumentIsAlreadyOpen);
        addURLCheckBox.setEnabled(aDocumentIsAlreadyOpen);
        addURLCheckBox.setToolTipText(GCViewer.localStrings.getString("urlopen_dialog_hint_add_checkbox"));
        panel.add(addURLCheckBox, gridBagConstraints);
        final int result = JOptionPane.showConfirmDialog(gcViewer, panel, GCViewer.localStrings.getString("urlopen_dialog_title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (JOptionPane.OK_OPTION == result) {
            try {
                final URL[] urls = new URL[]{new URL((String)autoCompletionComboBox.getSelectedItem())};
                if (addURLCheckBox.isSelected()) {
                    gcViewer.add(urls);
                }
                else {
                    gcViewer.open(urls);
                }
            } catch (MalformedURLException e1) {
                // todo: show errormessage
                e1.printStackTrace();
            }
        }
    }

}
