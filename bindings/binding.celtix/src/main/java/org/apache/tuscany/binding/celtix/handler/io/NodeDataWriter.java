package org.apache.tuscany.binding.celtix.handler.io;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.helper.TypeHelper;
import commonj.sdo.helper.XSDHelper;
import org.apache.tuscany.sdo.helper.DataFactoryImpl;
import org.apache.tuscany.sdo.helper.XMLHelperImpl;
import org.apache.tuscany.sdo.helper.XSDHelperImpl;
import org.objectweb.celtix.bindings.DataWriter;
import org.objectweb.celtix.context.ObjectMessageContext;

public class NodeDataWriter implements DataWriter<Node> {
    SCADataBindingCallback callback;

    public NodeDataWriter(SCADataBindingCallback cb) {
        callback = cb;
    }

    public void write(Object obj, Node output) {
        write(obj, null, output);
    }

    public void write(Object obj, QName elName, Node output) {
        //REVISIT - doc/lit and rpc/lit support
    }

    public void writeWrapper(ObjectMessageContext objCtx, boolean isOutbound, Node nd) {
        QName wrapperName;
        if (isOutbound) {
            wrapperName = callback.getOperationInfo().getResponseWrapperQName();
        } else {
            wrapperName = callback.getOperationInfo().getRequestWrapperQName();
        }

        DataObject obj = toWrappedDataObject(callback.getTypeHelper(),
                objCtx.getMessageObjects(),
                wrapperName);

        try {
            //REVISIT - this is SUCH a hack.   SDO needs to be able to 
            //go directly to some formats other than streams.  They are working
            //on stax, but not there yet.
            RawByteArrayOutputStream bout = new RawByteArrayOutputStream();
            new XMLHelperImpl(callback.getTypeHelper()).save(obj,
                    wrapperName.getNamespaceURI(),
                    wrapperName.getLocalPart(),
                    bout);

            ByteArrayInputStream bin = new ByteArrayInputStream(bout.getBytes(),
                    0,
                    bout.size());
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            parser.parse(bin, new NodeContentHandler(nd));
        } catch (IOException e) {
            throw new WebServiceException(e);
        } catch (ParserConfigurationException e) {
            throw new WebServiceException(e);
        } catch (SAXException e) {
            throw new WebServiceException(e);
        }
    }


    public static DataObject toWrappedDataObject(TypeHelper typeHelper,
                                                 Object[] os,
                                                 QName typeQN) {
        XSDHelper xsdHelper = new XSDHelperImpl(typeHelper);
        Property property = xsdHelper.getGlobalProperty(typeQN.getNamespaceURI(),
                typeQN.getLocalPart(), true);
        DataObject dataObject = new DataFactoryImpl(typeHelper).create(property.getType());
        List ips = dataObject.getInstanceProperties();
        for (int i = 0; i < ips.size(); i++) {
            if (os[i] instanceof Holder) {
                Holder<?> holder = (Holder<?>)os[i];
                dataObject.set(i, holder.value);
            } else {
                dataObject.set(i, os[i]);
            }
        }
        return dataObject;
    }

    private class NodeContentHandler extends DefaultHandler {
        Element current;
        Document doc;

        public NodeContentHandler(Node nd) {
            current = (Element)nd;
            doc = nd.getOwnerDocument();
        }

        public void characters(char[] ch, int start, int length) {
            current.appendChild(doc.createTextNode(new String(ch, start, length)));
        }

        public void startElement(String uri, String localName,
                                 String qName, Attributes attributes) {
            Element newEl = doc.createElementNS(uri, qName);
            current.appendChild(newEl);
            current = newEl;
            for (int x = 0; x < attributes.getLength(); x++) {
                current.setAttributeNS(attributes.getURI(x),
                        attributes.getQName(x),
                        attributes.getValue(x));
            }
        }

        public void endElement(String uri, String localName, String qName) {
            current = (Element)current.getParentNode();
        }
    }


}
