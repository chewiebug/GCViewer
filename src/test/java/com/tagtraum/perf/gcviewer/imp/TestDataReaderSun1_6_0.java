package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;

public class TestDataReaderSun1_6_0 {

    private final DateTimeFormatter dateTimeFormatter = AbstractDataReaderSun.DATE_TIME_FORMATTER;

    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_OPENJDK, fileName);
    }

    @Test
    public void testPrintGCDateStamps() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				("2011-10-05T04:23:39.427+0200: 19.845: [GC 19.845: [ParNew: 93184K->5483K(104832K), 0.0384413 secs] 93184K->5483K(1036928K), 0.0388082 secs] [Times: user=0.41 sys=0.06, real=0.04 secs]")
						.getBytes());
		 
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
		GCModel model = reader.read();

		assertTrue("hasDateStamp", model.hasDateStamp());
		assertEquals("DateStamp",
				ZonedDateTime.parse("2011-10-05T04:23:39.427+0200", dateTimeFormatter),
				model.getFirstDateStamp());
        assertEquals("gc pause", 0.0388082, model.getGCPause().getMax(), 0.000001);
	}

    @Test
	public void testCMSPromotionFailed() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				("2011-10-05T16:05:55.964+0200: 41985.374: [GC 41985.375: [ParNew (promotion failed): 104960K->100764K(104960K), 0.3379238 secs]41985.713: [CMS: 1239589K->897516K(1398144K), 38.3189415 secs] 1336713K->897516K(1503104K), [CMS Perm : 55043K->53511K(91736K)], 38.6583674 secs] [Times: user=39.22 sys=0.06, real=38.66 secs]")
						.getBytes());

		DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
		GCModel model = reader.read();

		assertEquals("gc pause", 38.6583674, model.getFullGCPause().getSum(), 0.000001);
	}

    @Test
    public void testCMSPromotionFailedPrintPromotionFailure() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2012-03-26T21:46:32.546+0200: 2.204: [GC 2.204: [ParNew (0: promotion failure size = 4098)  (1: promotion failure size = 4098)  (2: promotion failure size = 4098)  (3: promotion failure size = 4098)  (promotion failed): 39277K->39255K(39296K), 0.0175749 secs]2.221: [CMS: 87276K->43438K(87424K), 0.0276222 secs] 95765K->43438K(126720K), [CMS Perm : 2612K->2612K(21248K)], 0.0453577 secs] [Times: user=0.08 sys=0.00, real=0.05 secs]")
                        .getBytes());

        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("gc type", "GC; ParNew (promotion failed); CMS; CMS Perm", model.getFullGCEvents().next().getTypeAsString());
    }


    @Test
	public void testCMSConcurrentModeFailureDate() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				("2011-10-05T15:53:24.119+0200: 41403.025: [GC 41403.025: [ParNew (promotion failed): 104960K->101572K(104960K), 0.3275017 secs]41403.353: [CMS2011-10-05T15:53:24.629+0200: 41403.534: [CMS-concurrent-abortable-preclean: 1.992/2.650 secs] [Times: user=4.40 sys=0.06, real=2.65 secs]" +
				"\n (concurrent mode failure): 1295417K->906090K(1398144K), 32.4123146 secs] 1395643K->906090K(1503104K), [CMS Perm : 54986K->53517K(91576K)], 32.7410609 secs] [Times: user=33.10 sys=0.05, real=32.74 secs]")
						.getBytes());
		DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
		GCModel model = reader.read();

		assertEquals("GC count", 2, model.size());

		assertEquals("gc pause", 32.7410609, model.getFullGCPause().getMax(), 0.000001);
	}

    @Test
	public void testCMSConcurrentModeFailure() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				("25866.053: [GC 25866.054: [ParNew (promotion failed): 458123K->468193K(471872K), 0.9151441 secs]25866.969: [CMS25870.038: [CMS-concurrent-mark: 3.120/4.102 secs] [Times: user=26.00 sys=0.12, real=4.10 secs]" +
				"\n (concurrent mode failure): 1143630K->1154547K(1572864K), 40.1744087 secs] 1590086K->1154547K(2044736K), [CMS Perm : 65802K->63368K(109784K)], 41.0904457 secs] [Times: user=60.57 sys=0.07, real=41.09 secs]")
						.getBytes());
				 
		DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
		GCModel model = reader.read();

		assertEquals("GC count", 2, model.size());

		assertEquals("gc pause", 41.0904457, model.getFullGCPause().getMax(), 0.000001);
	}

    @Test
    public void testCMSConcurrentModeFailureCmsAbortPreclean() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("39323.400: [GC 39323.400: [ParNew (promotion failed): 471871K->457831K(471872K), 10.5045897 secs]39333.905: [CMS CMS: abort preclean due to time 39334.591: [CMS-concurrent-abortable-preclean: 4.924/15.546 secs] [Times: user=24.45 sys=9.40, real=15.55 secs]" +
                "\n (concurrent mode failure): 1301661K->1299268K(1572864K), 43.3433234 secs] 1757009K->1299268K(2044736K), [CMS Perm : 64534K->63216K(110680K)], 53.8487115 secs] [Times: user=54.83 sys=9.22, real=53.85 secs]")
                        .getBytes());
                 
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());

        assertEquals("gc pause", 53.8487115, model.getFullGCPause().getMax(), 0.000001);
    }

    @Test
	public void testCMSFullGcCmsInterrupted() throws Exception {
		// TODO CMS (concurrent mode interrupted) not recognised (ignored)
		ByteArrayInputStream in = new ByteArrayInputStream(
				"78.579: [Full GC (System) 78.579: [CMS (concurrent mode interrupted): 64171K->1538K(107776K), 0.0088356 secs] 75362K->1538K(126912K), [CMS Perm : 2554K->2554K(21248K)], 0.0089351 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]"
						.getBytes());
		DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
		GCModel model = reader.read();

		assertEquals("count", 1, model.getPause().getN());

		assertEquals("full gc pause", 0.0089351, model.getFullGCPause().getSum(), 0.00000001);
	}

    @Test
	public void testCMSAbortingPrecleanTimestamp() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				" CMS: abort preclean due to time 12467.886: [CMS-concurrent-abortable-preclean: 5.300/5.338 secs] [Times: user=10.70 sys=0.13, real=5.34 secs]"
						.getBytes());
		DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
		GCModel model = reader.read();

		assertEquals("GC count", 1, model.size());

		assertEquals("gc pause", 5.3, model.getConcurrentGCEvents().next().getPause(), 0.001);
	}

    @Test
	public void testCMSAbortingPrecleanDatestamp() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				" CMS: abort preclean due to time 2011-10-07T08:10:25.312+0200: 13454.979: [CMS-concurrent-abortable-preclean: 3.849/5.012 secs] [Times: user=5.58 sys=0.08, real=5.01 secs]"
						.getBytes());
		DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
		GCModel model = reader.read();

		assertEquals("GC count", 1, model.size());

		assertEquals("gc pause", 3.849, model.getConcurrentGCEvents().next().getPause(), 0.0001);
	}

    @Test
	public void testFullGcIncrementalTimestamp() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				("42927.215: [Full GC 42927.215: [CMS42927.255: [CMS-concurrent-sweep: 0.416/6.288 secs] [Times: user=17.38 sys=0.44, real=6.29 secs]"
						+ "\n (concurrent mode failure): 262166K->215967K(785256K), 7.8308614 secs] 273998K->215967K(800040K), [CMS Perm : 523009K->155678K(524288K)] icms_dc=8 , 7.8320634 secs] [Times: user=4.59 sys=0.04, real=7.83 secs]")
						.getBytes());
		DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
		GCModel model = reader.read();

		assertEquals("GC count", 2, model.size());

		assertEquals("full gc pause", 7.8320634, model.getFullGCPause().getSum(), 0.00000001);
	}

    @Test
	public void testFullGcIncrementalTimestamp2() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				("44189.823: [Full GC 44189.824: [CMS: 274825K->223922K(892264K), 8.0594203 secs] 327565K->223922K(992616K), [CMS Perm : 524287K->158591K(524288K)] icms_dc=0 , 8.0600619 secs] [Times: user=4.51 sys=0.05, real=8.06 secs]")
						.getBytes());
		DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
		GCModel model = reader.read();

		assertEquals("GC count", 1, model.size());

		assertEquals("full gc pause", 8.0600619, model.getFullGCPause().getSum(), 0.00000001);
	}

    @Test
    public void testCmsRemarkDatestamp() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2011-10-05T04:23:39.427+0200: 13455.879: [GC[YG occupancy: 325751 K (471872 K)]13455.879: [Rescan (parallel) , 1.0591220 secs]13456.939: [weak refs processing, 0.0794109 secs] [1 CMS-remark: 1023653K(1572864K)] 1349404K(2044736K), 1.1490033 secs] [Times: user=19.09 sys=0.26, real=1.15 secs]")
                        .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());

        assertEquals("gc pause", 1.1490033, model.getGCPause().getSum(), 0.00000001);
    }

    @Test
	public void testCmsRemarkTimestamp() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				("13455.879: [GC[YG occupancy: 325751 K (471872 K)]13455.879: [Rescan (parallel) , 1.0591220 secs]13456.939: [weak refs processing, 0.0794109 secs] [1 CMS-remark: 1023653K(1572864K)] 1349404K(2044736K), 1.1490033 secs] [Times: user=19.09 sys=0.26, real=1.15 secs]")
						.getBytes());
		DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
		GCModel model = reader.read();

 		assertEquals("GC count", 1, model.size());

		assertEquals("gc pause", 1.1490033, model.getGCPause().getSum(), 0.00000001);
	}

    @Test
    public void testCmsRemarkSerial() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("0.778: [GC[YG occupancy: 2179 K (19136 K)]0.778: [Rescan (non-parallel) 0.778: [grey object rescan, 0.0014243 secs]0.780: [root rescan, 0.0000909 secs], 0.0015484 secs]0.780: [weak refs processing, 0.0000066 secs] [1 CMS-remark: 444198K(444416K)] 446377K(463552K), 0.0015882 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());

        assertEquals("gc pause", 0.0015882, model.getGCPause().getSum(), 0.00000001);
    }

    @Test
	public void testFullGcIncrementalDatestamp() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				("2011-10-05T04:23:39.427+0200: 42927.215: [Full GC 42927.215: [CMS2011-10-05T04:23:39.427+0200: 42927.255: [CMS-concurrent-sweep: 0.416/6.288 secs] [Times: user=17.38 sys=0.44, real=6.29 secs]"
						+ "\n (concurrent mode failure): 262166K->215967K(785256K), 7.8308614 secs] 273998K->215967K(800040K), [CMS Perm : 523009K->155678K(524288K)] icms_dc=8 , 7.8320634 secs] [Times: user=4.59 sys=0.04, real=7.83 secs]")
						.getBytes());
		DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
		GCModel model = reader.read();

		assertEquals("GC count", 2, model.size());

		assertEquals("full gc pause", 7.8320634, model.getFullGCPause().getSum(), 0.00000001);
	}

    @Test
	public void testFullGcIncrementalDatestamp2() throws Exception {
		ByteArrayInputStream in = new ByteArrayInputStream(
				("2011-10-05T04:23:39.427+0200: 44189.823: [Full GC 44189.824: [CMS: 274825K->223922K(892264K), 8.0594203 secs] 327565K->223922K(992616K), [CMS Perm : 524287K->158591K(524288K)] icms_dc=0 , 8.0600619 secs] [Times: user=4.51 sys=0.05, real=8.06 secs]")
						.getBytes());
		DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
		GCModel model = reader.read();

		assertEquals("GC count", 1, model.size());

		assertEquals("full gc pause", 8.0600619, model.getFullGCPause().getSum(), 0.00000001);
	}

    @Test
	public void testMixedLineTimestamp() throws Exception {

		ByteArrayInputStream in = new ByteArrayInputStream(
				("36628.590: [GC 36628.591: [ParNew36628.625: [CMS-concurrent-abortable-preclean: 0.128/0.873 secs] [Times: user=2.52 sys=0.02, real=0.87 secs]"
						+ "\n: 14780K->1041K(14784K), 0.0417590 secs] 304001K->295707K(721240K) icms_dc=56 , 0.0419761 secs] [Times: user=0.81 sys=0.01, real=0.04 secs]")
						.getBytes());
		DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
		GCModel model = reader.read();

		assertEquals("GC count", 2, model.size());

		assertEquals("gc pause", 0.0419761, model.getGCPause().getSum(), 0.00000001);
	}

    @Test
	public void testFullGcSystem() throws Exception {

		ByteArrayInputStream in = new ByteArrayInputStream(
				("164.078: [Full GC (System) 164.078: [Tenured: 107024K->86010K(349568K), 0.7964528 secs] 143983K->86010K(506816K), [Perm : 85883K->85855K(86016K)], 0.7965714 secs] [Times: user=0.84 sys=0.00, real=0.80 secs]")
						.getBytes());
		DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
		GCModel model = reader.read();

		assertEquals("GC count", 1, model.size());

		assertEquals("full gc pause", 0.7965714, model.getFullGCPause().getSum(), 0.00000001);
	}

    @Test
    public void testCmsConcurrentMarkStart() throws Exception {

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2011-10-24T08:12:24.375+0200: 3388.929: [CMS-concurrent-mark-start]")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("full gc pause", 0.0, model.getFullGCPause().getSum(), 0.01);

    }

    @Test
    public void testCmsInitiatingOccupancyFraction() throws Exception {

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("12460.657: [GC [1 CMS-initial-mark: 789976K(1572864K)] 838178K(2044736K), 0.3114519 secs] [Times: user=0.32 sys=0.00, real=0.31 secs]")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("iof", 0.5022532145182292, model.getCmsInitiatingOccupancyFraction().average(), 0.0000001);

    }

    @Test
    public void testMixedLineWithEmptyLine() throws Exception {

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2011-01-25T17:10:16.889+0100: 12076.859: [GC 12076.859: [ParNew2011-01-25T17:10:16.896+0100: 12076.866: [CMS-concurrent-abortable-preclean: 0.929/4.899 secs] [Times: user=2.13 sys=0.04, real=4.90 secs]" +
                		"\n" +
                		"\nDesired survivor size 720896 bytes, new threshold 1 (max 4)" +
                		"\n- age   1:    1058016 bytes,    1058016 total" +
                		"\n: 13056K->1408K(13056K), 0.0128277 secs] 131480K->122757K(141328K), 0.0131346 secs] [Times: user=0.15 sys=0.00, real=0.01 secs]")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());
        assertEquals("event pause", 0.0131346, model.getGCPause().getMax(), 0.0000001);
        assertEquals("promotion", 2925, model.getPromotion().getMax());
    }

    @Test
    public void testPrintTenuringDistribution() throws Exception {

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2011-02-14T13:15:24.164+0100: 31581.748: [GC 31581.748: [ParNew" +
                        "\nDesired survivor size 5963776 bytes, new threshold 1 (max 4)" +
                        "\n- age   1:    8317928 bytes,    8317928 total" +
                        "\n: 92938K->8649K(104832K), 0.0527364 secs] 410416K->326127K(1036928K), 0.0533874 secs] [Times: user=0.46 sys=0.09, real=0.05 secs]")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("event pause", 0.0533874, model.getGCPause().getMax(), 0.0000001);
        assertEquals("promotion", 0, model.getPromotion().getMax());

    }

    @Test
    public void testPrintTenuringDistributionPromotionFailed() throws Exception {

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2011-02-14T13:14:36.298+0100: 31533.871: [GC 31533.871: [ParNew (promotion failed)" +
                        "\nDesired survivor size 524288 bytes, new threshold 1 (max 4)" +
                        "\n- age   1:     703560 bytes,     703560 total" +
                        "\n- age   2:     342056 bytes,    1045616 total" +
                        "\n : 9321K->9398K(9792K), 0.0563031 secs]31533.928: [CMS: 724470K->317478K(931248K), 13.5375713 secs] 733688K->317478K(941040K), [CMS Perm : 51870K->50724K(86384K)], 13.5959700 secs] [Times: user=14.03 sys=0.03, real=13.60 secs]")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("event pause", 13.5959700, model.getFullGCPause().getMax(), 0.0000001);

    }

    @Test
    public void testPrintTenuringDistributionPromotionFailedConcurrentModeFailure() throws Exception {

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2011-04-18T12:01:14.683+0200: 27401.763: [GC 27401.763: [ParNew (promotion failed)" +
                        "\nDesired survivor size 557056 bytes, new threshold 1 (max 4)" +
                        "\n- age   1:     906712 bytes,     906712 total" +
                        "\n: 9768K->9877K(10240K), 0.0453585 secs]27401.808: [CMS2011-04-18T12:01:20.261+0200: 27407.340: [CMS-concurrent-sweep: 5.738/5.787 secs] [Times: user=6.40 sys=0.02, real=5.79 secs]" +
                        "\n (concurrent mode failure): 858756K->670276K(932096K), 31.5781426 secs] 868036K->670276K(942336K), [CMS Perm : 54962K->51858K(91608K)], 31.6248756 secs] [Times: user=31.85 sys=0.03, real=31.63 secs]")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());
        assertEquals("event pause", 31.6248756, model.getFullGCPause().getMax(), 0.0000001);

    }

    @Test
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
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());
        assertEquals("event pause", 0.0067202, model.getGCPause().getMax(), 0.0000001);

    }

    @Test
    public void testCmsMemory() throws Exception {
        String fileName = "SampleSun1_6_0CMS.txt";
        InputStream in = getInputStream(fileName);
        DataReader reader = new DataReaderSun1_6_0(new GCResource(fileName), in, GcLogType.SUN1_6);
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
        assertEquals("young min used", 15160, model.getYoungUsedSizes().getMin());
        assertEquals("young max used", 118010, model.getYoungUsedSizes().getMax());
        assertEquals("tenured min used", 0, model.getTenuredUsedSizes().getMin());
        assertEquals("tenured max used", 115034, model.getTenuredUsedSizes().getMax());
        assertEquals("perm min used", 2560, model.getPermUsedSizes().getMin());
        assertEquals("perm max used", 2561, model.getPermUsedSizes().getMax());

        assertEquals("promotion avg", 16998.3846, model.getPromotion().average(), 0.0001);
        assertEquals("promotion total", 220979, model.getPromotion().getSum());

        assertThat("count tenured heap after full gc", model.getFootprintAfterFullGC().getN(), is(2));
        assertThat("max tenured heap after full gc", model.getFootprintAfterFullGC().getMax(), is(31297));
    }

    @Test
    public void testPrintCmsStatistics() throws Exception {
        // will not be able to extract sense from this line, but must not loop
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("0.521: [GC[YG occupancy: 2234 K (14784 K)]0.522: [Rescan (parallel)  (Survivor:0chunks) Finished young gen rescan work in 1th thread: 0.000 sec")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 0, model.size());
    }

    @Test
    public void testPrintHeapAtGC() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource("SampleSun1_6_0PrintHeapAtGC.txt");
        gcResource.getLogger().addHandler(handler);

        InputStream in = getInputStream(gcResource.getResourceName());
        DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());
        assertEquals("GC pause", 0.0134287, model.getGCPause().getMin(), 0.000000001);
        assertEquals("number of errors", 0, handler.getCount());

    }

    @Test
    public void testAdaptiveSizePolicy() throws Exception {
        // 0.175: [GCAdaptiveSizePolicy::compute_survivor_space_size_and_thresh:  survived: 2721008  promoted: 13580768  overflow: trueAdaptiveSizeStart: 0.186 collection: 1
        // PSAdaptiveSizePolicy::compute_generation_free_space: costs minor_time: 0.059538 major_cost: 0.000000 mutator_cost: 0.940462 throughput_goal: 0.990000 live_space: 273821824 free_space: 33685504 old_promo_size: 16842752 old_eden_size: 16842752 desired_promo_size: 16842752 desired_eden_size: 33685504
        // AdaptiveSizePolicy::survivor space sizes: collection: 1 (2752512, 2752512) -> (2752512, 2752512)
        // AdaptiveSizeStop: collection: 1
        //  [PSYoungGen: 16420K->2657K(19136K)] 16420K->15919K(62848K), 0.0109211 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]

        String fileName = "SampleSun1_6_0AdaptiveSizePolicy.txt";
        InputStream in = getInputStream(fileName);
        DataReader reader = new DataReaderSun1_6_0(new GCResource(fileName), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 10, model.size());
        assertEquals("GC pause", 0.0224480, model.getGCPause().getMax(), 0.00000001);
        assertEquals("Full GC pause", 0.0204436, model.getFullGCPause().getMax(), 0.00000001);

        assertThat("count tenured size after full gc", model.getFootprintAfterFullGC().getN(), is(4));
        assertThat("max tenured size after full gc", model.getFootprintAfterFullGC().getMax(), is(64781));
    }

    @Test
    public void testAdaptiveSizePolicyFullSystemGc() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2012-03-21T20:49:09.624+0100: 9.993: [Full GC (System)AdaptiveSizeStart: 10.000 collection: 61" +
                 "\nAdaptiveSizeStop: collection: 61" +
                 "\n[PSYoungGen: 480K->0K(270976K)] [PSOldGen: 89711K->671K(145536K)] 90191K->671K(416512K) [PSPermGen: 2614K->2614K(21248K)], 0.0070749 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]")
                       .getBytes());
         
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("Full GC pause", 0.0070749, model.getFullGCPause().getMax(), 0.00000001);
    }

    @Test
    public void testPrintWithoutUseAdaptiveSizePolicy() throws Exception {
        // issue #36
        // -XX:+PrintAdaptiveSizePolicy
        // -XX:-UseAdaptiveSizePolicy

        ByteArrayInputStream in = new ByteArrayInputStream(
                "2012-09-27T17:03:28.712+0200: 0.222: [GCAdaptiveSizePolicy::compute_survivor_space_size_and_thresh:  survived: 2720992  promoted: 13613552  overflow: true [PSYoungGen: 16420K->2657K(19136K)] 16420K->15951K(62848K), 0.0132830 secs] [Times: user=0.00 sys=0.03, real=0.02 secs] "
                        .getBytes());
         
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("GC pause", 0.0132830, model.getGCPause().getMax(), 0.00000001);
    }

    @Test
    public void testCMSScavengeBeforeRemarkTimeStamp() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2.036: [GC[YG occupancy: 235954 K (235968 K)]2.036: [GC 2.036: [ParNew: 235954K->30K(235968K), 0.0004961 secs] 317153K->81260K(395712K), 0.0005481 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]" +
                 "\n2.037: [Rescan (parallel) , 0.0002425 secs]2.037: [weak refs processing, 0.0000041 secs]2.037: [class unloading, 0.0000938 secs]2.037: [scrub symbol & string tables, 0.0003138 secs] [1 CMS-remark: 81230K(159744K)] 81260K(395712K), 0.0013653 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());
        assertEquals("1st event", "GC; ParNew", model.get(0).getTypeAsString());
        assertEquals("2nd event", "GC; CMS-remark", model.get(1).getTypeAsString());
        assertEquals("1st event pause", 0.0005481, ((GCEvent)model.get(0)).getPause(), 0.00000001);
        assertEquals("2nd event pause", 0.0013653-0.0005481, ((GCEvent)model.get(1)).getPause(), 0.00000001);
    }

    @Test
    public void testCMSScavengeBeforeRemarkDateStamp() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2012-03-07T22:19:49.110+0100: 2.479: [GC[YG occupancy: 227872 K (235968 K)]2012-03-07T22:19:49.110+0100: 2.479: [GC 2.479: [ParNew: 227872K->30K(235968K), 0.0005432 secs] 296104K->68322K(395712K), 0.0005809 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]" +
                 "\n2.480: [Rescan (parallel) , 0.0001934 secs]2.480: [weak refs processing, 0.0000061 secs]2.480: [class unloading, 0.0001131 secs]2.480: [scrub symbol & string tables, 0.0003175 secs] [1 CMS-remark: 68292K(159744K)] 68322K(395712K), 0.0013506 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());
        assertEquals("1st event", "GC; ParNew", model.get(0).getTypeAsString());
        assertEquals("2nd event", "GC; CMS-remark", model.get(1).getTypeAsString());
        assertEquals("1st event pause", 0.0005809, ((GCEvent)model.get(0)).getPause(), 0.00000001);
        assertEquals("2nd event pause", 0.0013506-0.0005809, ((GCEvent)model.get(1)).getPause(), 0.00000001);
    }

    @Test
    public void testCMSScavengeBeforeRemarkWithPrintTenuringDistribution() throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(
                ("2012-03-07T22:19:48.736+0100: 2.104: [GC[YG occupancy: 235952 K (235968 K)]2012-03-07T22:19:48.736+0100: 2.104: [GC 2.104: [ParNew" +
                 "\nDesired survivor size 13402112 bytes, new threshold 4 (max 4)" +
                 "\n- age   1:      24816 bytes,      24816 total" +
                 "\n: 235952K->30K(235968K), 0.0005641 secs] 317151K->81260K(395712K), 0.0006030 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]" +
                 "\n2.105: [Rescan (parallel) , 0.0002003 secs]2.105: [weak refs processing, 0.0000041 secs]2.105: [class unloading, 0.0000946 secs]2.105: [scrub symbol & string tables, 0.0003146 secs] [1 CMS-remark: 81230K(159744K)] 81260K(395712K), 0.0013199 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), input, GcLogType.SUN1_6);
        GCModel gcModel = reader.read();

        assertEquals("GC count", 2, gcModel.size());
        assertEquals("1st event", "GC; ParNew", gcModel.get(0).getTypeAsString());
        assertEquals("1st event pause", 0.0006030, ((GCEvent)gcModel.get(0)).getPause(), 0.00000001);
        assertEquals("2nd event", "GC; CMS-remark", gcModel.get(1).getTypeAsString());
        assertEquals("2nd event pause", 0.0013199 - 0.0006030, ((GCEvent)gcModel.get(1)).getPause(), 0.00000001);
    }

    @Test
    public void testPSWithoutPrintTimeStamp() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2012-04-03T20:35:40.033+0200: [GC [PSYoungGen: 16420K->2657K(19136K)] 16420K->15887K(62848K), 0.0143603 secs] [Times: user=0.02 sys=0.02, real=0.01 secs]" +
                 "\n2012-04-03T20:35:40.056+0200: [GC [PSYoungGen: 19084K->2657K(35584K)] 32314K->32279K(79296K), 0.0236295 secs] [Times: user=0.01 sys=0.00, real=0.02 secs]" +
                 "\n2012-04-03T20:35:40.079+0200: [Full GC [PSYoungGen: 2657K->0K(35584K)] [PSOldGen: 29622K->32262K(67392K)] 32279K->32262K(102976K) [PSPermGen: 2603K->2603K(21248K)], 0.0095147 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 3, model.size());
        assertEquals("heap", 102976, model.getHeapAllocatedSizes().getMax());
        assertEquals("pause", 0.0236295, model.getGCPause().getMax(), 0.00000001);
        assertEquals("2nd pause, timeStamp", 0.056 - 0.033, ((GCEvent)model.get(1)).getTimestamp(), 0.00001);
        assertEquals("3nd pause, timeStamp", 0.079 - 0.033, ((GCEvent)model.get(2)).getTimestamp(), 0.00001);
    }

    @Test
    public void testCMSWithoutPrintTimeStamp() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2012-04-03T20:36:35.035+0200: [GC [ParNew: 16993K->2105K(19136K), 0.0270541 secs] 16993K->16424K(83008K), 0.0272020 secs] [Times: user=0.02 sys=0.05, real=0.03 secs]")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("heap", 83008, model.getHeapAllocatedSizes().getMax());
        assertEquals("pause", 0.0272020, model.getGCPause().getMax(), 0.00000001);
    }

    @Test
    public void testCMSWithoutPrintTimeStampConcurrentModeFailure() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2012-04-03T20:36:35.284+0200: [GC [ParNew: 19081K->19081K(19136K), 0.0000205 secs][CMS2012-04-03T20:36:35.285+0200: [CMS-concurrent-abortable-preclean: 0.005/0.150 secs] [Times: user=0.14 sys=0.14, real=0.15 secs]"
                 + "\n (concurrent mode failure): 98182K->3832K(98624K), 0.0195864 secs] 117264K->3832K(117760K), [CMS Perm : 2614K->2613K(21248K)], 0.0199322 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());
        assertEquals("heap", 117760, model.getHeapAllocatedSizes().getMax());
        assertEquals("pause", 0.0199322, model.getFullGCPause().getMax(), 0.00000001);
    }

    /**
     * Tests -XX:+PrintTenuringDistribution with -XX:+UseParallelGC
     */
    @Test
    public void testPSPrintTenuringDistribution() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2012-04-10T20:58:43.009+0200: 0.690: [GC"
                 + "\nDesired survivor size 89456640 bytes, new threshold 7 (max 15)"
                 + "\n [PSYoungGen: 524288K->35633K(611648K)] 524288K->35633K(2009792K), 0.0240717 secs] [Times: user=0.01 sys=0.03, real=0.02 secs]")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("heap", 2009792, model.getHeapAllocatedSizes().getMax());
        assertEquals("pause", 0.0240717, model.getGCPause().getMax(), 0.00000001);
    }

    @Test
    public void testCMSAdaptiveSizePolicy() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource("SampleSun1_6_0CMSAdaptiveSizePolicy.txt");
        gcResource.getLogger().addHandler(handler);
        
        InputStream in = getInputStream(gcResource.getResourceName());
        DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_6);
        
        GCModel model = reader.read();

        assertEquals("event count", 24, model.size());
        assertEquals("young gc count", 11, model.getGCPause().getN());
        assertEquals("full gc count", 1, model.getFullGCPause().getN());
        assertEquals("number of errors", 0, handler.getCount());

    }

    @Test
    public void testCMSAdaptiveSizePolicyPrintHeapAtGC() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource("byteArray");
        gcResource.getLogger().addHandler(handler);
        
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2012-04-18T14:48:31.855+0200: 29.592: [GC 29.592: [ASParNew: 52825K->6499K(59008K), 0.0268761 secs] 120805K->120749K(517760K), 0.0269605 secs] [Times: user=0.05 sys=0.00, real=0.03 secs]"
                 + "\nHeap"
                 + "\nadaptive size par new generation total 59008K, used 15368K [0x00000000d8000000, 0x00000000dc000000, 0x00000000dc000000)"
                 + "\n eden space 52480K,  16% used [0x00000000d8000000, 0x00000000d88a95a0, 0x00000000db340000)"
                 + "\n from space 6528K,  99% used [0x00000000db340000, 0x00000000db998cb0, 0x00000000db9a0000)"
                 + "\n to   space 6528K,   0% used [0x00000000db9a0000, 0x00000000db9a0000, 0x00000000dc000000)"
                 + "\nconcurrent mark-sweep generation total 458752K, used 259541K [0x00000000dc000000, 0x00000000f8000000, 0x00000000f8000000)"
                 + "\nconcurrent-mark-sweep perm gen total 65536K, used 2621K [0x00000000f8000000, 0x00000000fc000000, 0x0000000100000000)")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("GC pause", 0.0269605, model.getGCPause().getMin(), 0.000000001);
        assertEquals("number of errors", 0, handler.getCount());
    }

    @Test
    public void testPrintCmsStatisticsConcurrentMark() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2012-10-20T18:04:57.850+0200: 1.327: [CMS-concurrent-mark: 0.011/0.011 secs] (CMS-concurrent-mark yielded 0 times)"
                 + "\n [Times: user=0.03 sys=0.00, real=0.01 secs]")
                       .getBytes());
        
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("GC pause", 0.011, model.getConcurrentEventPauses().values().iterator().next().getMin(), 0.000000001);
    }

    @Test
    public void testPrintCmsStatisticsConcurrentPreclean() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2012-10-20T18:04:57.850+0200: 1.329: [CMS-concurrent-preclean: 0.002/0.002 secs] (CMS-concurrent-preclean yielded 0 times)"
                 + "\n [Times: user=0.00 sys=0.00, real=0.00 secs]")
                       .getBytes());
        
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("concurrent time", 0.002, model.getConcurrentEventPauses().values().iterator().next().getMin(), 0.000000001);
    }

    @Test
    public void testPrintCmsStatisticsConcurrentModeFailure() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2012-10-20T18:04:58.147+0200: 1.621: [GC 1.621: [ParNew: 78631K->78631K(78656K), 0.0000164 secs]1.621: [CMS [9 iterations, 5 waits, 5367 cards)] 2012-10-20T18:04:58.147+0200: 1.621: [CMS-concurrent-abortable-preclean: 0.020/0.292 secs] (CMS-concurrent-abortable-preclean yielded 0 times)"
                 + "\n [Times: user=0.30 sys=0.23, real=0.30 secs]"
                 + "\n (concurrent mode failure): 402265K->61915K(436928K), 0.0394420 secs] 480896K->61915K(515584K), [CMS Perm : 2626K->2625K(65536K)], 0.0395686 secs] [Times: user=0.03 sys=0.00, real=0.03 secs]")
                       .getBytes());
        
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());
        assertEquals("failure pause", 0.0395686, model.getFullGCPause().getMax(), 0.0000001);
        assertEquals("concurrent time", 0.02, model.getConcurrentEventPauses().values().iterator().next().getMin(), 0.000000001);
    }

    @Test
    public void testPrintCmsStatisticsRemark() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2012-10-20T18:04:59.443+0200: 2.918: [GC[YG occupancy: 8752 K (78656 K)]2.918: [Rescan (parallel)  (Survivor:1chunks) Finished young gen rescan work in 2th thread: 0.000 sec"
                 + "\nFinished young gen rescan work in 1th thread: 0.000 sec"
                 + "\nFinished young gen rescan work in 0th thread: 0.000 sec"
                 + "\nFinished remaining root rescan work in 1th thread: 0.000 sec"
                 + "\nFinished remaining root rescan work in 2th thread: 0.000 sec"
                 + "\nFinished remaining root rescan work in 0th thread: 0.000 sec"
                 + "\nFinished dirty card rescan work in 0th thread: 0.001 sec"
                 + "\nFinished dirty card rescan work in 2th thread: 0.001 sec"
                 + "\nFinished dirty card rescan work in 1th thread: 0.001 sec"
                 + "\nFinished young gen rescan work in 3th thread: 0.000 sec"
                 + "\nFinished remaining root rescan work in 3th thread: 0.000 sec"
                 + "\nFinished dirty card rescan work in 3th thread: 0.000 sec"
                 + "\nFinished work stealing in 3th thread: 0.000 sec"
                 + "\nFinished work stealing in 2th thread: 0.000 sec"
                 + "\nFinished work stealing in 0th thread: 0.000 sec"
                 + "\nFinished work stealing in 1th thread: 0.000 sec"
                 + "\n, 0.0008918 secs]2.919: [weak refs processing, 0.0000057 secs]2.919: [class unloading, 0.0001020 secs]2.919: [scrub symbol & string tables, 0.0003265 secs] [1 CMS-remark: 376134K(436928K)] 384886K(515584K), 0.0014952 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]"
                 )
                       .getBytes());
        
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("GC pause", 0.0014952, model.getGCPause().getMin(), 0.000000001);
    }

    /**
     * Tests the combination of -XX:PrintCmsStatistics=2 and -XX:+CMSScavengeBeforeRemark
     */
    @Test
    public void testPrintCmsStatisticsScavengeBeforeRemark() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2012-10-26T18:31:09.699+0200: 15.473: [GC[YG occupancy: 8752 K (78656 K)]2012-10-26T18:31:09.699+0200: 15.473: [GC 15.473: [ParNew: 8752K->64K(78656K), 0.0052352 secs] 388874K->388870K(515584K), 0.0052868 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]"
                 + "\n15.478: [Rescan (parallel)  (Survivor:0chunks) Finished young gen rescan work in 1th thread: 0.000 sec"
                 + "\nFinished young gen rescan work in 1th thread: 0.000 sec"
                 + "\nFinished young gen rescan work in 0th thread: 0.000 sec"
                 + "\nFinished remaining root rescan work in 1th thread: 0.000 sec"
                 + "\nFinished remaining root rescan work in 2th thread: 0.000 sec"
                 + "\nFinished remaining root rescan work in 0th thread: 0.000 sec"
                 + "\nFinished dirty card rescan work in 0th thread: 0.001 sec"
                 + "\nFinished dirty card rescan work in 2th thread: 0.001 sec"
                 + "\nFinished dirty card rescan work in 1th thread: 0.001 sec"
                 + "\nFinished young gen rescan work in 3th thread: 0.000 sec"
                 + "\nFinished remaining root rescan work in 3th thread: 0.000 sec"
                 + "\nFinished dirty card rescan work in 3th thread: 0.000 sec"
                 + "\nFinished work stealing in 3th thread: 0.000 sec"
                 + "\nFinished work stealing in 2th thread: 0.000 sec"
                 + "\nFinished work stealing in 0th thread: 0.000 sec"
                 + "\nFinished work stealing in 1th thread: 0.000 sec"
                 + "\n, 0.0006571 secs]15.479: [weak refs processing, 0.0000041 secs]15.479: [class unloading, 0.0001106 secs]15.479: [scrub symbol table, 0.0004465 secs]15.480: [scrub string table, 0.0000168 secs] [1 CMS-remark: 388806K(436928K)] 388870K(515584K), 0.0067111 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]"
                 )
                       .getBytes());
        
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());
        GCEvent parNew = (GCEvent) model.get(0);
        GCEvent remarkEvent = (GCEvent) model.get(1);

        assertEquals("GC pause ParNew", 0.0052868, parNew.getPause(), 0.000000001);
        assertEquals("GC pause Remark", 0.0067111 - 0.0052868, remarkEvent.getPause(), 0.000000001);

    }

    @Test
    public void testPrintCmsStatisticsParNewBeforeRemark() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2012-10-20T18:04:59.412+0200: 2.897: [GC 2.897: [ParNew [15 iterations, 8 waits, 4115 cards)] 2012-10-20T18:04:59.443+0200: 2.917: [CMS-concurrent-abortable-preclean: 0.031/0.593 secs] (CMS-concurrent-abortable-preclean yielded 0 times)"
                 + "\n [Times: user=0.66 sys=0.00, real=0.59 secs]"
                 + "\n: 78624K->8686K(78656K), 0.0203494 secs] 424271K->384820K(515584K), 0.0204543 secs] [Times: user=0.13 sys=0.00, real=0.03 secs]")
                       .getBytes());
        
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());
        assertEquals("GC pause", 0.0204543, model.getGCPause().getMax(), 0.0000001);
        assertEquals("concurrent time", 0.031, model.getConcurrentEventPauses().values().iterator().next().getMin(), 0.000000001);
    }

    @Test
    public void testPrintCmsStatisticsParNewBeforeRemark2() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2012-10-20T18:05:00.083+0200: 3.556: [GC 3.556: [ParNew: 78624K->8688K(78656K), 0.0302267 secs] 451513K->438445K(515584K), 0.0302972 secs] [Times: user=0.13 sys=0.00, real=0.03 secs]"
                 + "\n [11 iterations, 6 waits, 7020 cards)] 2012-10-20T18:05:00.114+0200: 3.587: [CMS-concurrent-abortable-preclean: 0.024/0.391 secs] (CMS-concurrent-abortable-preclean yielded 0 times)"
                 + "\n [Times: user=0.61 sys=0.06, real=0.39 secs]")
                       .getBytes());
        
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());
        assertEquals("GC pause", 0.0302972, model.getGCPause().getMax(), 0.0000001);
        assertEquals("concurrent time", 0.024, model.getConcurrentEventPauses().values().iterator().next().getMin(), 0.000000001);
    }

    @Test
    public void testCommaInTimestamp() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                "12,655: [GC [PSYoungGen: 262656K->28075K(306432K)] 262656K->28075K(1006848K), 0,3541657 secs] [Times: user=0,22 sys=0,48, real=0,35 secs]"
                       .getBytes());
        
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("GC pause", 0.3541657, model.getGCPause().getMax(), 0.0000001);
        assertEquals("GC timestamp", 12.655, model.get(0).getTimestamp(), 0.000001);
    }

    /**
     * Often only the young generation information is explicitly present. Old generation memory
     * size can be derived from heap - young size. This test checks for presence of derived memory
     * information.
     */
    @Test
    public void testDerivedGenerationValues() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                "10.675: [GC [PSYoungGen: 21051K->4947K(22656K)] 23342K->7238K(67712K), 0.0191817 secs] [Times: user=0.09 sys=0.01, real=0.02 secs]"
                       .getBytes());
        
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("young used", 21051, model.getYoungUsedSizes().getMin());
        assertEquals("young allocated", 22656, model.getYoungAllocatedSizes().getMax());
        assertEquals("tenured used", 23342-21051, model.getTenuredUsedSizes().getMin());
        assertEquals("tenured allocated", 67712-22656, model.getTenuredAllocatedSizes().getMax());
    }

    @Test
    public void testPrintTenuringDistributionOpenJdk6() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                "3.141: [GCDesired survivor size 134217728 bytes, new threshold 7 (max 2) [PSYoungGen: 188744K->13345K(917504K)] 188744K->13345K(4063232K), 0.0285820 secs] [Times: user=0.06 sys=0.01, real=0.03 secs]"
                       .getBytes());
        
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("count", 1, model.size());
        assertEquals("main type", "GC", model.get(0).getExtendedType().getName());
        assertEquals("detail type", "PSYoungGen", model.get(0).details().next().getExtendedType().getName());
    }

    @Test
    public void testPromotionFailedWithReference() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                "2013-09-02T18:22:23.195+0000: 1837.498: [GC1837.600: [SoftReference, 0 refs, 0.0000060 secs]1837.600: [WeakReference, 374 refs, 0.0000550 secs]1837.600: [FinalReference, 347 refs, 0.0002090 secs]1837.600: [PhantomReference, 0 refs, 0.0000020 secs]1837.600: [JNI Weak Reference, 0.0000050 secs]-- [PSYoungGen: 1330796K->1330796K(1403264K)] 4172533K->4251241K(4323712K), 0.6580190 secs] [Times: user=2.35 sys=0.02, real=0.66 secs]"
                       .getBytes());
        
        DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("count", 1, model.size());
        assertEquals("main type", "GC--", model.get(0).getExtendedType().getName());
        assertEquals("detail type", "PSYoungGen", model.get(0).details().next().getExtendedType().getName());
    }

    @Test
    public void testCmsGcLocker() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource("byteArray");
        gcResource.getLogger().addHandler(handler);
        
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2269.664: [CMS-concurrent-sweep-start]"
                + "\n2270.039: [GC 2270.039: [ParNew: 3686400K->3686400K(3686400K), 0.0000270 secs] 19876932K->19876932K(20070400K), 0.0000980 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]"
                + "\nGC locker: Trying a full collection because scavenge failed"
                + "\n2270.039: [Full GC 2270.039: [CMS2281.247: [CMS-concurrent-sweep: 11.558/11.583 secs] [Times: user=13.89 sys=0.08, real=11.58 secs]"
                + "\n (concurrent mode failure): 16190532K->14091936K(16384000K), 64.4965310 secs] 19876932K->14091936K(20070400K), [CMS Perm : 111815K->111803K(262144K)], 64.4966380 secs] [Times: user=64.41 sys=0.00, real=64.50 secs]"
                + "\n2334.567: [GC [1 CMS-initial-mark: 14091936K(16384000K)] 14164705K(20070400K), 0.0180200 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]"
                + "\n2334.587: [CMS-concurrent-mark-start]"
                       ).getBytes());
        
        DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertThat("count", model.size(), is(6));
        assertThat("parse warning count", handler.getCount(), is(0));
    }

    @Test
    public void cmsConcurrentModeFailureWithComma() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource("byteArray");
        gcResource.getLogger().addHandler(handler);

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("163914,315: [GC 163914,316: [ParNew (promotion failed): 717051K->680031K(755008K), 2,6660558 secs]163916,982: [CMS163928,452: [CMS-concurrent-sweep: 20,622/86,301 secs] [Times: user=380,61 sys=2,63, real=86,30 secs]"
                        + "\n (concurrent mode failure): 2874510K->2846370K(3355456K), 104,9219455 secs] 3591303K->2846370K(4110464K), [CMS Perm : 155672K->155662K(259600K)] icms_dc=13 , 107,5896657 secs] [Times: user=108,25 sys=0,10, real=107,59 secs]"
                ).getBytes());

        DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertThat("count", model.size(), is(2));
        assertThat("1st event", model.get(0).getTypeAsString(), equalTo("CMS-concurrent-sweep"));
        assertThat("2nd event", model.get(1).getTypeAsString(), equalTo("GC; ParNew (promotion failed); CMS (concurrent mode failure); CMS Perm"));
        assertThat("parse warning count", handler.getCount(), is(0));
    }

}
