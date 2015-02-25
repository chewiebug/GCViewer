package com.tagtraum.perf.gcviewer.model;

/**
 * Stop the world events that are not GCEvents, but some other vm operations.
 *
 * @see <a href="http://stackoverflow.com/questions/2850514/meaning-of-message-operations-coalesced-during-safepoint">meaning-of-message-operations-coalesced-during-safepoint</a>
 * @see <a href="https://blogs.oracle.com/jonthecollector/entry/the_unspoken_gc_times">the_unspoken_gc_times</a>
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 */
public class VmOperationEvent extends AbstractGCEvent<VmOperationEvent> {

    @Override
    public void toStringBuffer(StringBuffer sb) {
        sb.append(getTimestamp());
        sb.append(": [");
        sb.append(getExtendedType().getName());
        sb.append(": ");
        sb.append(getPause());
        sb.append(" secs]");
    }

}
