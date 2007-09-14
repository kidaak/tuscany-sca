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

package org.apache.tuscany.sca.interfacedef.impl;

import java.util.List;

import org.apache.tuscany.sca.interfacedef.DataType;
import org.apache.tuscany.sca.interfacedef.IncompatibleInterfaceContractException;
import org.apache.tuscany.sca.interfacedef.Interface;
import org.apache.tuscany.sca.interfacedef.InterfaceContract;
import org.apache.tuscany.sca.interfacedef.InterfaceContractMapper;
import org.apache.tuscany.sca.interfacedef.Operation;

/**
 * @version $Rev$ $Date$
 */
public class InterfaceContractMapperImpl implements InterfaceContractMapper {

    public boolean isCompatible(DataType source, DataType target, boolean remotable) {
        if (source == target) {
            return true;
        }
        if (!remotable) {
            if (source == null || target == null) {
                return false;
            }
            // For local case
            return target.getPhysical().isAssignableFrom(source.getPhysical());
        } else {
            // FIXME: How to test if two remotable data type is compatible?
            // return target.getLogical().equals(source.getLogical());
            return true;
        }

    }

    public boolean isCompatible(Operation source, Operation target, boolean remotable) {
        if (source == target) {
            return true;
        }
        
        if(source.isDynamic() || target.isDynamic()) {
            return true;
        }

        // Check name
        if (!source.getName().equals(target.getName())) {
            return false;
        }
        
        if (source.getInterface().isRemotable() != target.getInterface().isRemotable()) {
            return false;
        }

        // FIXME: We need to deal with wrapped<-->unwrapped conversion

        // Check output type
        DataType sourceOutputType = source.getOutputType();
        DataType targetOutputType = target.getOutputType();

        // Note the target output type is now the source for checking
        // compatibility
        if (!isCompatible(targetOutputType, sourceOutputType, remotable)) {
            return false;
        }

        List<DataType> sourceInputType = source.getInputType().getLogical();
        if (source.isWrapperStyle()) {
            sourceInputType = source.getWrapper().getUnwrappedInputType().getLogical();
        }
        List<DataType> targetInputType = target.getInputType().getLogical();
        if (target.isWrapperStyle()) {
            targetInputType = target.getWrapper().getUnwrappedInputType().getLogical();
        }

        if (sourceInputType.size() != targetInputType.size()) {
            return false;
        }

        int size = sourceInputType.size();
        for (int i = 0; i < size; i++) {
            if (!isCompatible(sourceInputType.get(i), targetInputType.get(i), remotable)) {
                return false;
            }
        }

        // Check fault types
        for (DataType targetFaultType : target.getFaultTypes()) {
            // Source fault types must be the same or superset of target fault
            // types
            boolean found = true;
            for (DataType sourceFaultType : source.getFaultTypes()) {
                found = false;
                if (isCompatible(targetFaultType, sourceFaultType, remotable)) {
                    // Target fault type can be covered by the source fault type
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }

    // FIXME: How to improve the performance for the lookup
    private Operation getOperation(List<Operation> operations, String name) {
        for (Operation op : operations) {
            if (op.getName().equals(name)) {
                return op;
            }
        }
        return null;
    }

    public boolean checkCompatibility(InterfaceContract source,
                                      InterfaceContract target,
                                      boolean ignoreCallback,
                                      boolean silent) throws IncompatibleInterfaceContractException {
        if (source == target) {
            // Shortcut for performance
            return true;
        }

        if (source.getInterface().isDynamic() || target.getInterface().isDynamic()) {
            return true;
        }
        
        if (source.getInterface().isRemotable() != target.getInterface().isRemotable()) {
            if (!silent) {
                throw new IncompatibleInterfaceContractException("Remotable settings do not match", source, target);
            } else {
                return false;
            }
        }
        if (source.getInterface().isConversational() != target.getInterface().isConversational()) {
            if (!silent) {
                throw new IncompatibleInterfaceContractException("Interaction scopes do not match", source, target);
            } else {
                return false;
            }
        }

        for (Operation operation : source.getInterface().getOperations()) {
            Operation targetOperation = map(target.getInterface(), operation);
            if (targetOperation == null) {
                if (!silent) {
                    throw new IncompatibleInterfaceContractException("Operation not found on target", source, target);
                } else {
                    return false;
                }
            }
            if (!source.getInterface().isRemotable()) {
                // FIXME: for remotable operation, only compare name for now
                if (!isCompatible(operation, targetOperation, false)) {
                    if (!silent) {
                        throw new IncompatibleInterfaceContractException("Target operations are not compatible",
                                                                         source, target);
                    } else {
                        return false;
                    }
                }
            }
        }

        if (ignoreCallback) {
            return true;
        }

        if (source.getCallbackInterface() == null && target.getCallbackInterface() == null) {
            return true;
        }
        if (source.getCallbackInterface() == null || target.getCallbackInterface() == null) {
            if (!silent) {
                throw new IncompatibleInterfaceContractException("Callback interface doesn't match", source, target);
            } else {
                return false;
            }
        }

        for (Operation operation : source.getCallbackInterface().getOperations()) {
            Operation targetOperation = getOperation(target.getCallbackInterface().getOperations(), operation.getName());
            if (targetOperation == null) {
                if (!silent) {
                    throw new IncompatibleInterfaceContractException("Callback operation not found on target", source,
                                                                     target, null, targetOperation);
                } else {
                    return false;
                }
            }
            if (!source.getCallbackInterface().isRemotable()) {
                // FIXME: for remotable operation, only compare name for now
                if (!operation.equals(targetOperation)) {
                    if (!silent) {
                        throw new IncompatibleInterfaceContractException("Target callback operation is not compatible",
                                                                         source, target, operation, targetOperation);
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isCompatible(Interface source, Interface target) {
        if (source == target) {
            // Shortcut for performance
            return true;
        }
        if (source == null || target == null) {
            return false;
        }
        
        if (source.isDynamic() || target.isDynamic()) {
            return true;
        }
        
        if (source.isRemotable() != target.isRemotable()) {
            return false;
        }
        if (source.isConversational() != target.isConversational()) {
            return false;
        }

        for (Operation operation : source.getOperations()) {
            Operation targetOperation = getOperation(target.getOperations(), operation.getName());
            if (targetOperation == null) {
                return false;
            }
            if (!isCompatible(operation, targetOperation, source.isRemotable())) {
                return false;
            }
        }
        return true;
    }

    public boolean isCompatible(InterfaceContract source, InterfaceContract target) {
        try {
            return checkCompatibility(source, target, false, true);
        } catch (IncompatibleInterfaceContractException e) {
            return false;
        }
    }

    /**
     * @see org.apache.tuscany.sca.interfacedef.InterfaceContractMapper#map(org.apache.tuscany.sca.interfacedef.Interface,
     *      org.apache.tuscany.sca.interfacedef.Operation)
     */
    public Operation map(Interface target, Operation source) {
        // TODO: How to handle the case that source operation is dynamic?
        if (target.isDynamic()) {
            return source;
        } else if (target.isRemotable()) {
            for (Operation op : target.getOperations()) {
                if (op.getName().equals(source.getName())) {
                    return op;
                }
            }
            return null;
        } else {
            for (Operation op : target.getOperations()) {
                if (isCompatible(source, op, target.isRemotable())) {
                    return op;
                }
            }
            return null;
        }

    }

}
