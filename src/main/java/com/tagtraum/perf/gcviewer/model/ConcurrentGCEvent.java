package com.tagtraum.perf.gcviewer.model;

/**
 * ConcurrentGCEvent.
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ConcurrentGCEvent extends AbstractGCEvent<ConcurrentGCEvent> {

    private double duration;

    /**
     * Time this step in the concurrent GC mechanism took.
     * <p>
     * {@literal [CMS-concurrent-sweep: 0.005/<em>0.015</em> secs]}
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
     * Set duration.
     * @see #getDuration()
     * @param duration time spent in gc mechanism
     */
    public void setDuration(double duration) {
        this.duration = duration;
    }

    public boolean hasDuration() {
        return !getExtendedType().getName().endsWith("-start");
    }

    public void toStringBuffer(StringBuffer sb) {
        sb.append(getTimestamp());
        sb.append(": [");
        sb.append(getExtendedType().getName());
        if (hasDuration()) {
            sb.append(' ');
            sb.append(getPause());
            sb.append('/');
            sb.append(duration);
            sb.append(" secs");
        }
        sb.append(']');
    }
}
