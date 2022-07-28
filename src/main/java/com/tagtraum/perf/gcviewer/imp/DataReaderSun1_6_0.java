package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.CollectionType;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Concurrency;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.ExtendedType;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.GcPattern;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.VmOperationEvent;
import com.tagtraum.perf.gcviewer.util.NumberParser;
import com.tagtraum.perf.gcviewer.util.ParseInformation;

/**
 * Parses log output from Sun / Oracle Java 1.4 / 1.5 / 1.6. / 1.7 / 1.8.
 * <p>
 * Supports the following gc algorithms:
 * <ul>
 * <li>-XX:+UseSerialGC</li>
 * <li>-XX:+UseParallelGC</li>
 * <li>-XX:+UseParNewGC</li>
 * <li>-XX:+UseParallelOldGC</li>
 * <li>-XX:+UseConcMarkSweepGC</li>
 * <li>-XX:+UseShenandoahGC</li>
 * <li>-Xincgc (1.4 / 1.5)</li>
 * </ul>
 * <p>
 * -XX:+UseG1GC is not supported by this class, but by {@link DataReaderSun1_6_0G1}
 * <p>
 * Supports the following options:
 * <ul>
 * <li>-XX:+PrintGCDetails</li>
 * <li>-XX:+PrintGCTimeStamps</li>
 * <li>-XX:+PrintGCDateStamps</li>
 * <li>-XX:+CMSScavengeBeforeRemark</li>
 * <li>-XX:+PrintGCApplicationStoppedTime</li>
 * <li>-XX:+PrintHeapAtGC (output ignored)</li>
 * <li>-XX:+PrintTenuringDistribution (output ignored)</li>
 * <li>-XX:+PrintAdaptiveSizePolicy (output ignored)</li>
 * <li>-XX:+PrintPromotionFailure (output ignored)</li>
 * <li>-XX:+PrintGCApplicationConcurrentTime (output ignored)</li>
 * <li>-XX:PrintCMSStatistics=2 (output ignored)</li>
 * <li>-XX:+PrintReferenceGC (output ignored)</li>
 * <li>-XX:+PrintCMSInitiationStatistics (output ignored)</li>
 * <li>-XX:+PrintFLSStatistics (output ignored)</li>
 * </ul>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * @see DataReaderSun1_6_0G1
 */
public class DataReaderSun1_6_0 extends AbstractDataReaderSun {

    private static final String UNLOADING_CLASS = "[Unloading class ";
    private static final String APPLICATION_TIME = "Application time:";
    private static final String BEFORE_GC = "Before GC:"; // -XX:+PrintFLSStatistics=1
    private static final String AFTER_GC = "After GC:"; // -XX:+PrintFLSStatistics=1
    private static final String CMS_LARGE_BLOCK = "CMS: Large "; // -XX:+PrintFLSStatistics=1
    private static final String SIZE = "size["; // -XX:+PrintFLSStatistics=1
    private static final String TIMES = " [Times";
    private static final String ADAPTIVE_PATTERN = "AdaptiveSize";

    /** lines to be excluded, if they start with a string of this list */
    private static final List<String> EXCLUDE_STRINGS_LINE_START = new LinkedList<String>();
    /** lines to be excluded, if they contain a string of this list */
    private static final List<String> EXCLUDE_STRINGS_LINE_CONTAIN = new LinkedList<String>();

