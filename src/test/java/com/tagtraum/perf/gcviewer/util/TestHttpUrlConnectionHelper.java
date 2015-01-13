package com.tagtraum.perf.gcviewer.util;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.UnittestHelper;

public class TestHttpUrlConnectionHelper {

    private static final String SAMPLE_GCLOG_SUN1_6_0 = "SampleSun1_6_0PrintHeapAtGC.txt";
    private static final String SAMPLE_GCLOG_SUN1_6_0_GZ = SAMPLE_GCLOG_SUN1_6_0 + ".gz";  
    
    private static final String PARENT_PATH = "src/test/resources/" + UnittestHelper.FOLDER_OPENJDK + "/";
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private ByteArrayInputStream create404InputStream() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">");
        sb.append("\n<html><head>");
        sb.append("\n<title>404 Not Found</title>");
        sb.append("\n</head><body>");
        sb.append("\n<h1>Not Found</h1>");
        sb.append("\n<p>The requested URL /unknown.txt was not found on this server.</p>");
        sb.append("\n<hr>");
        sb.append("\n<address>Apache/2.2.22 (Debian) Server at 192.168.10.10 Port 80</address>");
        sb.append("\n</body></html>");

        return new ByteArrayInputStream(sb.toString().getBytes());
    }
    
    @Test
    public void openInputStream404GzipEncoding() throws Exception {
        String filename = SAMPLE_GCLOG_SUN1_6_0 + "xx";
        File file = new File(PARENT_PATH, filename);
        String contentType = null;
        String contentEncoding = HttpUrlConnectionHelper.GZIP;
        
        testOpenInputStreamNotOk(file, contentEncoding, contentType, null, filename);
    }
        
    @Test
    public void openInputStream404NoEncoding() throws Exception {
        String filename = SAMPLE_GCLOG_SUN1_6_0 + "xx";
        File file = new File(PARENT_PATH, filename);
        String contentEncoding = null;
        String contentType = null;
        
        testOpenInputStreamNotOk(file, contentEncoding, contentType, null, filename);
    }

    @Test
    public void openInputStreamGZipOk() throws Exception {
        String filename = SAMPLE_GCLOG_SUN1_6_0_GZ;
        File file = new File(PARENT_PATH, filename);
        String contentEncoding = HttpUrlConnectionHelper.GZIP;
        
        testOpenInputStreamOk(file, contentEncoding, contentEncoding, SAMPLE_GCLOG_SUN1_6_0);
    }
    
    @Test
    public void openInputStreamOk() throws Exception {
        String filename = SAMPLE_GCLOG_SUN1_6_0;
        File file = new File(PARENT_PATH, filename);
        String contentEncoding = null;
        
        testOpenInputStreamOk(file, contentEncoding, null, filename);
    }

    private void testOpenInputStreamNotOk(File file, 
            String contentEncoding, 
            String contentType,
            String acceptEncoding, 
            String expectedFile) 
                    throws IOException {
        
        URL url = new URL("http://localhost/gclog.txt");
        HttpURLConnection conn = mock(HttpURLConnection.class);
        long fileSize = file.length();
        ByteArrayInputStream bis = create404InputStream();
        
        when(conn.getContentEncoding()).thenReturn(contentEncoding);
        when(conn.getContentType()).thenReturn("text/html; charset=".concat(UTF8.name()));
        when(conn.getContentLength()).thenReturn((int)fileSize);
        when(conn.getContentLengthLong()).thenReturn(fileSize);
        when(conn.getLastModified()).thenReturn(file.lastModified());
        when(conn.getURL()).thenReturn(url);
        when(conn.getResponseCode()).thenReturn(404);
        when(conn.getResponseMessage()).thenReturn("Not found");
        when(conn.getErrorStream()).thenReturn(bis);

        try {
            HttpUrlConnectionHelper.openInputStream(conn, acceptEncoding);
            fail("IOException expected");
        }
        catch (IOException e) {
            verify(conn, atLeastOnce()).getResponseMessage();
            verify(conn, atLeastOnce()).getErrorStream();
        }

    }

    private void testOpenInputStreamOk(File file, 
            String contentEncoding, 
            String acceptEncoding, 
            String expectedFile) 
                    throws IOException {
        
        URL url = new URL("http://localhost/gclog.txt");
        HttpURLConnection conn = mock(HttpURLConnection.class);
        long fileSize = file.length();
        
        try (FileInputStream fis = new FileInputStream(file)) {
            when(conn.getContentEncoding()).thenReturn(contentEncoding);
            when(conn.getContentType()).thenReturn("text/plain");
            when(conn.getContentLength()).thenReturn((int)fileSize);
            when(conn.getContentLengthLong()).thenReturn(fileSize);
            when(conn.getLastModified()).thenReturn(file.lastModified());
            when(conn.getURL()).thenReturn(url);
            when(conn.getResponseCode()).thenReturn(200);
            when(conn.getResponseMessage()).thenReturn("OK");
            when(conn.getInputStream()).thenReturn(fis);

            InputStream in = HttpUrlConnectionHelper.openInputStream(conn, acceptEncoding);

            verify(conn, atLeastOnce()).setRequestProperty("Accept-Encoding", acceptEncoding);
            verify(conn, atLeastOnce()).setUseCaches(false);
            verify(conn, atLeastOnce()).connect();

            if (acceptEncoding == null) {
                assertEquals("openInputStream(conn, null) returns non-encoded stream", fis, in);
            } 
            else {
                assertInputStreamEqualToFile("openInputStream(conn," + acceptEncoding + ")", expectedFile, in);
            }
        }
    }
    
    /**
     * Compares the content of InputStream "in" with the specified file's content.
     *
     * @param msg   Prefix for all assert messages
     * @param file  The file to check against
     * @param in    The input stream to check (closed on exit)
     */
    private void assertInputStreamEqualToFile(String msg, String file, InputStream in) throws IOException {
        // this test must not be done on byte level, because line endings are platform dependent!
        Path path = Paths.get(PARENT_PATH, file);
        try (LineNumberReader expectedContentReader = new LineNumberReader(new FileReader(path.toFile()));
             LineNumberReader inputStreamReader = new LineNumberReader(new InputStreamReader(in))) {

            while (expectedContentReader.ready() && inputStreamReader.ready()) {
                String expectedLine = expectedContentReader.readLine();
                String line = inputStreamReader.readLine();
                assertThat(msg + " (line " + expectedContentReader.getLineNumber() + "): ", expectedLine, equalTo(line));
            }

            assertThat(msg + ": expectedContentReader must be at EOF", expectedContentReader.ready(), equalTo(false));
            assertThat(msg + ": inputStreamReader must be at EOF", inputStreamReader.ready(), equalTo(false));
        }
    }

}
