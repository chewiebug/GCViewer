package com.tagtraum.perf.gcviewer;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: hendrik
 * Date: May 31, 2005
 * Time: 3:41:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class GCOutputProducer {

    // = 64kb for the array and 2kb for each object = 32MB
    private static Object[] longLived = new Object[16 * 1024];
    private static int longLivedPos;
    // = 8kb for the array and 2kb for each object = 4096KB
    private static Object[] shortLived = new Object[2 * 1024];
    private static int shortLivedPos;
    private static final int SHORTLIVED_INTERVAL = shortLived.length/4;
    private static final int LONGLIVED_INTERVAL = longLived.length/512;

    public static void main(final String[] args) {
        while (true) {
            allocateShortLived();
            allocateLongLived();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void allocateLongLived() {
        longLivedPos = longLivedPos % longLived.length;
        for (int i=longLivedPos; i<longLivedPos + LONGLIVED_INTERVAL; i++) {
            longLived[i] = new MemoryConsumingObject();
        }
        longLivedPos += LONGLIVED_INTERVAL;
    }


    private static void allocateShortLived() {
        shortLivedPos = shortLivedPos % shortLived.length;
        for (int i=shortLivedPos; i<shortLivedPos + SHORTLIVED_INTERVAL; i++) {
            shortLived[i] = new MemoryConsumingObject();
        }
        shortLivedPos += SHORTLIVED_INTERVAL;
    }

    /**
     * Each of these objects should be about 512 * 4 bytes big (=2kb)
     */
    private static class MemoryConsumingObject {
        private int[] array = new int[512];

        public String toString() {
            return Arrays.toString(array);
        }
    }
}