    static {
        EXCLUDE_STRINGS_LINE_START.add(UNLOADING_CLASS);
        EXCLUDE_STRINGS_LINE_START.add(APPLICATION_TIME); // -XX:+PrintGCApplicationConcurrentTime
        //EXCLUDE_STRINGS.add(Type.APPLICATION_STOPPED_TIME.getName());  // -XX:+PrintGCApplicationStoppedTime
        EXCLUDE_STRINGS_LINE_START.add("Desired survivor"); // -XX:+PrintTenuringDistribution
        EXCLUDE_STRINGS_LINE_START.add("- age"); // -XX:+PrintTenuringDistribution
        EXCLUDE_STRINGS_LINE_START.add(TIMES);
        EXCLUDE_STRINGS_LINE_START.add("Finished"); // -XX:PrintCmsStatistics=2
        EXCLUDE_STRINGS_LINE_START.add(" (cardTable: "); // -XX:PrintCmsStatistics=2
        EXCLUDE_STRINGS_LINE_START.add("GC locker: Trying a full collection because scavenge failed");
        EXCLUDE_STRINGS_LINE_START.add("CMSCollector"); // -XX:+PrintCMSInitiationStatistics
        EXCLUDE_STRINGS_LINE_START.add("time_until_cms_gen_full"); // -XX:+PrintCMSInitiationStatistics
        EXCLUDE_STRINGS_LINE_START.add("free"); // -XX:+PrintCMSInitiationStatistics
        EXCLUDE_STRINGS_LINE_START.add("contiguous_available"); // -XX:+PrintCMSInitiationStatistics
        EXCLUDE_STRINGS_LINE_START.add("promotion_rate"); // -XX:+PrintCMSInitiationStatistics
        EXCLUDE_STRINGS_LINE_START.add("cms_allocation_rate"); // -XX:+PrintCMSInitiationStatistics
        EXCLUDE_STRINGS_LINE_START.add("occupancy"); // -XX:+PrintCMSInitiationStatistics
        EXCLUDE_STRINGS_LINE_START.add("initiating"); // -XX:+PrintCMSInitiationStatistics
        EXCLUDE_STRINGS_LINE_START.add("Statistics"); // -XX:+PrintFLSStatistics=1
        EXCLUDE_STRINGS_LINE_START.add("----------------"); // -XX:+PrintFLSStatistics=1
        EXCLUDE_STRINGS_LINE_START.add("Total Free Space:"); // -XX:+PrintFLSStatistics=1
        EXCLUDE_STRINGS_LINE_START.add("Max   Chunk Size:"); // -XX:+PrintFLSStatistics=1
        EXCLUDE_STRINGS_LINE_START.add("Number of Blocks:"); // -XX:+PrintFLSStatistics=1
        EXCLUDE_STRINGS_LINE_START.add("Av.  Block  Size:"); // -XX:+PrintFLSStatistics=1
        EXCLUDE_STRINGS_LINE_START.add("Tree      Height:"); // -XX:+PrintFLSStatistics=1
        EXCLUDE_STRINGS_LINE_START.add(BEFORE_GC); // -XX:+PrintFLSStatistics=1
        EXCLUDE_STRINGS_LINE_START.add(AFTER_GC); // -XX:+PrintFLSStatistics=1
        EXCLUDE_STRINGS_LINE_START.add(CMS_LARGE_BLOCK); // -XX:+PrintFLSStatistics=1
        EXCLUDE_STRINGS_LINE_START.add(" free"); // -XX:+PrintFLSStatistics=2
        EXCLUDE_STRINGS_LINE_START.add(SIZE); // -XX:+PrintFLSStatistics=2
        EXCLUDE_STRINGS_LINE_START.add("demand"); // -XX:+PrintFLSStatistics=2
        EXCLUDE_STRINGS_LINE_START.add(ADAPTIVE_PATTERN); // -XX:+PrintAdaptiveSizePolicy
        EXCLUDE_STRINGS_LINE_START.add("PS" + ADAPTIVE_PATTERN); // -XX:PrintAdaptiveSizePolicy
        EXCLUDE_STRINGS_LINE_START.add("  avg_survived_padded_avg"); // -XX:PrintAdaptiveSizePolicy
        EXCLUDE_STRINGS_LINE_START.add("/proc/meminfo"); // apple vms seem to print this out in the beginning of the logs
        EXCLUDE_STRINGS_LINE_START.add("Uncommitted"); // -XX:+UseShenandoahGC
        EXCLUDE_STRINGS_LINE_START.add("Cancelling concurrent GC"); // -XX:+UseShenandoahGC
        EXCLUDE_STRINGS_LINE_START.add("Capacity"); // -XX:+UseShenandoahGC -XX:+PrintGCDetails
        EXCLUDE_STRINGS_LINE_START.add("Periodic GC triggered"); // -XX:+UseShenandoahGC -XX:+PrintGCDetails
        EXCLUDE_STRINGS_LINE_START.add("Immediate Garbage"); // -XX:+UseShenandoahGC -XX:+PrintGCDetails
        EXCLUDE_STRINGS_LINE_START.add("Garbage to be collected"); // -XX:+UseShenandoahGC -XX:+PrintGCDetails
        EXCLUDE_STRINGS_LINE_START.add("Live"); // -XX:+UseShenandoahGC -XX:+PrintGCDetails
        EXCLUDE_STRINGS_LINE_START.add("Concurrent marking triggered"); // -XX:+UseShenandoahGC -XX:+PrintGCDetails
        EXCLUDE_STRINGS_LINE_START.add("Adjusting free threshold"); // -XX:+UseShenandoahGC
        EXCLUDE_STRINGS_LINE_START.add("Predicted cset threshold"); // -XX:+UseShenandoahGC
        EXCLUDE_STRINGS_LINE_START.add("Trigger"); // -XX:+UseShenandoahGC
        EXCLUDE_STRINGS_LINE_START.add("Free"); // -XX:+UseShenandoahGC
        EXCLUDE_STRINGS_LINE_START.add("Evacuation Reserve"); // -XX:+UseShenandoahGC
        EXCLUDE_STRINGS_LINE_START.add("Pacer for "); // -XX:+UseShenandoahGC
        EXCLUDE_STRINGS_LINE_START.add("    Using"); // -XX:+UseShenandoahGC
        EXCLUDE_STRINGS_LINE_START.add("    Pacer for "); // -XX:+UseShenandoahGC
        EXCLUDE_STRINGS_LINE_START.add("    Adaptive CSet Selection"); // -XX:+UseShenandoahGC
        EXCLUDE_STRINGS_LINE_START.add("    Collectable Garbage"); // -XX:+UseShenandoahGC
        EXCLUDE_STRINGS_LINE_START.add("    Immediate Garbage"); // -XX:+UseShenandoahGC

        EXCLUDE_STRINGS_LINE_CONTAIN.add(LOGFILE_ROLLING_BEGIN);
        EXCLUDE_STRINGS_LINE_CONTAIN.add(LOGFILE_ROLLING_END);
        // when it occurs including timestamp (since about jdk1.7.0_50) it should still be ignored
        EXCLUDE_STRINGS_LINE_CONTAIN.add(APPLICATION_TIME); // -XX:+PrintGCApplicationConcurrentTime
        EXCLUDE_STRINGS_LINE_CONTAIN.add(", start"); // -XX:+UseShenandoahGC

        LOG_INFORMATION_STRINGS.add("Region"); // -XX:+UseShenandoahGC
        LOG_INFORMATION_STRINGS.add("Humongous threshold"); // -XX:+UseShenandoahGC
        LOG_INFORMATION_STRINGS.add("Number of regions"); // -XX:+UseShenandoahGC
        LOG_INFORMATION_STRINGS.add("Shenandoah heuristics"); // -XX:+UseShenandoahGC
        LOG_INFORMATION_STRINGS.add("Parallel GC threads");// -XX:+UseShenandoahGC
        LOG_INFORMATION_STRINGS.add("Concurrent GC threads"); // -XX:+UseShenandoahGC
        LOG_INFORMATION_STRINGS.add("Parallel reference processing"); // -XX:+UseShenandoahGC
        LOG_INFORMATION_STRINGS.add("Humongous object threshold"); // -XX:+UseShenandoahGC
        LOG_INFORMATION_STRINGS.add("Max TLAB size"); // -XX:+UseShenandoahGC
        LOG_INFORMATION_STRINGS.add("GC threads"); // -XX:+UseShenandoahGC
        LOG_INFORMATION_STRINGS.add("Reference processing"); // -XX:+UseShenandoahGC
        LOG_INFORMATION_STRINGS.add("Heuristics ergonomically sets"); // -XX:+UseShenandoahGC
        LOG_INFORMATION_STRINGS.add("Initialize Shenandoah heap"); // -XX:+UseShenandoahGC
    }

