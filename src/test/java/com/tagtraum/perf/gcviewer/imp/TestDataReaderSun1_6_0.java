package com.tagtraum.perf.gcviewer.imp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import junit.framework.TestCase;

import com.tagtraum.perf.gcviewer.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.DataReader;
import com.tagtraum.perf.gcviewer.GCModel;

public class TestDataReaderSun1_6_0 extends TestCase {
	public void testPrintGCDateStamps() throws Exception {
		final ByteArrayInputStream in = new ByteArrayInputStream(
				("2011-10-05T04:23:39.427+0200: 19.845: [GC 19.845: [ParNew: 93184K->5483K(104832K), 0.0384413 secs] 93184K->5483K(1036928K), 0.0388082 secs] [Times: user=0.41 sys=0.06, real=0.04 secs]")
						.getBytes());
		 
		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertEquals("gc pause", 0.0388082, model.getGCPause().getMax(), 0.000001);
		assertTrue("hasDateStamp", model.hasDateStamp());
		SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
		assertEquals("DateStamp",
				dateParser.parse("2011-10-05T04:23:39.427+0200"),
				model.getFirstDateStamp());
	}

	public void testCMSPromotionFailed() throws Exception {
		final ByteArrayInputStream in = new ByteArrayInputStream(
				("2011-10-05T16:05:55.964+0200: 41985.374: [GC 41985.375: [ParNew (promotion failed): 104960K->100764K(104960K), 0.3379238 secs]41985.713: [CMS: 1239589K->897516K(1398144K), 38.3189415 secs] 1336713K->897516K(1503104K), [CMS Perm : 55043K->53511K(91736K)], 38.6583674 secs] [Times: user=39.22 sys=0.06, real=38.66 secs]")
						.getBytes());

		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertEquals("gc pause", 38.6583674, model.getFullGCPause().getSum(), 0.000001);
	}

	public void testCMSConcurrentModeFailureDate() throws Exception {
		final ByteArrayInputStream in = new ByteArrayInputStream(
				("2011-10-05T15:53:24.119+0200: 41403.025: [GC 41403.025: [ParNew (promotion failed): 104960K->101572K(104960K), 0.3275017 secs]41403.353: [CMS2011-10-05T15:53:24.629+0200: 41403.534: [CMS-concurrent-abortable-preclean: 1.992/2.650 secs] [Times: user=4.40 sys=0.06, real=2.65 secs]" +
				"\n (concurrent mode failure): 1295417K->906090K(1398144K), 32.4123146 secs] 1395643K->906090K(1503104K), [CMS Perm : 54986K->53517K(91576K)], 32.7410609 secs] [Times: user=33.10 sys=0.05, real=32.74 secs]")
						.getBytes());
		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertEquals("GC count", 2, model.size());

		assertEquals("gc pause", 32.7410609, model.getFullGCPause().getMax(), 0.000001);
	}

	public void testCMSConcurrentModeFailure() throws Exception {
		final ByteArrayInputStream in = new ByteArrayInputStream(
				("25866.053: [GC 25866.054: [ParNew (promotion failed): 458123K->468193K(471872K), 0.9151441 secs]25866.969: [CMS25870.038: [CMS-concurrent-mark: 3.120/4.102 secs] [Times: user=26.00 sys=0.12, real=4.10 secs]" +
				"\n (concurrent mode failure): 1143630K->1154547K(1572864K), 40.1744087 secs] 1590086K->1154547K(2044736K), [CMS Perm : 65802K->63368K(109784K)], 41.0904457 secs] [Times: user=60.57 sys=0.07, real=41.09 secs]")
						.getBytes());
				 
//		final InputStream in = getClass().getResourceAsStream("SampleSun1_6_0CMSConcurrentModeFailure.txt");
		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertEquals("GC count", 2, model.size());

		assertEquals("gc pause", 41.0904457, model.getFullGCPause().getMax(), 0.000001);
	}

	public void testCMSFullGcCmsInterrupted() throws Exception {
		// TODO CMS (concurrent mode interrupted) not recognised (ignored)
		ByteArrayInputStream in = new ByteArrayInputStream(
				"78.579: [Full GC (System) 78.579: [CMS (concurrent mode interrupted): 64171K->1538K(107776K), 0.0088356 secs] 75362K->1538K(126912K), [CMS Perm : 2554K->2554K(21248K)], 0.0089351 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]"
						.getBytes());
		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertEquals("count", 1, model.getPause().getN());

		assertEquals("full gc pause", 0.0089351, model.getFullGCPause().getSum(), 0.00000001);
	}

