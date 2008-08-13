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

import static org.apache.tuscany.sca.binding.atom.provider.AtomBindingUtil.entry;
import static org.apache.tuscany.sca.binding.atom.provider.AtomBindingUtil.feedEntry;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.model.Service;
import org.apache.abdera.model.Workspace;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.writer.WriterFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.tuscany.sca.data.collection.Entry;
import org.apache.tuscany.sca.databinding.Mediator;
import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.impl.DataTypeImpl;
import org.apache.tuscany.sca.interfacedef.Operation;
import org.apache.tuscany.sca.interfacedef.util.XMLType;
import org.apache.tuscany.sca.invocation.InvocationChain;
import org.apache.tuscany.sca.invocation.Invoker;
import org.apache.tuscany.sca.invocation.Message;
import org.apache.tuscany.sca.invocation.MessageFactory;
import org.apache.tuscany.sca.runtime.RuntimeWire;

/**
 * A resource collection binding listener, implemented as a Servlet and
 * registered in a Servlet host provided by the SCA hosting runtime.
 *
 * @version $Rev$ $Date$
 */
class AtomBindingListenerServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AtomBindingListenerServlet.class.getName());
    private static final long serialVersionUID = 1L;

    private static final Factory abderaFactory = Abdera.getNewFactory();
    private static final Parser abderaParser = Abdera.getNewParser();
    private static final String ETAG = "ETag";
    private static final String LASTMODIFIED = "Last-Modified";    
    private static final String LOCATION = "Location";    
    private static final String CONTENTLOCATION = "Content-Location";    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss Z" ); // RFC 822 date time
    
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
    AtomBindingListenerServlet(RuntimeWire wire, MessageFactory messageFactory, Mediator mediator, String title) {
        this.wire = wire;
        this.messageFactory = messageFactory;
        this.mediator = mediator;
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
        if (itemClass == org.apache.abdera.model.Entry.class) {
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
    	int servletPathLength = request.getContextPath().length() + request.getServletPath().length();
        String path = URLDecoder.decode(request.getRequestURI().substring(servletPathLength), "UTF-8");

        logger.fine("get " + request.getRequestURI());

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
            
            Service service = abderaFactory.newService();
            //service.setText("service");
            
            Workspace workspace = abderaFactory.newWorkspace();
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
                Message responseMessage;
                if (request.getQueryString() != null) {
                    requestMessage.setBody(new Object[] {request.getQueryString()});
                    responseMessage = queryInvoker.invoke(requestMessage);
                } else {
                    responseMessage = getFeedInvoker.invoke(requestMessage);
                }
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
                Entry<Object, Object>[] collection =
                    (Entry<Object, Object>[])responseMessage.getBody();
                if (collection != null) {
                    
                    // Create the feed
                    feed = abderaFactory.newFeed();
                    
                    // Set the feed title
                    if (title != null) {
                        feed.setTitle(title);
                    } else {
                        feed.setTitle("Feed");
                    }
                    // All feeds must provide Id and updated elements.
                    // However, some do not, so provide some program protection.
                    feed.setId( "Feed" + feed.hashCode());
                    Date lastModified = new Date( 0 );
                    
                    // Add entries to the feed
                    for (Entry<Object, Object> entry: collection) {
                        org.apache.abdera.model.Entry feedEntry = feedEntry(entry, itemClassType, itemXMLType, mediator, abderaFactory);
                        // Use the most recent entry update as the feed update
                        Date entryUpdated = feedEntry.getUpdated();
                        if (( entryUpdated != null ) && (entryUpdated.compareTo( lastModified  ) > 0 ))
                        	lastModified = entryUpdated;
                        feed.addEntry(feedEntry);
                    }
                    // If no entries were newly updated,
                    if ( lastModified.compareTo( new Date( 0 ) ) == 0 ) 
                    	lastModified = new Date();
                }
            }
            if (feed != null) {
                String feedETag = "\"" + generateFeedETag( feed ) + "\"";
                Date feedUpdated = feed.getUpdated();
                // Test request for predicates.
                String predicate = request.getHeader( "If-Match" );
                if (( predicate != null ) && ( !predicate.equals(feedETag) )) {
                	// No match, should short circuit
                    response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                    return;
                }
                predicate = request.getHeader( "If-None-Match" );
                if (( predicate != null ) && ( predicate.equals(feedETag) )) {
                	// Match, should short circuit
                    response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
                if ( feedUpdated != null ) {
                	predicate = request.getHeader( "If-Unmodified-Since" );                
                	if ( predicate != null ) {
                		try {
                			Date predicateDate = dateFormat.parse( predicate ); 
                			if ( predicateDate.compareTo( feedUpdated ) < 0 ) {
                				// Match, should short circuit
                				response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                				return;
                			}             		
                		} catch ( java.text.ParseException e ) {
                			// Ignore and move on
                		}
                	}
                	predicate = request.getHeader( "If-Modified-Since" );                
                	if ( predicate != null ) {
                		try {
                			Date predicateDate = dateFormat.parse( predicate ); 
                			if ( predicateDate.compareTo( feedUpdated ) > 0 ) {
                				// Match, should short circuit
                				response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                				return;
                			}             		
                		} catch ( java.text.ParseException e ) {
                			// Ignore and move on
                		}
                	}
                }
                // Content negotiation
            	String acceptType = request.getHeader( "Accept" );
            	String preferredType = getContentPreference( acceptType ); 
            	if (( preferredType != null ) && ((preferredType.indexOf( "json") > -1) || (preferredType.indexOf( "JSON") > -1 ))) {
            		// JSON response body
                    response.setContentType("application/json;type=feed");
                    
                    try {
                		Abdera abdera = new Abdera();
                		WriterFactory wf = abdera.getWriterFactory();
                		org.apache.abdera.writer.Writer json = wf.getWriter("json");
                    	feed.writeTo(json, response.getWriter());
                    } catch (Exception e) {
                        throw new ServletException(e);
                    }           		

            	} else {
            		// Write the Atom feed
            		response.setContentType("application/atom+xml;type=feed");
            		// Provide Etag based on Id and time.               
            		response.addHeader(ETAG, feedETag );
            		if ( feedUpdated != null )
            			response.addHeader(LASTMODIFIED, dateFormat.format( feedUpdated ));
            		try {
            			feed.getDocument().writeTo(response.getOutputStream());
            		} catch (IOException ioe) {
            			throw new ServletException(ioe);
            		}
            	}
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            
        } else if (path.startsWith("/")) {
            // Return a specific entry in the collection
            org.apache.abdera.model.Entry feedEntry;

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
                Entry<Object, Object> entry = new Entry<Object, Object>(id, responseMessage.getBody()); 
                feedEntry = feedEntry(entry, itemClassType, itemXMLType, mediator, abderaFactory);
            }
            // Write the Atom entry
            if (feedEntry != null) {
                IRI feedId = feedEntry.getId();
                if ( feedId != null )
                   response.addHeader(ETAG, "\"" + feedId.toString() + "\"" );
                Date entryUpdated = feedEntry.getUpdated();
                if ( entryUpdated != null )
                   response.addHeader(LASTMODIFIED, dateFormat.format( entryUpdated ));
                // TODO Check If-Modified-Since If-Unmodified-Since predicates against LASTMODIFIED. 
                // If true return 304 and null body.            
                Link link = feedEntry.getSelfLink();
                if (link != null) {
                    response.addHeader(LOCATION, link.getHref().toString());
                } else {
                   link = feedEntry.getLink( "Edit" );
                   if (link != null) {
                      response.addHeader(LOCATION, link.getHref().toString());
                   }
                }

                // Content negotiation
                String acceptType = request.getHeader( "Accept" );
            	String preferredType = getContentPreference( acceptType ); 
            	if (( preferredType != null ) && ((preferredType.indexOf( "json") > -1) || (preferredType.indexOf( "JSON") > -1 ))) {
            		// JSON response body
                    response.setContentType("application/json;type=entry");
                    try {
                		Abdera abdera = new Abdera();
                		WriterFactory wf = abdera.getWriterFactory();
                		org.apache.abdera.writer.Writer json = wf.getWriter("json");
                    	feedEntry.writeTo(json, response.getWriter());
                    } catch (Exception e) {
                        throw new ServletException(e);
                    }           		
            	} else {
            		// XML response body 
            		response.setContentType("application/atom+xml;type=entry");
            		try {
            			feedEntry.writeTo(getWriter(response));
            		} catch (IOException ioe) {
            			throw new ServletException(ioe);
            		}
            	}
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            // Path doesn't match any known pattern
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
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
            org.apache.abdera.model.Entry createdFeedEntry = null;

            // Create a new Atom entry
            String contentType = request.getContentType();
            if (contentType != null && contentType.startsWith("application/atom+xml")) {

                // Read the entry from the request
                org.apache.abdera.model.Entry feedEntry;
                try {
                        Document<org.apache.abdera.model.Entry> doc = abderaParser.parse(request.getReader());
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
                    Entry<Object, Object> entry = entry(feedEntry, itemClassType, itemXMLType, mediator);
                    requestMessage.setBody(new Object[] {entry.getKey(), entry.getData()});
                    Message responseMessage = postInvoker.invoke(requestMessage);
                    if (responseMessage.isFault()) {
                        throw new ServletException((Throwable)responseMessage.getBody());
                    }
                    entry.setKey(responseMessage.getBody());
                    createdFeedEntry = feedEntry(entry, itemClassType, itemXMLType, mediator, abderaFactory);
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
                IRI feedId = createdFeedEntry.getId();
                if ( feedId != null )
                   response.addHeader(ETAG, "\"" + feedId.toString() + "\"" );
                Date entryUpdated = createdFeedEntry.getUpdated();
                if ( entryUpdated != null )
                   response.addHeader(LASTMODIFIED, dateFormat.format( entryUpdated ));
                Link link = createdFeedEntry.getSelfLink();
                if (link != null) {
                    response.addHeader(LOCATION, link.getHref().toString());
                } else {
                   link = createdFeedEntry.getLink( "Edit" );
                   if (link != null) {
                      response.addHeader(LOCATION, link.getHref().toString());
                   }
                }

                // Write the created Atom entry
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.setContentType("application/atom+xml;type=entry");
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
            if (contentType != null && contentType.startsWith("application/atom+xml")) {

                // Read the entry from the request
                org.apache.abdera.model.Entry feedEntry;
                try {
                        Document<org.apache.abdera.model.Entry> doc = abderaParser.parse(request.getReader());
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
                    Entry<Object, Object> entry = entry(feedEntry, itemClassType, itemXMLType, mediator);
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
    
    /**
     * Generate ETag based on feed Id and updated fields.
     * @param feed
     * @return ETag
     */
    public static String generateFeedETag( Feed feed ) {
    	if ( feed == null ) {
    		return null; 
    	}
        
    	IRI feedIdIRI = feed.getId();
        String feedId = "ID";
        if ( feedIdIRI != null ) {
        	feedId = feedIdIRI.toString();
        }
        
        Date feedUpdated = feed.getUpdated();
        if ( feedUpdated == null ) {
        	return feedId;
        }
        
        return feedId + "-" + feedUpdated.hashCode();
    }

    public static String getContentPreference( String acceptType ) {
    	if (( acceptType == null ) || ( acceptType.length() < 1 )) {
            return "application/atom+xml";    		
    	}
    	StringTokenizer st = new StringTokenizer( acceptType, "," );
    	if ( st.hasMoreTokens() )
    		return st.nextToken();    		
        return "application/atom+xml";
    }
}