    private static final String EVENT_YG_OCCUPANCY = "YG occupancy";
    private static final String EVENT_PARNEW = "ParNew";
    private static final String EVENT_DEFNEW = "DefNew";

    private static final String CMS_ABORT_PRECLEAN = " CMS: abort preclean due to time ";

    private static final String HEAP = "Heap";
    private static final String HEAP_SIZING_BEFORE = HEAP + " before";
    private static final String HEAP_SIZING_AFTER = HEAP + " after";

    private static final List<String> HEAP_STRINGS = new LinkedList<String>();
    static {
        HEAP_STRINGS.add(HEAP); // java 6 and earlier -XX:+PrintHeapAtGC
        HEAP_STRINGS.add("def new generation"); // serial young collection -XX:+UseSerialGC
        HEAP_STRINGS.add("PSYoungGen"); // parallel young collection -XX:+UseParallelGC
        HEAP_STRINGS.add("par new generation"); // parallel young (CMS / -XX:+UseParNewGC)
        HEAP_STRINGS.add("adaptive size par new generation"); // parallel young (CMS / -XX:+UseParNewGC / -XX:+PrintAdaptiveSizePolicy)
        HEAP_STRINGS.add("eden");
        HEAP_STRINGS.add("from");
        HEAP_STRINGS.add("to");

        HEAP_STRINGS.add("ParOldGen"); // parallel old collection -XX:+UseParallelOldGC
        HEAP_STRINGS.add("PSOldGen"); // serial old collection -XX:+UseParallelGC without -XX:+UseParallelOldGC
        HEAP_STRINGS.add("object");
        HEAP_STRINGS.add("PSPermGen"); // serial (?) perm collection
        HEAP_STRINGS.add("tenured generation"); // serial old collection -XX:+UseSerialGC
        HEAP_STRINGS.add("the space");
        HEAP_STRINGS.add("ro space");
        HEAP_STRINGS.add("rw space");
        HEAP_STRINGS.add("compacting perm gen"); // serial perm collection -XX:+UseSerialGC
        HEAP_STRINGS.add("concurrent mark-sweep generation total"); // CMS old collection
        HEAP_STRINGS.add("concurrent-mark-sweep perm gen"); // CMS perm collection

        HEAP_STRINGS.add("Metaspace"); // java 8
        HEAP_STRINGS.add("data space"); // java 8
        HEAP_STRINGS.add("class space"); // java 8
        HEAP_STRINGS.add("No shared spaces configured.");

        HEAP_STRINGS.add("}");
    }

    // 1_6_0_u24 mixes lines, when outputing a "promotion failed" which leads to a "concurrent mode failure"
    // pattern looks always like "...[CMS<datestamp>..." or "...[CMS<timestamp>..."
    // the next line starts with " (concurrent mode failure)" which in earlier releases followed "CMS" immediately
    // the same can happen with "...ParNew<timestamp|datestamp>..."
    private static Pattern linesMixedPattern = Pattern.compile("(.*\\[(CMS|ParNew|DefNew|ASCMS|ASParNew))([0-9]+[-.,].*)");
    // Matcher group of start of line
    private static final int LINES_MIXED_STARTOFLINE_GROUP = 1;
    // Matcher group of end of line
    private static final int LINES_MIXED_ENDOFLINE_GROUP = 3;

