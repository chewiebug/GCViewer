package com.tagtraum.perf.gcviewer.model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * The abstract gc event is the base class for all types of events. All sorts of general
 * information can be queried from it and it provides the possibility to add detail events.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 */
public abstract class AbstractGCEvent<T extends AbstractGCEvent<T>> implements Serializable, Cloneable {
    /** Used before GC in KB */
    private int preUsed;
    /** Used after GC in KB */
    private int postUsed;
    /** Capacity in KB */
    private int total;
    /** end of gc event (after pause) */
    private ZonedDateTime datestamp;
    /** end of gc event (after pause) */
    private double timestamp;
    private ExtendedType extendedType = ExtendedType.UNDEFINED;
    private String typeAsString;
    protected Generation generation;
    protected List<T> details;
    private double pause;
    private int number = -1;
    private List<AbstractGCEvent<?>> phases;

    public Iterator<T> details() {
        if (details == null) return Collections.emptyIterator();
        return details.iterator();
    }

    public void add(T detail) {
        // most events have only one detail event
        if (details == null) {
        	details = new ArrayList<T>(2);
        }
        details.add(detail);
        typeAsString += "; " + detail.getExtendedType().getName();

        // reset cached value, which will be recalculated upon call to "getGeneration()"
        generation = null;
    }

    public boolean hasDetails() {
        return details != null
                && details.size() > 0;
    }

    public List<AbstractGCEvent<?>> getPhases() {
        if (phases == null) {
            return new ArrayList<>();
        }
        return phases;
    }

    public void addPhase(AbstractGCEvent<?> phase) {
        if (phase == null) {
            throw new IllegalArgumentException("Cannot add null phase to an event");
        }
        if (phases == null) {
            phases = new ArrayList<>();
        }

        phases.add(phase);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        AbstractGCEvent<T> clonedEvent = (AbstractGCEvent<T>)super.clone();
        if (getDatestamp() != null) {
            clonedEvent.setDateStamp(ZonedDateTime.from(this.getDatestamp()));
        }
        if (getExtendedType() != null) {
            clonedEvent.setExtendedType(new ExtendedType(getExtendedType().getType(), getExtendedType().fullName));
        }
        if (details != null) {
            List<T> detailClones = new ArrayList<>();
            for (T t : details) {
                detailClones.add((T)t.clone());
            }
            clonedEvent.details = detailClones;
        }

        // don't need to do anything with "generation", because that value is reset by the "add()" method

        return clonedEvent;
    }

    public AbstractGCEvent<T> cloneAndMerge(AbstractGCEvent<T> otherEvent) {
        try {
            AbstractGCEvent<T> clonedEvent = (AbstractGCEvent<T>)otherEvent.clone();
            clonedEvent.setExtendedType(new ExtendedType(getExtendedType().getType(), getExtendedType().fullName + "+" + clonedEvent.getExtendedType().fullName));
            clonedEvent.setPreUsed(clonedEvent.getPreUsed() + getPreUsed());
            clonedEvent.setPostUsed(clonedEvent.getPostUsed() + getPostUsed());
            clonedEvent.setTotal(clonedEvent.getTotal() + getTotal());
            clonedEvent.setPause(clonedEvent.getPause() + getPause());
            return clonedEvent;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("hmm, clone was not supported, that's unexpected...", e);
        }
    }

