package com.tagtraum.perf.gcviewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.tagtraum.perf.gcviewer.log.TextAreaLogHandler;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;

/**
 * Display the GCModel loading process for a GCDocument.
 */
public class GCModelLoaderView extends JPanel implements ActionListener,
														 ChangeListener,
														 PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	
	private final GCModelLoader modelLoader;
	private final JLabel messageLabel;
    private final JProgressBar progressBar;
    private final TextAreaLogHandler textAreaLogHandler = new TextAreaLogHandler();
    private boolean active;

	/**
	 * @param modelLoaders
	 */
	public GCModelLoaderView(GCModelLoader modelLoader) {
		super(new BorderLayout());
		this.modelLoader = modelLoader;
		
		final String title = modelLoader.getGcResource().getName();
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        final JTextArea textArea = textAreaLogHandler.getTextArea();
        textArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        final JScrollPane textAreaScrollPane = new JScrollPane(textArea);
        textAreaScrollPane.setPreferredSize(new Dimension(700, 500));
        add(textAreaScrollPane, BorderLayout.CENTER);

        final JPanel topInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        progressBar = new JProgressBar(0, 100);
        progressBar.setVisible(true);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        final JLabel msgLabel = new JLabel();
        msgLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        msgLabel.setVisible(false);       
        this.messageLabel = msgLabel;

        topInfo.add(msgLabel);
        topInfo.add(progressBar);
        
        add(topInfo, BorderLayout.NORTH);
        setVisible(false);   
	}

    /**
     * Invoked when task's progress property changes.
     */
	@Override
    public void propertyChange(PropertyChangeEvent evt) {
		final String eventPropertyName = evt.getPropertyName();
		
        if ("progress".equals(eventPropertyName)) {
            final int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
           
            if (progress >= 100) {
            	progressBar.setVisible(false);
            } else {
            	if (progress >= 0) {
            		// make GCModelLoaderView visible when first progress update is received
            		// i.e. when loading is not very fast
            		if (!active) {
            			active = true;
            			setVisible(true);
            			modelLoader.getGcDocument().relayout();
            		}
            	}
            }
        	invalidate();
        } else {
        	if ("state".equals(eventPropertyName)
                    && SwingWorker.StateValue.DONE == evt.getNewValue()) {
        		
        		final int nErrors = textAreaLogHandler.getErrorCount();
        		
        		modelLoader.getGcResource().getLogger().info("Loading " + modelLoader.getGcResource().getName() + " produced " + nErrors + " errors");
        		
        		if (nErrors > 0) {
        			final String title = modelLoader.getGcResource().getName();		
        	        final MessageFormat mf = new MessageFormat(LocalisationHelper.getString("datareader_parseerror_dialog_message"));        
        	        messageLabel.setText(mf.format(new Object[]{nErrors, title}));
        	        progressBar.setVisible(false);
        	        messageLabel.setVisible(true);
        			active = true;// signals that there's something to show
        			setVisible(true);
        			modelLoader.getGcDocument().relayout();
        		}		
        	}
        }
    }

	@Override
	public void stateChanged(ChangeEvent e) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public TextAreaLogHandler getTextAreaLogHandler() {
		return textAreaLogHandler;
	}

	public boolean isActive() {
		return active;
	}

}
