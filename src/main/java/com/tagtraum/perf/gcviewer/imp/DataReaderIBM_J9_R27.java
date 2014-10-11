package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.NumberParser;

/**
 * @author <a href="gcviewer@gmx.ch">Joerg Wuethrich</a>
 *         <p>created on 08.10.2014</p>
 */
public class DataReaderIBM_J9_R27 implements DataReader {

    private static final String INITIALIZED = "initialized";
    private static final String EXCLUSIVE_START = "exclusive-start";
    private static final String GC_START = "gc-start";
    private static final String EXCLUSIVE_END = "exclusive-end";

    private static Logger LOG = Logger.getLogger(DataReaderIBM_J9_5_0.class.getName());
    private final SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");

    private LineNumberReader in;

    public DataReaderIBM_J9_R27(InputStream in) {
        this.in = new LineNumberReader(new InputStreamReader(in));
    }

    @Override
    public GCModel read() throws IOException {
        GCModel model = new GCModel();
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = factory.createXMLEventReader(in);
            GCEvent currentGcEvent = null;
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    switch (startElement.getName().getLocalPart()) {
                        case INITIALIZED:
                            handleInitialized(eventReader);
                            break;
                        case EXCLUSIVE_START:
                            currentGcEvent = handleExclusiveStart(eventReader, startElement);
                            break;
                        case GC_START:
                            handleGcStart(eventReader, startElement, currentGcEvent);
                            break;
//                        case "gc-end":
//                            handleGcEnd(eventReader);
//                            break;
                        case EXCLUSIVE_END:
                            // TODO read pause time
                            model.add(currentGcEvent);
                            currentGcEvent = null;
                            break;
                    }
                }

            }
        }
        catch (XMLStreamException e) {
            e.printStackTrace();
        }

        return model;
    }

    private GCEvent handleExclusiveStart(XMLEventReader eventReader, StartElement startElement) {
        GCEvent event = new GCEvent();
        try {
            event.setDateStamp(dateParser.parse(getAttributeValue(startElement, "timestamp")));
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        
        return event;
    }

    private void handleGcStart(XMLEventReader eventReader, StartElement startElement, GCEvent event) throws XMLStreamException {
        event.setType(Type.lookup(getAttributeValue(startElement, "type")));
        if (event.getExtendedType() == null) {
            LOG.warning("could not determine type of event " + startElement.toString());
            return;
        }
        
        String currentElementName = "";
        while (eventReader.hasNext() && !currentElementName.equals(GC_START)) {
            
            XMLEvent xmlEvent = eventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startEl = xmlEvent.asStartElement();
                if (startEl.getName().getLocalPart().equals("mem-info")) {
                    event.setTotal(NumberParser.parseInt(getAttributeValue(startEl, "total")) / 1024);
                    event.setPreUsed(event.getTotal() - (NumberParser.parseInt(getAttributeValue(startEl, "free")) / 1024));
                }
                else if (startEl.getName().getLocalPart().equals("mem")) {
                    switch (getAttributeValue(startEl, "type")) {
                        case "nursery":
                            // TODO read young
                            break;
                    }
                }
            }
            else if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                currentElementName = endElement.getName().getLocalPart();
            }
        }
    }

    private void handleInitialized(XMLEventReader eventReader) throws XMLStreamException {
        String currentElementName = "";
        while (eventReader.hasNext() && !currentElementName.equals(INITIALIZED)) {
            XMLEvent event = eventReader.nextEvent();
            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                if (startElement.getName().getLocalPart().equals("attribute")) {
                    String name = getAttributeValue(startElement, "name");
                    if (name != null && name.equals("gcPolicy")) {
                        LOG.info("gcPolicy = " + getAttributeValue(startElement, "value"));
                    }
                }
            }
            else if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                currentElementName = endElement.getName().getLocalPart();
            }
        }
    }
    
    private String getAttributeValue(StartElement event, String name) {
        String value = null;
        Attribute attr = event.getAttributeByName(new QName(name));
        if (attr != null) {
            value = attr.getValue();
        }
        
        return value;
    }
}
