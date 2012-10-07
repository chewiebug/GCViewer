package com.tagtraum.perf.gcviewer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.exp.DataWriterFactory;
import com.tagtraum.perf.gcviewer.exp.DataWriterType;
import com.tagtraum.perf.gcviewer.imp.DataReader;
import com.tagtraum.perf.gcviewer.imp.DataReaderFactory;
import com.tagtraum.perf.gcviewer.model.GCModel;

public class Main {

	public static void main(final String[] args) {
        if (args.length == 2) {
        	final String gcfile = args[0];
        	final String summaryFilePath = args[1];

            //export summary:
            try {
                exportSummary(summaryFilePath, gcfile);
                System.exit(0);
            }
            catch(IOException e1) {
                e1.printStackTrace();
                System.exit(-1);
            }

        } else if (args.length > 1) {
            usage();
        } else {
        	GCViewer.main(args);
        }
    }
	
	private static void exportSummary(String summaryFilePath, String gcFilename) throws IOException {
	    DataWriter summaryWriter = DataWriterFactory.getDataWriter(new File(summaryFilePath), DataWriterType.SUMMARY);
	    try {
	        GCModel model = loadModel(new File(gcFilename).toURI().toURL());
	        summaryWriter.write(model);
	    } 
	    finally {
            summaryWriter.close();
	    }
	}
    
    private static GCModel loadModel(final URL url) throws IOException {
		DataReaderFactory factory = new DataReaderFactory();
	    final InputStream in = url.openStream();
	    final DataReader reader = factory.getDataReader(in);
	    final GCModel model = reader.read();
	    model.setURL(url);
	    return model;

    }
	
	private static void usage() {
		System.out.println("Welcome to GCViewer with cmdline");
        System.out.println("java -jar gcviewer.jar [<gc-log-file>] [<export.csv>]");
    }

}
