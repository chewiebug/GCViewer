package com.tagtraum.perf.gcviewer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The abstract gc event is the base class for all types of events. All sorts of general
 * information can be queried from it and it provides the possibility to add detail events.
 *
 * Date: Jun 1, 2005
 * Time: 1:47:32 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 */
public abstract class AbstractGCEvent<T extends AbstractGCEvent<T>> implements Serializable {
    @SuppressWarnings("unchecked")
    private final Iterator<T> EMPTY_ITERATOR = Collections.EMPTY_LIST.iterator();
    private Date datestamp;
    private double timestamp;
    private Type type = Type.UNDEFINED;
    private boolean tenuredDetail;
    private String typeAsString;
    private Generation generation;
    protected List<T> details;

    public Iterator<T> details() {
        if (details == null) return EMPTY_ITERATOR;
        return details.iterator();
    }

    public void add(T detail) {
        // most events have only one detail event
        if (details == null) {
        	details = new ArrayList<T>(2);
        }
        details.add(detail);
        typeAsString += " " + detail.getType();
        if (detail.getType().getGeneration() == Generation.TENURED) {
        	tenuredDetail = true;
        }
        
        // will be calculated upon call to "getGeneration()"
        generation = null;
    }

    public boolean hasDetails() {
        return details != null 
                && details.size() > 0;
    }
    
    public boolean hasTenuredDetail() {
        return tenuredDetail;
    }

    public void setDateStamp(Date datestamp) {
    	this.datestamp = datestamp;
    }
    
    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public void setType(Type type) {
        this.type = type;
        this.typeAsString = type.getType();
        if (details != null && details.size() > 0) {
            this.typeAsString = buildTypeAsString();
        }
    }

    private String buildTypeAsString() {
    	StringBuilder sb = new StringBuilder(getType().getType());
    	if (details != null) {
    		for (T detailType : details) {
    			sb.append(" ").append(detailType.getType());
    		}
    	}
    	
    	return sb.toString();
    }
    
    public Type getType() {
        return type;
    }
    
    public String getTypeAsString() {
    	return typeAsString;
    }
    
    public boolean isStopTheWorld() {
    	boolean isStopTheWorld = getType().getConcurrency() == Concurrency.SERIAL;
    	if (details != null) {
    		for (T detailEvent : details) {
    			if (!isStopTheWorld) {
    				isStopTheWorld = detailEvent.getType().getConcurrency() == Concurrency.SERIAL;
    			}
    		}
    	}
    	
    	return isStopTheWorld;
    }
    
    /**
     * Returns the generation of the event including generation of detail events if present.
     * @return generation of event including generation of detail events
     */
    public Generation getGeneration() {
        if (generation == null) {
            if (!hasDetails()) {
                generation = getType().getGeneration();
            }
            else {
                // find out, what generations the detail events contain
                Set<Generation> generationSet = new TreeSet<Generation>();
                for (T detailEvent : details) {
                    generationSet.add(detailEvent.getType().getGeneration());
                }
                
                if (generationSet.size() > 1 || generationSet.contains(Generation.ALL)) {
                    generation = Generation.ALL;
                }
                else if (generationSet.size() == 1) {
                    generation  = generationSet.iterator().next();
                }
                else {
                    // default
                    generation = Generation.YOUNG;
                }
            }
        }
        
        return generation;
    }
    
    public double getTimestamp() {
        return timestamp;
    }
    
    public Date getDatestamp() {
    	return datestamp;
    }

    public abstract void toStringBuffer(StringBuffer sb);

    public boolean isTenuredDetail() {
        return tenuredDetail;
    }

    public void setTenuredDetail(boolean tenuredDetail) {
        this.tenuredDetail = tenuredDetail;
    }

    // better than nothing hashcode
    public int hashCode() {
        return toString().hashCode();
    }

