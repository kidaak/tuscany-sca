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
package org.apache.tuscany.binding.axis2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.WSDLToAxisServiceBuilder;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2004Constants;
import org.apache.tuscany.binding.axis2.util.WebServicePortMetaData;
import org.apache.tuscany.spi.builder.BuilderConfigException;
import org.apache.tuscany.spi.component.CompositeComponent;
import org.apache.tuscany.spi.component.WorkContext;
import org.apache.tuscany.spi.extension.ServiceExtension;
import org.apache.tuscany.spi.host.ServletHost;
import org.apache.tuscany.spi.model.ServiceContract;
import org.apache.tuscany.spi.wire.Interceptor;
import org.apache.tuscany.spi.wire.InvocationChain;
import org.apache.tuscany.spi.wire.Message;
import org.apache.tuscany.spi.wire.MessageId;
import org.apache.tuscany.spi.wire.MessageImpl;
import org.apache.tuscany.spi.wire.TargetInvoker;
import org.apache.tuscany.spi.wire.WireService;
import org.osoa.sca.annotations.Destroy;

/**
 * An implementation of a {@link ServiceExtension} configured with the Axis2 binding
 * 
 * @version $Rev$ $Date$
 */
public class Axis2Service extends ServiceExtension {
    private ServiceContract<?> serviceContract;

    private ServletHost servletHost;

    private ConfigurationContext configContext;

    private WebServiceBinding binding;

    private WorkContext workContext;

    private Map<MessageId, InvocationContext> invCtxMap = new HashMap<MessageId, InvocationContext>();

    public Axis2Service(String theName, ServiceContract<?> serviceContract, CompositeComponent parent,
            WireService wireService, WebServiceBinding binding, ServletHost servletHost,
            ConfigurationContext configContext, WorkContext workContext) {

        super(theName, serviceContract.getInterfaceClass(), parent, wireService);

        this.serviceContract = serviceContract;
        this.binding = binding;
        this.servletHost = servletHost;
        this.configContext = configContext;
        this.workContext = workContext;
    }

    public void start() {
        super.start();

        try {
            configContext.getAxisConfiguration().addService(createAxisService(binding));
        } catch (AxisFault e) {
            throw new Axis2BindingRunTimeException(e);
        }

        Axis2ServiceServlet servlet = new Axis2ServiceServlet();
        servlet.init(configContext);
        configContext.setContextRoot(getName());
        servletHost.registerMapping("/" + getName(), servlet);
    }

    @Destroy
    public void stop() {
        servletHost.unregisterMapping("/" + getName());
        try {
            configContext.getAxisConfiguration().removeService(getName());
        } catch (AxisFault e) {
            throw new Axis2BindingRunTimeException(e);
        }
        super.stop();
    }

    private AxisService createAxisService(WebServiceBinding wsBinding) throws AxisFault {
        Definition definition = wsBinding.getWSDLDefinition();
        WebServicePortMetaData wsdlPortInfo =
                new WebServicePortMetaData(definition, wsBinding.getWSDLPort(), null, false);

        // TODO investigate if this is 20 wsdl what todo?
        WSDLToAxisServiceBuilder builder =
                new WSDL11ToAxisServiceBuilder(definition, wsdlPortInfo.getServiceName(), wsdlPortInfo.getPort()
                        .getName());
        builder.setServerSide(true);
        AxisService axisService = builder.populateService();

        axisService.setName(this.getName());
        axisService.setServiceDescription("Tuscany configured AxisService for service: '" + this.getName() + '\'');

        // Use the existing WSDL
        Parameter wsdlParam = new Parameter(WSDLConstants.WSDL_4_J_DEFINITION, null);
        wsdlParam.setValue(definition);
        axisService.addParameter(wsdlParam);
        Parameter userWSDL = new Parameter("useOriginalwsdl", "true");
        axisService.addParameter(userWSDL);

        PortType wsdlPortType = wsdlPortInfo.getPortType();
        for (Object o : wsdlPortType.getOperations()) {
            Operation wsdlOperation = (Operation) o;
            String operationName = wsdlOperation.getName();
            QName operationQN = new QName(definition.getTargetNamespace(), operationName);

            org.apache.tuscany.spi.model.Operation<?> op = serviceContract.getOperations().get(operationName);

            MessageReceiver msgrec = null;
            if (inboundWire.getCallbackReferenceName() != null) {
                msgrec = new Axis2ServiceInOutAsyncMessageReceiver(this, op, workContext);
            } else {
                msgrec = new Axis2ServiceInOutSyncMessageReceiver(this, op);
            }

            AxisOperation axisOp = axisService.getOperation(operationQN);
            axisOp.setMessageExchangePattern(WSDL20_2004Constants.MEP_URI_IN_OUT);
            axisOp.setMessageReceiver(msgrec);
        }

        return axisService;
    }

