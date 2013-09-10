package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the implementation of {@link DataReaderFacade}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 28.11.2012</p>
 */
public class TestDataReaderFacade {

    private static final String SAMPLE_GCLOG_SUN1_6_0 = "SampleSun1_6_0PrintHeapAtGC.txt";
    private static final String SAMPLE_GCLOG_SUN1_6_0_GZ = SAMPLE_GCLOG_SUN1_6_0 + ".gz";  
    
    private static final Logger IMP_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.imp");
    private static final String PARENT_PATH = "src/test/resources/" + UnittestHelper.FOLDER_OPENJDK + "/";
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private DataReaderFacade dataReaderFacade;
    
    @Before
    public void setUp() throws Exception {
        dataReaderFacade = new DataReaderFacade();
    }
    
    private static String quotedString(String s) {
    	if (s == null) {
    		return null;
    	}
    	StringBuilder sb = new StringBuilder("\"");
    	s = s.replace("\\", "\\\\");
    	s = s.replace("\"", "\\\"");
    	sb.append(s).append("\"");
    	return sb.toString();
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
    	final FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
    	final long size = fileChannel.size();
    	final ReadableByteChannel inChannel = Channels.newChannel(in);
    	//final ByteBuffer bFilem = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, 0x4000);
    	final ByteBuffer bFile = ByteBuffer.allocate(0x4000);
    	final ByteBuffer bIn = ByteBuffer.allocate(0x4000);
    	long checked = 0;
    	
    	try {
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
    	} finally {
    		try { inChannel.close(); } catch(IOException e) { IMP_LOGGER.warning("close(in) failed: " + e.getMessage());}
    		try { fileChannel.close(); } catch(IOException e) { IMP_LOGGER.warning("close(" + file + ") failed: " + e.getMessage());}
    	}
    }
        
    /**
     * Tests {@link DataReaderFacade#contentDecodingInputStream(InputStream, String)}
     * with no contentEncoding.
     */
    @Test
    public void contentDecodingInputStreamNull() throws Exception {
    	File file = new File(PARENT_PATH, SAMPLE_GCLOG_SUN1_6_0);
    	FileInputStream fis = new FileInputStream(file);
    	try {
	    	final InputStream content = DataReaderFacade.contentDecodingInputStream(fis, null);
	    	assertSame("contentDecodingInputStream() returns input", fis, content);
    	} finally {
    		fis.close();
    	}
    }

    private void doTestContentDecodingInputStream(final File file, final String contentEncoding, final Class<?> type) throws IOException {
    	FileInputStream fis = new FileInputStream(file);
    	try {
	    	final InputStream content = DataReaderFacade.contentDecodingInputStream(fis, contentEncoding);
	    	assertNotNull("contentDecodingInputStream() returns InputStream", content);
	    	assertEquals("contentDecodingInputStream() result", type, content.getClass());
	    	verifyInputStreamWithFile("contentDecodingInputStream(in, \"" + contentEncoding + "\")", SAMPLE_GCLOG_SUN1_6_0, content);
	    	content.close();
    	} finally {
    		fis.close();
    	}    	
    }
    
    /**
     * Tests {@link DataReaderFacade#contentDecodingInputStream(InputStream, String)}
     * with gzip contentEncoding.
     */
    @Test
    public void contentDecodingInputStreamGz() throws Exception {
    	final File file = new File(PARENT_PATH, SAMPLE_GCLOG_SUN1_6_0_GZ);
    	doTestContentDecodingInputStream(file, DataReaderFacade.GZIP, GZIPInputStream.class);
    }
   
    public void doReadAndCloseStream(final String chkResource, final String resource, final String contentEncoding, final String contentType) throws Exception {
    	final Path chkPath = Paths.get(UnittestHelper.getResource(UnittestHelper.FOLDER_HTTP, chkResource).toURI());
    	final byte[] chkBytes = Files.readAllBytes(chkPath);
    	final String expected = new String(chkBytes, UTF8);
    	
    	final InputStream in = UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_HTTP, resource);
    	final String actual = DataReaderFacade.readAndCloseStream(in, contentEncoding, contentType);
    	