	public void testCMSAbortingPrecleanTimestamp() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				" CMS: abort preclean due to time 12467.886: [CMS-concurrent-abortable-preclean: 5.300/5.338 secs] [Times: user=10.70 sys=0.13, real=5.34 secs]"
						.getBytes());
		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertEquals("GC count", 1, model.size());

		assertEquals("gc pause", 5.3, ((ConcurrentGCEvent) model.getConcurrentGCEvents().next()).getPause(), 0.001);
	}

	public void testCMSAbortingPrecleanDatestamp() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				" CMS: abort preclean due to time 2011-10-07T08:10:25.312+0200: 13454.979: [CMS-concurrent-abortable-preclean: 3.849/5.012 secs] [Times: user=5.58 sys=0.08, real=5.01 secs]"
						.getBytes());
		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertEquals("GC count", 1, model.size());

		assertEquals("gc pause", 3.849, ((ConcurrentGCEvent) model.getConcurrentGCEvents().next()).getPause(), 0.0001);
	}

	public void testFullGcIncrementalTimestamp() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				("42927.215: [Full GC 42927.215: [CMS42927.255: [CMS-concurrent-sweep: 0.416/6.288 secs] [Times: user=17.38 sys=0.44, real=6.29 secs]"
						+ "\n (concurrent mode failure): 262166K->215967K(785256K), 7.8308614 secs] 273998K->215967K(800040K), [CMS Perm : 523009K->155678K(524288K)] icms_dc=8 , 7.8320634 secs] [Times: user=4.59 sys=0.04, real=7.83 secs]")
						.getBytes());
		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertEquals("GC count", 2, model.size());

		assertEquals("full gc pause", 7.8320634, model.getFullGCPause().getSum(), 0.00000001);
	}

	public void testFullGcIncrementalTimestamp2() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				("44189.823: [Full GC 44189.824: [CMS: 274825K->223922K(892264K), 8.0594203 secs] 327565K->223922K(992616K), [CMS Perm : 524287K->158591K(524288K)] icms_dc=0 , 8.0600619 secs] [Times: user=4.51 sys=0.05, real=8.06 secs]")
						.getBytes());
		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertEquals("GC count", 1, model.size());

		assertEquals("full gc pause", 8.0600619, model.getFullGCPause().getSum(), 0.00000001);
	}

	public void testCmsRemarkTimestamp() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				("13455.879: [GC[YG occupancy: 325751 K (471872 K)]13455.879: [Rescan (parallel) , 1.0591220 secs]13456.939: [weak refs processing, 0.0794109 secs] [1 CMS-remark: 1023653K(1572864K)] 1349404K(2044736K), 1.1490033 secs] [Times: user=19.09 sys=0.26, real=1.15 secs]")
						.getBytes());
		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertEquals("GC count", 1, model.size());

		assertEquals("gc pause", 1.1490033, model.getGCPause().getSum(), 0.00000001);
	}

	public void testFullGcIncrementalDatestamp() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				("2011-10-05T04:23:39.427+0200: 42927.215: [Full GC 42927.215: [CMS2011-10-05T04:23:39.427+0200: 42927.255: [CMS-concurrent-sweep: 0.416/6.288 secs] [Times: user=17.38 sys=0.44, real=6.29 secs]"
						+ "\n (concurrent mode failure): 262166K->215967K(785256K), 7.8308614 secs] 273998K->215967K(800040K), [CMS Perm : 523009K->155678K(524288K)] icms_dc=8 , 7.8320634 secs] [Times: user=4.59 sys=0.04, real=7.83 secs]")
						.getBytes());
		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertEquals("GC count", 2, model.size());

		assertEquals("full gc pause", 7.8320634, model.getFullGCPause().getSum(), 0.00000001);
	}

	public void testFullGcIncrementalDatestamp2() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				("2011-10-05T04:23:39.427+0200: 44189.823: [Full GC 44189.824: [CMS: 274825K->223922K(892264K), 8.0594203 secs] 327565K->223922K(992616K), [CMS Perm : 524287K->158591K(524288K)] icms_dc=0 , 8.0600619 secs] [Times: user=4.51 sys=0.05, real=8.06 secs]")
						.getBytes());
		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertEquals("GC count", 1, model.size());

		assertEquals("full gc pause", 8.0600619, model.getFullGCPause().getSum(), 0.00000001);
	}

	public void testCmsRemarkDatestamp() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				("2011-10-05T04:23:39.427+0200: 13455.879: [GC[YG occupancy: 325751 K (471872 K)]13455.879: [Rescan (parallel) , 1.0591220 secs]13456.939: [weak refs processing, 0.0794109 secs] [1 CMS-remark: 1023653K(1572864K)] 1349404K(2044736K), 1.1490033 secs] [Times: user=19.09 sys=0.26, real=1.15 secs]")
						.getBytes());
		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertEquals("GC count", 1, model.size());

		assertEquals("gc pause", 1.1490033, model.getGCPause().getSum(), 0.00000001);
	}

	public void testMixedLineTimestamp() throws Exception {

		ByteArrayInputStream in = new ByteArrayInputStream(
				("36628.590: [GC 36628.591: [ParNew36628.625: [CMS-concurrent-abortable-preclean: 0.128/0.873 secs] [Times: user=2.52 sys=0.02, real=0.87 secs]"
						+ "\n: 14780K->1041K(14784K), 0.0417590 secs] 304001K->295707K(721240K) icms_dc=56 , 0.0419761 secs] [Times: user=0.81 sys=0.01, real=0.04 secs]")
						.getBytes());
		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertEquals("GC count", 2, model.size());

		assertEquals("gc pause", 0.0419761, model.getGCPause().getSum(), 0.00000001);
	}

	public void testFullGcSystem() throws Exception {

		ByteArrayInputStream in = new ByteArrayInputStream(
				("164.078: [Full GC (System) 164.078: [Tenured: 107024K->86010K(349568K), 0.7964528 secs] 143983K->86010K(506816K), [Perm : 85883K->85855K(86016K)], 0.7965714 secs] [Times: user=0.84 sys=0.00, real=0.80 secs]")
						.getBytes());
		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertEquals("GC count", 1, model.size());

		assertEquals("full gc pause", 0.7965714, model.getFullGCPause().getSum(), 0.00000001);
	}


}