    // better than nothing equals
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(128);
        toStringBuffer(sb);
        return sb.toString();
    }

    public boolean isFull() {
        if (getType().getGeneration().compareTo(Generation.ALL) == 0) {
            return true;
        }
        
        if (details != null) {
            // the assumption is, that a full collection is everything, that collects from more 
            // than one generation.
            // Probably this is not always strictly right, but often enough a good assumption
            return details.size() > 1;
        }
        else {
            return false;
        }
    }

    public boolean isInc() {
        return getType() == GCEvent.Type.INC_GC;
    }
    
    public boolean isConcurrent() {
        return getType().getConcurrency().equals(Concurrency.CONCURRENT);
    }

    public boolean isConcurrencyHelper() {
        return getType().getCollectionType().equals(CollectionType.CONCURRENCY_HELPER);
    }
    
    public boolean isConcurrentCollectionStart() {
        return getType().getType().equals(Type.CMS_CONCURRENT_MARK_START.getType()) // CMS
                || getType().getType().equals(Type.ASCMS_CONCURRENT_MARK_START.getType()) // CMS AdaptiveSizePolicy
                || getType().getType().equals(Type.G1_CONCURRENT_MARK_START.getType());// G1
    }
    
    public boolean isConcurrentCollectionEnd() {
        return getType().getType().equals(Type.CMS_CONCURRENT_RESET.getType()) // CMS
                || getType().getType().equals(Type.ASCMS_CONCURRENT_RESET.getType()) // CMS AdaptiveSizePolicy
                || getType().getType().equals(Type.G1_CONCURRENT_CLEANUP_END.getType()); // G1
    }
    
    public boolean isInitialMark() {
        return getTypeAsString().indexOf(Type.CMS_INITIAL_MARK.getType()) >= 0
                || getTypeAsString().indexOf(Type.ASCMS_INITIAL_MARK.getType()) >= 0
                || getTypeAsString().indexOf(Type.G1_YOUNG_INITIAL_MARK.getType()) >= 0
                || getTypeAsString().indexOf(Type.G1_YOUNG_INITIAL_MARK_TO_SPACE_OVERFLOW.getType()) >= 0
                || getTypeAsString().indexOf(Type.G1_PARTIAL_INITIAL_MARK.getType()) >= 0
                || getTypeAsString().indexOf(Type.G1_PARTIAL_INITIAL_MARK_TO_SPACE_OVERFLOW.getType()) >= 0;
    }
    
    public static class Type implements Serializable {
        private final String type;
        private final String rep;
        private Generation generation;
        private Concurrency concurrency;
        /** pattern this event has in the logfile */
        private GcPattern pattern;
        private CollectionType collectionType;
        private static final Map<String, Type> TYPE_MAP = new HashMap<String, Type>();

        private Type(String type, Generation generation) {
            this(type, type, generation);
        }

        private Type(String type, String rep, Generation generation) {
            this(type, rep, generation, Concurrency.SERIAL);
        }

        private Type(String type, String rep, Generation generation, Concurrency concurrency) {
        	this(type, rep, generation, concurrency, GcPattern.GC_MEMORY_PAUSE);
        }

        private Type(String type, String rep, Generation generation, Concurrency concurrency, GcPattern pattern) {
            this(type, rep, generation, concurrency, pattern, CollectionType.COLLECTION);
        }

        private Type(String type, String rep, Generation generation, Concurrency concurrency, GcPattern pattern, CollectionType collectionType) {
            this.type = type.intern();
            this.rep = rep;
            this.generation = generation;
            this.concurrency = concurrency;
            this.pattern = pattern;
            this.collectionType = collectionType;
            
            TYPE_MAP.put(this.type, this);
        }

        public static Type parse(String type) {
            return TYPE_MAP.get(type.trim());
        }

        public static Type parse(final int reason) {
            return reason == -1 ? Type.GC : Type.FULL_GC;
        }

        public static Type parse(final int typeOfGC, final float details) {
            final Type type;
            switch (typeOfGC) {
                case 1:
                    if (details == 0) {
                        type = Type.GC;
                        break;
                    }
                    type = Type.PAR_NEW;
                    break;
                case 2:
                    type = Type.FULL_GC;
                    break;
                case 3:
                    type = Type.CMS;
                    break;
                case 4:
                    type = Type.CMS;
                    break;
                default:
                    type = Type.FULL_GC;
            }
            return type;
        }

        public String getType() {
            return type;
        }

        public Generation getGeneration() {
            return generation;
        }

        public Concurrency getConcurrency() {
            return concurrency;
        }

        public GcPattern getPattern() {
        	return pattern;
        }
        
        public CollectionType getCollectionType() {
            return collectionType;
        }

        public String toString() {
            return rep;
        }

        public static final Type UNDEFINED = new Type("undefined", Generation.YOUNG);

        // TODO: is jrockit GC really of type Generation.ALL or rather Generation.TENURED ?
        public static final Type JROCKIT_GC = new Type("jrockit.GC", Generation.TENURED);
        public static final Type JROCKIT_NURSERY_GC = new Type("jrockit.Nursery GC", Generation.YOUNG);
        public static final Type JROCKIT_PARALLEL_NURSERY_GC = new Type("jrockit.parallel nursery GC", Generation.YOUNG);

        public static final Type JROCKIT_16_OLD_GC = new Type("jrockit.OC", Generation.TENURED);
        public static final Type JROCKIT_16_YOUNG_GC = new Type("jrockit.YC", Generation.YOUNG);
        public static final Type JROCKIT_16_PARALLEL_NURSERY_GC = new Type("jrockit.parallel nursery GC", Generation.YOUNG);
        
        public static final Type FULL_GC = new Type("Full GC", Generation.ALL);
        public static final Type FULL_GC_SYSTEM = new Type("Full GC (System)", Generation.ALL);
        public static final Type GC = new Type("GC", Generation.YOUNG);
        public static final Type GC__ = new Type("GC--", Generation.YOUNG);
        public static final Type DEF_NEW = new Type("DefNew", "DefNew:", Generation.YOUNG, Concurrency.SERIAL); // single threaded
        public static final Type PAR_NEW = new Type("ParNew", "ParNew:", Generation.YOUNG); // parallel
        public static final Type ASPAR_NEW = new Type("ASParNew", "ASParNew:", Generation.YOUNG); // parallel (CMS AdaptiveSizePolicy)
        public static final Type PAR_OLD_GEN = new Type("ParOldGen", "ParOldGen:", Generation.TENURED);
        public static final Type PS_YOUNG_GEN = new Type("PSYoungGen", "PSYoungGen:", Generation.YOUNG);
        public static final Type PS_OLD_GEN = new Type("PSOldGen", "PSOldGen:", Generation.TENURED);
        public static final Type PS_PERM_GEN = new Type("PSPermGen", "PSPermGen:", Generation.PERM);
        public static final Type TENURED = new Type("Tenured", "Tenured:", Generation.TENURED);
        public static final Type INC_GC = new Type("Inc GC", Generation.YOUNG);
        public static final Type TRAIN = new Type("Train", "Train:", Generation.TENURED);
        public static final Type TRAIN_MSC = new Type("Train MSC", "Train MSC:", Generation.TENURED);
        public static final Type PERM = new Type("Perm", "Perm:", Generation.PERM);

        // CMS types
        public static final Type CMS = new Type("CMS", "CMS:", Generation.TENURED);
        public static final Type CMS_PERM = new Type("CMS Perm", "CMS Perm :", Generation.PERM);
        
        // Parnew (promotion failed)
        public static final Type PAR_NEW_PROMOTION_FAILED = new Type("ParNew (promotion failed)", "ParNew (promotion failed):", Generation.YOUNG, Concurrency.SERIAL);
        
        // CMS (concurrent mode failure / interrupted)
        public static final Type CMS_CMF = new Type("CMS (concurrent mode failure)", "CMS (concurrent mode failure):", Generation.TENURED, Concurrency.SERIAL);
        public static final Type CMS_CMI = new Type("CMS (concurrent mode interrupted)", "CMS (concurrent mode interrupted):", Generation.TENURED, Concurrency.SERIAL);

        // CMS (Concurrent Mark Sweep) Event Types
        public static final Type CMS_CONCURRENT_MARK_START = new Type("CMS-concurrent-mark-start", "CMS-concurrent-mark-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type CMS_CONCURRENT_MARK = new Type("CMS-concurrent-mark", "CMS-concurrent-mark:", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);
        public static final Type CMS_CONCURRENT_PRECLEAN_START = new Type("CMS-concurrent-preclean-start", "CMS-concurrent-preclean-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type CMS_CONCURRENT_PRECLEAN = new Type("CMS-concurrent-preclean", "CMS-concurrent-preclean", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);
        public static final Type CMS_CONCURRENT_SWEEP_START = new Type("CMS-concurrent-sweep-start", "CMS-concurrent-sweep-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type CMS_CONCURRENT_SWEEP = new Type("CMS-concurrent-sweep", "CMS-concurrent-sweep:", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);
        public static final Type CMS_CONCURRENT_RESET_START = new Type("CMS-concurrent-reset-start", "CMS-concurrent-reset-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type CMS_CONCURRENT_RESET = new Type("CMS-concurrent-reset", "CMS-concurrent-reset:", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);
        public static final Type CMS_CONCURRENT_ABORTABLE_PRECLEAN_START = new Type("CMS-concurrent-abortable-preclean-start", "CMS-concurrent-abortable-preclean-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type CMS_CONCURRENT_ABORTABLE_PRECLEAN = new Type("CMS-concurrent-abortable-preclean", "CMS-concurrent-abortable-preclean:", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);

        public static final Type CMS_INITIAL_MARK = new Type("CMS-initial-mark", "CMS-initial-mark:", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type CMS_REMARK = new Type("CMS-remark", "CMS-remark:", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY);
        
        // CMS (Concurrent Mark Sweep) AdaptiveSizePolicy Event Types
        public static final Type ASCMS = new Type("ASCMS", "ASCMS:", Generation.TENURED);

        // Parnew (promotion failed) AdaptiveSizePolicy
        public static final Type ASPAR_NEW_PROMOTION_FAILED = new Type("ASParNew (promotion failed)", "ASParNew (promotion failed):", Generation.YOUNG, Concurrency.SERIAL);
        
        // CMS (concurrent mode failure / interrupted) AdaptiveSizePolicy
        public static final Type ASCMS_CMF = new Type("ASCMS (concurrent mode failure)", "ASCMS (concurrent mode failure):", Generation.TENURED, Concurrency.SERIAL);
        public static final Type ASCMS_CMI = new Type("ASCMS (concurrent mode interrupted)", "ASCMS (concurrent mode interrupted):", Generation.TENURED, Concurrency.SERIAL);

        public static final Type ASCMS_CONCURRENT_MARK_START = new Type("ASCMS-concurrent-mark-start", "ASCMS-concurrent-mark-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type ASCMS_CONCURRENT_MARK = new Type("ASCMS-concurrent-mark", "ASCMS-concurrent-mark:", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);
        public static final Type ASCMS_CONCURRENT_PRECLEAN_START = new Type("ASCMS-concurrent-preclean-start", "ASCMS-concurrent-preclean-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type ASCMS_CONCURRENT_PRECLEAN = new Type("ASCMS-concurrent-preclean", "ASCMS-concurrent-preclean", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);
        public static final Type ASCMS_CONCURRENT_SWEEP_START = new Type("ASCMS-concurrent-sweep-start", "ASCMS-concurrent-sweep-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type ASCMS_CONCURRENT_SWEEP = new Type("ASCMS-concurrent-sweep", "ASCMS-concurrent-sweep:", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);
        public static final Type ASCMS_CONCURRENT_RESET_START = new Type("ASCMS-concurrent-reset-start", "ASCMS-concurrent-reset-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type ASCMS_CONCURRENT_RESET = new Type("ASCMS-concurrent-reset", "ASCMS-concurrent-reset:", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);
        public static final Type ASCMS_CONCURRENT_ABORTABLE_PRECLEAN_START = new Type("ASCMS-concurrent-abortable-preclean-start", "ASCMS-concurrent-abortable-preclean-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type ASCMS_CONCURRENT_ABORTABLE_PRECLEAN = new Type("ASCMS-concurrent-abortable-preclean", "ASCMS-concurrent-abortable-preclean:", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);

        public static final Type ASCMS_INITIAL_MARK = new Type("ASCMS-initial-mark", "ASCMS-initial-mark:", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type ASCMS_REMARK = new Type("ASCMS-remark", "ASCMS-remark:", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY);
        
        // G1 stop the world types
        public static final Type G1_FULL_GC_SYSTEM = new Type("Full GC (System.gc())", "Full GC (System.gc())", Generation.ALL, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        
        // only young collection
        public static final Type G1_YOUNG = new Type("GC pause (young)", "GC pause (young)", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_YOUNG_MARK_STACK_FULL = new Type("GC pause (young)Mark stack is full.", "GC pause (young) Mark stack is full", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        // young collections with problems (to-space overflow)
        public static final Type G1_YOUNG__ = new Type("GC pause (young)--", "GC pause (young)--", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        // the same as above but more verbose in detailed mode
        public static final Type G1_YOUNG_TO_SPACE_OVERFLOW = new Type("GC pause (young) (to-space overflow)", "GC pause (young) (to-space overflow)", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        // partially young collection (
        public static final Type G1_PARTIAL = new Type("GC pause (partial)", "GC pause (partial)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_PARTIAL_TO_SPACE_OVERFLOW = new Type("GC pause (partial) (to-space overflow)", "GC pause (partial) (to-space overflow)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        // mixed collection (might have replaced "partial" collection in jdk1.7.0_u5)
        public static final Type G1_MIXED = new Type("GC pause (mixed)", "GC pause (mixed)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_MIXED_TO_SPACE_OVERFLOW = new Type("GC pause (mixed) (to-space overflow)", "GC pause (mixed) (to-space overflow)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        
        // TODO: Generation: young and tenured!
        public static final Type G1_YOUNG_INITIAL_MARK = new Type("GC pause (young) (initial-mark)", "GC pause (young) (initial-mark)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_YOUNG_INITIAL_MARK_TO_SPACE_OVERFLOW = new Type("GC pause (young) (to-space overflow) (initial-mark)", "GC pause (young) (to-space overflow) (initial-mark)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_PARTIAL_INITIAL_MARK = new Type("GC pause (partial) (initial-mark)", "GC pause (partial) (initial-mark)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_PARTIAL_INITIAL_MARK_TO_SPACE_OVERFLOW = new Type("GC pause (partial) (to-space overflow) (initial-mark)", "GC pause (partial) (to-space overflow) (initial-mark)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_REMARK = new Type("GC remark", "GC remark", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_PAUSE, CollectionType.CONCURRENCY_HELPER);
        // Java 7; detail event inside G1_REMARK
        public static final Type G1_GC_REFPROC = new Type("GC ref-proc", "GC ref-proc", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_PAUSE, CollectionType.CONCURRENCY_HELPER);
        public static final Type G1_CLEANUP = new Type("GC cleanup", "GC cleanup", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE, CollectionType.CONCURRENCY_HELPER);
        // Java 7_u2; detailed info in all detailed events
        public static final Type G1_EDEN = new Type("Eden", "Eden:", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        
        // G1 concurrent types
        public static final Type G1_CONCURRENT_ROOT_REGION_SCAN_START = new Type("GC concurrent-root-region-scan-start", "GC concurrent-root-region-scan-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type G1_CONCURRENT_ROOT_REGION_SCAN_END = new Type("GC concurrent-root-region-scan-end", "GC concurrent-root-region-scan-end", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type G1_CONCURRENT_MARK_START = new Type("GC concurrent-mark-start", "GC concurrent-mark-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type G1_CONCURRENT_MARK_END = new Type("GC concurrent-mark-end", "GC concurrent-mark-end,", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type G1_CONCURRENT_MARK_ABORT = new Type("GC concurrent-mark-abort", "GC concurrent-mark-abort", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type G1_CONCURRENT_MARK_RESET_FOR_OVERFLOW = new Type("GC concurrent-mark-reset-for-overflow", "GC concurrent-mark-reset-for-overflow", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type G1_CONCURRENT_COUNT_START = new Type("GC concurrent-count-start", "GC concurrent-count-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type G1_CONCURRENT_COUNT_END = new Type("GC concurrent-count-end", "GC concurrent-count-end,", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type G1_CONCURRENT_CLEANUP_START = new Type("GC concurrent-cleanup-start", "GC concurrent-cleanup-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type G1_CONCURRENT_CLEANUP_END = new Type("GC concurrent-cleanup-end", "GC concurrent-cleanup-end,", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
    }

    public static enum GcPattern {
    	// <timestamp>: [<GC type>]
    	GC,
    	// <timestamp>: [<GC type>, <pause>]
    	GC_PAUSE,
    	// <timestamp>: [<GC type>, <pause>/<duration>]
    	GC_PAUSE_DURATION,
    	// [<GC type>, <mem current>(<mem total>)]
    	GC_MEMORY,
    	// <timestamp>: [<GC type> <mem before>-><mem after>(<mem total>), <pause>]
    	GC_MEMORY_PAUSE} 
    
    public static enum Concurrency { CONCURRENT, SERIAL };

    public static enum Generation { YOUNG, TENURED, PERM, ALL };
    
    public static enum CollectionType {
        // plain GC pause collection garbage
        COLLECTION,
        // stop the world pause but used to prepare concurrent collection, does not collect garbage
        CONCURRENCY_HELPER };
}