    	assertEquals("readAndCloseStream(in, " + quotedString(contentEncoding) + ", " + quotedString(contentType) + ") returns correct 404 message", expected, actual);
    }
    
    @Test
    public void readAndCloseStreamNoEncoding() throws Exception {
    	doReadAndCloseStream("404.txt", "404.txt", null, null);
    }
    
    @Test
    public void readAndCloseStreamGZipEncoding() throws Exception {
    	doReadAndCloseStream("404.txt", "404.txt.gz", DataReaderFacade.GZIP, null);
    }
    
    @Test
    public void readAndCloseStreamGZipEncodingCharSetUTF8() throws Exception {
    	doReadAndCloseStream("404.txt", "404.txt.gz", DataReaderFacade.GZIP, "text/html; charset=".concat(UTF8.name()));
    }
    
	private void testOpenInputStream(final File file, final String contentEncoding, final String acceptEncoding, final String expectedFile) throws IOException {
    	final FileInputStream fis = new FileInputStream(file);
    	final URL url = new URL("http://localhost/gclog.txt");
    	final HttpURLConnection conn = mock(HttpURLConnection.class);
    	final long fileSize = file.length();
    	
    	try {
    		when(conn.getContentEncoding()).thenReturn(contentEncoding);
    		when(conn.getContentType()).thenReturn("text/plain");
    		when(conn.getContentLength()).thenReturn((int)fileSize);
    		when(conn.getContentLengthLong()).thenReturn(fileSize);
    		when(conn.getLastModified()).thenReturn(file.lastModified());
    		when(conn.getContentType()).thenReturn("text/plain");
	    	when(conn.getURL()).thenReturn(url);
	    	when(conn.getResponseCode()).thenReturn(200);
	    	when(conn.getResponseMessage()).thenReturn("OK");
	    	when(conn.getInputStream()).thenReturn(fis);

	    	final InputStream in = dataReaderFacade.openInputStream(conn, acceptEncoding);

	    	verify(conn, atLeastOnce()).setRequestProperty(DataReaderFacade.ACCEPT_ENCODING, acceptEncoding);
    		verify(conn, atLeastOnce()).setUseCaches(false);
    		verify(conn, atLeastOnce()).connect();

	    	if (acceptEncoding == null) {
	    		assertEquals("openInputStream(conn, null) returns non-encoded stream", fis, in);
	    	} else {
	    		verifyInputStreamWithFile("openInputStream(conn," + acceptEncoding + ")", expectedFile, in);
	    	}
    	} finally {
    		fis.close();
    	}
	}
    
    @Test
    public void openInputStream() throws Exception {
    	final String filename = SAMPLE_GCLOG_SUN1_6_0;
		final File file = new File(PARENT_PATH, filename);
    	final String contentEncoding = null;
    	testOpenInputStream(file, contentEncoding, null, filename);
    }

    @Test
    public void openInputStreamGZip() throws Exception {
    	final String filename = SAMPLE_GCLOG_SUN1_6_0_GZ;
		final File file = new File(PARENT_PATH, filename);
    	final String contentEncoding = DataReaderFacade.GZIP;
    	testOpenInputStream(file, contentEncoding, contentEncoding, SAMPLE_GCLOG_SUN1_6_0);
    }

    /**
     * Tests {@link DataReaderFacade#loadModel(String, boolean, java.awt.Component)}
     * with filename that does exist.
     */
    @Test
    public void loadModelStringFileExistsNoWarnings() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);

        dataReaderFacade.loadModel(PARENT_PATH + SAMPLE_GCLOG_SUN1_6_0, false, null);
        
        assertEquals("has no errors", 0, handler.getCount());
    }

    /**
     * Tests {@link DataReaderFacade#loadModel(String, boolean, java.awt.Component)}
     * with a malformed url.
     */
    @Test
    public void loadModelStringMalformedUrl() throws Exception {

        try {
            dataReaderFacade.loadModel("httpblabla", false, null);
        }
        catch (DataReaderException e) {
            assertNotNull("cause", e.getCause());
            assertEquals("expected exception in cause",
                    MalformedURLException.class.getName(),
                    e.getCause().getClass().getName());
        }
    }

    /**
     * Tests {@link DataReaderFacade#loadModel(String, boolean, java.awt.Component)}
     * with a malformed url.
     */
    @Test
    public void loadModelStringIllegalArgument() throws Exception {

        try {
            dataReaderFacade.loadModel("http://", false, null);
        }
        catch (DataReaderException e) {
            assertNotNull("cause", e.getCause());
            assertEquals("expected exception in cause",
                    IllegalArgumentException.class.getName(),
                    e.getCause().getClass().getName());
        }
    }

    /**
     * Tests {@link DataReaderFacade#loadModel(java.net.URL, boolean, java.awt.Component)}
     * with filename that does not exist.
     */
    @Test
    public void loadModelUrlFileDoesntExists() throws Exception {
        try {
            dataReaderFacade.loadModel(new File("dummy.txt").toURI().toURL(), false, null);
            fail("DataReaderException expected");
        }
        catch (DataReaderException e) {
            assertNotNull("cause", e.getCause());
            assertEquals("expected exception in cause", 
                    FileNotFoundException.class.getName(), 
                    e.getCause().getClass().getName());
        }
    }

    /**
     * Tests {@link DataReaderFacade#loadModel(java.net.URL, boolean, java.awt.Component)}
     * with filename that does exist.
     */
    @Test
    public void loadModelUrlFileExistsNoWarnings() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);

        dataReaderFacade.loadModel(new File(PARENT_PATH, SAMPLE_GCLOG_SUN1_6_0).toURI().toURL(), false, null);
        
        assertEquals("has no errors", 0, handler.getCount());
    }

}
