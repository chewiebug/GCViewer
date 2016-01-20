package com.tagtraum.perf.gcviewer.imp;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import com.tagtraum.perf.gcviewer.util.LocalisationHelper;

/**
 *
 * Date: Jan 30, 2002
 * Time: 6:47:45 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class DataReaderFactory {

    private static Logger LOG = Logger.getLogger(DataReaderFactory.class.getName());
    private static final int ONE_KB = 1024;
    private static final int FOUR_KB = ONE_KB * 4;
    private static final int MAX_ATTEMPT_COUNT = 100;
    
    /**
     * Returns the {@link DataReader} determined by content analysis. If no datareader can
     * be determined, an Exception is thrown.
     * 
     * @param inStream input stream to be read
     * @return DataReader appropriate datareader if it could be determined
     * @throws IOException if no appropriate datareader could be determined
     */
    public DataReader getDataReader(final InputStream inStream) throws IOException {
        InputStream in = new BufferedInputStream(inStream, FOUR_KB);
        // isGZipped relies on streams to support "mark" -> BufferdInputStream does
        if (isGZipped(in)) {
            LOG.info("GZip stream detected");
            in = new BufferedInputStream(new GZIPInputStream(in, FOUR_KB), FOUR_KB);
        }
        
        DataReader dataReader = null;
        long nextPos = 0;
        String chunkOfLastLine = null;
        int attemptCount = 0;
        String s = "";
        while (attemptCount < MAX_ATTEMPT_COUNT) {
            in.mark(FOUR_KB + (int) nextPos);
            if (nextPos > 0) {
                long skipped = in.skip(nextPos);
                if (skipped != nextPos) {
                    break;
                }
            }
            byte[] buf = new byte[ONE_KB * 3];
            int length = in.read(buf);
            in.reset();

            if (length <= 0) {
                break;
            }
            nextPos += length;

            s = new String(buf, 0, length, "ASCII");
            // prepend chunk of last block
            if (chunkOfLastLine != null && chunkOfLastLine.length() > 0) {
                s = chunkOfLastLine + s;
            }
            // deal with chunk of last line in the new block; cut it from end of block
            chunkOfLastLine = getChunkOfLastLine(s);
            if (chunkOfLastLine.length() > 0) {
                s = s.substring(0, s.lastIndexOf(chunkOfLastLine));
            }

            dataReader = getDataReaderBySample(s, in);
            if (dataReader != null) {
                break;
            }

            attemptCount++;
        }

        if (dataReader == null) {
            if (LOG.isLoggable(Level.SEVERE)) LOG.severe(LocalisationHelper.getString("datareaderfactory_instantiation_failed")
                    + "\ncontent:"
                    + "\n" + s);
            throw new IOException(LocalisationHelper.getString("datareaderfactory_instantiation_failed"));
        }
        return dataReader;
    }

    private String getChunkOfLastLine(String currentTextBlock) {
        String chunkOfLastLine;
        int index = currentTextBlock.lastIndexOf('\n');
        if (index >= 0) {
            chunkOfLastLine = currentTextBlock.substring(index + 1, currentTextBlock.length());
        }
        else {
            chunkOfLastLine = "";
        }
        return chunkOfLastLine;
    }

    private DataReader getDataReaderBySample(String s, InputStream in) throws IOException {
        // if there is a [memory ] somewhere in the first chunk of the logs, it is JRockit
        if (s.indexOf("[memory ]") != -1) {
            int startOfRealLog = s.lastIndexOf("<");
            // skip ahead of <start>-<end>: <type> <before>KB-><after>KB (<heap>KB
            String realLog;
            if (startOfRealLog >= 0){
                realLog = s.substring(startOfRealLog); 
                // skip all start report info to real log to determine JRockit version
            } else {
                realLog = s;
            }            
            if (realLog.indexOf("->") == -1) {
                return null; // No GC logs of format 1641728K->148365K (3145728K) yet, read next chunk
            }
            // JRockit 1.5 and 1.6 logs look like: [memory ][Tue Nov 13 08:39:01 2012][01684] [OC#1]
            if ((realLog.indexOf("[YC#") != -1) ||(realLog.indexOf("[OC#") != -1)) {
                if (LOG.isLoggable(Level.INFO)) LOG.info("File format: JRockit 1.6");
                return new DataReaderJRockit1_6_0(in);
            } else if ((realLog.indexOf("\n[memory") == -1) && (realLog.indexOf("[INFO ][memory") == -1)) {
                // Only JRockit 1.4 can have GC logs with verbose timestamp precedig "[memory ]"
                //[Wed Nov 16 15:19:38 2005][29147][memory ] 30.485-30.596: GC 23386K->8321K (32768K), 29.223 ms
                if (LOG.isLoggable(Level.INFO)) LOG.info("File format: JRockit 1.4.2");
                return new DataReaderJRockit1_4_2(in);
            } else {
                // may include some non-verbose JRockit 1.4 logs but should parse correctly as 1.5
                if (LOG.isLoggable(Level.INFO)) LOG.info("File format: JRockit 1.5");
                return new DataReaderJRockit1_5_0(in);
            }
        } else if (s.indexOf("since last AF or CON>") != -1) {
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: IBM 1.4.2");
            return new DataReaderIBM1_4_2(in);
        } else if (s.indexOf("GC cycle started") != -1) {
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: IBM 1.3.1");
            return new DataReaderIBM1_3_1(in);
        } else if (s.indexOf("<AF") != -1) {
            // this should be an IBM JDK < 1.3.0
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: IBM <1.3.0");
            return new DataReaderIBM1_3_0(in);
        } else if (s.indexOf(" (young)") > 0 || s.indexOf("G1Ergonomics") > 0) {
            // G1 logger usually starts with "<timestamp>: [GC pause (young)...]"
            // but can start with  <timestamp>: [G1Ergonomics (Heap Sizing) expand the heap...
            // with certain logging flaggs.
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: Sun 1.6.x G1 collector");
            return new DataReaderSun1_6_0G1(in, GcLogType.SUN1_6G1);
        } else if (s.indexOf("[Times:") > 0) {
            // all 1.6 lines end with a block like this "[Times: user=1.13 sys=0.08, real=0.95 secs]"
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: Sun 1.6.x");
            return new DataReaderSun1_6_0(in, GcLogType.SUN1_6);
        } else if (s.indexOf("CMS-initial-mark") != -1 || s.indexOf("PSYoungGen") != -1) {
            // format is 1.5, but datareader for 1_6_0 can handle it
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: Sun 1.5.x");
            return new DataReaderSun1_6_0(in, GcLogType.SUN1_5);
        } else if (s.indexOf(": [GC") != -1) {
            // format is 1.4, but datareader for 1_6_0 can handle it
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: Sun 1.4.x");
            return new DataReaderSun1_6_0(in, GcLogType.SUN1_4);
        } else if (s.indexOf("[GC") != -1 || s.indexOf("[Full GC") != -1 || s.indexOf("[Inc GC")!=-1) {
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: Sun 1.3.1");
            return new DataReaderSun1_3_1(in, GcLogType.SUN1_3_1);
        } else if (s.indexOf("<GC: managing allocation failure: need ") != -1) {
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: Sun 1.2.2");
            return new DataReaderSun1_2_2(in);
        } else if (s.indexOf("<GC: ") == 0 && s.indexOf('>') != -1 && new StringTokenizer(s.substring(0, s.indexOf('>')+1), " ").countTokens() == 20) {
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: HP-UX 1.2/1.3/1.4.0");
            return new DataReaderHPUX1_2(in);
        } else if (s.indexOf("<GC: ") == 0 && s.indexOf('>') != -1 && new StringTokenizer(s.substring(0, s.indexOf('>')+1), " ").countTokens() == 22) {
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: HP-UX 1.4.1/1.4.2");
            return new DataReaderHPUX1_4_1(in);
        } else if (s.contains("<verbosegc") && (s.contains("version=\"R26_Java6") || s.contains("version=\"R27_Java7") || s.contains("version=\"R28_Java8"))) {
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: IBM J9 R26 / R27 / R28");
            return new DataReaderIBM_J9_R28(in);
        } else if (s.indexOf("<verbosegc version=\"") != -1) {
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: IBM J9 5.0");
            return new DataReaderIBM_J9_5_0(in);
        } else if (s.indexOf("starting collection, threshold allocation reached.") != -1) {
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: IBM i5/OS 1.4.2");
            return new DataReaderIBMi5OS1_4_2(in);
        }
        return null;
    }

    /**
     * Checks whether the given input stream is in GZIP format.
     * 
     * @param in The input stream
     * @return <code>true</code> if the first two bytes are equal to the GZIP_MAGIC number
     * @throws IOException 
     */
    private boolean isGZipped(final InputStream in) throws IOException {
        int firstBytes = 0;
        if (in.markSupported()) {
            // Reads unsigned short in Intel byte order and resets the stream to the start position.
            in.mark(2);
            final int b1 = in.read();
            final int b2 = in.read();
            if (b2 < 0) {
                throw new EOFException();
            }
            firstBytes = (b2 << 8) | b1;
            in.reset();
        }
        else {
            LOG.warning("mark() not supported for current stream!");
        }
        
        return firstBytes == GZIPInputStream.GZIP_MAGIC;
    }

}
