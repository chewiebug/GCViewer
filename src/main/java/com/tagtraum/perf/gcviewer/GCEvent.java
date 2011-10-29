package com.tagtraum.perf.gcviewer;


/**
 * GCEvent.
 *
 * Date: Jan 30, 2002
 * Time: 5:05:43 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class GCEvent extends AbstractGCEvent {

    /**
     * Used before GC in KB.
     */
    private int preUsed;
    /**
     * Used after GC in KB.
     */
    private int postUsed;
    /**
     * Capacity in KB.
     */
    private int total;
	/**
	 * Pause in seconds.
	 */
	protected double pause;
    public GCEvent() {
    }

    public GCEvent(double timestamp, int preUsed, int postUsed, int total, double pause, GCEvent.Type type) {
        this.setTimestamp(timestamp);
        this.preUsed = preUsed;
        this.postUsed = postUsed;
        this.total = total;
        this.pause = pause;
        this.setType(type);
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
        sb.append(getType());
        sb.append(' ');
        if (details != null) {
            for (AbstractGCEvent event : details) {
                event.toStringBuffer(sb);
            }
        }
        sb.append(preUsed);
        sb.append("K->");
        sb.append(postUsed);
        sb.append("K(");
        sb.append(total);
        sb.append("K), ");
        sb.append(pause);
        sb.append(" secs]");
    }

	public void setPause(double pause) {
	    this.pause = pause;
	}

	public double getPause() {
	    return pause;
	}


}
