package com.tagtraum.perf.gcviewer.model;

import java.util.Objects;

/**
 * The GCEvent is the type of event that contains memory (preused, postused, total) and 
 * pause information.
 *
 * <p>Date: Jan 30, 2002</p>
 * <p>Time: 5:05:43 PM</p>
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 */
public class GCEvent extends AbstractGCEvent<GCEvent> {

    /** store references to related/inferred events */
    private GCEvent young;
    private GCEvent tenured;
    private GCEvent perm;
    
    public GCEvent() {
    }

    public GCEvent(double timestamp, int preUsed, int postUsed, int total, double pause, Type type) {
        this.setTimestamp(timestamp);
        this.setPreUsed(preUsed);
        this.setPostUsed(postUsed);
        this.setTotal(total);
        this.setPause(pause);
        this.setType(type);
    }

    @Override
    public void add(GCEvent event) {
        super.add(event);

        switch (event.getExtendedType().getGeneration()) {
            case YOUNG:
                young = event;
                break;
            case TENURED:
                tenured = event;
                break;
            case PERM:
                perm = event;
                break;
            // ALL and OTHER are never read
            case ALL:
                break;
            case OTHER:
                break;
        }
    }
    
    /**
     * Returns information on young generation. If it was not present in the gc log, but 
     * tenured was, it is inferred from there (with -XX:+PrintGCDetails). Otherwise it is 
     * <code>null</code> (without -XX:+PrintGCDetails).
     * 
     * @return Information on young generation if possible, <code>null</code> otherwise.
     */
    public GCEvent getYoung() {
        if (young == null) {
            if (tenured != null) {
                young = new GCEvent();
                young.setTimestamp(tenured.getTimestamp());
                young.setPreUsed(getPreUsed() - tenured.getPreUsed());
                young.setPostUsed(getPostUsed() - tenured.getPostUsed());
                young.setTotal(getTotal() - tenured.getTotal());
                young.setPause(tenured.getPause());
            }
        }
        
        return young;
    }
    
    /**
     * Returns information on young generation. If it was not present in the gc log, but 
     * tenured was, it is inferred from there (with -XX:+PrintGCDetails). Otherwise it 
     * is <code>null</code> (without -XX:+PrintGCDetails).
     * 
     * @return Information on young generation if possible, <code>null</code> otherwise.
     */
    public GCEvent getTenured() {
        if (tenured == null) {
            if (young != null) {
                tenured = new GCEvent();
                tenured.setTimestamp(young.getTimestamp());
                tenured.setPreUsed(getPreUsed() - young.getPreUsed());
                tenured.setPostUsed(getPostUsed() - young.getPostUsed());
                tenured.setTotal(getTotal() - young.getTotal());
                tenured.setPause(young.getPause());
            }
        }
        
        return tenured;
    }
    
    /**
     * Returns information on perm generation. If it was not present in the gc log,
     * <code>null</code> will be returned, because the values cannot be inferred.
     * 
     * @return Information on perm generation or <code>null</code> if not present.
     */
    public GCEvent getPerm() {
        return perm;
    }

    public void toStringBuffer(StringBuffer sb) {
        sb.append(getTimestamp());
        sb.append(": [");
        sb.append(getExtendedType() != null ? getExtendedType().getName() : ExtendedType.UNDEFINED);
        if (details != null) {
            sb.append(' ');
            for (AbstractGCEvent event : details) {
                event.toStringBuffer(sb);
            }
            sb.append(' ');
        }
        else {
            sb.append(": ");
        }
        sb.append(getPreUsed());
        sb.append("K->");
        sb.append(getPostUsed());
        sb.append("K(");
        sb.append(getTotal());
        sb.append("K), ");
        sb.append(getPause());
        sb.append(" secs]");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GCEvent))
            return false;
        if (!super.equals(o))
            return false;
        GCEvent gcEvent = (GCEvent) o;
        return getPreUsed() == gcEvent.getPreUsed() &&
               getPostUsed() == gcEvent.getPostUsed() &&
               getTotal() == gcEvent.getTotal();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPreUsed(), getPostUsed(), getTotal());
    }
}
