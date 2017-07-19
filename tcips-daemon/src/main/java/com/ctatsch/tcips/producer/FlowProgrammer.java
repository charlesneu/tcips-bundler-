/*
 * Copyright (c) 2017 Cassio Tatsch and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.producer;

import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctatsch.tcips.db.TcipsFlow;
import com.ctatsch.tcips.utils.OpenflowUtils;

/**
 * Provides flow write preprocessor methods.
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */
public class FlowProgrammer {

    private static final Logger LOG = LoggerFactory.getLogger(FlowProgrammer.class);

    private final FlowCommitImpl flowCommit;
    private final DataBroker dataBroker;

    /**
     * Creates a new {@link FlowProgrammer} passing arguments dataBroker, to
     * handle configuration and operational data from switches.
     * 
     * @param dataBroker
     *            the provider to access data tree store.
     * @param flowCommit
     *            a {@code FlowProgrammer} instance responsible for add flows on
     *            the controller.
     */
    public FlowProgrammer(DataBroker dataBroker, FlowCommitImpl flowCommit) {
        this.dataBroker = dataBroker;
        this.flowCommit = flowCommit;
    }

    /**
     * Process a received packet.
     * 
     * @param packet
     *            a {@link TcipsFlow} object with packet header fields.
     * @param ingressPortPath
     *            the incoming node port path.
     */
    public void processIncommingFlow(TcipsFlow packet, InstanceIdentifier<?> ingressPortPath) {
        InstanceIdentifier<Node> nodeIId = ingressPortPath.firstIdentifierOf(Node.class);
        InstanceIdentifier<NodeConnector> inNodeConnectorIi = ingressPortPath.firstIdentifierOf(NodeConnector.class);

        LOG.trace("Pacote: {}", packet.toString());
        Integer outPort = getOutPort(nodeIId, packet);
        LOG.trace("OutPort {}", outPort);
        Node node = OpenflowUtils.getOperationalData(dataBroker.newReadOnlyTransaction(), nodeIId);
        List<NodeConnector> nodeConnetorList = node.getNodeConnector();
        NodeConnector outNodeConnector = null;
        for (NodeConnector nc : nodeConnetorList) {
            LOG.trace("Found {}", nc.getId().getValue());
            if (("" + outPort).equals(nc.getId().getValue().split(":")[2])) {
                outNodeConnector = nc;
            }
        }
        if (outNodeConnector == null) {
            LOG.error("Output node connector not found.");
            return;
        }

        InstanceIdentifier<NodeConnector> outNodeConnectorIi = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, nodeIId.firstKeyOf(Node.class))
                .child(NodeConnector.class, outNodeConnector.getKey())
                .build();

        flowCommit.addTempFlow(nodeIId, inNodeConnectorIi, outNodeConnectorIi, (short) 0, 60000, packet);

    }

    /**
     * Returns the output port number according destination IP address and Node.
     * 
     * @param nodeId
     *            the node instance identifier.
     * @param packet
     *            the packet header fields.
     * @return the output port number.
     */
    private Integer getOutPort(InstanceIdentifier<Node> nodeIId, TcipsFlow packet) {
       

        switch (nodeIId.firstKeyOf(Node.class).getId().getValue().split(":")[1]) {
        case "1":
            switch (packet.getDstIpv4()) {
            case "10.0.0.11/32":
                return 1;
            case "10.0.0.12/32":
                return 2;
            case "10.0.0.13/32":
                return 3;
            case "10.0.0.21/32":
            case "10.0.0.31/32":
            case "10.0.0.32/32":
            case "10.0.0.33/32":
            case "10.0.0.41/32":
            case "10.0.0.42/32":
                return 4;
            }
        case "2":
            switch (packet.getDstIpv4()) {
            case "10.0.0.11/32":
            case "10.0.0.12/32":
            case "10.0.0.13/32":
                return 1;
            case "10.0.0.21/32":
                return 2;
            case "10.0.0.31/32":
            case "10.0.0.32/32":
            case "10.0.0.33/32":
                return 3;
            case "10.0.0.41/32":
            case "10.0.0.42/32":
                return 4;
            }
        case "3":
            switch (packet.getDstIpv4()) {
            case "10.0.0.11/32":
            case "10.0.0.12/32":
            case "10.0.0.13/32":
            case "10.0.0.21/32":
                return 1;
            case "10.0.0.31/32":
                return 2;
            case "10.0.0.32/32":
                return 3;
            case "10.0.0.33/32":
                return 4;
            case "10.0.0.41/32":
            case "10.0.0.42/32":
                return 1;
            }
        case "4":
            switch (packet.getDstIpv4()) {
            case "10.0.0.11/32":
            case "10.0.0.12/32":
            case "10.0.0.13/32":
            case "10.0.0.21/32":
            case "10.0.0.31/32":
            case "10.0.0.32/32":
            case "10.0.0.33/32":
                return 1;
            case "10.0.0.41/32":
                return 2;
            case "10.0.0.42/32":
                return 3;
            }
        }
        return null;
    }
}
