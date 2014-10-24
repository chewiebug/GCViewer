package com.tagtraum.perf.gcviewer.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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
        final String filename = SAMPLE_GCLOG_SUN1_6_0 + "xx";
        final File file = new File(PARENT_PATH, filename);
        final String contentType = null;
        final String contentEncoding = HttpUrlConnectionHelper.GZIP;
        
        testOpenInputStreamNotOk(file, contentEncoding, contentType, null, filename);
    }
        
    @Test
    public void openInputStream404NoEncoding() throws Exception {
        final String filename = SAMPLE_GCLOG_SUN1_6_0 + "xx";
        final File file = new File(PARENT_PATH, filename);
        final String contentEncoding = null;
        final String contentType = null;
        
        testOpenInputStreamNotOk(file, contentEncoding, contentType, null, filename);
    }

    @Test
    public void openInputStreamGZipOk() throws Exception {
        final String filename = SAMPLE_GCLOG_SUN1_6_0_GZ;
        final File file = new File(PARENT_PATH, filename);
        final String contentEncoding = HttpUrlConnectionHelper.GZIP;
        
        testOpenInputStreamOk(file, contentEncoding, contentEncoding, SAMPLE_GCLOG_SUN1_6_0);
    }
    
    @Test
    public void openInputStreamOk() throws Exception {
        final String filename = SAMPLE_GCLOG_SUN1_6_0;
        final File file = new File(PARENT_PATH, filename);
        final String contentEncoding = null;
        
        testOpenInputStreamOk(file, contentEncoding, null, filename);
    }

    private void testOpenInputStreamNotOk(final File file, 
            final String contentEncoding, 
            final String contentType,
            final String acceptEncoding, 
            final String expectedFile) 
                    throws IOException {
        
        final URL url = new URL("http://localhost/gclog.txt");
        final HttpURLConnection conn = mock(HttpURLConnection.class);
        final long fileSize = file.length();
        final ByteArrayInputStream bis = create404InputStream();
        
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

    private void testOpenInputStreamOk(final File file, 
            final String contentEncoding, 
            final String acceptEncoding, 
            final String expectedFile) 
                    throws IOException {
        
        final URL url = new URL("http://localhost/gclog.txt");
        final HttpURLConnection conn = mock(HttpURLConnection.class);
        final long fileSize = file.length();
        
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

            final InputStream in = HttpUrlConnectionHelper.openInputStream(conn, acceptEncoding);

            verify(conn, atLeastOnce()).setRequestProperty("Accept-Encoding", acceptEncoding);
            verify(conn, atLeastOnce()).setUseCaches(false);
            verify(conn, atLeastOnce()).connect();

            if (acceptEncoding == null) {
                assertEquals("openInputStream(conn, null) returns non-encoded stream", fis, in);
            } 
            else {
                verifyInputStreamWithFile("openInputStream(conn," + acceptEncoding + ")", expectedFile, in);
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
    private void verifyInputStreamWithFile(final String msg, final String file, final InputStream in) throws IOException {
        final Path path = Paths.get(PARENT_PATH, file);     
        final ByteBuffer bFile = ByteBuffer.allocate(0x4000);
        final ByteBuffer bIn = ByteBuffer.allocate(0x4000);
        long checked = 0;
        
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
             ReadableByteChannel inChannel = Channels.newChannel(in)) {
            
            final long size = fileChannel.size();

            // check "size" bytes (invariant: all bytes < "checked" are equal)
            while (checked < size) {
                final int nRead1 = fileChannel.read(bFile);
                final int nRead2 = inChannel.read(bIn);
                assertEquals(msg + ": # bytes read at position " + checked, nRead1, nRead2);            
                bFile.flip();
                bIn.flip();
    
                if (!bFile.equals(bIn)) {
                    // find first failing byte
                    for (int index = 0; index < nRead1; index++) {
                        final byte b1 = bFile.get(index);
                        final byte b2 = bIn.get(index);
                        assertEquals(msg + ": Compare byte " + (checked + index), b1, b2);
                    }
                }
                checked += nRead1;
                bFile.compact();
                bIn.compact();
            }
            assertEquals(msg + ": Compared all bytes", size, checked);
            assertEquals(msg + ": InputStream must be at EOF", -1, inChannel.read(bIn));
        } 
    }
    
}
