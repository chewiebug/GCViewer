package com.tagtraum.perf.gcviewer.imp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Concurrency;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.ExtendedType;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.GcPattern;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.util.ParsePosition;

/**
 * <p>Parses getLogger() output from Sun / Oracle Java 1.4 / 1.5 / 1.6. / 1.7
 * <br>Supports the following gc algorithms:
 * <ul>
 * <li>-XX:+UseSerialGC</li>
 * <li>-XX:+UseParallelGC</li>
 * <li>-XX:+UseParNewGC</li>
 * <li>-XX:+UseParallelOldGC</li>
 * <li>-XX:+UseConcMarkSweepGC</li>
 * <li>-Xincgc (1.4 / 1.5)</li>
 * </ul>
 * </p>
 * <p>-XX:+UseG1GC is not supported by this class, but by {@link DataReaderSun1_6_0G1}
 * </p>
 * <p>Supports the following options:
 * <ul>
 * <li>-XX:+PrintGCDetails</li>
 * <li>-XX:+PrintGCTimeStamps</li>
 * <li>-XX:+PrintGCDateStamps</li>
 * <li>-XX:+CMSScavengeBeforeRemark</li>
 * <li>-XX:+PrintHeapAtGC (output ignored)</li>
 * <li>-XX:+PrintTenuringDistribution (output ignored)</li>
 * <li>-XX:+PrintAdaptiveSizePolicy (output ignored)</li>
 * <li>-XX:+PrintPromotionFailure (output ignored)</li>
 * <li>-XX:+PrintGCApplicationStoppedTime (output ignored)</li>
 * <li>-XX:+PrintGCApplicationConcurrentTime (output ignored)</li>
 * <li>-XX:PrintCMSStatistics=2 (output ignored)</li>
 * <li>-XX:+PrintReferenceGC (output ignored)</li>
 * </ul>
 * </p>
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 23.10.2011 (copied from 1.5 implementation)</p>
 * @see DataReaderSun1_6_0G1
 */
public class DataReaderSun1_6_0 extends AbstractDataReaderSun {

    private static final String UNLOADING_CLASS = "[Unloading class ";
    private static final String APPLICATION_TIME = "Application time:"; // -XX:+PrintGCApplicationConcurrentTime
    private static final String TOTAL_TIME_THREADS_STOPPED = "Total time for which application threads were stopped:"; // -XX:+PrintGCApplicationStoppedTime
    private static final String DESIRED_SURVIVOR = "Desired survivor"; // -XX:+PrintTenuringDistribution
    private static final String SURVIVOR_AGE = "- age"; // -XX:+PrintTenuringDistribution
    private static final String TIMES_ALONE = " [Times";
    private static final String FINISHED = "Finished"; // -XX:PrintCmsStatistics=2
    private static final String CARDTABLE = " (cardTable: "; // -XX:PrintCmsStatistics=2 
    private static final String GC_LOCKER = "GC locker: Trying a full collection because scavenge failed";
    private static final List<String> EXCLUDE_STRINGS = new LinkedList<String>();

    static {
        EXCLUDE_STRINGS.add(UNLOADING_CLASS);
        EXCLUDE_STRINGS.add(DESIRED_SURVIVOR);
        EXCLUDE_STRINGS.add(APPLICATION_TIME);
        EXCLUDE_STRINGS.add(TOTAL_TIME_THREADS_STOPPED);
        EXCLUDE_STRINGS.add(SURVIVOR_AGE);
        EXCLUDE_STRINGS.add(TIMES_ALONE);
        EXCLUDE_STRINGS.add(FINISHED);
        EXCLUDE_STRINGS.add(CARDTABLE);
        EXCLUDE_STRINGS.add(GC_LOCKER);
    }
    
    private static final String EVENT_YG_OCCUPANCY = "YG occupancy";
    private static final String EVENT_PARNEW = "ParNew";
    private static final String EVENT_DEFNEW = "DefNew";
    
