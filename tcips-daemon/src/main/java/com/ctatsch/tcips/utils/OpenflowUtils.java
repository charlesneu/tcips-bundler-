/*
 * Copyright (c) 2017 Cassio Tatsch and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.utils;

import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.base.Optional;

/**
 * Provides utility methods for node manipulation.
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */
public class OpenflowUtils {

    public static AtomicLong FLOW_ID_INC = new AtomicLong();
    public static AtomicLong FLOW_COOKIE_INC = new AtomicLong(0x2b00000000000000L);

    private OpenflowUtils() {
        // do not create instance
    }

    /**
     * Gets operational data from logical data store.
     * 
     * @param readOnlyTransaction
     *            a read only transaction instance.
     * @param identifier
     *            the element (node, flow, table) instance identifier (path).
     * @return a data container which has structured contents, {@code null} if
     *         no data is present.
     */
    public static <T extends DataObject> T getOperationalData(final ReadTransaction readOnlyTransaction,
            final InstanceIdentifier<T> identifier) {
        Optional<T> optionalData = null;
        try {
            optionalData = readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, identifier).get();
            if (optionalData.isPresent()) {
                return optionalData.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets configuration data from logical data store.
     * 
     * @param readOnlyTransaction
     *            a read only transaction instance.
     * @param identifier
     *            the element (node, flow, table) instance identifier (path).
     * @return a data container which has structured contents, {@code null} if
     *         no data is present.
     */
    public static <T extends DataObject> T getConfigData(final ReadTransaction readOnlyTransaction,
            final InstanceIdentifier<T> identifier) {
        Optional<T> optionalData = null;
        try {
            optionalData = readOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION, identifier).get();
            if (optionalData.isPresent()) {
                return optionalData.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a table instance identifier from the node and flow table id passed
     * by arguments.
     * 
     * @param nodeId
     *            the node instance identifier.
     * @param flowTableId
     *            the flow table id.
     * @return a table instance identifier.
     */
    public static InstanceIdentifier<Table> getTableInstanceId(InstanceIdentifier<Node> nodeId, Short flowTableId) {
        // get flow table key
        TableKey flowTableKey = new TableKey(flowTableId);

        return nodeId.builder()
                .augmentation(FlowCapableNode.class)
                .child(Table.class, flowTableKey)
                .build();
    }

    /**
     * Gets a flow instance identifier from the table instance id passed by
     * argument.
     * 
     * @param tableId
     *            the table node instance identifier.
     * @return a flow instance identifier.
     */
    public static InstanceIdentifier<Flow> getFlowInstanceId(InstanceIdentifier<Table> tableId, String flowName) {
        // generate unique flow key
        FlowId flowId = new FlowId(flowName);
        FlowKey flowKey = new FlowKey(flowId);
        return tableId.child(Flow.class, flowKey);
    }

}