    // -XX:+PrintAdaptiveSizePolicy combined with -XX:+UseAdaptiveSizePolicy outputs the following lines:
    // 0.175: [GCAdaptiveSizePolicy::compute_survivor_space_size_and_thresh:  survived: 2721008  promoted: 13580768  overflow: trueAdaptiveSizeStart: 0.186 collection: 1
    // PSAdaptiveSizePolicy::compute_generation_free_space: costs minor_time: 0.059538 major_cost: 0.000000 mutator_cost: 0.940462 throughput_goal: 0.990000 live_space: 273821824 free_space: 33685504 old_promo_size: 16842752 old_eden_size: 16842752 desired_promo_size: 16842752 desired_eden_size: 33685504
    // AdaptiveSizePolicy::survivor space sizes: collection: 1 (2752512, 2752512) -> (2752512, 2752512)
    // AdaptiveSizeStop: collection: 1
    //  [PSYoungGen: 16420K->2657K(19136K)] 16420K->15919K(62848K), 0.0109211 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
    // -> to parse it, the first line must be split, and the following left out until the rest of the gc information follows
    private static final String ADAPTIVE_SIZE_POLICY_PATTERN_STRING = "(.*GC \\([a-zA-Z ]*\\)|.*GC)(?:[0-9.,:]*.*)[ ]?AdaptiveSize.*";
    private static final Pattern adaptiveSizePolicyPattern = Pattern.compile(ADAPTIVE_SIZE_POLICY_PATTERN_STRING);

    // -XX:+PrintAdaptiveSizePolicy combined with -XX:-UseAdaptiveSizePolicy (not using the policy, just printing)
    // outputs the following line:
    // 0.222: [GCAdaptiveSizePolicy::compute_survivor_space_size_and_thresh:  survived: 2720992  promoted: 13613552  overflow: true [PSYoungGen: 16420K->2657K(19136K)] 16420K->15951K(62848K), 0.0132830 secs] [Times: user=0.00 sys=0.03, real=0.02 secs]
    private static final Pattern printAdaptiveSizePolicyPattern = Pattern.compile(ADAPTIVE_SIZE_POLICY_PATTERN_STRING + "(?:true|false)([ ]?\\[.*).*");
    private static final int PRINT_ADAPTIVE_SIZE_GROUP_BEFORE = 1;
    private static final int PRINT_ADAPTIVE_SIZE_GROUP_AFTER = 2;

    // -XX:PrintCmsStatistics=2
    private static final String PRINT_CMS_STATISTICS_ITERATIONS = "iterations";
    private static final Pattern printCmsStatisticsIterationsPattern = Pattern.compile("(.*)[ ][\\[][0-9]+[ ]iterations[, 0-9]+[ ]waits[, 0-9]+[ ]cards[)][\\]][ ](.*)");
    private static final int PRINT_CMS_STATISTICS_ITERATIONS_GROUP_BEFORE = 1;
    private static final int PRINT_CMS_STATISTICS_ITERATIONS_GROUP_AFTER = 2;
    private static final String PRINT_CMS_STATISTICS_SURVIVOR = "  (Survivor:";

    // -XX:+PrintTenuringDistribution in OpenJDK 1.6.0_22 (RHEL 64-bit)
    // outputs the following line (looks similar but not identical to what older versions of jdk1.5 wrote):
    // 3.141: [GCDesired survivor size 134217728 bytes, new threshold 7 (max 2) [PSYoungGen: 188744K->13345K(917504K)] 188744K->13345K(4063232K), 0.0285820 secs] [Times: user=0.06 sys=0.01, real=0.03 secs]
    // in JDK1.4 / 1.5 it looked like this:
    // 5.0: [GC Desired survivor size 3342336 bytes, new threshold 1 (max 32) - age   1:  6684672 bytes,  6684672 total 52471K->22991K(75776K), 1.0754938 secs]
    private static final String PRINT_TENURING_DISTRIBUTION = "Desired survivor size";
    private static final Pattern printTenuringDistributionPattern = Pattern.compile("(.*GC)[ ]?Desired.*(?:[0-9]\\)|total)( \\[.*|[ ][0-9]*.*)");
    private static final int PRINT_TENURING_DISTRIBUTION_PATTERN_GROUP_BEFORE = 1;
    private static final int PRINT_TENURING_DISTRIBUTION_PATTERN_GROUP_AFTER = 2;

    // -XX:+UseShenandoahGC -XX:+PrintGCDetails
    // 590327.714: [Pause Final MarkTotal Garbage: 57403M
    // Immediate Garbage: 32M, 2 regions (1% of total)
    // Garbage to be collected: 5134M (8% of total), 164 regions
    // Live objects to be evacuated: 113M
    // Live/garbage ratio in collected regions: 2%
    // 219G->219G(256G), 5.000 ms]
    private static final String SHENANDOAH_DETAILS_FINAL_MARK = "Final MarkTotal";
    private static final String SHENANDOAH_DETAILS_FINAL_MARK_SPLIT_START = "Total";

    private static final String SHENANDOAH_INTRODUCTION_TO_GC_STATISTICS = "Shenandoah Heap";

