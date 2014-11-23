package com.tagtraum.perf.gcviewer.model;

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

    /** Used before GC in KB */
    private int preUsed;
    
    /** Used after GC in KB */
    private int postUsed;
    
    /** Capacity in KB */
    private int total;
    
    /** store references to related/inferred events */
    private GCEvent young;
    private GCEvent tenured;
    private GCEvent perm;
    
    public GCEvent() {
    }

    public GCEvent(double timestamp, int preUsed, int postUsed, int total, double pause, Type type) {
        this.setTimestamp(timestamp);
        this.preUsed = preUsed;
        this.postUsed = postUsed;
        this.total = total;
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
                young.setPreUsed(preUsed - tenured.getPreUsed());
                young.setPostUsed(postUsed - tenured.getPostUsed());
                young.setTotal(total - tenured.getTotal());
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
                tenured.setPreUsed(preUsed - young.getPreUsed());
                tenured.setPostUsed(postUsed - young.getPostUsed());
                tenured.setTotal(total - young.getTotal());
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

    public void toStringBuffer(StringBuffer sb) {
        sb.append(getTimestamp());
        sb.append(": [");
        sb.append(getExtendedType() != null ? getExtendedType().getName() : ExtendedType.UNDEFINED);
        if (details != null) {
            sb.append(' ');
            for (GCEvent event : details) {
                event.toStringBuffer(sb);
            }
            sb.append(' ');
        }
        else {
            sb.append(": ");
        }
        sb.append(preUsed);
        sb.append("K->");
        sb.append(postUsed);
        sb.append("K(");
        sb.append(total);
        sb.append("K), ");
        sb.append(getPause());
        sb.append(" secs]");
    }

}
