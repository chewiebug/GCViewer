package com.tagtraum.perf.gcviewer.imp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Date: Jan 30, 2002
 * Time: 6:47:45 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class DataReaderFactory {

    private static ResourceBundle localStrings = ResourceBundle.getBundle("com.tagtraum.perf.gcviewer.localStrings");
    private static Logger LOG = Logger.getLogger(DataReaderFactory.class.getName());
    private static final int ONE_KB = 1024;
    private static final int FOUR_KB = ONE_KB * 4;

    public DataReader getDataReader(InputStream inStream) throws IOException {
        BufferedInputStream in = new BufferedInputStream(inStream, FOUR_KB);
        in.mark(FOUR_KB);
        byte[] buf = new byte[ONE_KB * 3];
        int length = in.read(buf);
        in.reset();
        String s = new String(buf, 0, length, "ASCII");
        if (s.indexOf("[memory ] ") != -1) {
            if ((s.indexOf("[memory ] [YC") != -1) ||(s.indexOf("[memory ] [OC") != -1)) {
                if (LOG.isLoggable(Level.INFO)) LOG.info("File format: JRockit 1.6");
                return new DataReaderJRockit1_6_0(in);
            } else {
                if (LOG.isLoggable(Level.INFO)) LOG.info("File format: JRockit 1.4.2");
                return new DataReaderJRockit1_4_2(in);
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
        } else if (s.indexOf("pause (young)") > 0) {
        	// G1 logger usually starts with "<timestamp>: [GC pause (young)...]"
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: Sun 1.6.x G1 collector");
            return new DataReaderSun1_6_0G1(in);
        } else if (s.indexOf("[Times:") > 0) {
        	// all 1.6 lines end with a block like this "[Times: user=1.13 sys=0.08, real=0.95 secs]"
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: Sun 1.6.x");
            return new DataReaderSun1_6_0(in);
        } else if (s.indexOf("CMS-initial-mark") != -1 || s.indexOf("PSYoungGen") != -1) {
            // format is 1.5, but datareader for 1_6_0 can handle it
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: Sun 1.5.x");
            return new DataReaderSun1_6_0(in);
        } else if (s.indexOf(": [") != -1) {
            // format is 1.4, but datareader for 1_6_0 can handle it
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: Sun 1.4.x");
            return new DataReaderSun1_6_0(in);
        } else if (s.indexOf("[GC") != -1 || s.indexOf("[Full GC") != -1 || s.indexOf("[Inc GC")!=-1) {
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: Sun 1.3.1");
            return new DataReaderSun1_3_1(in);
        } else if (s.indexOf("<GC: managing allocation failure: need ") != -1) {
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: Sun 1.2.2");
            return new DataReaderSun1_2_2(in);
        } else if (s.indexOf("<GC: ") == 0 && s.indexOf('>') != -1 && new StringTokenizer(s.substring(0, s.indexOf('>')+1), " ").countTokens() == 20) {
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: HP-UX 1.2/1.3/1.4.0");
            return new DataReaderHPUX1_2(in);
        } else if (s.indexOf("<GC: ") == 0 && s.indexOf('>') != -1 && new StringTokenizer(s.substring(0, s.indexOf('>')+1), " ").countTokens() == 22) {
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: HP-UX 1.4.1/1.4.2");
            return new DataReaderHPUX1_4_1(in);
        } else if (s.indexOf("<verbosegc version=\"") != -1) { 
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: IBM J9 5.0");
            return new DataReaderIBM_J9_5_0(in);
        } else if (s.indexOf("starting collection, threshold allocation reached.") != -1) { 
            if (LOG.isLoggable(Level.INFO)) LOG.info("File format: IBM i5/OS 1.4.2");
            return new DataReaderIBMi5OS1_4_2(in);
        } else
            if (LOG.isLoggable(Level.SEVERE)) LOG.severe(localStrings.getString("datareaderfactory_instantiation_failed"));
            throw new IOException(localStrings.getString("datareaderfactory_instantiation_failed"));
    }
}