    private static final String CMS_ABORT_PRECLEAN = " CMS: abort preclean due to time ";

    private static final String HEAP_SIZING_START = "Heap";
    
    private static final List<String> HEAP_STRINGS = new LinkedList<String>();
    static {
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
    
    private static final List<String> ADAPTIVE_SIZE_POLICY_STRINGS = new LinkedList<String>();
    static {
        ADAPTIVE_SIZE_POLICY_STRINGS.add("PSAdaptiveSize");
        ADAPTIVE_SIZE_POLICY_STRINGS.add("AdaptiveSize");
        ADAPTIVE_SIZE_POLICY_STRINGS.add("avg_survived_padded_avg");
    }

    // 1_6_0_u24 mixes lines, when outputing a "promotion failed" which leads to a "concurrent mode failure"
    // pattern looks always like "...[CMS<datestamp>..." or "...[CMS<timestamp>..."
    // the next line starts with " (concurrent mode failure)" which in earlier releases followed "CMS" immediately
    // the same can happen with "...ParNew<timestamp|datestamp>..."
    private static Pattern linesMixedPattern = Pattern.compile("(.*\\[(CMS|ParNew|DefNew|ASCMS|ASParNew))([0-9]+[-.].*)");
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
    private static final String ADAPTIVE_SIZE_POLICY_PATTERN_STRING = "(.*GC \\([a-zA-Z ]*\\)|.*GC)(?:[0-9.:]*.*)[ ]?AdaptiveSize.*";
    private static final Pattern adaptiveSizePolicyPattern = Pattern.compile(ADAPTIVE_SIZE_POLICY_PATTERN_STRING);
    private static final String ADAPTIVE_PATTERN = "AdaptiveSize";
    
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
    
    // -XX:+PrintReferenceGC
    private static final String PRINT_REFERENCE_GC_INDICATOR = "Reference";
    
    // -XX:+CMSScavengeBeforeRemark JDK 1.5
    private static final String SCAVENGE_BEFORE_REMARK = Type.SCAVENGE_BEFORE_REMARK.getName();
    
    private Date firstDateStamp = null;

    public DataReaderSun1_6_0(GCResource gcResource, InputStream in, GcLogType gcLogType) throws UnsupportedEncodingException {
        super(gcResource, in, gcLogType);
    }

    public GCModel read() throws IOException {
        if (getLogger().isLoggable(Level.INFO)) getLogger().info("Reading Sun / Oracle 1.4.x / 1.5.x / 1.6.x / 1.7.x format...");
        
        try (BufferedReader in = this.in) {
            final GCModel model = new GCModel(false);
            model.setFormat(GCModel.Format.SUN_X_LOG_GC);
            Matcher mixedLineMatcher = linesMixedPattern.matcher("");
            Matcher adaptiveSizePolicyMatcher = adaptiveSizePolicyPattern.matcher("");
            Matcher printAdaptiveSizePolicyMatcher = printAdaptiveSizePolicyPattern.matcher("");
            Matcher printCmsStatisticsIterationsMatcher = printCmsStatisticsIterationsPattern.matcher("");
            Matcher printTenuringDistributionMatcher = printTenuringDistributionPattern.matcher("");
            String line;
            // beginningOfLine must be a stack because more than one beginningOfLine might be needed
            Deque<String> beginningOfLine = new LinkedList<String>();
            int lineNumber = 0;
            boolean lastLineWasScavengeBeforeRemark = false;
            boolean lineSkippedForScavengeBeforeRemark = false;
            boolean printTenuringDistributionOn = false;
            final ParsePosition parsePosition = new ParsePosition(0);

            while ((line = in.readLine()) != null) {
                ++lineNumber;
                parsePosition.setLineNumber(lineNumber);
                if ("".equals(line)) {
                    continue;
                }
                try {
                    printTenuringDistributionOn = false;
                    // filter out lines that don't need to be parsed
                    if (startsWith(line, EXCLUDE_STRINGS, false)) {
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
                            getLogger().severe("printCmsStatisticsIterationsMatcher did not match for line " + lineNumber + ": '" + line + "'");
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
                            getLogger().severe("printDistributionMatcher did not match for line " + lineNumber + ": '" + line + "'");
                            continue;
                        }
                        
                        line = printTenuringDistributionMatcher.group(PRINT_TENURING_DISTRIBUTION_PATTERN_GROUP_BEFORE)
                                    + printTenuringDistributionMatcher.group(PRINT_TENURING_DISTRIBUTION_PATTERN_GROUP_AFTER);
                    }
                    if (line.indexOf(PRINT_REFERENCE_GC_INDICATOR) > 0) {
                        line = filterAwayReferenceGc(line);
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
                    final int unloadingClassIndex = line.indexOf(UNLOADING_CLASS);
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
                    else if (isMixedLine(line, mixedLineMatcher)) {
                        // if PrintTenuringDistribution is used and a line is mixed, 
                        // beginningOfLine may already contain a value, which must be preserved
                        String firstPartOfBeginningOfLine = beginningOfLine.pollFirst();
                        if (firstPartOfBeginningOfLine == null) {
                            firstPartOfBeginningOfLine = "";
                        }
                        beginningOfLine.addFirst(firstPartOfBeginningOfLine + mixedLineMatcher.group(LINES_MIXED_STARTOFLINE_GROUP));
                        
                        model.add(parseLine(mixedLineMatcher.group(LINES_MIXED_ENDOFLINE_GROUP), parsePosition));
                        parsePosition.setIndex(0);
                        continue;
                    }
                    else if (line.indexOf(ADAPTIVE_PATTERN) >= 0) {
                        if (line.indexOf("Times") > 0) {
                            // -XX:+PrintAdaptiveSizePolicy -XX:-UseAdaptiveSizePolicy
                            printAdaptiveSizePolicyMatcher.reset(line);
                            if (!printAdaptiveSizePolicyMatcher.matches()) {
                                getLogger().severe("printAdaptiveSizePolicyMatcher did not match for line " + lineNumber + ": '" + line + "'");
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
                                getLogger().severe("adaptiveSizePolicyMatcher did not match for line " + lineNumber + ": '" + line + "'");
                                continue;
                            }
                            beginningOfLine.addFirst(adaptiveSizePolicyMatcher.group(1));
                            lineNumber = skipLines(in, parsePosition, lineNumber, ADAPTIVE_SIZE_POLICY_STRINGS);
                        }
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
                    else if (line.indexOf(HEAP_SIZING_START) >= 0) {
                        // the next few lines will be the sizing of the heap
                        lineNumber = skipLines(in, parsePosition, lineNumber, HEAP_STRINGS);
                        continue;
                    }

                    AbstractGCEvent<?> gcEvent = parseLine(line, parsePosition);
                     
                     if (lastLineWasScavengeBeforeRemark && !printTenuringDistributionOn) {
                         // according to http://mail.openjdk.java.net/pipermail/hotspot-gc-use/2012-August/001297.html
                         // the pause time reported for cms-remark includes the scavenge-before-remark time
                         // so it has to be corrected to show only the time spent in remark event
                         lastLineWasScavengeBeforeRemark = false;
                         lineSkippedForScavengeBeforeRemark = false;
                         GCEvent scavengeBeforeRemarkEvent = (GCEvent) model.get(model.size() - 1);
                         GCEvent remarkEvent = (GCEvent) gcEvent;
                         remarkEvent.setPause(remarkEvent.getPause() - scavengeBeforeRemarkEvent.getPause());
                     }
                     
                     model.add(gcEvent);
                } 
                catch (Exception pe) {
                    if (getLogger().isLoggable(Level.WARNING)) getLogger().warning(pe.toString());
                    if (getLogger().isLoggable(Level.FINE)) getLogger().log(Level.FINE, pe.getMessage(), pe);
                    beginningOfLine.clear();
                }
                // reset ParsePosition
                parsePosition.setIndex(0);
            }
            return model;
        } 
        finally {
            if (getLogger().isLoggable(Level.INFO)) getLogger().info("Done reading.");
        }
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
                || line.endsWith("[ParNew (promotion failed)") // CMS
                || line.endsWith("[GC"); // PSYoungGen (parallel sweep)
    }

    private boolean isCmsScavengeBeforeRemark(String line) {
        return line.indexOf(EVENT_YG_OCCUPANCY) >= 0 
                && (line.indexOf(EVENT_PARNEW) >= 0 || line.indexOf(EVENT_DEFNEW) >= 0);
    }

    protected AbstractGCEvent<?> parseLine(final String line, final ParsePosition pos) throws ParseException {
        AbstractGCEvent<?> ae = null;
        try {
            // parse datestamp          "yyyy-MM-dd'T'hh:mm:ssZ:"
            // parse timestamp          "double:"
            // parse collection type    "[TYPE"
            // either GC data or another collection type starting with timestamp
            // pre-used->post-used, total, time
            final Date datestamp = parseDatestamp(line, pos);
            if (firstDateStamp == null) {
                firstDateStamp = datestamp;
            }
            
            double timestamp = getTimeStamp(line, pos, datestamp);
            final ExtendedType type = parseType(line, pos);
            // special provision for CMS events
            if (type.getConcurrency() == Concurrency.CONCURRENT) {
                final ConcurrentGCEvent event = new ConcurrentGCEvent();

                // simple concurrent events (ending with -start) just are of type GcPattern.GC
                event.setDateStamp(datestamp);
                event.setTimestamp(timestamp);
                event.setExtendedType(type);
                if (type.getPattern() == GcPattern.GC_PAUSE_DURATION) {
                    // the -end events contain a pause and duration as well
                    int start = pos.getIndex();
                    int end = line.indexOf('/', pos.getIndex());
                    event.setPause(Double.parseDouble(line.substring(start, end)));
                    start = end + 1;
                    end = line.indexOf(' ', start);
                    event.setDuration(Double.parseDouble(line.substring(start, end)));
                }
                ae = event;
                // nothing more to parse...
            }
            else {
                final GCEvent event = new GCEvent();
                event.setDateStamp(datestamp);
                event.setTimestamp(timestamp);
                event.setExtendedType(type);
                // now add detail gcevents, should they exist
                parseDetailEventsIfExist(line, pos, event);
                setMemoryAndPauses(event, line, pos);
                if (event.getPause() == 0) {
                    // this is usually the case for full collections
                    // there the "perm" collection is inserted between memory and pause part of main event
                    parseDetailEventsIfExist(line, pos, event);
                    parsePause(event, line, pos);
                }
                ae = event;
            }
            return ae;
        }
        catch (RuntimeException rte) {
            throw new ParseException("Error parsing entry (" + rte.toString() + ")", line, pos);
        }
    }

    /**
     * If the next thing in <code>line</code> is a timestamp, it is parsed and returned.
     * 
     * @param line current line
     * @param pos current parse positition
     * @param datestamp datestamp that may have been parsed
     * @return timestamp (either parsed or derived from datestamp)
     * @throws ParseException it seemed to be a timestamp but still couldn't be parsed
     */
    private double getTimeStamp(final String line, final ParsePosition pos, final Date datestamp) 
            throws ParseException {
        
        double timestamp = 0;
        if (nextIsTimestamp(line, pos)) {
            timestamp = parseTimestamp(line, pos);
        }
        else if (datestamp != null && firstDateStamp != null) {
            // if no timestamp was present, calculate difference between last and this date
            timestamp = (datestamp.getTime() - firstDateStamp.getTime()) / (double)1000; 
        }
        return timestamp;
    }
    
}
