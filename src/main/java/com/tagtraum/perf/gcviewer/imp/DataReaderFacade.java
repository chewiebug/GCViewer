package com.tagtraum.perf.gcviewer.imp;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import com.tagtraum.perf.gcviewer.imp.MonitoredBufferedInputStream.ProgressCallback;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.BuildInfoReader;
import com.tagtraum.perf.gcviewer.util.HttpUrlConnectionHelper;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import com.tagtraum.perf.gcviewer.util.LoggerHelper;

/**
 * DataReaderFacade is a helper class providing a simple interface to read a gc log file
 * including standard error handling. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 24.11.2012</p>
 */
public class DataReaderFacade {

    private List<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeListeners.add(listener);
    }
    
    /**
     * Loads a model from a given <code>pathToData</code> logging all exceptions that occur.
     * 
     * @param fileOrUrl path to a file or URL
     * @return instance of GCModel containing all information that was parsed
     * @throws DataReaderException if any exception occurred, it is logged and added as the cause
     * to this exception
     * @see {@link #loadModel(URL, ProgressCallback)}
     */
    public GCModel loadModel(final String fileOrUrl) throws DataReaderException {
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
            return loadModel(url, null);
        }
        catch (MalformedURLException e) {
            throw new DataReaderException("could not load from '" + fileOrUrl + "'", e);
        }
    }
    
    /**
     * Loads a model from a given <code>url</code> logging all exceptions that occur.
     * 
     * @param url where to look for the data to be interpreted
     * @param callback Call this to report progress
     * @return instance of GCModel containing all information that was parsed
     * @throws DataReaderException if any exception occurred, it is logged and added as the cause
     * to this exception
     */
    public GCModel loadModel(final URL url, final ProgressCallback callback) throws DataReaderException {
        DataReaderException dataReaderException = new DataReaderException();
        GCModel model = null;
        // TODO SWINGWORKER fix Logging -> info to user
        final Logger parserLogger = LoggerHelper.getThreadSpecificLogger(this);

        try {
        	final String msg = "GCViewer version " + BuildInfoReader.getVersion() + " (" + BuildInfoReader.getBuildDate() + ")"; 
            parserLogger.info(msg);
            model = readModel(url);
        } 
        catch (RuntimeException | IOException e) {
        	final String msg = LocalisationHelper.getString("fileopen_dialog_read_file_failed")
                    + "\n" + e.toString() + " " + e.getLocalizedMessage();
            dataReaderException.initCause(e);
            parserLogger.warning(msg);
        } 
        finally {
        }
        
        if (dataReaderException.getCause() != null) {
            throw dataReaderException;
        }
        model.setURL(url);
        return model;
    }
    
    protected GCModel loadModel(final URL url) throws DataReaderException {
    	return loadModel(url, null);
    }
    
    /**
     * Open and parse data designated by <code>url</code>.
     * @param url where to find data to be parsed
     * @return GCModel
     * @throws IOException problem reading the data
     */
    private GCModel readModel(final URL url) throws IOException {
        DataReaderFactory factory = new DataReaderFactory();
        long contentLength = 0L;
        InputStream in;
        if (url.getProtocol().startsWith("http")) {
        	final AtomicLong cl = new AtomicLong();
        	final URLConnection conn = url.openConnection();        	
        	in = HttpUrlConnectionHelper.openInputStream((HttpURLConnection)conn, HttpUrlConnectionHelper.GZIP, cl);
        	contentLength = cl.get();
        } 
        else {
        	in = url.openStream();
        	if (url.getProtocol().startsWith("file")) {
        		final File file = new File(url.getFile());
        		if (file.exists()) {
        			contentLength = file.length();
        		}
        	}
        }
        if (contentLength > 100L) {
        	in = new MonitoredBufferedInputStream(in, DataReaderFactory.FOUR_KB, contentLength);
        	for (PropertyChangeListener listener : propertyChangeListeners) {
                ((MonitoredBufferedInputStream)in).addPropertyChangeListener(listener);
        	}
        }
        final DataReader reader = factory.getDataReader(in);
        return reader.read();
    }

}
