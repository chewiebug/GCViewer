package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.util.NumberParser;

/**
 * Parser for IBM gc logs R26_Java6 + R27_Java7 + R28_Java8
 */
public class DataReaderIBM_J9_R28 extends AbstractDataReader {
    // TODO IBM_J9: support system gcs

    private static final String VERBOSEGC = "verbosegc";
    private static final String INITIALIZED = "initialized";
    private static final String EXCLUSIVE_START = "exclusive-start";
    private static final String SYS_START = "sys-start";
    private static final String AF_START = "af-start";
    private static final String CONCURRENT_COLLECTION_START = "concurrent-collection-start";
    private static final String GC_START = "gc-start";
    private static final String GC_END = "gc-end";
    private static final String EXCLUSIVE_END = "exclusive-end";

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private LineNumberReader in;

    public DataReaderIBM_J9_R28(GCResource gcResource, InputStream in) throws UnsupportedEncodingException {
        super(gcResource, in);
        this.in = super.in;
    }

    @Override
    public GCModel read() throws IOException {
        GCModel model = new GCModel();
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = factory.createXMLEventReader(in);
            GCEvent currentGcEvent = null;
            String eventNameStart = null;
            while (eventReader.hasNext()) {
                try {
                    XMLEvent event = eventReader.nextEvent();
                    if (event.isStartElement()) {
                        StartElement startElement = event.asStartElement();
                        switch (startElement.getName().getLocalPart()) {
                            case VERBOSEGC:
                                handleVerboseGC(startElement);
                                break;
                            case INITIALIZED:
                                handleInitialized(eventReader);
                                break;
                            case EXCLUSIVE_START:
                                currentGcEvent = handleExclusiveStart(startElement);
                                break;
                            case SYS_START:
                                assert eventNameStart == null : "eventNameStart was expected to be null, but was " + eventNameStart;
                                eventNameStart = handleSysStart(eventReader, startElement);
                                break;
                            case AF_START:
                                assert eventNameStart == null : "eventNameStart was expected to be null, but was " + eventNameStart;
                                eventNameStart = handleAfStart(eventReader, startElement);
                                break;
                            case GC_START:
                                handleGcStart(eventReader, startElement, currentGcEvent, eventNameStart);
                                break;
                            case GC_END:
                                if (currentGcEvent.getTypeAsString() != null) {
                                    handleGcEnd(eventReader, currentGcEvent);
                                }
                                break;
                            case EXCLUSIVE_END:
                                handleExclusiveEnd(startElement, currentGcEvent);
                                if (currentGcEvent.getExtendedType() == null) {
                                    if (getLogger().isLoggable(Level.FINE))
                                        getLogger().fine("event at " + in.getLineNumber() + " doesn't contain any information, the parser can handle");
                                }
                                else {
                                    model.add(currentGcEvent);
                                }
                                currentGcEvent = null;
                                eventNameStart = null;
                                break;
                        }
                    }

                }
                catch (Exception e) {
                    if (e instanceof XMLStreamException) {
                        throw e;
                    }
                    if (getLogger().isLoggable(Level.WARNING)) getLogger().warning("line " + in.getLineNumber() + ": " + e.toString());
                    if (getLogger().isLoggable(Level.FINE)) getLogger().log(Level.FINE, "line " + in.getLineNumber() + ": " + e.getMessage(), e);
                }
            }
        }
        catch (XMLStreamException e) {
            if (getLogger().isLoggable(Level.WARNING)) getLogger().warning("line " + in.getLineNumber() + ": " + e.toString());
            if (getLogger().isLoggable(Level.FINE)) getLogger().log(Level.FINE, "line " + in.getLineNumber() + ": " + e.getMessage(), e);
        }
        finally {
            if (getLogger().isLoggable(Level.INFO)) getLogger().info("Done reading.");
        }

