package com.tagtraum.perf.gcviewer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * Helper class dealing with opening an HttpURLConnection and it's error situations.
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * @author <a href="hans@amd64.comerwell.xs4all.nl">Hans Bausewein</a>
 */
public class HttpUrlConnectionHelper {

    public static final String GZIP = "gzip";

    private static final Logger LOGGER = Logger.getLogger(HttpUrlConnectionHelper.class.getName());
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String CHARSET_KEY = "charset=";

    private HttpUrlConnectionHelper() {
        super();
    }

    private static InputStream checkContentDecodingForInputStream(InputStream in, String contentEncoding) throws IOException {
        if (GZIP.equals(contentEncoding)) {
            in = new GZIPInputStream(in, 4096);
        }
        return in;
    }

    /**
     * Reads input stream into result and closes the stream.
     *
     * @param in                    content to read
     * @param contentEncoding       optional encoding of stream (gzip)
     * @param contentType           optional; if 'text/xx', may contain charset.
     * @return Content of InputStream or null if input is null
     */
    private static String readAndCloseErrorStream(InputStream in,
            String contentEncoding,
            String contentType) {

        String result = null;

        if (in != null) {
            try {
                in = checkContentDecodingForInputStream(in, contentEncoding);
            }
            catch(IOException e) {
                LOGGER.log(Level.FINE, contentEncoding + " read failed; try plain reading", e);;
            }
            // extract charset from content-type header
            // e.g. Content-Type: text/html; charset=ISO-8859-4
            Charset charSet = Charset.defaultCharset();
            int charSetIdx;
            if ((contentType != null) && ((charSetIdx = contentType.indexOf(CHARSET_KEY)) > 0)) {
                charSetIdx += CHARSET_KEY.length(); // skip
                int nextSemicolon = contentType.indexOf(';', charSetIdx);
                int endIndex = nextSemicolon < 0 ? contentType.length() : nextSemicolon;
                String charSetName = contentType.substring(charSetIdx, endIndex);
                try {
                    // if this fails, use the default character set.
                    charSet = Charset.forName(charSetName);
                }
                catch(RuntimeException re) {
                    LOGGER.fine("Failed to create CharSet from \"" + charSetName + "\":" + re.getMessage());
                }
            }

            StringBuilder sb = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, charSet))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append(System.lineSeparator());
                }
            }
            catch(IOException e) {
                LOGGER.log(Level.FINE, "readLine() from error page failed", e);
            }

            result = sb.toString();
        }
        return result;
    }

    /**
     *  Sets request properties, connects and opens the input stream depending on the HTTP response.
     *
     *  @param httpConn The HTTP connection
     *  @param acceptEncoding Content-encoding (gzip, deflate or null)
     *  @param contentLength length of content (output parameter)
     *  @return The input stream
     *  @throws IOException if problem occured.
     */
    public static InputStream openInputStream(HttpURLConnection httpConn, String acceptEncoding, AtomicLong contentLength)
            throws IOException {

        // set request properties
        httpConn.setRequestProperty(ACCEPT_ENCODING, acceptEncoding);
        httpConn.setUseCaches(false);
        httpConn.connect();
        // from here we're reading the server's response
        String contentEncoding = httpConn.getContentEncoding();
        String contentType = httpConn.getContentType();
        long contentLengthLong = httpConn.getContentLengthLong();
        long lastModified = httpConn.getLastModified();
        LOGGER.log(Level.INFO, "Reading " + (contentLengthLong < 0L
                 ? "?"
                 : Long.toString(contentLengthLong) ) + " bytes from " + httpConn.getURL() +
                   "; contentType = " + contentType +
                   "; contentEncoding = " + contentEncoding +
                   "; last modified = " + (lastModified <= 0L ? "-" : new Date(lastModified).toString()));

        final int responseCode = httpConn.getResponseCode();
        if (responseCode/100 == 2) {
        	if (contentLength != null) {
        		// abuse of AtomicLong, but I need a pointer to long (or FileInformation)
        		contentLength.set(contentLengthLong);
        	}
        } else {
            String responseMessage = httpConn.getResponseMessage();
            String msg = "Server sent " + responseCode + ": " + responseMessage;
            LOGGER.info(msg);
            // NOTE: Apache gzips the error page if client sets Accept-Encoding header
            String detailMsg = readAndCloseErrorStream(httpConn.getErrorStream(),
                    contentEncoding,
                    contentType);

            if (detailMsg != null) {
                LOGGER.fine(detailMsg);
            }
            throw new IOException(msg);
        }
        InputStream in = httpConn.getInputStream();
        in = checkContentDecodingForInputStream(in, contentEncoding);
        return in;
    }

    public static InputStream openInputStream(HttpURLConnection httpConn, String acceptEncoding) throws IOException {
    	return openInputStream(httpConn, acceptEncoding, null);
    }
}
