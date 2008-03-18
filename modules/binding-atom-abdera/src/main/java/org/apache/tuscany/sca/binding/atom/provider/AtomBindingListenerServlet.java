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
package org.apache.tuscany.sca.binding.atom.provider;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Content;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.model.Service;
import org.apache.abdera.model.Workspace;
import org.apache.abdera.parser.ParseException;
import org.apache.commons.codec.binary.Base64;
import org.apache.tuscany.sca.databinding.Mediator;
import org.apache.tuscany.sca.implementation.data.collection.Item;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.impl.DataTypeImpl;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.apache.tuscany.sca.invocation.InvocationChain;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.MessageFactory;
import org.apache.tuscany.sca.runtime.RuntimeWire;



/**
 * A resource collection binding listener, implemented as a servlet and
 * registered in a servlet host provided by the SCA hosting runtime.
 */
class AtomBindingListenerServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AtomBindingListenerServlet.class.getName());
    private static final long serialVersionUID = 1L;

    //private final static Namespace APP_NS = Namespace.getNamespace("app", "http://purl.org/atom/app#");
    //private final static Namespace ATOM_NS = Namespace.getNamespace("atom", "http://www.w3.org/2005/Atom");

    private final Abdera abdera = new Abdera();
    
    private RuntimeWire wire;
    private Invoker getFeedInvoker;
    private Invoker getAllInvoker;
    private Invoker queryInvoker;
    private Invoker getInvoker;
    private Invoker postInvoker;
    private Invoker postMediaInvoker;
    private Invoker putInvoker;
    private Invoker putMediaInvoker;
    private Invoker deleteInvoker;
    private MessageFactory messageFactory;
    private String feedType;
    private String title;
    private Mediator mediator;
    private DataType<?> itemClassType;
    private DataType<?> itemXMLType;
    private boolean supportsFeedEntries;

    /**
     * Constructs a new binding listener.
     * 
     * @param wire
     * @param messageFactory
     * @param feedType
     */
    AtomBindingListenerServlet(RuntimeWire wire, MessageFactory messageFactory, Mediator mediator, String feedType, String title) {
        this.wire = wire;
        this.messageFactory = messageFactory;
        this.mediator = mediator;
        this.feedType = feedType;
        this.title = title;

        // Get the invokers for the supported operations
        Operation getOperation = null;
        for (InvocationChain invocationChain : this.wire.getInvocationChains()) {
            invocationChain.setAllowsPassByReference(true);
            Operation operation = invocationChain.getTargetOperation();
            String operationName = operation.getName();
            if (operationName.equals("getFeed")) {
                getFeedInvoker = invocationChain.getHeadInvoker();
            } else if (operationName.equals("getAll")) {
                getAllInvoker = invocationChain.getHeadInvoker();
            } else if (operationName.equals("query")) {
                queryInvoker = invocationChain.getHeadInvoker();
            } else if (operationName.equals("get")) {
                getInvoker = invocationChain.getHeadInvoker();
                getOperation = operation;
            } else if (operationName.equals("put")) {
                putInvoker = invocationChain.getHeadInvoker();
            } else if (operationName.equals("putMedia")) {
                putMediaInvoker = invocationChain.getHeadInvoker();
            } else if (operationName.equals("post")) {
                postInvoker = invocationChain.getHeadInvoker();
            } else if (operationName.equals("postMedia")) {
                postMediaInvoker = invocationChain.getHeadInvoker();
            } else if (operationName.equals("delete")) {
                deleteInvoker = invocationChain.getHeadInvoker();
            }
        }

        // Determine the collection item type
        itemXMLType = new DataTypeImpl<Class<?>>(String.class.getName(), String.class, String.class);
        Class<?> itemClass = getOperation.getOutputType().getPhysical();
        if (itemClass == Entry.class) {
            supportsFeedEntries = true;
        }
        DataType<XMLType> outputType = getOperation.getOutputType();
        QName qname = outputType.getLogical().getElementName();
        qname = new QName(qname.getNamespaceURI(), itemClass.getSimpleName());
        itemClassType = new DataTypeImpl<XMLType>("java:complexType", itemClass, new XMLType(qname, null));
        
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // No authentication required for a get request

        // Get the request path
        String path = URLDecoder.decode(request.getRequestURI().substring(request.getServletPath().length()), "UTF-8");

        // The feedType parameter is used to override what type of feed is going
        // to be produced
        String requestFeedType = request.getParameter("feedType");
        if (requestFeedType == null)
            requestFeedType = feedType;

        if (! requestFeedType.startsWith("atom_")) {
                throw new UnsupportedOperationException(requestFeedType + " Not supported !");
        }
        
        logger.info(">>> FeedEndPointServlet (" + requestFeedType + ") " + request.getRequestURI());

        // Handle an Atom request
        if (path != null && path.equals("/atomsvc")) {
                /*
             <?xml version='1.0' encoding='UTF-8'?>
             <service xmlns="http://www.w3.org/2007/app" xmlns:atom="http://www.w3.org/2005/Atom">
                <workspace>
                   <atom:title type="text">resource</atom:title>
                   <collection href="http://luck.ibm.com:8084/customer">
                      <atom:title type="text">entries</atom:title>
                      <accept>application/atom+xml;type=entry</accept>
                      <categories />
                   </collection>
                </workspace>
             </service>
                 */
                
            // Return the Atom service document
            response.setContentType("application/atomsvc+xml; charset=utf-8");
            
            Service service = this.abdera.getFactory().newService();
            //service.setText("service");
            
            Workspace workspace = this.abdera.getFactory().newWorkspace();
            workspace.setTitle("resource");

            String href = request.getRequestURL().toString();
            href = href.substring(0, href.length() - "/atomsvc".length());
            
            Collection collection = workspace.addCollection("collection", "atom/feed");
            collection.setTitle("entries");
            collection.setAttributeValue("href", href);
            collection.setAccept("entry");
            collection.addCategories().setFixed(false);
            
            workspace.addCollection(collection);

            service.addWorkspace(workspace);

            //FIXME add prettyPrint support
            try {
                service.getDocument().writeTo(response.getOutputStream());
            } catch (IOException ioe) {
                throw new ServletException(ioe);
            }

        } else if (path == null || path.length() == 0 || path.equals("/")) {

            // Return a feed containing the entries in the collection
            Feed feed = null;
            if (supportsFeedEntries) {

                // The service implementation supports feed entries, invoke its getFeed operation
                Message requestMessage = messageFactory.createMessage();
                Message responseMessage = getFeedInvoker.invoke(requestMessage);
                if (responseMessage.isFault()) {
                    throw new ServletException((Throwable)responseMessage.getBody());
                }
                feed = (Feed)responseMessage.getBody();
                
            } else {

                // The service implementation does not support feed entries,
                // invoke its getAll operation to get the data item collection, then create
                // feed entries from the items
                Message requestMessage = messageFactory.createMessage();
                Message responseMessage;
                if (request.getQueryString() != null) {
                    requestMessage.setBody(new Object[] {request.getQueryString()});
                    responseMessage = queryInvoker.invoke(requestMessage);
                } else {
                    responseMessage = getAllInvoker.invoke(requestMessage);
                }
                if (responseMessage.isFault()) {
                    throw new ServletException((Throwable)responseMessage.getBody());
                }
                org.apache.tuscany.sca.implementation.data.collection.Entry<Object, Object>[] collection =
                    (org.apache.tuscany.sca.implementation.data.collection.Entry<Object, Object>[])responseMessage.getBody();
                if (collection != null) {
                    
                    // Create the feed
                    feed = this.abdera.getFactory().newFeed();
                    
                    // Set the feed title
                    if (title != null) {
                        feed.setTitle(title);
                    } else {
                        feed.setTitle("Feed");
                    }
                    
                    // Add entries to the feed
                    for (org.apache.tuscany.sca.implementation.data.collection.Entry<Object, Object> entry: collection) {
                        Entry feedEntry = createFeedEntry(entry);
                        feed.addEntry(feedEntry);
                    }
                }
            }
            if (feed != null) {
                
                // Write the Atom feed
                response.setContentType("application/atom+xml; charset=utf-8");
                try {
                         feed.getDocument().writeTo(response.getOutputStream());
                } catch (IOException ioe) {
                    throw new ServletException(ioe);
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            
        } else if (path.startsWith("/")) {

            // Return a specific entry in the collection
            Entry feedEntry;

            // Invoke the get operation on the service implementation
            Message requestMessage = messageFactory.createMessage();
            String id = path.substring(1);
            requestMessage.setBody(new Object[] {id});
            Message responseMessage = getInvoker.invoke(requestMessage);
            if (responseMessage.isFault()) {
                throw new ServletException((Throwable)responseMessage.getBody());
            }
            
            if (supportsFeedEntries) {
                // The service implementation returns a feed entry 
                feedEntry = responseMessage.getBody();
            } else {
                // The service implementation only returns a data item, create an entry
                // from it
                feedEntry = createFeedEntry(new org.apache.tuscany.sca.implementation.data.collection.Entry<Object, Object>(id, responseMessage.getBody()));
            }

            // Write the Atom entry
            if (feedEntry != null) {
                response.setContentType("application/atom+xml; charset=utf-8");
                try {
                        feedEntry.writeTo(getWriter(response));
                } catch (IOException ioe) {
                    throw new ServletException(ioe);
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            // Path doesn't match any known pattern
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        
    }

    /**
     * Create an Atom entry for a key and item from a collection.
     * @param key
     * @param item
     * @return
     */
    private Entry createFeedEntry(org.apache.tuscany.sca.implementation.data.collection.Entry<Object, Object> entry) {
        Object key = entry.getKey();
        Object data = entry.getData();
        if (data instanceof Item) {
            Item item = (Item)data;
            
            Entry feedEntry = abdera.getFactory().newEntry();
            if (key != null) {
                feedEntry.setId(key.toString());
            }
            feedEntry.setTitle(item.getTitle());
            feedEntry.setContentAsHtml(item.getContents());

            String href = item.getLink();
            if (href == null && key != null) {
                href = key.toString();
            }

            if (href != null) {
                feedEntry.addLink(href);
            }
            String related = item.getRelated();
            if (related != null) {
                feedEntry.addLink(related, "related");
            }
            String alternate = item.getAlternate();
            if (alternate != null) {
                feedEntry.addLink(alternate, "alternate");
            }
                
            Date date = item.getDate();
            if (date != null) {
                feedEntry.setUpdated(date);
            }
            return feedEntry;
            
        } else if (data != null) {
            Entry feedEntry = abdera.getFactory().newEntry();
             feedEntry.setId(key.toString());
             feedEntry.setTitle("item");
             
             
             // Convert the item to XML
             String value = mediator.mediate(data, itemClassType, itemXMLType, null).toString();
             
             Content content = this.abdera.getFactory().newContent();
             content.setContentType(Content.Type.XML);
             content.setValue(value);
             
             feedEntry.setContentElement(content);

             feedEntry.addLink(key.toString());
                  
             return feedEntry;
        } else {
            return null;
        }
    }

    /**
     * Create a data item from an Atom entry.
     * @param key
     * @param item
     * @return
     */
    private org.apache.tuscany.sca.implementation.data.collection.Entry<Object, Object> createEntry(Entry feedEntry) {
        if (feedEntry != null) {
            if (itemClassType.getPhysical() == Item.class) {
                String key = feedEntry.getId().toString();
                
                Item item = new Item();
                item.setTitle(feedEntry.getTitle());
                item.setContents(feedEntry.getContent());
                
                for (Link link : feedEntry.getLinks()) {
                    if (link.getRel() == null || "self".equals(link.getRel())) {
                        if (item.getLink() == null) {
                            item.setLink(link.getHref().toString());
                        }
                    } else if ("related".equals(link.getRel())) {
                        item.setRelated(link.getHref().toString());
                    } else if ("alternate".equals(link.getRel())) {
                        item.setAlternate(link.getHref().toString());
                    }
                }
                
                item.setDate(feedEntry.getUpdated());
                
                return new org.apache.tuscany.sca.implementation.data.collection.Entry<Object, Object>(key, item);
                
            } else {
                String key = null; 
                if ( feedEntry.getId() != null) {
                        feedEntry.getId().toString();
                }
                
                // Create the item from XML
                if (feedEntry.getContentElement().getElements().size() == 0) {
                        return null;
                }
                
                String value = feedEntry.getContent();
                Object data = mediator.mediate(value, itemXMLType, itemClassType, null);

                return new org.apache.tuscany.sca.implementation.data.collection.Entry<Object, Object>(key, data);
            }
        } else {
            return null;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {

        // Authenticate the user
        String user = processAuthorizationHeader(request);
        if (user == null) {
            unauthorized(response);
            return;
        }

        // Get the request path
        String path = URLDecoder.decode(request.getRequestURI().substring(request.getServletPath().length()), "UTF-8");

        if (path == null || path.length() == 0 || path.equals("/")) {
            Entry createdFeedEntry = null;

            // Create a new Atom entry
            String contentType = request.getContentType();
            if (contentType.startsWith("application/atom+xml")) {

                // Read the entry from the request
                Entry feedEntry;
                try {
                        Document<Entry> doc = Abdera.getNewParser().parse(request.getReader());
                        feedEntry = doc.getRoot();
                } catch (ParseException pe) {
                    throw new ServletException(pe);
                }

                // Let the component implementation create it
                if (supportsFeedEntries) {
                    
                    // The service implementation supports feed entries, pass the entry to it
                    Message requestMessage = messageFactory.createMessage();
                    requestMessage.setBody(new Object[] {feedEntry});
                    Message responseMessage = postInvoker.invoke(requestMessage);
                    if (responseMessage.isFault()) {
                        throw new ServletException((Throwable)responseMessage.getBody());
                    }
                    createdFeedEntry = responseMessage.getBody();
                } else {
                    
                    // The service implementation does not support feed entries, pass the data item to it
                    Message requestMessage = messageFactory.createMessage();
                    org.apache.tuscany.sca.implementation.data.collection.Entry<Object, Object> entry = createEntry(feedEntry);
                    requestMessage.setBody(new Object[] {entry.getKey(), entry.getData()});
                    Message responseMessage = postInvoker.invoke(requestMessage);
                    if (responseMessage.isFault()) {
                        throw new ServletException((Throwable)responseMessage.getBody());
                    }
                    entry.setKey(responseMessage.getBody());
                    createdFeedEntry = createFeedEntry(entry);
                }

            } else if (contentType != null) {

                // Create a new media entry

                // Get incoming headers
                String title = request.getHeader("Title");
                String slug = request.getHeader("Slug");

                // Let the component implementation create the media entry
                Message requestMessage = messageFactory.createMessage();
                requestMessage.setBody(new Object[] {title, slug, contentType, request.getInputStream()});
                Message responseMessage = postMediaInvoker.invoke(requestMessage);
                if (responseMessage.isFault()) {
                    throw new ServletException((Throwable)responseMessage.getBody());
                }
                createdFeedEntry = responseMessage.getBody();
            } else {
                response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            }

            // A new entry was created successfully
            if (createdFeedEntry != null) {

                // Set location of the created entry in the Location header
                Link link = createdFeedEntry.getSelfLink();
                if (link != null) {
                    response.addHeader("Location", link.getHref().toString());
                }

                // Write the created Atom entry
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.setContentType("application/atom+xml; charset=utf-8");
                try {
                        createdFeedEntry.writeTo(getWriter(response));
                } catch (ParseException pe) {
                    throw new ServletException(pe);
                }

            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private Writer getWriter(HttpServletResponse response) throws UnsupportedEncodingException, IOException {
        Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
        return writer;
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Authenticate the user
        String user = processAuthorizationHeader(request);
        if (user == null) {
            unauthorized(response);
            return;
        }

        // Get the request path
        String path = request.getRequestURI().substring(request.getServletPath().length());

        if (path != null && path.startsWith("/")) {
            String id = path.substring(1);

            // Update an Atom entry
            String contentType = request.getContentType();
            if (contentType.startsWith("application/atom+xml")) {

                // Read the entry from the request
                Entry feedEntry;
                try {
                        Document<Entry> doc = Abdera.getNewParser().parse(request.getReader());
                        feedEntry = doc.getRoot();
                } catch (ParseException pe) {
                    throw new ServletException(pe);
                }

                // Let the component implementation create it
                if (supportsFeedEntries) {
                    
                    // The service implementation supports feed entries, pass the entry to it
                    Message requestMessage = messageFactory.createMessage();
                    requestMessage.setBody(new Object[] {id, feedEntry});
                    Message responseMessage = putInvoker.invoke(requestMessage);
                    if (responseMessage.isFault()) {
                        Object body = responseMessage.getBody();
                        if (body.getClass().getName().endsWith(".NotFoundException")) {
                            response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        } else {
                            throw new ServletException((Throwable)responseMessage.getBody());
                        }
                    }
                } else {
                    
                    // The service implementation does not support feed entries, pass the data item to it
                    Message requestMessage = messageFactory.createMessage();
                    org.apache.tuscany.sca.implementation.data.collection.Entry<Object, Object> entry = createEntry(feedEntry);
                    requestMessage.setBody(new Object[] {entry.getKey(), entry.getData()});
                    Message responseMessage = putInvoker.invoke(requestMessage);
                    if (responseMessage.isFault()) {
                        Object body = responseMessage.getBody();
                        if (body.getClass().getName().endsWith(".NotFoundException")) {
                            response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        } else {
                            throw new ServletException((Throwable)responseMessage.getBody());
                        }
                    }
                }

            } else if (contentType != null) {

                // Updated a media entry

                // Let the component implementation create the media entry
                Message requestMessage = messageFactory.createMessage();
                requestMessage.setBody(new Object[] {id, contentType, request.getInputStream()});
                Message responseMessage = putMediaInvoker.invoke(requestMessage);
                Object body = responseMessage.getBody();
                if (responseMessage.isFault()) {
                    if (body.getClass().getName().endsWith(".NotFoundException")) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    } else {
                        throw new ServletException((Throwable)responseMessage.getBody());
                    }
                }
            } else {
                response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException {

        // Authenticate the user
        String user = processAuthorizationHeader(request);
        if (user == null) {
            unauthorized(response);
            return;
        }

        // Get the request path
        String path = URLDecoder.decode(request.getRequestURI().substring(request.getServletPath().length()), "UTF-8");

        String id;
        if (path != null && path.startsWith("/")) {
            id = path.substring(1);
        } else {
            id = "";
        }

        // Delete a specific entry from the collection
        Message requestMessage = messageFactory.createMessage();
        requestMessage.setBody(new Object[] {id});
        Message responseMessage = deleteInvoker.invoke(requestMessage);
        if (responseMessage.isFault()) {
            Object body = responseMessage.getBody();
            if (body.getClass().getName().endsWith(".NotFoundException")) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                throw new ServletException((Throwable)responseMessage.getBody());
            }
        }
    }

    /**
     * Process the authorization header
     * 
     * @param request
     * @return
     * @throws ServletException
     */
    private String processAuthorizationHeader(HttpServletRequest request) throws ServletException {
        
        // FIXME temporarily disabling this as it doesn't work with all browsers 
        if (true)
            return "admin";
        
        try {
            String authorization = request.getHeader("Authorization");
            if (authorization != null) {
                StringTokenizer tokens = new StringTokenizer(authorization);
                if (tokens.hasMoreTokens()) {
                    String basic = tokens.nextToken();
                    if (basic.equalsIgnoreCase("Basic")) {
                        String credentials = tokens.nextToken();
                        String userAndPassword = new String(Base64.decodeBase64(credentials.getBytes()));
                        int colon = userAndPassword.indexOf(":");
                        if (colon != -1) {
                            String user = userAndPassword.substring(0, colon);
                            String password = userAndPassword.substring(colon + 1);

                            // Authenticate the User.
                            if (authenticate(user, password)) {
                                return user;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
        return null;
    }

    /**
     * Authenticate a user.
     * 
     * @param user
     * @param password
     * @return
     */
    private boolean authenticate(String user, String password) {
        // TODO Handle this using SCA security policies
        return ("admin".equals(user) && "admin".equals(password));
    }

    /**
     * Reject an unauthorized request.
     * 
     * @param response
     */
    private void unauthorized(HttpServletResponse response) throws IOException {
        response.setHeader("WWW-Authenticate", "BASIC realm=\"Tuscany\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
