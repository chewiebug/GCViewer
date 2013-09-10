package com.tagtraum.perf.gcviewer.imp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.tagtraum.perf.gcviewer.log.TextAreaLogHandler;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.BuildInfoReader;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;

/**
 * DataReaderFacade is a helper class providing a simple interface to read a gc log file
 * including standard error handling. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 24.11.2012</p>
 */
public class DataReaderFacade {

    protected static final String ACCEPT_ENCODING = "Accept-Encoding";
    protected static final String GZIP = "gzip";
    private static final String CHARSET_KEY = "charset=";
    private static final Logger PARSER_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer");
    private static final Logger LOGGER = Logger.getLogger(DataReaderFacade.class.getName());

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
    public GCModel loadModel(URL url, boolean showErrorDialog, Component parent) throws DataReaderException {
        // set up special handler
        TextAreaLogHandler textAreaLogHandler = new TextAreaLogHandler();
        PARSER_LOGGER.addHandler(textAreaLogHandler);
        DataReaderException dataReaderException = new DataReaderException();
        GCModel model = null;
        try {
            LOGGER.info("GCViewer version " + BuildInfoReader.getVersion() + " (" + BuildInfoReader.getBuildDate() + ")");
            model = readModel(url);
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
    
    protected static InputStream contentDecodingInputStream(InputStream in, final String contentEncoding) throws IOException {
    	if (GZIP.equals(contentEncoding)) {
    		in = new GZIPInputStream(in, 4096);
    	}
    	return in;
    }
    
    /**
     * Reads input stream into result and closes the stream.
     * @param in 					content to read
     * @param contentEncoding		optional encoding of stream (gzip)
     * @param contentType			optional; if 'text/xx', may contain charset.
     * @return Content of InputStream or null if input is null
     */    
    protected static String readAndCloseStream(InputStream in, final String contentEncoding, final String contentType) {
    	String result = null;
    	
    	if (in != null) {
    		try {
            	in = contentDecodingInputStream(in, contentEncoding);
			} catch(IOException e) {
    			LOGGER.log(Level.FINE, contentEncoding + " read failed; try plain reading", e);;    					
			}
    		// extract charset from content-type header
    		// e.g. Content-Type: text/html; charset=ISO-8859-4
    		Charset charSet = Charset.defaultCharset();
    		int charSetIdx;
    		if ((contentType != null) && ((charSetIdx = contentType.indexOf(CHARSET_KEY)) > 0)) {
    			charSetIdx += CHARSET_KEY.length(); // skip 
    			final int nextSemicolon = contentType.indexOf(';', charSetIdx);
    			final int endIndex = nextSemicolon < 0 ? contentType.length() : nextSemicolon;    			    			
    			final String charSetName = contentType.substring(charSetIdx, endIndex);
    			try {
    				// if this fails, use the default character set.
    				charSet = Charset.forName(charSetName);    				
    			} catch(RuntimeException re) {
    				LOGGER.fine("Failed to create CharSet from \"" + charSetName + "\":" + re.getMessage());
    			}
    		}
    		
    		StringBuilder sb = new StringBuilder();
    		BufferedReader br = new BufferedReader(new InputStreamReader(in, charSet));
    		
    		try {
    			String line;
    			while ((line = br.readLine()) != null) {
    				sb.append(line).append(System.lineSeparator());
    			}
    		} catch(IOException e) {
    			LOGGER.log(Level.FINE, "readLine() from error page failed", e);;
    		} finally {
    			if (br != null) {
    				try {
    					br.close();
    				} catch(IOException e) {
    	    			LOGGER.log(Level.FINE, "close() of error page failed", e);;    					
    				}
    			}
    		}
    		result = sb.toString();
    	}
    	return result;    	
    }
    
    /**
     *  Sets request properties, connects and opens the input stream depending on the HTTP response.
     *  
     *  @param conn            The HTTP connection
     *  @param acceptEncoding  Content-encoding (gzip,defate or null)
     *  @return The input stream
     */
    protected InputStream openInputStream(final HttpURLConnection httpConn, final String acceptEncoding) throws IOException {
    	// set request properties
    	httpConn.setRequestProperty(ACCEPT_ENCODING, acceptEncoding);
    	httpConn.setUseCaches(false);
    	httpConn.connect();
    	// from here we're reading the server's response
    	final String contentEncoding = httpConn.getContentEncoding();
    	final String contentType = httpConn.getContentType();
    	final int contentLength = httpConn.getContentLength();
    	final long lastModified = httpConn.getLastModified();
    	LOGGER.log(Level.INFO, "Reading " + (contentLength < 0 ? "?" : Integer.toString(contentLength) ) + " bytes from " + httpConn.getURL() +
    			   "; contentType = " + contentType +
    			   "; contentEncoding = " + contentEncoding +
    			   "; last modified = " + (lastModified <= 0L ? "-" : new Date(lastModified).toString()));
    	
		final int responseCode = httpConn.getResponseCode();
		switch(responseCode/100) {
		case 2: // OK
			break;
		default:
			final String responseMessage = httpConn.getResponseMessage();
			String msg = "Server sent " + responseCode + ": " + responseMessage;
			LOGGER.info(msg);
			// NOTE: Apache gzips the error page if client sets Accept-Encoding header
			msg = readAndCloseStream(httpConn.getErrorStream(), contentEncoding, contentType);
			if (msg != null) {
				LOGGER.fine(msg);
			}
			throw new IOException(msg);
		}
		InputStream in = httpConn.getInputStream();
    	in = contentDecodingInputStream(in, contentEncoding);	
        return in;
    }

    /**
     * Open and parse data designated by <code>url</code>.
     * @param url where to find data to be parsed
     * @return GCModel
     * @throws IOException problem reading the data
     */
    private GCModel readModel(URL url) throws IOException {
        DataReaderFactory factory = new DataReaderFactory();
    	final URLConnection conn = url.openConnection();    	
    	final InputStream in = conn instanceof HttpURLConnection
    							? openInputStream((HttpURLConnection)conn, GZIP)
    							: conn.getInputStream();
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

}
