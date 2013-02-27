/*
 * =================================================
 * Copyright 2007 Justin Kilimnik (IBM UK) 
 * All rights reserved.
 * Date: May 22, 2007
 * Time: 12:15:44 PM
 * =================================================
 */
package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.model.GCModel;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses -verbose:gc output from IBM J9 JVM 5.0. Uses SAX parser and custom 
 * handler to read in GC information.
 * Note: Only supports -Xgcpolicy:optthruput (the default GC policy).
 * Note(2): This implementation uses the "Young" area to represent the J9 idea
 * of the Small-Object-Area (soa) and the "Old" generation area for the Large-
 * Object-Area (loa).
 * 
 * @author <a href="mailto:justink@au1.ibm.com">Justin Kilimnik (IBM)</a>
 * @version $Id: $
 */
public class DataReaderIBM_J9_5_0 implements DataReader {

    private static Logger LOG = Logger.getLogger(DataReaderIBM_J9_5_0.class.getName());
   
    private InputStream in;
    
     public DataReaderIBM_J9_5_0(final InputStream in) {
    	 this.in = in;
    }

    public GCModel read() throws IOException {
        if (LOG.isLoggable(Level.INFO)) LOG.info("Reading IBM J9 5.0 format...");
        try {
            final GCModel model = new GCModel(true);
            model.setFormat(GCModel.Format.IBM_VERBOSE_GC);           
            DefaultHandler handler = new IBMJ9SAXHandler(model);

            // Use the default (non-validating) parser
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            
            javax.xml.parsers.SAXParser saxParser;
			try {
				saxParser = factory.newSAXParser();
				saxParser.parse( in, handler );
			} catch (ParserConfigurationException e) {
                final IOException exception = new IOException(e.toString());
                exception.initCause(e);
                throw exception;
			} catch (SAXException e) {
				// TODO: if(e.getMessage().startsWith("XML document structures must start and end within the same entity")) {
				if (e instanceof SAXParseException && ((SAXParseException) e).getColumnNumber() == 1) {
					// ignore. this just means a xml tag terminated.
				} else {
                    final IOException exception = new IOException(e.toString());
                    exception.initCause(e);
                    throw exception;
				}
			}
            
            return model;
  
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            if (LOG.isLoggable(Level.INFO)) LOG.info("Done reading.");
        }
    }
}
