package com.tagtraum.perf.gcviewer.model;

/**
 * ConcurrentGCEvent.
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public final class ConcurrentGCEvent extends AbstractGCEvent<ConcurrentGCEvent> {

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

    @Override
    public ConcurrentGCEvent cloneAndMerge(AbstractGCEvent<ConcurrentGCEvent> otherEvent) {
        return (ConcurrentGCEvent)super.cloneAndMerge(otherEvent);
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
        if (getNumber() >= 0) {
            sb.append(" GC(").append(getNumber()).append(") ");
        }
        sb.append(getExtendedType().getName());
        if (hasMemoryInformation()) {
            sb.append(' ');
            sb.append(getPreUsed());
            sb.append("K->");
            sb.append(getPostUsed());
            sb.append("K(");
            sb.append(getTotal());
            sb.append("K), ");
        }
        if (hasDuration()) {
            sb.append(' ');
            sb.append(getPause());
            sb.append('/');
            sb.append(getDuration());
            sb.append(" secs");
        }
        sb.append(']');
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        ConcurrentGCEvent that = (ConcurrentGCEvent) o;

        return Double.compare(that.duration, duration) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(duration);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
