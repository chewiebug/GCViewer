package com.tagtraum.perf.gcviewer;

import java.io.File;
import java.io.IOException;

import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.exp.DataWriterType;
import com.tagtraum.perf.gcviewer.exp.impl.DataWriterFactory;
import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
import com.tagtraum.perf.gcviewer.model.GCModel;

public class GCViewer {

	public static void main(final String[] args) {
	    // TODO: unify parameter handling from command line
        if (args.length == 2) {
        	final String gcfile = args[0];
        	final String summaryFilePath = args[1];

            //export summary:
            try {
                exportSummary(summaryFilePath, gcfile);
                System.exit(0);
            }
            catch(Exception e) {
                e.printStackTrace();
                System.exit(-1);
            } 

        } 
        else if (args.length > 1) {
            usage();
        } 
        else {
        	GCViewerGui.main(args);
        }
    }
	
	private static void exportSummary(String summaryFilePath, String gcFilename) throws DataReaderException, IOException {
	    DataWriter summaryWriter = DataWriterFactory.getDataWriter(new File(summaryFilePath), DataWriterType.SUMMARY);
	    try {
	        DataReaderFacade dataReaderFacade = new DataReaderFacade();
	        GCModel model = dataReaderFacade.loadModel(new File(gcFilename).toURI().toURL(), false, null);
	        summaryWriter.write(model);
	    } 
	    finally {
            summaryWriter.close();
	    }
	}
    
	private static void usage() {
		System.out.println("Welcome to GCViewer with cmdline");
        System.out.println("java -jar gcviewer.jar [<gc-log-file>] [<export.csv>]");
    }

}