    // -XX:+PrintReferenceGC
    private static final String PRINT_REFERENCE_GC_INDICATOR = "Reference";

    // -XX:+CMSScavengeBeforeRemark JDK 1.5
    private static final String SCAVENGE_BEFORE_REMARK = Type.SCAVENGE_BEFORE_REMARK.getName();

    public DataReaderSun1_6_0(GCResource gcResource, InputStream in, GcLogType gcLogType) throws UnsupportedEncodingException {
        super(gcResource, in, gcLogType);
    }

    public GCModel read() throws IOException {
        if (getLogger().isLoggable(Level.INFO)) getLogger().info("Reading Sun / Oracle 1.4.x / 1.5.x / 1.6.x / 1.7.x / 1.8.x format...");

        try (LineNumberReader in = this.in) {
            GCModel model = new GCModel();
            model.setFormat(GCModel.Format.SUN_X_LOG_GC);
            Matcher mixedLineMatcher = linesMixedPattern.matcher("");
            Matcher adaptiveSizePolicyMatcher = adaptiveSizePolicyPattern.matcher("");
            Matcher printAdaptiveSizePolicyMatcher = printAdaptiveSizePolicyPattern.matcher("");
            Matcher printCmsStatisticsIterationsMatcher = printCmsStatisticsIterationsPattern.matcher("");
            Matcher printTenuringDistributionMatcher = printTenuringDistributionPattern.matcher("");
            String line;
            // beginningOfLine must be a stack because more than one beginningOfLine might be needed
            Deque<String> beginningOfLine = new LinkedList<String>();
            boolean lastLineWasScavengeBeforeRemark = false;
            boolean lineSkippedForScavengeBeforeRemark = false;
            boolean printTenuringDistributionOn = false;
            boolean isInFlsStatisticsBlock = false;
            ParseInformation parsePosition = new ParseInformation(0);

            while ((line = in.readLine()) != null && shouldContinue()) {
                parsePosition.setIndex(0);
                parsePosition.setLineNumber(in.getLineNumber());
                if ("".equals(line)) {
                    continue;
                }
                try {
                    printTenuringDistributionOn = false;
                    // filter out lines that don't need to be parsed
                    if (startsWith(line, EXCLUDE_STRINGS_LINE_START, false)) {
                        continue;
                    }
                    else if (contains(line, EXCLUDE_STRINGS_LINE_CONTAIN, false)) {
                        continue;
                    }
                    else if (startsWith(line, LOG_INFORMATION_STRINGS, false)) {
                        getLogger().info(line);
                        continue;
                    }

                    if (line.contains(SHENANDOAH_DETAILS_FINAL_MARK)) {
                        beginningOfLine.addFirst(line.substring(0, line.indexOf(SHENANDOAH_DETAILS_FINAL_MARK_SPLIT_START)));
                        continue;
                    } else if (line.startsWith(SHENANDOAH_INTRODUCTION_TO_GC_STATISTICS)) {
                        // Assumption: As soon as the shenandoah gc statistics block starts, the vm is shutting down
                        skipAndLogToEndOfFile(in);
                        continue;
                    }
                    if (line.indexOf(CMS_ABORT_PRECLEAN) >= 0) {
                        // line contains like " CMS: abort preclean due to time "
                        // -> remove the text
                        int indexOfStart = line.indexOf(CMS_ABORT_PRECLEAN);
                        StringBuilder sb = new StringBuilder(line);
                        sb.replace(indexOfStart, indexOfStart + CMS_ABORT_PRECLEAN.length(), "");
                        line = sb.toString();
                    }
                    if (line.indexOf(PRINT_CMS_STATISTICS_ITERATIONS) > 0) {
                        // -XX:PrintCmsStatistics -> filter text that the parser doesn't know
                        printCmsStatisticsIterationsMatcher.reset(line);
                        if (!printCmsStatisticsIterationsMatcher.matches()) {
                            getLogger().severe("printCmsStatisticsIterationsMatcher did not match for line " + in.getLineNumber() + ": '" + line + "'");
                            continue;
                        }

                        line = printCmsStatisticsIterationsMatcher.group(PRINT_CMS_STATISTICS_ITERATIONS_GROUP_BEFORE)
                                + printCmsStatisticsIterationsMatcher.group(PRINT_CMS_STATISTICS_ITERATIONS_GROUP_AFTER);
                    }
                    if (line.indexOf(PRINT_CMS_STATISTICS_SURVIVOR) > 0) {
                        String currentBeginning = "";
                        if (beginningOfLine.size() > 0) {
                            // if -XX:PrintCmsStatistics=2 is combined with -XX:+CMSScavengeBeforeRemark
                            // then a remark line is broken into three parts, which have to be glued together
                            currentBeginning = beginningOfLine.removeFirst();
                        }
                        beginningOfLine.addFirst(currentBeginning + line.substring(0, line.indexOf(PRINT_CMS_STATISTICS_SURVIVOR)));
                        continue;
                    }
                    if (line.indexOf(PRINT_TENURING_DISTRIBUTION) > 0) {
                        printTenuringDistributionMatcher.reset(line);
                        if (!printTenuringDistributionMatcher.matches()) {
                            getLogger().severe("printDistributionMatcher did not match for line " + in.getLineNumber() + ": '" + line + "'");
                            continue;
                        }

                        line = printTenuringDistributionMatcher.group(PRINT_TENURING_DISTRIBUTION_PATTERN_GROUP_BEFORE)
                                    + printTenuringDistributionMatcher.group(PRINT_TENURING_DISTRIBUTION_PATTERN_GROUP_AFTER);
                    }
                    if (line.indexOf(PRINT_REFERENCE_GC_INDICATOR) > 0) {
                        line = filterAwayReferenceGc(line);
                    }
                    if (isInFlsStatisticsBlock || lineHasPrintFlsStatistics(line)) {
                        isInFlsStatisticsBlock = handlePrintFlsStatistics(line, beginningOfLine, parsePosition, model, mixedLineMatcher);
                        continue;
                    }

                    int unloadingClassIndex = line.indexOf(UNLOADING_CLASS);
                    if (unloadingClassIndex > 0) {
                        beginningOfLine.addFirst(line.substring(0, unloadingClassIndex));
                        continue;
                    }
                    else if (isPrintTenuringDistribution(line)) {
                        // this is the case, when e.g. -XX:+PrintTenuringDistribution is used
                        // where we want to skip "Desired survivor..." and "- age..." lines
                        beginningOfLine.addFirst(line);
                        continue;
                    }
                    else if (line.indexOf(ADAPTIVE_PATTERN) >= 0) {
                        if (line.indexOf("Times") > 0) {
                            // -XX:+PrintAdaptiveSizePolicy -XX:-UseAdaptiveSizePolicy
                            printAdaptiveSizePolicyMatcher.reset(line);
                            if (!printAdaptiveSizePolicyMatcher.matches()) {
                                getLogger().severe("printAdaptiveSizePolicyMatcher did not match for line " + in.getLineNumber() + ": '" + line + "'");
                                continue;
                            }

                            model.add(parseLine(
                                    printAdaptiveSizePolicyMatcher.group(PRINT_ADAPTIVE_SIZE_GROUP_BEFORE)
                                        + printAdaptiveSizePolicyMatcher.group(PRINT_ADAPTIVE_SIZE_GROUP_AFTER),
                                    parsePosition));
                            parsePosition.setIndex(0);
                        }
                        else {
                            // -XX:+PrintAdaptiveSizePolicy
                            adaptiveSizePolicyMatcher.reset(line);
                            if (!adaptiveSizePolicyMatcher.matches()) {
                                getLogger().severe("adaptiveSizePolicyMatcher did not match for line " + in.getLineNumber() + ": '" + line + "'");
                                continue;
                            }
                            beginningOfLine.addFirst(adaptiveSizePolicyMatcher.group(1));
                        }
                        continue;
                    }
                    else if (isPrintHeapAtGcStarting(line)) {
                        // if -XX:+ScavengeBeforeRemark and -XX:+PrintHeapAtGC are combined, the following lines are common
                        // 2015-05-14T18:55:12.588+0200: 1.157: [GC (CMS Final Remark) [YG occupancy: 10451 K (47936 K)]{Heap before GC invocations=22 (full 13):
                        if (line.contains("]{" + HEAP)) {
                            beginningOfLine.add(line.substring(0, line.indexOf("{" + HEAP)));
                            lastLineWasScavengeBeforeRemark = true;
                        }

                        // the next few lines will be the sizing of the heap
                        skipLines(in, parsePosition, HEAP_STRINGS);
                        continue;
                    }
                    else if (beginningOfLine.size() > 0) {
                        // -XX:+CMSScavengeBeforeRemark combined with -XX:+PrintTenuringDistribution
                        // is the only case where beginningOfLine.size() > 1
                        printTenuringDistributionOn = beginningOfLine.size() == 2;
                        if (gcLogType == GcLogType.SUN1_5
                                && lastLineWasScavengeBeforeRemark
                                && ! lineSkippedForScavengeBeforeRemark) {

                            // -XX:+CMSScavengeBeforeRemark inserts a pause on its own line between
                            // the first and the second part of the enclosing remark event. Probably
                            // that is the duration of the Scavenge-Before-Remark event; this information
                            // will be dropped to reduce complexity of the parser at the cost of
                            // some accuracy in that case.
                            lineSkippedForScavengeBeforeRemark = true;
                            continue;
                        }
                        else {
                            line = beginningOfLine.removeFirst() + line;
                        }
                    }
                    if (isCmsScavengeBeforeRemark(line)) {
                        // This is the case, when option -XX:+CMSScavengeBeforeRemark is used.
                        // we have two events in the first line -> split it
                        // if this option is combined with -XX:+PrintTenuringDistribution, the
                        // first event is also distributed over more than one line
                        int startOf2ndEvent = line.indexOf("]", line.indexOf(EVENT_YG_OCCUPANCY)) + 1;
                        beginningOfLine.addFirst(line.substring(0, startOf2ndEvent));
                        if (!isPrintTenuringDistribution(line)) {
                            if (line.indexOf(SCAVENGE_BEFORE_REMARK) >= 0) {
                                // jdk1.5 scavenge before remark: just after another separate event occurs
                                startOf2ndEvent = line.indexOf(SCAVENGE_BEFORE_REMARK) + SCAVENGE_BEFORE_REMARK.length();
                            }
                            model.add(parseLine(line.substring(startOf2ndEvent), parsePosition));
                            parsePosition.setIndex(0);
                        }
                        else {
                            beginningOfLine.addFirst(line.substring(startOf2ndEvent));
                        }

                        lastLineWasScavengeBeforeRemark = true;
                        continue;
                    }

                    if (isMixedLine(line, mixedLineMatcher)) {
                        handleMixedLine(model, mixedLineMatcher, beginningOfLine, parsePosition);
                        parsePosition.setIndex(0);
                        continue;
                    }

                    AbstractGCEvent<?> gcEvent = parseLine(line, parsePosition);

                    if (lastLineWasScavengeBeforeRemark && !printTenuringDistributionOn) {
                         // according to http://mail.openjdk.java.net/pipermail/hotspot-gc-use/2012-August/001297.html
                         // the pause time reported for cms-remark includes the scavenge-before-remark time
                         // so it has to be corrected to show only the time spent in remark event
                         lastLineWasScavengeBeforeRemark = false;
                         lineSkippedForScavengeBeforeRemark = false;
                         AbstractGCEvent<?> scavengeBeforeRemarkEvent = model.get(model.size() - 1);
                         AbstractGCEvent<?> remarkEvent = gcEvent;
                         remarkEvent.setPause(remarkEvent.getPause() - scavengeBeforeRemarkEvent.getPause());
                     }

                     model.add(gcEvent);
                }
                catch (Exception pe) {
                    if (getLogger().isLoggable(Level.WARNING)) getLogger().warning(pe.toString());
                    if (getLogger().isLoggable(Level.FINE)) getLogger().log(Level.FINE, pe.getMessage(), pe);
                    beginningOfLine.clear();
                }
            }
            return model;
        }
        finally {
            if (getLogger().isLoggable(Level.INFO)) getLogger().info("Done reading.");
        }
    }

