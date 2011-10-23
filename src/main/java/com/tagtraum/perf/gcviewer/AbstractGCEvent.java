package com.tagtraum.perf.gcviewer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * Date: Jun 1, 2005
 * Time: 1:47:32 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public abstract class AbstractGCEvent implements Serializable {
    private static final Iterator<AbstractGCEvent> EMPTY_ITERATOR = Collections.EMPTY_LIST.iterator();
    private Date datestamp;
    private double timestamp;
    private Type type = Type.GC;
    private boolean tenuredDetail;
    protected List<AbstractGCEvent> details;

    public Iterator<AbstractGCEvent> details() {
        if (details == null) return EMPTY_ITERATOR;
        return details.iterator();
    }

    public void add(AbstractGCEvent detail) {
        // most events have only one detail event
        if (details == null) details = new ArrayList<AbstractGCEvent>(2);
        details.add(detail);
        if (detail.getType().getGeneration() == Generation.TENURED) tenuredDetail = true;
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
    }

    public Type getType() {
        return type;
    }
    
    public String getTypeAsString() {
    	StringBuilder sb = new StringBuilder(getType().getType());
    	if (details != null) {
    		for (AbstractGCEvent detailType : details) {
    			sb.append(" ").append(detailType.getType());
    		}
    	}
    	
    	return sb.toString();
    }
    
    public boolean isStopTheWorld() {
    	boolean isStopTheWorld = getType().getConcurrency() == Concurrency.SERIAL;
    	if (details != null) {
    		for (AbstractGCEvent detailEvent : details) {
    			if (!isStopTheWorld) {
    				isStopTheWorld = detailEvent.getType().getConcurrency() == Concurrency.SERIAL;
    			}
    		}
    	}
    	
    	return isStopTheWorld;
    }
    
    public Generation getDetailGeneration() {
    	Generation generation = getType().getGeneration();
    	if (details != null) {
    		for (AbstractGCEvent detailEvent : details) {
    			if (generation.compareTo(detailEvent.getType().getGeneration()) < 0
    					&& detailEvent.getType().getGeneration() != Generation.PERM) {
    				
    				generation = detailEvent.getType().getGeneration();
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

    public static class Type implements Serializable {
        private final String type;
        private final String rep;
        private Generation generation;
        private Concurrency concurrency;
        /** pattern this event has in the logfile */
        private Pattern pattern;
        private static final Map<String, Type> TYPE_MAP = new HashMap<String, Type>();

        private Type(String type, Generation generation) {
            this(type, type, generation);
        }

        private Type(String type, String rep, Generation generation) {
            this(type, rep, generation, Concurrency.SERIAL);
        }

        private Type(String type, String rep, Generation generation, Concurrency concurrency) {
        	this(type, rep, generation, concurrency, Pattern.GC_MEMORY_PAUSE);
        }

        private Type(String type, String rep, Generation generation, Concurrency concurrency, Pattern pattern) {
            this.type = type.intern();
            this.rep = rep;
            this.generation = generation;
            this.concurrency = concurrency;
            this.pattern = pattern;
            TYPE_MAP.put(this.type, this);
        }

        public static Type parse(String type) {
            type = type.trim();
            Type gcType = TYPE_MAP.get(type);
            // TODO why does lookup fail here -> change Type definition?
            if (gcType == null) {
            	if (type.endsWith(Type.CMS_INITIAL_MARK.getType())) {
                	gcType = Type.CMS_INITIAL_MARK;
            	}
            	else if (Type.PAR_NEW_PROMOTION_FAILED.getType().startsWith(type)) {
            		gcType = Type.PAR_NEW_PROMOTION_FAILED;
            	}
            }
            return gcType;
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

        public Pattern getPattern() {
        	return pattern;
        }
        
        public String toString() {
            return rep;
        }

        // TODO: is jrockit GC really of type Generation.ALL or rather Generation.TENURED ?
        public static final Type JROCKIT_GC = new Type("jrockit.GC", Generation.TENURED);
        public static final Type JROCKIT_NURSERY_GC = new Type("jrockit.Nursery GC", Generation.YOUNG);
        public static final Type JROCKIT_PARALLEL_NURSERY_GC = new Type("jrockit.parallel nursery GC", Generation.YOUNG);

        public static final Type FULL_GC = new Type("Full GC", Generation.ALL);
        public static final Type FULL_GC_SYSTEM = new Type("Full GC (System)", Generation.ALL);
        public static final Type GC = new Type("GC", Generation.YOUNG);
        public static final Type GC__ = new Type("GC--", Generation.YOUNG);
        public static final Type DEF_NEW = new Type("DefNew", "DefNew:", Generation.YOUNG);
        public static final Type PAR_NEW = new Type("ParNew", "ParNew:", Generation.YOUNG);
        public static final Type PAR_OLD_GEN = new Type("ParOldGen", "ParOldGen:", Generation.TENURED);
        public static final Type PS_YOUNG_GEN = new Type("PSYoungGen", "PSYoungGen:", Generation.YOUNG);
        public static final Type PS_OLD_GEN = new Type("PSOldGen", "PSOldGen:", Generation.TENURED);
        public static final Type PS_PERM_GEN = new Type("PSPermGen", "PSPermGen:", Generation.PERM);
        public static final Type TENURED = new Type("Tenured", "Tenured:", Generation.TENURED);
        public static final Type INC_GC = new Type("Inc GC", Generation.YOUNG);
        public static final Type TRAIN = new Type("Train", "Train:", Generation.TENURED);
        public static final Type TRAIN_MSC = new Type("Train MSC", "Train MSC:", Generation.TENURED);
        public static final Type PERM = new Type("Perm", "Perm:", Generation.PERM);
        public static final Type CMS = new Type("CMS", "CMS:", Generation.TENURED);
        public static final Type CMS_PERM = new Type("CMS Perm", "CMS Perm :", Generation.PERM);
        
        // Parnew (promotion failed)
        public static final Type PAR_NEW_PROMOTION_FAILED = new Type("ParNew (promotion failed)", "ParNew (promotion failed):", Generation.ALL, Concurrency.SERIAL);
        
        // CMS (concurrent mode failure)
        public static final Type CMS_CMF = new Type("CMS (concurrent mode failure)", "CMS (concurrent mode failure):", Generation.ALL, Concurrency.SERIAL);

        // CMS (Concurrent Mark Sweep) Event Types
        public static final Type CMS_CONCURRENT_MARK_START = new Type("CMS-concurrent-mark-start", "CMS-concurrent-mark-start", Generation.TENURED, Concurrency.CONCURRENT);
        public static final Type CMS_CONCURRENT_MARK = new Type("CMS-concurrent-mark", "CMS-concurrent-mark:", Generation.TENURED, Concurrency.CONCURRENT);
        public static final Type CMS_CONCURRENT_PRECLEAN_START = new Type("CMS-concurrent-preclean-start", "CMS-concurrent-preclean-start", Generation.TENURED, Concurrency.CONCURRENT);
        public static final Type CMS_CONCURRENT_PRECLEAN = new Type("CMS-concurrent-preclean", "CMS-concurrent-preclean", Generation.TENURED, Concurrency.CONCURRENT);
        public static final Type CMS_CONCURRENT_SWEEP_START = new Type("CMS-concurrent-sweep-start", "CMS-concurrent-sweep-start", Generation.TENURED, Concurrency.CONCURRENT);
        public static final Type CMS_CONCURRENT_SWEEP = new Type("CMS-concurrent-sweep", "CMS-concurrent-sweep:", Generation.TENURED, Concurrency.CONCURRENT);
        public static final Type CMS_CONCURRENT_RESET_START = new Type("CMS-concurrent-reset-start", "CMS-concurrent-reset-start", Generation.TENURED, Concurrency.CONCURRENT);
        public static final Type CMS_CONCURRENT_RESET = new Type("CMS-concurrent-reset", "CMS-concurrent-reset:", Generation.TENURED, Concurrency.CONCURRENT);
        public static final Type CMS_CONCURRENT_ABORTABLE_PRECLEAN_START = new Type("CMS-concurrent-abortable-preclean-start", "CMS-concurrent-abortable-preclean-start", Generation.TENURED, Concurrency.CONCURRENT);
        public static final Type CMS_CONCURRENT_ABORTABLE_PRECLEAN = new Type("CMS-concurrent-abortable-preclean", "CMS-concurrent-abortable-preclean:", Generation.TENURED, Concurrency.CONCURRENT);

        public static final Type CMS_INITIAL_MARK = new Type("CMS-initial-mark", "CMS-initial-mark:", Generation.TENURED, Concurrency.SERIAL);
        public static final Type CMS_REMARK = new Type("CMS-remark", "CMS-remark:", Generation.TENURED, Concurrency.SERIAL);
        
        // G1 stop the world types
        // only young collection
        public static final Type G1_YOUNG = new Type("GC pause (young)", "GC pause (young)", Generation.YOUNG, Concurrency.SERIAL, Pattern.GC_MEMORY_PAUSE);
        public static final Type G1_YOUNG__ = new Type("GC pause (young)--", "GC pause (young)--", Generation.YOUNG, Concurrency.SERIAL, Pattern.GC_MEMORY_PAUSE);
        // partially young collection (
        public static final Type G1_PARTIAL = new Type("GC pause (partial)", "GC pause (partial)", Generation.TENURED, Concurrency.SERIAL, Pattern.GC_MEMORY_PAUSE);

        // TODO: Generation: young and tenured!
        public static final Type G1_INITIAL_MARK = new Type("GC pause (young) (initial-mark)", "GC pause (young) (initial-mark)", Generation.TENURED, Concurrency.SERIAL, Pattern.GC_MEMORY_PAUSE);
        public static final Type G1_REMARK = new Type("GC remark", "GC remark", Generation.TENURED, Concurrency.SERIAL, Pattern.GC_PAUSE);
        public static final Type G1_CLEANUP = new Type("GC cleanup", "GC cleanup", Generation.TENURED, Concurrency.SERIAL, Pattern.GC_MEMORY_PAUSE);
        
        // G1 concurrent types
        public static final Type G1_CONCURRENT_MARK_START = new Type("GC concurrent-mark-start", "GC concurrent-mark-start", Generation.TENURED, Concurrency.CONCURRENT, Pattern.GC);
        public static final Type G1_CONCURRENT_MARK_END = new Type("GC concurrent-mark-end", "GC concurrent-mark-end,", Generation.TENURED, Concurrency.CONCURRENT, Pattern.GC_PAUSE);
        public static final Type G1_CONCURRENT_MARK_ABORT = new Type("GC concurrent-mark-abort", "GC concurrent-mark-abort", Generation.TENURED, Concurrency.CONCURRENT, Pattern.GC);
        public static final Type G1_CONCURRENT_COUNT_START = new Type("GC concurrent-count-start", "GC concurrent-count-start", Generation.TENURED, Concurrency.CONCURRENT, Pattern.GC);
        public static final Type G1_CONCURRENT_COUNT_END = new Type("GC concurrent-count-end", "GC concurrent-count-end,", Generation.TENURED, Concurrency.CONCURRENT, Pattern.GC_PAUSE);
        public static final Type G1_CONCURRENT_CLEANUP_START = new Type("GC concurrent-cleanup-start", "GC concurrent-cleanup-start", Generation.TENURED, Concurrency.CONCURRENT, Pattern.GC);
        public static final Type G1_CONCURRENT_CLEANUP_END = new Type("GC concurrent-cleanup-end", "GC concurrent-cleanup-end,", Generation.TENURED, Concurrency.CONCURRENT, Pattern.GC_PAUSE);
    }

    public static enum Pattern {
    	// <timestamp>: [<GC type>]
    	GC,
    	// <timestamp>: [<GC type>, <pause>]
    	GC_PAUSE,
    	// <timestamp>: [<GC type> <mem before>-><mem after>(<mem total>), <pause>]
    	GC_MEMORY_PAUSE} 
    
    public static enum Concurrency {CONCURRENT, SERIAL};

    public static enum Generation { YOUNG, TENURED, PERM, ALL };
}
