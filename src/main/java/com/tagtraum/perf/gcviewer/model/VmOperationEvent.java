package com.tagtraum.perf.gcviewer.model;

/**
 * Stop the world events that are not GCEvents, but some other vm operations.
 * 
 * @see http://stackoverflow.com/questions/2850514/meaning-of-message-operations-coalesced-during-safepoint
 * @see https://blogs.oracle.com/jonthecollector/entry/the_unspoken_gc_times
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 27.04.2014</p>
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