    private void skipAndLogToEndOfFile(LineNumberReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            getLogger().info(line);
        }
    }

    private boolean isPrintHeapAtGcStarting(String line) {
        return line.startsWith(HEAP) // jdk 6 and before
                || line.indexOf(HEAP_SIZING_BEFORE) >= 0 // jdk 7 and after
                || line.indexOf(HEAP_SIZING_AFTER) >= 0;
    }

    private void handleMixedLine(GCModel model,
                                 Matcher mixedLineMatcher,
                                 Deque<String> beginningOfLine,
                                 ParseInformation parsePosition) throws ParseException {

        // if PrintTenuringDistribution is used and a line is mixed,
        // beginningOfLine may already contain a value, which must be preserved
        String firstPartOfBeginningOfLine = beginningOfLine.pollFirst();
        if (firstPartOfBeginningOfLine == null) {
            firstPartOfBeginningOfLine = "";
        }
        beginningOfLine.addFirst(firstPartOfBeginningOfLine + mixedLineMatcher.group(LINES_MIXED_STARTOFLINE_GROUP));

        model.add(parseLine(mixedLineMatcher.group(LINES_MIXED_ENDOFLINE_GROUP), parsePosition));
    }

    private boolean lineHasPrintFlsStatistics(String line) {
        return line.endsWith(BEFORE_GC)
                || line.endsWith(AFTER_GC)
                || line.indexOf(CMS_LARGE_BLOCK) > 0
                || line.indexOf(SIZE) > 0;
    }

    private boolean handlePrintFlsStatistics(String line,
                                             Deque<String> beginningOfLine,
                                             ParseInformation parseInformation,
                                             GCModel model,
                                             Matcher mixedLinesMatcher) throws ParseException {

        boolean isInFlsStatsBlock = true;
        if (line.endsWith(BEFORE_GC)) {
            beginningOfLine.addFirst(line.substring(0, line.indexOf(BEFORE_GC)));
        }
        else if (line.endsWith(AFTER_GC)) {
            String beginning = beginningOfLine.removeFirst();
            beginningOfLine.addFirst(beginning + line.substring(0, line.indexOf(AFTER_GC)));
        }
        else if (line.indexOf(CMS_LARGE_BLOCK) > 0) {
            String beginning = beginningOfLine.removeFirst();
            beginningOfLine.addFirst(beginning + line.substring(0, line.indexOf(CMS_LARGE_BLOCK)));
        }
        else if (line.indexOf(SIZE) > 0) {
            String beginning = beginningOfLine.removeFirst();
            beginningOfLine.addFirst(beginning + line.substring(0, line.indexOf(SIZE)));
        }
        else if (isPrintTenuringDistribution(line)) {
            String beginning = beginningOfLine.removeFirst();
            beginningOfLine.addFirst(beginning + line);
        }
        else if (line.contains(TIMES)) {
            if (isMixedLine(line, mixedLinesMatcher)) {
                handleMixedLine(model, mixedLinesMatcher, beginningOfLine, parseInformation);
                parseInformation.setIndex(0);
            }
            else if (line.contains("concurrent-")) {
                // some concurrent event is mixed as a complete line inside the fls statistics block
                model.add(parseLine(line, parseInformation));
            }
            else {
                isInFlsStatsBlock = false;
                model.add(parseLine(beginningOfLine.removeFirst() + line, parseInformation));
            }
        }
        else {
            getLogger().warning("line should contain some known PrintFLSStatistics output, which it doesn't (" + line + ")");
        }

        return isInFlsStatsBlock;
    }

    private String filterAwayReferenceGc(String line) {
        int lastIndexOfReference = line.lastIndexOf(PRINT_REFERENCE_GC_INDICATOR);
        int endOfLastReference = line.indexOf("]", lastIndexOfReference) + 1;
        int index = findEndOfNextEventNameBefore(line, line.indexOf(PRINT_REFERENCE_GC_INDICATOR));

        return line.substring(0, index + 1) + line.substring(endOfLastReference);
    }

    private int findEndOfNextEventNameBefore(String line, int pos) {
        int index = line.lastIndexOf("[", pos) - 1;
        char ch = 0;
        do {
            ch = line.charAt(index--);
        }
        while (index >= 0
               && (ch == ' ' || Character.isDigit(ch) || ch == 'T'
                   || ch == '.' || ch == ':' || ch == '+' || ch == '-'));

        if (index < 0) {
            getLogger().warning("could not find name of event before " + pos);
            index = pos-1;
        }

        return index + 1;
    }

    private boolean isMixedLine(String line, Matcher mixedLineMatcher) {
        mixedLineMatcher.reset(line);
        return mixedLineMatcher.matches();
    }

    private boolean isPrintTenuringDistribution(String line) {
        return line.endsWith("[DefNew") // serial young (CMS, Serial GC)
                || line.endsWith("[ParNew") // parallel young (CMS, parallel GC)
                || line.endsWith(" (promotion failed)") // CMS (if -XX:+PrintPromotionFailure is active, additional text between "ParNew" + "(promotion failed)" is introduced...)
                || line.endsWith("[GC") // PSYoungGen (parallel sweep)
                || (line.contains("[GC (") && (line.endsWith(") ") || line.endsWith(")"))); // parallel GC (-XX:+PrintGCCause); ends actually with "[GC (Allocation Failure) ", but text in paranthesis can vary; there may be a " " in the end
    }

    private boolean isCmsScavengeBeforeRemark(String line) {
        return line.indexOf(EVENT_YG_OCCUPANCY) >= 0
                && (line.indexOf(EVENT_PARNEW) >= 0 || line.indexOf(EVENT_DEFNEW) >= 0);
    }

    protected AbstractGCEvent<?> parseLine(String line, ParseInformation pos) throws ParseException {
        try {
            // parse datestamp          "yyyy-MM-dd'T'hh:mm:ssZ:"
            // parse timestamp          "double:"
            // parse collection type    "[TYPE"
            // either GC data or another collection type starting with timestamp
            // pre-used->post-used, total, time
            ZonedDateTime datestamp = parseDatestamp(line, pos);
            double timestamp = getTimestamp(line, pos, datestamp);
            // Support for PrintGCID
            parseId(line, pos);
            ExtendedType type = parseType(line, pos);
            AbstractGCEvent<?> ae;
            if (type.getConcurrency() == Concurrency.CONCURRENT) {
                ae = new ConcurrentGCEvent();
            } else if (type.getCollectionType().equals(CollectionType.VM_OPERATION)) {
                ae = new VmOperationEvent();
            } else {
                ae = new GCEvent();
            }

            ae.setDateStamp(datestamp);
            ae.setTimestamp(timestamp);
            ae.setExtendedType(type);
            // now add detail gcevents, should they exist
            if (ae instanceof GCEvent) {
                parseDetailEventsIfExist(line, pos, (GCEvent) ae);
            }
            if (type.getPattern() == GcPattern.GC_MEMORY_PAUSE
                || type.getPattern() == GcPattern.GC_MEMORY) {

                setMemory(ae, line, pos);
            }
            // then more detail events follow (perm gen is usually here)
            if (ae instanceof GCEvent) {
                parseDetailEventsIfExist(line, pos, (GCEvent)ae);
            }
            if (type.getPattern() == GcPattern.GC_MEMORY_PAUSE
                    || type.getPattern() == GcPattern.GC_PAUSE) {

                ae.setPause(parsePause(line, pos));
            } else if (type.getPattern() == GcPattern.GC_PAUSE_DURATION) {
                // special case only occurring with concurrent collections...
                // the -end events contain a pause and duration as well
                int start = pos.getIndex();
                int end = line.indexOf('/', pos.getIndex());
                ae.setPause(NumberParser.parseDouble(line.substring(start, end)));
                start = end + 1;
                end = line.indexOf(' ', start);
                ((ConcurrentGCEvent) ae).setDuration(NumberParser.parseDouble(line.substring(start, end)));
            }

            return ae;
        }
        catch (RuntimeException | UnknownGcTypeException e) {
            throw new ParseException(e.toString(), line, pos);
        }
    }

}
