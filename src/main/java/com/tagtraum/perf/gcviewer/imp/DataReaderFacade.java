package com.tagtraum.perf.gcviewer.imp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.tagtraum.perf.gcviewer.imp.MonitoredBufferedInputStream.ProgressCallback;
import com.tagtraum.perf.gcviewer.log.TextAreaLogHandler;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.BuildInfoReader;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.util.HttpUrlConnectionHelper;

/**
 * DataReaderFacade is a helper class providing a simple interface to read a gc log file
 * including standard error handling. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 24.11.2012</p>
 */
public class DataReaderFacade {

    private static final Logger PARSER_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer");
    private static final Logger LOGGER = Logger.getLogger(DataReaderFacade.class.getName());

    // Copy of Executors.DefaultThreadFactory
    public static final class NamedThreadFactory implements ThreadFactory {
    	private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        private NamedThreadFactory(final ThreadGroup threadGroup) {
            group = threadGroup;
            namePrefix = threadGroup.getName().concat("-thread-");
        }

        public Thread newThread(Runnable r) {
        	final Thread t = new Thread(group, r,
                                  		namePrefix + threadNumber.getAndIncrement(),
                                  		0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
    
    private static DataReaderFacade dataReaderFacade;
    
    final ThreadGroup threadGroup;
    final ThreadFactory threadFactory;

    private DataReaderFacade() {
    	threadGroup = new ThreadGroup("DataReader");
    	threadFactory = new NamedThreadFactory(threadGroup);
    }
    
    public static DataReaderFacade getInstance() {
    	if (dataReaderFacade == null) {
    		dataReaderFacade = new DataReaderFacade();
    	}
    	return dataReaderFacade;
    }
    
    /**
     * Loads a model from a given <code>pathToData</code> logging all exceptions that occur.
     * 
     * @param fileOrUrl path to a file or URL
     * @param showErrorDialog <code>true</code> if a window with an error description should be shown
     * if one occurred
     * @param parent parent for the error dialog
     * @return instance of GCModel containing all information that was parsed
     * @throws DataReaderException if any exception occurred, it is logged and added as the cause
     * to this exception
     * @see {@link #loadModel(URL, boolean, Component)}
     */
    public GCModel loadModel(String fileOrUrl, boolean showErrorDialog, Component parent) throws DataReaderException {
        if (fileOrUrl == null) {
            throw new NullPointerException("fileOrUrl must never be null");
        }
        
        try {
            URL url = null;
            if (fileOrUrl.startsWith("http")) {
                url = new URL(fileOrUrl);
            }
            else {
                url = new File(fileOrUrl).toURI().toURL();
            }
            
            return loadModel(url, showErrorDialog, parent);
        }
        catch (MalformedURLException e) {
            throw new DataReaderException("could not load from '" + fileOrUrl + "'", e);
        }
    }
    
    /**
     * Loads a model from a given <code>url</code> logging all exceptions that occur.
     * 
     * @param url where to look for the data to be interpreted
     * @param showErrorDialog <code>true</code> if a window with an error description should be shown
     * if one occurred
     * @param parent parent for the error dialog
     * @return instance of GCModel containing all information that was parsed
     * @throws DataReaderException if any exception occurred, it is logged and added as the cause
     * to this exception
     */
    public GCModel loadModel(URL url, boolean showErrorDialog, Component parent, final ProgressCallback callback) throws DataReaderException {
        // set up special handler
        TextAreaLogHandler textAreaLogHandler = new TextAreaLogHandler();
        PARSER_LOGGER.addHandler(textAreaLogHandler);
        DataReaderException dataReaderException = new DataReaderException();
        GCModel model = null;
        try {
            LOGGER.info("GCViewer version " + BuildInfoReader.getVersion() + " (" + BuildInfoReader.getBuildDate() + ")");
            model = readModel(url, callback);
            model.setURL(url);
        } 
        catch (RuntimeException | IOException e) {
            LOGGER.severe(LocalisationHelper.getString("fileopen_dialog_read_file_failed")
                    + "\n" + e.toString() + " " + e.getLocalizedMessage());
            dataReaderException.initCause(e);
        } 
        finally {
            // remove special handler after we are done with reading.
            PARSER_LOGGER.removeHandler(textAreaLogHandler);
        }

        if (textAreaLogHandler.hasErrors() && showErrorDialog) {
            showErrorDialog(url, textAreaLogHandler, parent);
        }
        
        if (dataReaderException.getCause() != null) {
            throw dataReaderException;
        }
        
        return model;
    }
    
    public GCModel loadModel(final URL url, final boolean showErrorDialog, final Component parent) throws DataReaderException {
    	return loadModel(url, showErrorDialog, parent, null);
    }
    
    /**
     * Open and parse data designated by <code>url</code>.
     * @param url where to find data to be parsed
     * @return GCModel
     * @throws IOException problem reading the data
     */
    private GCModel readModel(final URL url, final ProgressCallback callback) throws IOException {
        DataReaderFactory factory = new DataReaderFactory();
        long contentLength = 0L;
        InputStream in;
        if (url.getProtocol().startsWith("http")) {
        	final AtomicLong cl = new AtomicLong();
    	final URLConnection conn = url.openConnection();    	
        	in = HttpUrlConnectionHelper.openInputStream((HttpURLConnection)conn, HttpUrlConnectionHelper.GZIP, cl);
        	contentLength = cl.get();
        } else {
        	in = url.openStream();
        	if (url.getProtocol().startsWith("file")) {
        		final File file = new File(url.getFile());
        		if (file.exists()) {
        			contentLength = file.length();
        		}
        	}
        }
        
        in = new MonitoredBufferedInputStream(in, DataReaderFactory.FOUR_KB, contentLength, callback); 
        final DataReader reader = factory.getDataReader(in);
        final GCModel model = reader.read();
        return model;
    }

    /**
     * Show error dialog containing all information related to the error.
     * @param url url where data should have been read from
     * @param textAreaLogHandler handler where all logging information was gathered
     * @param parent parent component for the dialog
     */
    private void showErrorDialog(final URL url, TextAreaLogHandler textAreaLogHandler, final Component parent) {
        final JPanel panel = new JPanel(new BorderLayout());
        final JLabel messageLabel = new JLabel(new MessageFormat(LocalisationHelper.getString("datareader_parseerror_dialog_message")).format(new Object[]{textAreaLogHandler.getErrorCount(), url}));
        messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.add(messageLabel, BorderLayout.NORTH);
        final JScrollPane textAreaScrollPane = new JScrollPane(textAreaLogHandler.getTextArea());
        textAreaScrollPane.setPreferredSize(new Dimension(700, 500));
        panel.add(textAreaScrollPane, BorderLayout.CENTER);
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                JOptionPane.showMessageDialog(parent, panel, new MessageFormat(LocalisationHelper.getString("datareader_parseerror_dialog_title")).format(new Object[]{url}), JOptionPane.ERROR_MESSAGE);
            }
        });
    }

	public ThreadGroup getThreadGroup() {
		return threadGroup;
	}

	public ThreadFactory getThreadFactory() {
		return threadFactory;
	}

}
