/**
 *
 *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tuscany.samples.helloworldwsclient;

import org.osoa.sca.annotations.Scope;
import org.osoa.sca.annotations.Service;
import org.osoa.sca.annotations.Reference;

/**
 * This class implements the HelloWorld service component.
 */
@Service(HelloWorldServiceComponent.class)
//FIXME workaround for JIRA TUSCANY-41
@Scope("MODULE")
public class HelloWorldServiceComponentImpl implements HelloWorldServiceComponent {
    
    @Reference
    public HelloWorldServiceComponent hellowWorldService = null; // Injected by the SCA container.

    /**
     * @see org.apache.tuscany.samples.helloworldwsclient.HelloWorldServiceComponent#getGreetings(java.lang.String)
     */
    public String getGreetings(final String name) {
        assert null != hellowWorldService : "helloWorldService was not set by the SCA runtime!";

        return hellowWorldService.getGreetings(name);
    }

}
