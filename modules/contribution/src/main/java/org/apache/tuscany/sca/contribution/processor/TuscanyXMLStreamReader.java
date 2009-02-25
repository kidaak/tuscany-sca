package org.apache.tuscany.sca.contribution.processor;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

/*
 * Custom implementaion of the XMLStreamReader to keep track of the namespace context for each element
 */
public class TuscanyXMLStreamReader extends StreamReaderDelegate implements XMLStreamReader {

    Stack<List<String>[]> context = new Stack<List<String>[]>();

    String characterEncodingScheme;
    List<String>[] contextList;
    List<String> prefixList;
    List<String> uriList;

    public TuscanyXMLStreamReader(XMLStreamReader reader) {
        super(reader);
        this.saveCharacterEncodingScheme();
    }

    public void pushContext() throws XMLStreamException {
        contextList = new List[2];
        prefixList = new ArrayList<String>();
        uriList = new ArrayList<String>();
        int namespaceCount = this.getNamespaceCount();
        for (int i = 0; i < namespaceCount; i++) {
            prefixList.add(checkString(this.getNamespacePrefix(i)));
            uriList.add(this.getNamespaceURI(i));
        }
        contextList[0] = prefixList;
        contextList[1] = uriList;
        context.push(contextList);
    }

    private String checkString(String namespacePrefix) {
        if (namespacePrefix == null) {
            return XMLConstants.DEFAULT_NS_PREFIX;
        } else {
            return namespacePrefix;
        }
    }

    public void popContext() throws XMLStreamException {
        context.pop();
    }
    
    public void saveCharacterEncodingScheme() {
        this.characterEncodingScheme = super.getCharacterEncodingScheme();
    }

    // A flag to indicate if the next() is called from nextTag()
    private boolean withinNextTagMethod = false;
   
    /*
     * Overriding the next() method to perform PUSH and POP operations 
     * for the NamespaceContext for the current element
     */
    @Override
    public int next() throws XMLStreamException {
        // POP the namespaces if the reader leaves the end element
        if (!withinNextTagMethod && this.getEventType() == END_ELEMENT) {
            popContext();
        }

        // get the next event 
        int nextEvent = super.next();
        
        // PUSH the namespaces onto the stack as the reader enters the start element
        if (!withinNextTagMethod && nextEvent == START_ELEMENT) {
            pushContext();
        }
        return nextEvent;
    }

    @Override
    public int nextTag() throws XMLStreamException {
        withinNextTagMethod = true;
        // POP the namespaces out of the stack if the reader leaves the end element
        if (this.getEventType() == END_ELEMENT) {
            popContext();
        }        
        
        // REVIEW: what if nextTag() calls next()?
        int event = super.nextTag();
        
        // PUSH the namespaces onto the stack as the reader enters the start element
        if (event == START_ELEMENT) {
            pushContext();
        }
        withinNextTagMethod = false;
        return event;
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return new TuscanyNamespaceContext((Stack<List<String>[]>)context.clone());
    }
    

    @Override
    public String getCharacterEncodingScheme() {
        return characterEncodingScheme;
    }
}