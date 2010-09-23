package com.tagtraum.perf.gcviewer;

import java.io.Serializable;
import java.util.*;

/**
 *
 * Date: Jun 1, 2005
 * Time: 1:47:32 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public abstract class AbstractGCEvent implements Serializable {
    private static final Iterator EMPTY_ITERATOR = Collections.EMPTY_LIST.iterator();
    private double timestamp;
    private Type type = Type.GC;
    private boolean tenuredDetail;
    protected List details;

    public Iterator details() {
        if (details == null) return EMPTY_ITERATOR;
        return details.iterator();
    }

    public void add(AbstractGCEvent detail) {
        // most events have only one detail event
        if (details == null) details = new ArrayList(2);
        details.add(detail);
        if (detail.getType().getGeneration() == Generation.TENURED) tenuredDetail = true;
    }

    public boolean hasTenuredDetail() {
        return tenuredDetail;
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

    public double getTimestamp() {
        return timestamp;
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
        private GCEvent.Generation generation;
        private GCEvent.Concurrency concurrency;
        private static final Map TYPE_MAP = new HashMap();

        private Type(String type, GCEvent.Generation generation) {
            this(type, type, generation);
        }

        private Type(String type, String rep, GCEvent.Generation generation) {
            this(type, rep, generation, GCEvent.Concurrency.SERIAL);
        }

        private Type(String type, String rep, GCEvent.Generation generation, GCEvent.Concurrency concurrency) {
            this.type = type.intern();
            this.rep = rep;
            this.generation = generation;
            this.concurrency = concurrency;
            TYPE_MAP.put(this.type, this);
        }

        public static GCEvent.Type parse(String type) {
            type = type.trim();
            GCEvent.Type gcType = (GCEvent.Type)TYPE_MAP.get(type);
            if (gcType == null && type.endsWith(GCEvent.Type.CMS_INITIAL_MARK.getType())) gcType = GCEvent.Type.CMS_INITIAL_MARK;
            return gcType;
        }

        public static GCEvent.Type parse(final int reason) {
            return reason == -1 ? Type.GC : Type.FULL_GC;
        }

        public static GCEvent.Type parse(final int typeOfGC, final float details) {
            final GCEvent.Type type;
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

        public GCEvent.Generation getGeneration() {
            return generation;
        }

        public GCEvent.Concurrency getConcurrency() {
            return concurrency;
        }

        public String toString() {
            return rep;
        }

        // TODO: is jrockit GC really of type Generation.ALL or rather Generation.TENURED ?
        public static final GCEvent.Type JROCKIT_GC = new GCEvent.Type("jrockit.GC", GCEvent.Generation.TENURED);
        public static final GCEvent.Type JROCKIT_NURSERY_GC = new GCEvent.Type("jrockit.Nursery GC", GCEvent.Generation.YOUNG);
        public static final GCEvent.Type JROCKIT_PARALLEL_NURSERY_GC = new GCEvent.Type("jrockit.parallel nursery GC", GCEvent.Generation.YOUNG);

        public static final GCEvent.Type FULL_GC = new GCEvent.Type("Full GC", GCEvent.Generation.ALL);
        public static final GCEvent.Type GC = new GCEvent.Type("GC", GCEvent.Generation.YOUNG);
        public static final GCEvent.Type GC__ = new GCEvent.Type("GC--", GCEvent.Generation.YOUNG);
        public static final GCEvent.Type DEF_NEW = new GCEvent.Type("DefNew", "DefNew:", GCEvent.Generation.YOUNG);
        public static final GCEvent.Type PAR_NEW = new GCEvent.Type("ParNew", "ParNew:", GCEvent.Generation.YOUNG);
        public static final GCEvent.Type PAR_OLD_GEN = new GCEvent.Type("ParOldGen", "ParOldGen:", GCEvent.Generation.TENURED);
        public static final GCEvent.Type PS_YOUNG_GEN = new GCEvent.Type("PSYoungGen", "PSYoungGen:", GCEvent.Generation.YOUNG);
        public static final GCEvent.Type PS_OLD_GEN = new GCEvent.Type("PSOldGen", "PSOldGen:", GCEvent.Generation.TENURED);
        public static final GCEvent.Type PS_PERM_GEN = new GCEvent.Type("PSPermGen", "PSPermGen:", GCEvent.Generation.PERM);
        public static final GCEvent.Type TENURED = new GCEvent.Type("Tenured", "Tenured:", GCEvent.Generation.TENURED);
        public static final GCEvent.Type INC_GC = new GCEvent.Type("Inc GC", GCEvent.Generation.YOUNG);
        public static final GCEvent.Type TRAIN = new GCEvent.Type("Train", "Train:", GCEvent.Generation.TENURED);
        public static final GCEvent.Type TRAIN_MSC = new GCEvent.Type("Train MSC", "Train MSC:", GCEvent.Generation.TENURED);
        public static final GCEvent.Type PERM = new GCEvent.Type("Perm", "Perm:", GCEvent.Generation.PERM);
        public static final GCEvent.Type CMS = new GCEvent.Type("CMS", "CMS:", GCEvent.Generation.TENURED);
        public static final GCEvent.Type CMS_PERM = new GCEvent.Type("CMS Perm", "CMS Perm :", GCEvent.Generation.PERM);
        
        // CMS (concurrent mode failure)
        public static final GCEvent.Type CMS_CMF = new GCEvent.Type("CMS (concurrent mode failure)", "CMS (concurrent mode failure):", GCEvent.Generation.TENURED, GCEvent.Concurrency.CONCURRENT);

        // CMS (Concurrent Mark Sweep) Event Types
        public static final GCEvent.Type CMS_CONCURRENT_MARK_START = new GCEvent.Type("CMS-concurrent-mark-start", "CMS-concurrent-mark-start", GCEvent.Generation.TENURED, GCEvent.Concurrency.CONCURRENT);
        public static final GCEvent.Type CMS_CONCURRENT_MARK = new GCEvent.Type("CMS-concurrent-mark", "CMS-concurrent-mark:", GCEvent.Generation.TENURED, GCEvent.Concurrency.CONCURRENT);
        public static final GCEvent.Type CMS_CONCURRENT_PRECLEAN_START = new GCEvent.Type("CMS-concurrent-preclean-start", "CMS-concurrent-preclean-start", GCEvent.Generation.TENURED, GCEvent.Concurrency.CONCURRENT);
        public static final GCEvent.Type CMS_CONCURRENT_PRECLEAN = new GCEvent.Type("CMS-concurrent-preclean", "CMS-concurrent-preclean", GCEvent.Generation.TENURED, GCEvent.Concurrency.CONCURRENT);
        public static final GCEvent.Type CMS_CONCURRENT_SWEEP_START = new GCEvent.Type("CMS-concurrent-sweep-start", "CMS-concurrent-sweep-start", GCEvent.Generation.TENURED, GCEvent.Concurrency.CONCURRENT);
        public static final GCEvent.Type CMS_CONCURRENT_SWEEP = new GCEvent.Type("CMS-concurrent-sweep", "CMS-concurrent-sweep:", GCEvent.Generation.TENURED, GCEvent.Concurrency.CONCURRENT);
        public static final GCEvent.Type CMS_CONCURRENT_RESET_START = new GCEvent.Type("CMS-concurrent-reset-start", "CMS-concurrent-reset-start", GCEvent.Generation.TENURED, GCEvent.Concurrency.CONCURRENT);
        public static final GCEvent.Type CMS_CONCURRENT_RESET = new GCEvent.Type("CMS-concurrent-reset", "CMS-concurrent-reset:", GCEvent.Generation.TENURED, GCEvent.Concurrency.CONCURRENT);
        public static final GCEvent.Type CMS_CONCURRENT_ABORTABLE_PRECLEAN_START = new GCEvent.Type("CMS-concurrent-abortable-preclean-start", "CMS-concurrent-abortable-preclean-start", GCEvent.Generation.TENURED, GCEvent.Concurrency.CONCURRENT);
        public static final GCEvent.Type CMS_CONCURRENT_ABORTABLE_PRECLEAN = new GCEvent.Type("CMS-concurrent-abortable-preclean", "CMS-concurrent-abortable-preclean:", GCEvent.Generation.TENURED, GCEvent.Concurrency.CONCURRENT);

        public static final GCEvent.Type CMS_INITIAL_MARK = new GCEvent.Type("CMS-initial-mark", "CMS-initial-mark:", GCEvent.Generation.TENURED, GCEvent.Concurrency.SERIAL);
        public static final GCEvent.Type CMS_REMARK = new GCEvent.Type("CMS-remark", "CMS-remark:", GCEvent.Generation.TENURED, GCEvent.Concurrency.SERIAL);

    }

    public static class Concurrency {
        private String name;
        private Concurrency(String name) {
            this.name = name.intern();
        }

        public String toString() {
            return name;
        }
        public static final GCEvent.Concurrency CONCURRENT = new GCEvent.Concurrency("Concurrent");
        public static final GCEvent.Concurrency SERIAL = new GCEvent.Concurrency("Serial");
    }

    public static class Generation {
        private String name;
        private Generation(String name) {
            this.name = name.intern();
        }

        public String toString() {
            return name;
        }

        public static final GCEvent.Generation YOUNG = new GCEvent.Generation("Young");
        public static final GCEvent.Generation TENURED = new GCEvent.Generation("Tenured");
        public static final GCEvent.Generation PERM = new GCEvent.Generation("Perm");
        public static final GCEvent.Generation ALL = new GCEvent.Generation("All");
    }
}
