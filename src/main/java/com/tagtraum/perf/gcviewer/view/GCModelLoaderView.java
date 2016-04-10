package com.tagtraum.perf.gcviewer.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import com.tagtraum.perf.gcviewer.log.TextAreaLogHandler;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;

/**
 * Display the GCModel loading process for a GCDocument.
 */
public class GCModelLoaderView extends JPanel implements PropertyChangeListener, ActionListener {

    private static final long serialVersionUID = 1L;
    public static final String CMD_CANCEL = "cancel";

    private GCResource gcResource;

    private JLabel messageLabel;
    private JProgressBar progressBar;
    private JButton cancelButton;
    private TextAreaLogHandler textAreaLogHandler = new TextAreaLogHandler();

	/**
	 * @param gcResource resource to be tracked
	 */
	public GCModelLoaderView(GCResource gcResource) {
		super(new BorderLayout());

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel parserInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        progressBar = new JProgressBar(0, 100);
        progressBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        cancelButton = new JButton(new SquareIcon());
        cancelButton.setActionCommand(CMD_CANCEL);
        cancelButton.addActionListener(this);

        messageLabel = new JLabel();
        messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        messageLabel.setVisible(false);

        parserInfo.add(progressBar);
        parserInfo.add(cancelButton);
        parserInfo.add(messageLabel);

        add(parserInfo, BorderLayout.NORTH);

        JTextArea textArea = textAreaLogHandler.getTextArea();
        textArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JScrollPane textAreaScrollPane = new JScrollPane(textArea);
        textAreaScrollPane.setPreferredSize(new Dimension(700, 500));

        add(textAreaScrollPane, BorderLayout.CENTER);

        setGCResource(gcResource);
	}

	public void setGCResource(GCResource gcResource) {
	    textAreaLogHandler.reset();
        progressBar.setVisible(true);
        progressBar.setValue(0);
        cancelButton.setVisible(true);
        messageLabel.setVisible(false);
        gcResource.getLogger().addHandler(textAreaLogHandler);

        this.gcResource = gcResource;
	}

    /**
     * Invoked when task's progress property changes.
     */
	@Override
    public void propertyChange(PropertyChangeEvent evt) {
		final String eventPropertyName = evt.getPropertyName();

        if ("progress".equals(eventPropertyName)) {
            progressBar.setValue((int)evt.getNewValue());
        }
        else if ("state".equals(eventPropertyName)) {
            if (SwingWorker.StateValue.STARTED == evt.getNewValue()) {
                // don't clear textArea here, because event comes late!
            }
            else if (SwingWorker.StateValue.DONE == evt.getNewValue()) {
                progressBar.setValue(100);
                final int nErrors = textAreaLogHandler.getErrorCount();

                messageLabel.setText(LocalisationHelper.getString("datareader_parseerror_dialog_message", nErrors));
                messageLabel.setVisible(true);
                progressBar.setVisible(false);
                cancelButton.setVisible(false);
            }
        }
    }

	public TextAreaLogHandler getTextAreaLogHandler() {
		return textAreaLogHandler;
	}

    @Override
    public void actionPerformed(ActionEvent e) {
        if (CMD_CANCEL.equals(e.getActionCommand())) {
            this.gcResource.setIsReadCancelled(true);
        }
    }

    /**
     * @see <a href="http://www.java2s.com/Code/Java/Swing-JFC/ButtondemoMnemonicalignmentandactioncommand.htm">java2s - ButtonDemo</a>
     */
    private class SquareIcon implements Icon {
        private static final int SIZE = 10;

        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (c.isEnabled()) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.GRAY);
            }

            g.fillRect(x, y, SIZE, SIZE);
        }

        public int getIconWidth() {
            return SIZE;
        }

        public int getIconHeight() {
            return SIZE;
        }
    }
}
