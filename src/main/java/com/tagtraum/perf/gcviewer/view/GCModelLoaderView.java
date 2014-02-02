package com.tagtraum.perf.gcviewer.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
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
public class GCModelLoaderView extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	
	private JLabel messageLabel;
    private JProgressBar progressBar;
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

        messageLabel = new JLabel();
        messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        messageLabel.setVisible(false);       

        parserInfo.add(progressBar);
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
        messageLabel.setVisible(false);
        gcResource.getLogger().addHandler(textAreaLogHandler);
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
            }
        }
    }

	public TextAreaLogHandler getTextAreaLogHandler() {
		return textAreaLogHandler;
	}

}
