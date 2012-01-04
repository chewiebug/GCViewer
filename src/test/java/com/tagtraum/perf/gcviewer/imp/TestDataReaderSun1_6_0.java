package com.tagtraum.perf.gcviewer.imp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.tagtraum.perf.gcviewer.model.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

public class TestDataReaderSun1_6_0 extends TestCase {
    private static final Logger IMP_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.imp");
    private static final Logger DATA_READER_FACTORY_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.DataReaderFactory");

    public void testPrintGCDateStamps() throws Exception {
		final ByteArrayInputStream in = new ByteArrayInputStream(
				("2011-10-05T04:23:39.427+0200: 19.845: [GC 19.845: [ParNew: 93184K->5483K(104832K), 0.0384413 secs] 93184K->5483K(1036928K), 0.0388082 secs] [Times: user=0.41 sys=0.06, real=0.04 secs]")
						.getBytes());
		 
		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertTrue("hasDateStamp", model.hasDateStamp());
		SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
		assertEquals("DateStamp",
				dateParser.parse("2011-10-05T04:23:39.427+0200"),
				model.getFirstDateStamp());
        assertEquals("gc pause", 0.0388082, model.getGCPause().getMax(), 0.000001);
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
				 
		final DataReader reader = new DataReaderSun1_6_0(in);
		GCModel model = reader.read();

		assertEquals("GC count", 2, model.size());

		assertEquals("gc pause", 41.0904457, model.getFullGCPause().getMax(), 0.000001);
	}

    public void testCMSConcurrentModeFailureCmsAbortPreclean() throws Exception {
        final ByteArrayInputStream in = new ByteArrayInputStream(
                ("39323.400: [GC 39323.400: [ParNew (promotion failed): 471871K->457831K(471872K), 10.5045897 secs]39333.905: [CMS CMS: abort preclean due to time 39334.591: [CMS-concurrent-abortable-preclean: 4.924/15.546 secs] [Times: user=24.45 sys=9.40, real=15.55 secs]" +
                "\n (concurrent mode failure): 1301661K->1299268K(1572864K), 43.3433234 secs] 1757009K->1299268K(2044736K), [CMS Perm : 64534K->63216K(110680K)], 53.8487115 secs] [Times: user=54.83 sys=9.22, real=53.85 secs]")
                        .getBytes());
                 
        final DataReader reader = new DataReaderSun1_6_0(in);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());

        assertEquals("gc pause", 53.8487115, model.getFullGCPause().getMax(), 0.000001);
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

