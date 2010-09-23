package com.tagtraum.perf.gcviewer;

import com.tagtraum.perf.gcviewer.imp.DataReaderSun1_5_0;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jun 7, 2005
 * Time: 11:42:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class ImportPerformanceTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        for (int i=0; i<1; i++) {
            DataReader dataReader = new DataReaderSun1_5_0(new FileInputStream(args[0]));
            readModel(dataReader);
        }
        System.out.println((System.currentTimeMillis() - start));
    }

    private static void readModel(DataReader dataReader) throws IOException, InterruptedException {
        dataReader.read();
    }
}
