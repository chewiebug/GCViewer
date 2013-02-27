package com.tagtraum.perf.gcviewer.model;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: Jun 1, 2005
 * Time: 11:55:26 AM
 *
 */
public class ConcurrentGCEvent extends AbstractGCEvent<ConcurrentGCEvent> {

    private double duration;
    private double pause;

    /**
     * Time this step in the concurrent GC mechanism took.<p>
     * <xmp>[CMS-concurrent-sweep: 0.005/<em>0.015</em> secs]</xmp>
     * <p>
     * So in this example the time that was exclusively spent on
     * the step would be 0.005secs of an overall duration of 0.015secs.
     * <p>
     * So {@link #getPause()} returns the time that was exclusively spent.
     *
     * @return time a certain concurrent GC step took.
     */
    public double getDuration() {
        return duration;
    }

    /**
     * @see #getDuration()
     * @param duration
     */
    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getPause() {
        return pause;
    }

    public void setPause(double pause) {
        this.pause = pause;
    }
    
    public boolean hasDuration() {
        return !getType().getType().endsWith("-start");
    }

    public void toStringBuffer(StringBuffer sb) {
        sb.append(getTimestamp());
        sb.append(": [");
        sb.append(getType());
        if (hasDuration()) {
            sb.append(' ');
            sb.append(pause);
            sb.append('/');
            sb.append(duration);
            sb.append(" secs");
        }
        sb.append(']');
    }
}
