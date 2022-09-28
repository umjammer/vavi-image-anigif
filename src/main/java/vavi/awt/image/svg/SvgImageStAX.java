/*
 * Copyright (c) 2007 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.awt.image.svg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;


/**
 * SvgImageStAX.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 070709 nsano initial version <br>
 */
public class SvgImageStAX {
    private Map<Integer, EventHandler> handlers;

    public SvgImageStAX(String xmlfile) throws IOException,
            XMLStreamException {
        handlers = initHandlers();

        XMLInputFactory factory = XMLInputFactory.newInstance();

        BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(Paths.get(xmlfile)));
        XMLEventReader reader = factory.createXMLEventReader(stream);

        EventFilter filter = event -> event.isStartElement() || event.isEndElement();
        reader = factory.createFilteredReader(reader, filter);

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();

            EventHandler handler = handlers.get(event.getEventType());
            handler.handleEvent(event);
        }

        reader.close();
    }

    private Map<Integer, EventHandler> initHandlers() {
        Map<Integer, EventHandler> handlers = new HashMap<>();

        handlers.put(XMLEvent.START_ELEMENT, new StartElementHandler());
        handlers.put(XMLEvent.END_ELEMENT, new EndElementHandler());

        return handlers;
    }

    public static void main(String[] args) throws IOException, XMLStreamException {
        new SvgImageStAX(args[0]);
    }
}

interface EventHandler {
    public void handleEvent(XMLEvent element);
}

class StartElementHandler implements EventHandler {
    public void handleEvent(XMLEvent event) {
        StartElement element = (StartElement) event;
        System.out.println("StartElement: " + element.getName().getLocalPart());
    }
}

class EndElementHandler implements EventHandler {
    public void handleEvent(XMLEvent event) {
        EndElement element = (EndElement) event;
        System.out.println("EndElement: " + element.getName().getLocalPart());
    }
}