    public void testCmsRemarkDatestamp() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2011-10-05T04:23:39.427+0200: 13455.879: [GC[YG occupancy: 325751 K (471872 K)]13455.879: [Rescan (parallel) , 1.0591220 secs]13456.939: [weak refs processing, 0.0794109 secs] [1 CMS-remark: 1023653K(1572864K)] 1349404K(2044736K), 1.1490033 secs] [Times: user=19.09 sys=0.26, real=1.15 secs]")
                        .getBytes());
        final DataReader reader = new DataReaderSun1_6_0(in);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());

        assertEquals("gc pause", 1.1490033, model.getGCPause().getSum(), 0.00000001);
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

    public void testCmsRemarkSerial() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("0.778: [GC[YG occupancy: 2179 K (19136 K)]0.778: [Rescan (non-parallel) 0.778: [grey object rescan, 0.0014243 secs]0.780: [root rescan, 0.0000909 secs], 0.0015484 secs]0.780: [weak refs processing, 0.0000066 secs] [1 CMS-remark: 444198K(444416K)] 446377K(463552K), 0.0015882 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]")
                       .getBytes());
        final DataReader reader = new DataReaderSun1_6_0(in);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());

        assertEquals("gc pause", 0.0015882, model.getGCPause().getSum(), 0.00000001);
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

    public void testCmsConcurrentMarkStart() throws Exception {

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2011-10-24T08:12:24.375+0200: 3388.929: [CMS-concurrent-mark-start]")
                       .getBytes());
        final DataReader reader = new DataReaderSun1_6_0(in);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("full gc pause", 0.0, model.getFullGCPause().getSum(), 0.01);

    }
    
    public void testCmsInitiatingOccupancyFraction() throws Exception {

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("12460.657: [GC [1 CMS-initial-mark: 789976K(1572864K)] 838178K(2044736K), 0.3114519 secs] [Times: user=0.32 sys=0.00, real=0.31 secs]")
                       .getBytes());
        final DataReader reader = new DataReaderSun1_6_0(in);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("iof", 0.5022532145182292, model.getCmsInitiatingOccupancyFraction().average(), 0.0000001);

    }
    
    public void testMixedLineWithEmptyLine() throws Exception {

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2011-01-25T17:10:16.889+0100: 12076.859: [GC 12076.859: [ParNew2011-01-25T17:10:16.896+0100: 12076.866: [CMS-concurrent-abortable-preclean: 0.929/4.899 secs] [Times: user=2.13 sys=0.04, real=4.90 secs]" +
                		"\n" +
                		"\nDesired survivor size 720896 bytes, new threshold 1 (max 4)" +
                		"\n- age   1:    1058016 bytes,    1058016 total" +
                		"\n: 13056K->1408K(13056K), 0.0128277 secs] 131480K->122757K(141328K), 0.0131346 secs] [Times: user=0.15 sys=0.00, real=0.01 secs]")
                       .getBytes());
        final DataReader reader = new DataReaderSun1_6_0(in);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());
        assertEquals("event pause", 0.0131346, model.getGCPause().getMax(), 0.0000001);
        assertEquals("promotion", 2925, model.getPromotion().getMax());
    }

    public void testPrintTenuringDistribution() throws Exception {

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2011-02-14T13:15:24.164+0100: 31581.748: [GC 31581.748: [ParNew" +
                        "\nDesired survivor size 5963776 bytes, new threshold 1 (max 4)" +
                        "\n- age   1:    8317928 bytes,    8317928 total" +
                        "\n: 92938K->8649K(104832K), 0.0527364 secs] 410416K->326127K(1036928K), 0.0533874 secs] [Times: user=0.46 sys=0.09, real=0.05 secs]")
                       .getBytes());
        final DataReader reader = new DataReaderSun1_6_0(in);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("event pause", 0.0533874, model.getGCPause().getMax(), 0.0000001);
        assertEquals("promotion", 0, model.getPromotion().getMax());

    }
                                                              
    public void testPrintTenuringDistributionPromotionFailed() throws Exception {

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2011-02-14T13:14:36.298+0100: 31533.871: [GC 31533.871: [ParNew (promotion failed)" +
                        "\nDesired survivor size 524288 bytes, new threshold 1 (max 4)" +
                        "\n- age   1:     703560 bytes,     703560 total" +
                        "\n- age   2:     342056 bytes,    1045616 total" +
                        "\n : 9321K->9398K(9792K), 0.0563031 secs]31533.928: [CMS: 724470K->317478K(931248K), 13.5375713 secs] 733688K->317478K(941040K), [CMS Perm : 51870K->50724K(86384K)], 13.5959700 secs] [Times: user=14.03 sys=0.03, real=13.60 secs]")
                       .getBytes());
        final DataReader reader = new DataReaderSun1_6_0(in);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("event pause", 13.5959700, model.getFullGCPause().getMax(), 0.0000001);

    }

    public void testPrintTenuringDistributionPromotionFailedConcurrentModeFailure() throws Exception {

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2011-04-18T12:01:14.683+0200: 27401.763: [GC 27401.763: [ParNew (promotion failed)" +
                        "\nDesired survivor size 557056 bytes, new threshold 1 (max 4)" +
                        "\n- age   1:     906712 bytes,     906712 total" +
                        "\n: 9768K->9877K(10240K), 0.0453585 secs]27401.808: [CMS2011-04-18T12:01:20.261+0200: 27407.340: [CMS-concurrent-sweep: 5.738/5.787 secs] [Times: user=6.40 sys=0.02, real=5.79 secs]" +
                        "\n (concurrent mode failure): 858756K->670276K(932096K), 31.5781426 secs] 868036K->670276K(942336K), [CMS Perm : 54962K->51858K(91608K)], 31.6248756 secs] [Times: user=31.85 sys=0.03, real=31.63 secs]")
                       .getBytes());
        final DataReader reader = new DataReaderSun1_6_0(in);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());
        assertEquals("event pause", 31.6248756, model.getFullGCPause().getMax(), 0.0000001);

    }
    
    public void testLineMixesPrintTenuringDistribution() throws Exception {

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2011-03-31T06:01:25.675+0200: 5682.440: [GC 5682.441: [ParNew2011-03-31T06:01:25.682+0200: 5682.447: [CMS-concurrent-abortable-preclean: 0.035/0.348 secs]" +
                        "\nDesired survivor size 557056 bytes, new threshold 4 (max 4)" +
                        "\n- age   1:       1104 bytes,       1104 total" +
                        "\n- age   2:      52008 bytes,      53112 total" +
                        "\n- age   3:       4400 bytes,      57512 total" +
                        "\n [Times: user=0.59 sys=0.01, real=0.35 secs]" +
                        "\n: 9405K->84K(10368K), 0.0064674 secs] 151062K->141740K(164296K), 0.0067202 secs] [Times: user=0.11 sys=0.01, real=0.01 secs]")
                       .getBytes());
        final DataReader reader = new DataReaderSun1_6_0(in);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());
        assertEquals("event pause", 0.0067202, model.getGCPause().getMax(), 0.0000001);

    }

    public void testCmsMemory() throws Exception {
        final InputStream in = getClass().getResourceAsStream("SampleSun1_6_0CMS.txt");
        final DataReader reader = new DataReaderSun1_6_0(in);
        GCModel model = reader.read();

        assertEquals("GC count", 41, model.size());
        assertEquals("heap min allocated", 249088, model.getHeapAllocatedSizes().getMin());
        assertEquals("heap max allocated", 249088, model.getHeapAllocatedSizes().getMax());
        assertEquals("young min allocated", 118016, model.getYoungAllocatedSizes().getMin());
        assertEquals("young max allocated", 118016, model.getYoungAllocatedSizes().getMax());
        assertEquals("tenured min allocated", 131072, model.getTenuredAllocatedSizes().getMin());
        assertEquals("tenured max allocated", 131072, model.getTenuredAllocatedSizes().getMax());
        assertEquals("perm min allocated", 21248, model.getPermAllocatedSizes().getMin());
        assertEquals("perm max allocated", 21248, model.getPermAllocatedSizes().getMax());

        assertEquals("heap min used", 80841, model.getHeapUsedSizes().getMin());
        assertEquals("heap max used", 209896, model.getHeapUsedSizes().getMax());
        assertEquals("young min used", 104960, model.getYoungUsedSizes().getMin());
        assertEquals("young max used", 118010, model.getYoungUsedSizes().getMax());
        assertEquals("tenured min used", 65665, model.getTenuredUsedSizes().getMin());
        assertEquals("tenured max used", 115034, model.getTenuredUsedSizes().getMax());
        assertEquals("perm min used", 2560, model.getPermUsedSizes().getMin());
        assertEquals("perm max used", 2561, model.getPermUsedSizes().getMax());

        assertEquals("promotion avg", 16998.3846, model.getPromotion().average(), 0.0001);
        assertEquals("promotion total", 220979, model.getPromotion().getSum());
    }
    
    public void testPrintCmsStatistics() throws Exception {
        // will not be able to extract sense from this line, but must not loop
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("0.521: [GC[YG occupancy: 2234 K (14784 K)]0.522: [Rescan (parallel)  (Survivor:0chunks) Finished young gen rescan work in 1th thread: 0.000 sec")
                       .getBytes());
        final DataReader reader = new DataReaderSun1_6_0(in);
        GCModel model = reader.read();

        assertEquals("GC count", 0, model.size());
    }
    
    public void testPrintHeapAtGC() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);
        
        final InputStream in = getClass().getResourceAsStream("SampleSun1_6_0PrintHeapAtGC.txt");
        final DataReader reader = new DataReaderSun1_6_0(in);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());
        assertEquals("GC pause", 0.0134287, model.getGCPause().getMin(), 0.000000001);
        assertEquals("number of errors", 0, handler.getCount());

    }
     
    public void testAdaptiveSizePolicy() throws Exception {
        // 0.175: [GCAdaptiveSizePolicy::compute_survivor_space_size_and_thresh:  survived: 2721008  promoted: 13580768  overflow: trueAdaptiveSizeStart: 0.186 collection: 1 
        // PSAdaptiveSizePolicy::compute_generation_free_space: costs minor_time: 0.059538 major_cost: 0.000000 mutator_cost: 0.940462 throughput_goal: 0.990000 live_space: 273821824 free_space: 33685504 old_promo_size: 16842752 old_eden_size: 16842752 desired_promo_size: 16842752 desired_eden_size: 33685504
        // AdaptiveSizePolicy::survivor space sizes: collection: 1 (2752512, 2752512) -> (2752512, 2752512) 
        // AdaptiveSizeStop: collection: 1 
        //  [PSYoungGen: 16420K->2657K(19136K)] 16420K->15919K(62848K), 0.0109211 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
        
        final InputStream in = getClass().getResourceAsStream("SampleSun1_6_0AdaptiveSizePolicy.txt");
        final DataReader reader = new DataReaderSun1_6_0(in);
        GCModel model = reader.read();

        assertEquals("GC count", 10, model.size());
        assertEquals("GC pause", 0.0224480, model.getGCPause().getMax());
        assertEquals("Full GC pause", 0.0204436, model.getFullGCPause().getMax());
    }
     
}
