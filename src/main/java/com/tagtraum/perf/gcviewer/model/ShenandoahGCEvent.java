package com.tagtraum.perf.gcviewer.model;

/**
 * Created by Mart on 19/04/2017.
 * GC Event class that holds extra information about concurrency and heap.
 */
public class ShenandoahGCEvent extends GCEvent {

    /** Used before GC in KB */
    private int preUsed;

    /** Used after GC in KB */
    private int postUsed;

    /** Capacity in KB */
    private int total;

    /** By default events are not concurrent, must be set so */
    private boolean concurrency;

    public ShenandoahGCEvent() {
        concurrency = false;
    }

    public boolean isConcurrent() {
        return concurrency;
    }

    public void setConcurrency(boolean concurrency) {
        this.concurrency = concurrency;
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

}
