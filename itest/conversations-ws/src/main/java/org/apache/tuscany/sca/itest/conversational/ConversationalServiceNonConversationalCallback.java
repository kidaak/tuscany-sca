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
package org.apache.tuscany.sca.itest.conversational;

import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Conversational;
import org.osoa.sca.annotations.EndsConversation;
import org.osoa.sca.annotations.Remotable;


/**
 * The service interface used when testing conversations
 *
 * @version $Rev: 537240 $ $Date: 2007-05-11 18:35:03 +0100 (Fri, 11 May 2007) $
 */
@Remotable
@Conversational
@Callback(NonConversationalCallback.class)
public interface ConversationalServiceNonConversationalCallback {

    
    public void initializeCount(int count);
    
    public void incrementCount();
    
    public int retrieveCount();
    
    public void businessException() throws BusinessException;        
    
    public void initializeCountCallback(int count);
    
    public void incrementCountCallback();
    
    public int retrieveCountCallback();
    
    public void businessExceptionCallback() throws BusinessException;    
    
    @EndsConversation
    public String endConversation();
    
    public String endConversationCallback();

}
