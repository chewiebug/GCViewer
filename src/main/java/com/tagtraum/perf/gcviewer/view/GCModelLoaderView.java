package com.tagtraum.perf.gcviewer.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import com.tagtraum.perf.gcviewer.ctrl.GCModelLoader;
import com.tagtraum.perf.gcviewer.log.TextAreaLogHandler;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.util.LoggerHelper;

/**
 * Display the GCModel loading process for a GCDocument.
 */
public class GCModelLoaderView extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	
	private JLabel messageLabel;
    private JProgressBar progressBar;
    private TextAreaLogHandler textAreaLogHandler = new TextAreaLogHandler();

	/**
	 * @param modelLoaders
	 */
	public GCModelLoaderView() {
		super(new BorderLayout());
		
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel parserInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        progressBar = new JProgressBar(0, 100);
        progressBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        JLabel msgLabel = new JLabel();
        msgLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        msgLabel.setVisible(false);       
        this.messageLabel = msgLabel;

        parserInfo.add(msgLabel);
        parserInfo.add(progressBar);
        
        add(parserInfo, BorderLayout.NORTH);

        JTextArea textArea = textAreaLogHandler.getTextArea();
        textArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
        JScrollPane textAreaScrollPane = new JScrollPane(textArea);
        textAreaScrollPane.setPreferredSize(new Dimension(700, 500));
        
        add(textAreaScrollPane, BorderLayout.CENTER);
	}

    /**
     * Invoked when task's progress property changes.
     */
	@Override
    public void propertyChange(PropertyChangeEvent evt) {
		final String eventPropertyName = evt.getPropertyName();
		
		if ("loggername".equals(eventPropertyName)) {
            textAreaLogHandler.getTextArea().setText("");
            Logger.getLogger((String) evt.getNewValue()).addHandler(textAreaLogHandler);
		}
        if ("progress".equals(eventPropertyName)) {
            progressBar.setValue((int)evt.getNewValue());
        }
        else if ("state".equals(eventPropertyName)
                    && SwingWorker.StateValue.DONE == evt.getNewValue()) {
    		
            progressBar.setValue(100);
    		final int nErrors = textAreaLogHandler.getErrorCount();
    		
    		//LoggerHelper.getThreadSpecificLogger(this).info("Loading " + modelLoader.getGcResource().getResourceName() + " produced " + nErrors + " errors");
    		
    		if (nErrors > 0) {
    			//String title = modelLoader.getGcResource().getResourceName();		
    	        messageLabel.setText(LocalisationHelper.getString("datareader_parseerror_dialog_message", nErrors, "title"));
    			//modelLoader.getGcDocument().relayout();
    		}		
    		
    		((GCModelLoader)evt.getSource()).removePropertyChangeListener(this);
            LoggerHelper.getThreadSpecificLogger(null).removeHandler(textAreaLogHandler);
    	}
    }

	public TextAreaLogHandler getTextAreaLogHandler() {
		return textAreaLogHandler;
	}

}
