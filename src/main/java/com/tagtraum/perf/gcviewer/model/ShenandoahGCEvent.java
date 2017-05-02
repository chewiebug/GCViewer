package com.tagtraum.perf.gcviewer.model;

/**
 * Created by Mart on 19/04/2017.
 * GC Event class that holds extra information about concurrency and heap.
 */
public class ShenandoahGCEvent extends GCEvent {

    /** Heap usage in KB, override for convertion from MB to KB */
    private int preUsed;
    private int postUsed;
    private int total;

    /** Override GCEvent as Shenandoah pause times are in milliseconds and GCViewer displays seconds */
    private double pause;

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

    @Override
    public void setPreUsed(int preUsed) {
        this.preUsed = preUsed * 1000;
    }

    @Override
    public void setPostUsed(int postUsed) {
        this.postUsed = postUsed * 1000;
    }

    @Override
    public void setTotal(int total) {
        this.total = total * 1000;
    }

    @Override
    public int getPreUsed() {
        return preUsed;
    }

    @Override
    public int getPostUsed() {
        return postUsed;
    }

    @Override
    public int getTotal() {
        return total;
    }

    @Override
    public double getPause() {
        return pause;
    }

    @Override
    public void setPause(double pause) {
        this.pause = pause / 1000;
    }



}
