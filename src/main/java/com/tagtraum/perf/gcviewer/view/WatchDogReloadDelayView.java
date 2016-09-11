package com.tagtraum.perf.gcviewer.view;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.InternationalFormatter;

import com.tagtraum.perf.gcviewer.util.LocalisationHelper;

/**
 *
 * @author <a href="mailto:web@olleb.com">Àngel Ollé Blázquez</a>
 *
 */
public class WatchDogReloadDelayView {
	private final JFrame parent;
	private final JPanel panel;
	private final JFormattedTextField reloadDelayField;

	public WatchDogReloadDelayView(JFrame parent, int delay) {
		this.parent = parent;
		panel = new JPanel(new GridBagLayout());

		reloadDelayField = new JFormattedTextField(delay);
		reloadDelayField.setFormatterFactory(new AbstractFormatterFactory() {
			@Override
			public AbstractFormatter getFormatter(JFormattedTextField tf) {
				NumberFormat format = NumberFormat.getInstance();
				InternationalFormatter formatter = new InternationalFormatter(format);
				formatter.setAllowsInvalid(false);
				formatter.setMinimum(1);
				formatter.setMaximum(Integer.MAX_VALUE);
				return formatter;
			}
		});

		reloadDelayField.setPreferredSize(new Dimension(50, 25));
		panel.add(reloadDelayField);

	}

	public int getReloadDelayValue() {
		return (int) reloadDelayField.getValue();
	}

	public boolean showDialog() {
		return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(parent, panel,
				LocalisationHelper.getString("watchreloaddelay_dialog_title"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
	}

}