    public void setDateStamp(ZonedDateTime datestamp) {
    	this.datestamp = datestamp;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public void setType(Type type) {
        setExtendedType(ExtendedType.lookup(type));
    }

    public void setExtendedType(ExtendedType extendedType) {
        this.extendedType = extendedType;
        this.typeAsString = extendedType.getName();
        if (details != null && details.size() > 0) {
            this.typeAsString = buildTypeAsString();
        }
    }

    private String buildTypeAsString() {
        StringBuilder sb = new StringBuilder(getExtendedType().getName());
        if (details != null) {
            for (T event : details) {
                sb.append("; ").append(event.getExtendedType().getName());
            }
        }

        return sb.toString();
    }

    public ExtendedType getExtendedType() {
        return extendedType;
    }

    public int getNumber() {
        return number;
    }

    public String getTypeAsString() {
    	return typeAsString;
    }

    public boolean isStopTheWorld() {
        boolean isStopTheWorld = getExtendedType().getConcurrency() == Concurrency.SERIAL;
        if (details != null) {
            for (T detailEvent : details) {
                if (!isStopTheWorld) {
                    isStopTheWorld = detailEvent.getExtendedType().getConcurrency() == Concurrency.SERIAL;
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
                generation = getExtendedType().getGeneration();
            }
            else {
                // find out, what generations the detail events contain
                Set<Generation> generationSet = new TreeSet<Generation>();
                for (T detailEvent : details) {
                    generationSet.add(detailEvent.getExtendedType().getGeneration());
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

    public ZonedDateTime getDatestamp() {
        return datestamp;
    }

    public boolean hasMemoryInformation() {
        return getPreUsed() > 0
                || getPostUsed() > 0
                || getTotal() > 0;
    }

    public void setPreUsed(int preUsed) {
        this.preUsed = preUsed;
    }

    public void setPostUsed(int postUsed) {
        this.postUsed = postUsed;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPreUsed() {
        return preUsed;
    }

    public int getPostUsed() {
        return postUsed;
    }

    public int getTotal() {
        return total;
    }

    public abstract void toStringBuffer(StringBuffer sb);

    @Override
	public String toString() {
        StringBuffer sb = new StringBuffer(128);
        toStringBuffer(sb);
        return sb.toString();
    }

    public boolean isFull() {
        if (getExtendedType().getGeneration().compareTo(Generation.ALL) == 0) {
            return true;
        }

        if (details != null) {
            // the assumption is, that a full collection is everything, that collects from more
            // than one generation.
            // Probably this is not always strictly right, but often enough a good assumption
            return details.size() > 1;
        }
        return false;
    }

    /**
     * Returns true, if this event was triggered by a call to "System.gc()"
     * @return <code>true</code> if triggered by "System.gc()"
     */
    public boolean isSystem() {
        return getExtendedType().getName().contains("System");
    }

    public boolean isInc() {
        return getExtendedType().getType() == GCEvent.Type.INC_GC;
    }

    public boolean isConcurrent() {
        return getExtendedType().getConcurrency().equals(Concurrency.CONCURRENT);
    }

    public boolean isConcurrencyHelper() {
        return getExtendedType().getCollectionType().equals(CollectionType.CONCURRENCY_HELPER);
    }

    public boolean isConcurrentCollectionStart() {
        return getExtendedType().getName().equals(Type.CMS_CONCURRENT_MARK_START.getName()) // CMS
                || getExtendedType().getName().equals(Type.ASCMS_CONCURRENT_MARK_START.getName()) // CMS AdaptiveSizePolicy
                || (getExtendedType().getName().equals(Type.UJL_CMS_CONCURRENT_MARK.getName()) && getPause() > 0.000001) // Universal jvm logging, CMS
                || getExtendedType().getName().equals(Type.G1_CONCURRENT_MARK_START.getName()) // G1
                || (getExtendedType().getName().equals(Type.UJL_G1_CONCURRENT_CYCLE.getName()) && getPause() < 0.00001) // Universal jvm logging, G1
                || getExtendedType().getName().equals(Type.UJL_SHEN_CONCURRENT_RESET.getName()); // Universal Jvm logging, Shenandoah
    }

    public boolean isConcurrentCollectionEnd() {
        return getExtendedType().getName().equals(Type.CMS_CONCURRENT_RESET.getName()) // CMS
                || getExtendedType().getName().equals(Type.ASCMS_CONCURRENT_RESET.getName()) // CMS AdaptiveSizePolicy
                || (getExtendedType().getName().equals(Type.UJL_CMS_CONCURRENT_RESET.getName()) && getPause() > 0.0000001) // Universal jvm logging, CMS
                || getExtendedType().getName().equals(Type.G1_CONCURRENT_CLEANUP_END.getName()) // G1
                || (getExtendedType().getName().equals(Type.UJL_G1_CONCURRENT_CYCLE.getName()) && getPause() > 0.0000001) // Universal jvm logging, G1
                || getExtendedType().getName().equals(Type.UJL_SHEN_CONCURRENT_CLEANUP.getName()); // Universal jvm logging, Shenandoah
    }

    public boolean isInitialMark() {
        return getTypeAsString().indexOf("initial-mark") >= 0      // all others
                || getTypeAsString().indexOf("Initial Mark") >= 0 // Unified jvm logging, CMS + G1
                || getTypeAsString().indexOf("Init Mark") >= 0; // Unified jvm logging, Shenandoah
    }

    public boolean isRemark() {
        return getTypeAsString().indexOf(Type.CMS_REMARK.getName()) >= 0
                || getTypeAsString().indexOf(Type.ASCMS_REMARK.getName()) >= 0
                || getTypeAsString().indexOf(Type.G1_REMARK.getName()) >= 0
                || getTypeAsString().indexOf(Type.UJL_PAUSE_REMARK.getName()) >= 0
                || getTypeAsString().indexOf(Type.UJL_SHEN_FINAL_MARK.getName()) >= 0;
    }

    public boolean hasPause() {
        return getExtendedType().getPattern().equals(GcPattern.GC_MEMORY_PAUSE)
                || getExtendedType().getPattern().equals(GcPattern.GC_PAUSE)
                || getExtendedType().getPattern().equals(GcPattern.GC_PAUSE_DURATION);
    }

    public double getPause() {
        return pause;
    }

    public void setPause(double pause) {
        this.pause = pause;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AbstractGCEvent))
            return false;
        AbstractGCEvent<?> that = (AbstractGCEvent<?>) o;
        return Double.compare(that.timestamp, timestamp) == 0 &&
                Double.compare(that.pause, pause) == 0 &&
                Objects.equals(datestamp, that.datestamp) &&
                Objects.equals(extendedType, that.extendedType) &&
                Objects.equals(typeAsString, that.typeAsString) &&
                generation == that.generation &&
                Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datestamp, timestamp, extendedType, typeAsString, generation, details);
    }

    /**
     * Wrapper for the {@link Type} class adding a field for the full type name. That name may
     * be different from the name in <code>Type</code>. Since all other attributes of
     * <code>Type</code> are shared, only this attribute is additionally held.
     *
     * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
     */
    public static class ExtendedType implements Serializable {
        private static final Map<String, ExtendedType> WRAPPER_MAP = new HashMap<>();
        static {
            WRAPPER_MAP.put(Type.UNDEFINED.getName(), new ExtendedType(Type.UNDEFINED));
        }
        public static final ExtendedType UNDEFINED = WRAPPER_MAP.get(Type.UNDEFINED);

        private String fullName;
        private Type type;

        private ExtendedType(Type type) {
            this(type, type.getName());
        }

        private ExtendedType(Type type, String fullName) {
            this.type = type;
            this.fullName = fullName.intern();
        }

        public static ExtendedType lookup(Type type) {
            return lookup(type, type.getName());
        }

        public static ExtendedType lookup(Type type, String fullName) {
            ExtendedType extType = WRAPPER_MAP.get(fullName);
            if (extType == null) {
                extType = new ExtendedType(type, fullName);
                WRAPPER_MAP.put(fullName, extType);
            }


            return extType;
        }

        public String getName() {
            return fullName;
        }

        public Type getType() {
            return type;
        }

        public GcPattern getPattern() {
            return type.getPattern();
        }

        public Generation getGeneration() {
            return type.getGeneration();
        }

        public CollectionType getCollectionType() {
            return type.getCollectionType();
        }

        public Concurrency getConcurrency() {
            return type.getConcurrency();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ExtendedType that = (ExtendedType) o;
            return Objects.equals(fullName, that.fullName);
        }

        @Override
        public int hashCode() {

            return Objects.hash(fullName);
        }

        @Override
        public String toString() {
            return fullName;
        }
	}

    /**
     * Representation of an event type
     */
    public static class Type implements Serializable {
        private String name;
        private Generation generation;
        private Concurrency concurrency;
        /** pattern this event has in the logfile */
        private GcPattern pattern;
        private CollectionType collectionType;
        private static final Map<String, Type> TYPE_MAP = new HashMap<String, Type>();

        private Type(String name, Generation generation) {
            this(name, generation, Concurrency.SERIAL);
        }

        private Type(String name, Generation generation, Concurrency concurrency) {
        	this(name, generation, concurrency, GcPattern.GC_MEMORY_PAUSE);
        }

        private Type(String name, Generation generation, Concurrency concurrency, GcPattern pattern) {
            this(name, generation, concurrency, pattern, CollectionType.COLLECTION);
        }

        private Type(String name, Generation generation, Concurrency concurrency, GcPattern pattern, CollectionType collectionType) {
            this.name = name.intern();
            this.generation = generation;
            this.concurrency = concurrency;
            this.pattern = pattern;
            this.collectionType = collectionType;

            TYPE_MAP.put(this.name, this);
        }

        public static Type lookup(String type) {
            return TYPE_MAP.get(type.trim());
        }

        public String getName() {
            return name;
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

        @Override
        public String toString() {
            return name;
        }

        public static final Type UNDEFINED = new Type("undefined", Generation.YOUNG);

        // TODO: is jrockit GC really of type Generation.ALL or rather Generation.TENURED ?
        public static final Type JROCKIT_GC = new Type("jrockit.GC", Generation.TENURED);
        public static final Type JROCKIT_NURSERY_GC = new Type("jrockit.Nursery GC", Generation.YOUNG);
        public static final Type JROCKIT_PARALLEL_NURSERY_GC = new Type("jrockit.parallel nursery GC", Generation.YOUNG);

        public static final Type JROCKIT_16_OLD_GC = new Type("jrockit.OC", Generation.TENURED);
        public static final Type JROCKIT_16_YOUNG_GC = new Type("jrockit.YC", Generation.YOUNG);
        public static final Type JROCKIT_16_PARALLEL_NURSERY_GC = new Type("jrockit.parallel nursery GC", Generation.YOUNG);

        // Sun JDK 1.5
        public static final Type SCAVENGE_BEFORE_REMARK = new Type("Scavenge-Before-Remark", Generation.ALL);

        public static final Type FULL_GC = new Type("Full GC", Generation.ALL);
        public static final Type GC = new Type("GC", Generation.YOUNG);
        public static final Type DEF_NEW = new Type("DefNew", Generation.YOUNG, Concurrency.SERIAL); // single threaded
        public static final Type PAR_NEW = new Type("ParNew", Generation.YOUNG); // parallel
        public static final Type ASPAR_NEW = new Type("ASParNew", Generation.YOUNG); // parallel (CMS AdaptiveSizePolicy)
        public static final Type PAR_OLD_GEN = new Type("ParOldGen", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY);
        public static final Type PS_YOUNG_GEN = new Type("PSYoungGen", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_MEMORY);
        public static final Type PS_OLD_GEN = new Type("PSOldGen", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY);
        public static final Type PS_PERM_GEN = new Type("PSPermGen", Generation.PERM, Concurrency.SERIAL, GcPattern.GC_MEMORY);
        public static final Type TENURED = new Type("Tenured", Generation.TENURED);
        public static final Type INC_GC = new Type("Inc GC", Generation.YOUNG);
        public static final Type TRAIN = new Type("Train", Generation.TENURED);
        public static final Type TRAIN_MSC = new Type("Train MSC", Generation.TENURED);
        public static final Type PERM = new Type("Perm", Generation.PERM, Concurrency.SERIAL, GcPattern.GC_MEMORY);
        // since about java 7_u45 these have a time stamp prepended
        public static final Type APPLICATION_STOPPED_TIME = new Type("Total time for which application threads were stopped", Generation.OTHER, Concurrency.SERIAL, GcPattern.GC_PAUSE, CollectionType.VM_OPERATION);
        // java 8: perm gen is moved to metaspace
        public static final Type Metaspace = new Type("Metaspace", Generation.PERM, Concurrency.SERIAL, GcPattern.GC_MEMORY);

        // CMS types
        public static final Type CMS = new Type("CMS", Generation.TENURED);
        public static final Type CMS_PERM = new Type("CMS Perm", Generation.PERM, Concurrency.SERIAL, GcPattern.GC_MEMORY);

        // Parnew (promotion failed)
         public static final Type PAR_NEW_PROMOTION_FAILED = new Type("ParNew (promotion failed)", Generation.YOUNG, Concurrency.SERIAL);

        // CMS (concurrent mode failure / interrupted)
         public static final Type CMS_CMF = new Type("CMS (concurrent mode failure)", Generation.TENURED, Concurrency.SERIAL);
         public static final Type CMS_CMI = new Type("CMS (concurrent mode interrupted)", Generation.TENURED, Concurrency.SERIAL);

        // CMS (Concurrent Mark Sweep) Event Types
        public static final Type CMS_CONCURRENT_MARK_START = new Type("CMS-concurrent-mark-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type CMS_CONCURRENT_MARK = new Type("CMS-concurrent-mark", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);
        public static final Type CMS_CONCURRENT_PRECLEAN_START = new Type("CMS-concurrent-preclean-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type CMS_CONCURRENT_PRECLEAN = new Type("CMS-concurrent-preclean", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);
        public static final Type CMS_CONCURRENT_SWEEP_START = new Type("CMS-concurrent-sweep-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type CMS_CONCURRENT_SWEEP = new Type("CMS-concurrent-sweep", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);
        public static final Type CMS_CONCURRENT_RESET_START = new Type("CMS-concurrent-reset-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type CMS_CONCURRENT_RESET = new Type("CMS-concurrent-reset", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);
        public static final Type CMS_CONCURRENT_ABORTABLE_PRECLEAN_START = new Type("CMS-concurrent-abortable-preclean-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type CMS_CONCURRENT_ABORTABLE_PRECLEAN = new Type("CMS-concurrent-abortable-preclean", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);

        public static final Type CMS_INITIAL_MARK = new Type("CMS-initial-mark", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY, CollectionType.CONCURRENCY_HELPER);
        public static final Type CMS_REMARK = new Type("CMS-remark", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY, CollectionType.CONCURRENCY_HELPER);

        // CMS (Concurrent Mark Sweep) AdaptiveSizePolicy Event Types
        public static final Type ASCMS = new Type("ASCMS", Generation.TENURED);

        // Parnew (promotion failed) AdaptiveSizePolicy
         public static final Type ASPAR_NEW_PROMOTION_FAILED = new Type("ASParNew (promotion failed)", Generation.YOUNG, Concurrency.SERIAL);

        // CMS (concurrent mode failure / interrupted) AdaptiveSizePolicy
        public static final Type ASCMS_CMF = new Type("ASCMS (concurrent mode failure)", Generation.TENURED, Concurrency.SERIAL);
        public static final Type ASCMS_CMI = new Type("ASCMS (concurrent mode interrupted)", Generation.TENURED, Concurrency.SERIAL);

        public static final Type ASCMS_CONCURRENT_MARK_START = new Type("ASCMS-concurrent-mark-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type ASCMS_CONCURRENT_MARK = new Type("ASCMS-concurrent-mark", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);
        public static final Type ASCMS_CONCURRENT_PRECLEAN_START = new Type("ASCMS-concurrent-preclean-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type ASCMS_CONCURRENT_PRECLEAN = new Type("ASCMS-concurrent-preclean", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);
        public static final Type ASCMS_CONCURRENT_SWEEP_START = new Type("ASCMS-concurrent-sweep-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type ASCMS_CONCURRENT_SWEEP = new Type("ASCMS-concurrent-sweep", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);
        public static final Type ASCMS_CONCURRENT_RESET_START = new Type("ASCMS-concurrent-reset-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type ASCMS_CONCURRENT_RESET = new Type("ASCMS-concurrent-reset", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);
        public static final Type ASCMS_CONCURRENT_ABORTABLE_PRECLEAN_START = new Type("ASCMS-concurrent-abortable-preclean-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type ASCMS_CONCURRENT_ABORTABLE_PRECLEAN = new Type("ASCMS-concurrent-abortable-preclean", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE_DURATION);

        public static final Type ASCMS_INITIAL_MARK = new Type("ASCMS-initial-mark", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_PAUSE, CollectionType.CONCURRENCY_HELPER);
        public static final Type ASCMS_REMARK = new Type("ASCMS-remark", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY, CollectionType.CONCURRENCY_HELPER);

        // only young collection
        public static final Type G1_YOUNG = new Type("GC pause (young)", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_YOUNG_MARK_STACK_FULL = new Type("GC pause (young)Mark stack is full.", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_YOUNG_TO_SPACE_OVERFLOW = new Type("GC pause (young) (to-space overflow)", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        // java 7 (>u25) / 8 renamed "to-space overflow" to "to-space exhausted"
        public static final Type G1_YOUNG_TO_SPACE_EXHAUSTED = new Type("GC pause (young) (to-space exhausted)", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        // partially young collection (
        public static final Type G1_PARTIAL = new Type("GC pause (partial)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_PARTIAL_TO_SPACE_OVERFLOW = new Type("GC pause (partial) (to-space overflow)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        // mixed collection (might have replaced "partial" collection in jdk1.7.0_u5)
        public static final Type G1_MIXED = new Type("GC pause (mixed)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_MIXED_TO_SPACE_OVERFLOW = new Type("GC pause (mixed) (to-space overflow)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_MIXED_TO_SPACE_EXHAUSTED = new Type("GC pause (mixed) (to-space exhausted)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);

        // TODO: Generation: young and tenured!
        public static final Type G1_YOUNG_INITIAL_MARK = new Type("GC pause (young) (initial-mark)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_YOUNG_INITIAL_MARK_TO_SPACE_OVERFLOW = new Type("GC pause (young) (to-space overflow) (initial-mark)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        // The following two Types are basically the same but in a different order. In JDK 6 the order was defined, no longer the case with JDK 7 (see: https://github.com/chewiebug/GCViewer/issues/100)
        public static final Type G1_YOUNG_INITIAL_MARK_TO_SPACE_EXHAUSTED = new Type("GC pause (young) (initial-mark) (to-space exhausted)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_YOUNG_TO_SPACE_EXHAUSTED_INITIAL_MARK = new Type("GC pause (young) (to-space exhausted) (initial-mark)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_PARTIAL_INITIAL_MARK = new Type("GC pause (partial) (initial-mark)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_PARTIAL_INITIAL_MARK_TO_SPACE_OVERFLOW = new Type("GC pause (partial) (to-space overflow) (initial-mark)", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type G1_REMARK = new Type("GC remark", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_PAUSE, CollectionType.CONCURRENCY_HELPER);
        // Java 7; detail event inside G1_REMARK
        public static final Type G1_GC_REFPROC = new Type("GC ref-proc", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_PAUSE, CollectionType.CONCURRENCY_HELPER);
        public static final Type G1_CLEANUP = new Type("GC cleanup", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE, CollectionType.CONCURRENCY_HELPER);
        // Java 7_u2; detailed info in all detailed events
        public static final Type G1_EDEN = new Type("Eden", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);

        // G1 concurrent types
        public static final Type G1_CONCURRENT_ROOT_REGION_SCAN_START = new Type("GC concurrent-root-region-scan-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type G1_CONCURRENT_ROOT_REGION_SCAN_END = new Type("GC concurrent-root-region-scan-end", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type G1_CONCURRENT_MARK_START = new Type("GC concurrent-mark-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type G1_CONCURRENT_MARK_END = new Type("GC concurrent-mark-end", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type G1_CONCURRENT_MARK_ABORT = new Type("GC concurrent-mark-abort", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type G1_CONCURRENT_MARK_RESET_FOR_OVERFLOW = new Type("GC concurrent-mark-reset-for-overflow", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type G1_CONCURRENT_COUNT_START = new Type("GC concurrent-count-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type G1_CONCURRENT_COUNT_END = new Type("GC concurrent-count-end", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type G1_CONCURRENT_CLEANUP_START = new Type("GC concurrent-cleanup-start", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC);
        public static final Type G1_CONCURRENT_CLEANUP_END = new Type("GC concurrent-cleanup-end", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);

        // unified jvm logging generic event types
        public static final Type UJL_PAUSE_YOUNG = new Type("Pause Young", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type UJL_PAUSE_FULL = new Type("Pause Full", Generation.ALL, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);

        // unified jvm logging serial / cms event phase types
        public static final Type UJL_SERIAL_PHASE_MARK_LIFE_OBJECTS = new Type("Phase 1: Mark live objects", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_SERIAL_PHASE_COMPUTE_NEW_OBJECT_ADDRESSES = new Type("Phase 2: Compute new object addresses", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_SERIAL_PHASE_ADJUST_POINTERS = new Type("Phase 3: Adjust pointers", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_SERIAL_PHASE_MOVE_OBJECTS = new Type("Phase 4: Move objects", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_PAUSE);

        // unified jvm logging parallel event phase types
        public static final Type UJL_PARALLEL_PHASE_MARKING = new Type("Marking Phase", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_PARALLEL_PHASE_SUMMARY = new Type("Summary Phase", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_PARALLEL_PHASE_ADJUST_ROOTS = new Type("Adjust Roots", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_PARALLEL_PHASE_COMPACTION = new Type("Compaction Phase", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_PARALLEL_PHASE_POST_COMPACT = new Type("Post Compact", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_PAUSE);

        // unified jvm logging cms / g1 event types
        public static final Type UJL_PAUSE_INITIAL_MARK = new Type("Pause Initial Mark", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE, CollectionType.CONCURRENCY_HELPER);
        public static final Type UJL_PAUSE_REMARK = new Type("Pause Remark", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE, CollectionType.CONCURRENCY_HELPER);

        // unified jvm logging cms event types
        public static final Type UJL_CMS_CONCURRENT_MARK = new Type("Concurrent Mark", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type UJL_CMS_CONCURRENT_PRECLEAN = new Type("Concurrent Preclean", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type UJL_CMS_CONCURRENT_ABORTABLE_PRECLEAN = new Type("Concurrent Abortable Preclean", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type UJL_CMS_CONCURRENT_SWEEP = new Type("Concurrent Sweep", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type UJL_CMS_CONCURRENT_RESET = new Type("Concurrent Reset", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type UJL_CMS_CONCURRENT_OLD = new Type("Old", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_MEMORY);

        // unified jvm logging g1 event types
        public static final Type UJL_G1_PAUSE_MIXED = new Type("Pause Mixed", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type UJL_G1_TO_SPACE_EXHAUSTED = new Type("To-space exhausted", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC);
        public static final Type UJL_G1_CONCURRENT_CYCLE = new Type("Concurrent Cycle", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type UJL_G1_PAUSE_CLEANUP = new Type("Pause Cleanup", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE, CollectionType.CONCURRENCY_HELPER);
        public static final Type UJL_G1_EDEN = new Type("Eden regions", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_REGION);
        public static final Type UJL_G1_SURVIVOR = new Type("Survivor regions", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_REGION);
        public static final Type UJL_G1_OLD = new Type("Old regions", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_REGION);
        public static final Type UJL_G1_HUMongous = new Type("Humongous regions", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_REGION);
        public static final Type UJL_G1_ARCHIVE = new Type("Archive regions", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_REGION);

        public static final Type UJL_G1_PHASE_PRE_EVACUATE_COLLECTION_SET = new Type("Pre Evacuate Collection Set", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_G1_PHASE_EVACUATE_COLLECTION_SET = new Type("Evacuate Collection Set", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_G1_PHASE_POST_EVACUATE_COLLECTION_SET = new Type("Post Evacuate Collection Set", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_G1_PHASE_OTHER = new Type("Other", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_G1_PHASE_PREPARE_FOR_COMPACTION = new Type("Phase 2: Prepare for compaction", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_G1_PHASE_COMPACT_HEAP = new Type("Phase 4: Compact heap", Generation.YOUNG, Concurrency.SERIAL, GcPattern.GC_PAUSE);

        // unified jvm logging shenandoah event types
        public static final Type UJL_SHEN_INIT_MARK = new Type("Pause Init Mark", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_SHEN_FINAL_MARK = new Type("Pause Final Mark", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_SHEN_INIT_UPDATE_REFS = new Type("Pause Init Update Refs", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_SHEN_FINAL_UPDATE_REFS = new Type("Pause Final Update Refs", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_SHEN_DEGENERATED_GC = new Type("Pause Degenerated GC", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PAUSE);
        public static final Type UJL_SHEN_CONCURRENT_CONC_MARK = new Type("Concurrent marking", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_MEMORY_PAUSE);
        public static final Type UJL_SHEN_CONCURRENT_CONC_EVAC = new Type("Concurrent evacuation", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_MEMORY_PAUSE);
        public static final Type UJL_SHEN_CONCURRENT_CANCEL_CONC_MARK = new Type("Cancel concurrent mark", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type UJL_SHEN_CONCURRENT_RESET = new Type("Concurrent reset", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_MEMORY_PAUSE);
        public static final Type UJL_SHEN_CONCURRENT_CONC_RESET_BITMAPS = new Type("Concurrent reset bitmaps", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_MEMORY_PAUSE);
        public static final Type UJL_SHEN_CONCURRENT_CONC_UPDATE_REFS = new Type("Concurrent update references", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_MEMORY_PAUSE);
        public static final Type UJL_SHEN_CONCURRENT_CLEANUP = new Type("Concurrent cleanup", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_MEMORY_PAUSE);
        public static final Type UJL_SHEN_CONCURRENT_PRECLEANING = new Type("Concurrent precleaning", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_MEMORY_PAUSE);
        public static final Type UJL_SHEN_CONCURRENT_UNCOMMIT = new Type("Concurrent uncommit", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_MEMORY_PAUSE);

        // unified jvm logging ZGC event types
        public static final Type UJL_ZGC_GARBAGE_COLLECTION = new Type("Garbage Collection", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_MEMORY_PERCENTAGE);
        public static final Type UJL_ZGC_PAUSE_MARK_START = new Type("Pause Mark Start", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_ZGC_PAUSE_MARK_END = new Type("Pause Mark End", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_ZGC_PAUSE_RELOCATE_START = new Type("Pause Relocate Start", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_PAUSE);
        public static final Type UJL_ZGC_CONCURRENT_MARK = new Type("Concurrent Mark", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type UJL_ZGC_CONCURRENT_NONREF = new Type("Concurrent Process Non-Strong References", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type UJL_ZGC_CONCURRENT_RESET_RELOC_SET = new Type("Concurrent Reset Relocation Set", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type UJL_ZGC_CONCURRENT_DETATCHED_PAGES = new Type("Concurrent Destroy Detached Pages", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type UJL_ZGC_CONCURRENT_SELECT_RELOC_SET = new Type("Concurrent Select Relocation Set", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type UJL_ZGC_CONCURRENT_PREPARE_RELOC_SET = new Type("Concurrent Prepare Relocation Set", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type UJL_ZGC_CONCURRENT_RELOCATE = new Type("Concurrent Relocate", Generation.TENURED, Concurrency.CONCURRENT, GcPattern.GC_PAUSE);
        public static final Type UJL_ZGC_HEAP_CAPACITY = new Type("Capacity", Generation.TENURED, Concurrency.SERIAL, GcPattern.GC_HEAP_MEMORY_PERCENTAGE);

        // IBM Types
        // TODO: are scavenge always young only??
        public static final Type IBM_AF = new Type("af", Generation.YOUNG);
        public static final Type IBM_SYS = new Type("sys explicit", Generation.ALL);
        public static final Type IBM_AF_SCAVENGE = new Type("af scavenge", Generation.YOUNG);
        public static final Type IBM_AF_GLOBAL = new Type("af global", Generation.TENURED);
        public static final Type IBM_SYS_GLOBAL = new Type("sys global", Generation.ALL);
        public static final Type IBM_SYS_EXPLICIT_GLOBAL = new Type("sys explicit global", Generation.ALL);
        public static final Type IBM_SCAVENGE = new Type("scavenge", Generation.YOUNG, Concurrency.SERIAL);
        public static final Type IBM_GLOBAL = new Type("global", Generation.ALL, Concurrency.SERIAL);
        public static final Type IBM_NURSERY = new Type("nursery", Generation.YOUNG);
        public static final Type IBM_TENURE = new Type("tenure", Generation.TENURED);

        public static final Type IBM_CONCURRENT_COLLECTION_START = new Type("concurrent-collection-start", Generation.ALL, Concurrency.CONCURRENT);

    }

    /**
     * Patterns of additional information for gc events.
     * <ul>
     *     <li><code>GC</code>: just the name of the event type</li>
     *     <li><code>PAUSE</code>: length of a pause</li>
     *     <li><code>DURATION</code>: cycle time of a (usually concurrent) event</li>
     *     <li><code>MEMORY</code>: information about heap changes</li>
     *     <li><code>REGION</code>: information about number of regions used (only G1 up to now)</li>
     * </ul>
     */
    public enum GcPattern {
        /** "GC type" (just the name, no additional information) */
        GC,
        /** "GC type": "pause" */
        GC_PAUSE,
        /** "GC type": "pause"/"duration" */
        GC_PAUSE_DURATION,
        /** "GC type": "memory current"("memory total") */
        GC_MEMORY,
        /** "GC type": "memory before"-&gt;"memory after"("memory total"), "pause" */
        GC_MEMORY_PAUSE,
        /** "GC type": "# regions before"-&gt;"# regions after"[("#total regions")] ("total regions" is optional; needs a region size to calculate memory usage)*/
        GC_REGION,
        /** "Garbage Collection (Reason)" "memory before"("percentage of total")-&gt;"memory after"("percentage of total") */
        GC_MEMORY_PERCENTAGE,
        /** "Heap memory type" "memory current"("memory percentage") */
        GC_HEAP_MEMORY_PERCENTAGE
    }

    public enum Concurrency { CONCURRENT, SERIAL }

    public enum Generation { YOUNG,
        TENURED,
        /** also used for "metaspace" that is introduced with java 8 */
        PERM,
        ALL,
        /** special value for vm operations that are not collections */
        OTHER }

    public enum CollectionType {
        /** plain GC pause collection garbage */
        COLLECTION,
        /**
         * Not really a collection, but some other event that stops the vm.
         *
         * @see <a href="http://stackoverflow.com/questions/2850514/meaning-of-message-operations-coalesced-during-safepoint">
         * Stackoverflow: meaning of message operations coalesced during safepoint</a>
         */
        VM_OPERATION,
        /** stop the world pause but used to prepare concurrent collection, might not collect garbage */
        CONCURRENCY_HELPER }
}