        return model;
    }

    private void handleVerboseGC(StartElement startElement) {
        assert startElement.getName().getLocalPart().equals(VERBOSEGC) : "expected name of startElement: " + VERBOSEGC + ", but got " + startElement.getName();
        getLogger().info("gc log version = " + getAttributeValue(startElement, "version"));
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
                        getLogger().info("gcPolicy = " + getAttributeValue(startElement, "value"));
                    }
                }
            }
            else if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                currentElementName = endElement.getName().getLocalPart();
            }
        }
    }

    private GCEvent handleExclusiveStart(StartElement startElement) {
        GCEvent event = new GCEvent();
        try {
            event.setDateStamp(ZonedDateTime.of(
                    LocalDateTime.parse(getAttributeValue(startElement, "timestamp"), dateTimeFormatter),
                    ZoneId.systemDefault()));
        }
        catch (DateTimeParseException e) {
            if (getLogger().isLoggable(Level.WARNING)) getLogger().warning("line " + in.getLineNumber() + ": " + e.toString());
            if (getLogger().isLoggable(Level.FINE)) getLogger().log(Level.FINE, "line " + in.getLineNumber() + ": " + e.getMessage(), e);
        }

        return event;
    }

    private void handleExclusiveEnd(StartElement startElement, GCEvent event) {
        event.setPause(NumberParser.parseDouble(getAttributeValue(startElement, "durationms")) / 1000);
    }

    private String handleSysStart(XMLEventReader eventReader, StartElement startElement) throws XMLStreamException {
        String reason = getAttributeValue(startElement, "reason");
        return "sys " + (reason != null ? reason + " " : "");
    }

    private String handleAfStart(XMLEventReader eventReader, StartElement startElement) throws XMLStreamException {
        return "af ";
    }

    private void handleConcurrentCollectionStart(XMLEventReader eventReader, StartElement startElement) {
    }

    private void handleGcStart(XMLEventReader eventReader, StartElement startElement, GCEvent event, String eventNameStart) throws
            XMLStreamException,
            UnknownGcTypeException {

        String typeName = eventNameStart + getAttributeValue(startElement, "type");
        AbstractGCEvent.Type type = Type.lookup(typeName);
        if (type == null) {
            throw new UnknownGcTypeException(typeName, startElement.toString());
        }
        event.setType(type);

        String currentElementName = "";
        while (eventReader.hasNext() && !currentElementName.equals(GC_START)) {
            
            XMLEvent xmlEvent = eventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startEl = xmlEvent.asStartElement();
                if (startEl.getName().getLocalPart().equals("mem-info")) {
                    setTotalAndPreUsed(event, startEl);
                }
                else if (startEl.getName().getLocalPart().equals("mem")) {
                    switch (getAttributeValue(startEl, "type")) {
                        case "nursery":
                            GCEvent young = new GCEvent();
                            young.setType(Type.lookup("nursery"));
                            setTotalAndPreUsed(young, startEl);
                            event.add(young);
                            break;
                        case "tenure":
                            GCEvent tenured = new GCEvent();
                            tenured.setType(Type.lookup("tenure"));
                            setTotalAndPreUsed(tenured, startEl);
                            event.add(tenured);
                            break;
                        // all other are ignored
                    }
                }
            }
            else if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                currentElementName = endElement.getName().getLocalPart();
            }
        }
    }

    private void handleGcEnd(XMLEventReader eventReader, GCEvent event) throws XMLStreamException {
        String currentElementName = "";
        while (eventReader.hasNext() && !currentElementName.equals(GC_END)) {

            XMLEvent xmlEvent = eventReader.nextEvent();
            if (xmlEvent.isStartElement()) {
                StartElement startEl = xmlEvent.asStartElement();
                if (startEl.getName().getLocalPart().equals("mem-info")) {
                    setPostUsed(event, startEl);
                }
                else if (startEl.getName().getLocalPart().equals("mem")) {
                    switch (getAttributeValue(startEl, "type")) {
                        case "nursery":
                            setPostUsed(event.getYoung(), startEl);
                            break;
                        case "tenure":
                            setPostUsed(event.getTenured(), startEl);
                            break;
                        // all other are ignored
                    }
                }
            }
            else if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                currentElementName = endElement.getName().getLocalPart();
            }
        }
    }

    private void setTotalAndPreUsed(GCEvent event, StartElement startEl) {
        long total = NumberParser.parseLong(getAttributeValue(startEl, "total"));
        event.setTotal(toKiloBytes(total));
        event.setPreUsed(toKiloBytes(total - NumberParser.parseLong(getAttributeValue(startEl, "free"))));
    }

    private void setPostUsed(GCEvent event, StartElement startEl) {
        long total = NumberParser.parseLong(getAttributeValue(startEl, "total"));
        event.setPostUsed(toKiloBytes(total - NumberParser.parseLong(getAttributeValue(startEl, "free"))));
    }

    private String getAttributeValue(StartElement event, String name) {
        String value = null;
        Attribute attr = event.getAttributeByName(new QName(name));
        if (attr != null) {
            value = attr.getValue();
        }
        
        return value;
    }

    private int toKiloBytes(long bytes) {
        return (int)Math.rint(bytes / (double)1024);
    }
}