    public Object invokeTarget(org.apache.tuscany.spi.model.Operation<?> op, Object[] args)
        throws InvocationTargetException {
        InvocationChain chain = inboundWire.getInvocationChains().get(op);
        Interceptor headInterceptor = chain.getHeadInterceptor();
        if (headInterceptor == null) {
            try {
                // short-circuit the dispatch and invoke the target directly
                if (chain.getTargetInvoker() == null) {
                    throw new AssertionError("No target invoker [" + chain.getOperation().getName() + "]");
                }
                return chain.getTargetInvoker().invokeTarget(args);
            } catch (InvocationTargetException e) {
                // the cause was thrown by the target so throw it
                throw e;
            }
        } else {
            Object messageId = workContext.getCurrentMessageId();
            workContext.setCurrentMessageId(null);
            Object correlationId = workContext.getCurrentCorrelationId();
            workContext.setCurrentCorrelationId(null);

            Message msg = new MessageImpl();
            msg.setTargetInvoker(chain.getTargetInvoker());
            if (messageId == null) {
                messageId = new MessageId();
            }
            msg.setMessageId(messageId);
            msg.setCorrelationId(correlationId);
            msg.setBody(args);
            Message resp;
            // dispatch the wire down the chain and get the response
            // TODO http://issues.apache.org/jira/browse/TUSCANY-777
            ClassLoader oldtccl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
                resp = headInterceptor.invoke(msg);
            } finally {
                Thread.currentThread().setContextClassLoader(oldtccl);
            }
            Object body = resp.getBody();
            if (resp.isFault()) {
                throw new InvocationTargetException((Throwable) body);
            }
            return body;
        }
    }

    /**
     * Get the Method from an interface matching the WSDL operation name
     */
    protected Method getMethod(Class<?> serviceInterface, String operationName) {
        // Note: this doesn't support overloaded operations
        Method[] methods = serviceInterface.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(operationName)) {
                return m;
            }
            // tolerate WSDL with capatalized operation name
            StringBuilder sb = new StringBuilder(operationName);
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
            if (m.getName().equals(sb.toString())) {
                return m;
            }
        }
        throw new BuilderConfigException("no operation named " + operationName + " found on service interface: "
                + serviceInterface.getName());
    }

    public TargetInvoker createCallbackTargetInvoker(
            ServiceContract contract,
            org.apache.tuscany.spi.model.Operation operation) {

        return new Axis2ServiceCallbackTargetInvoker(workContext, this);
    }

    public void addMapping(MessageId msgId, InvocationContext invCtx) {
        this.invCtxMap.put(msgId, invCtx);
    }

    public InvocationContext retrieveMapping(MessageId msgId) {
        return this.invCtxMap.get(msgId);
    }

    public void removeMapping(MessageId msgId) {
        this.invCtxMap.remove(msgId);
    }

    protected class InvocationContext {
        public MessageContext inMessageContext;

        public org.apache.tuscany.spi.model.Operation<?> operation;

        public SOAPFactory soapFactory;

        public InvocationContext(MessageContext messageCtx, org.apache.tuscany.spi.model.Operation<?> operation,
                SOAPFactory soapFactory) {
            this.inMessageContext = messageCtx;
            this.operation = operation;
            this.soapFactory = soapFactory;
        }
    }
}
