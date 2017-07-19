/*
 * Copyright (c) 2017 Cassio Tatsch and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.consumer;

import java.util.Iterator;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.statistics.FlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctatsch.tcips.db.TcipsFlow;
import com.ctatsch.tcips.db.TcipsFlowService;
import com.ctatsch.tcips.utils.OpenflowUtils;

/**
 * Collects openflow statistics.
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */
public class FlowStatsCollection implements Runnable, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(FlowStatsCollection.class);

    private boolean stop;
    private DataBroker dataBroker;

    private TcipsFlowService tcipsService;

    public FlowStatsCollection(DataBroker dataBroker, TcipsFlowService tcipsService) {
        this.dataBroker = dataBroker;
        this.tcipsService = tcipsService;
    }

    @Override
    public void run() {

        LOG.info("FlowStatsCollection started");
        try {
            while (!stop) {
                collectAllFlowStatistics();
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void collectAllFlowStatistics() {

        // Create a top node identifier
        ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();

        // Get all nodes
        InstanceIdentifier<Nodes> nodesID = InstanceIdentifier.create(Nodes.class);
        Nodes nodes = OpenflowUtils.getOperationalData(readOnlyTransaction, nodesID);
        if (nodes == null) {
            throw new RuntimeException("Nodes are not found, pls add the node.");
        }

        // For each node...
        for (Iterator<Node> iterator = nodes.getNode().iterator(); iterator.hasNext();) {
            // Reads child id
            NodeKey childNodeKey = iterator.next().getKey();
            InstanceIdentifier<FlowCapableNode> childNodeRef = InstanceIdentifier
                    .create(Nodes.class)
                    .child(Node.class, childNodeKey)
                    .augmentation(FlowCapableNode.class);
            FlowCapableNode childNode = OpenflowUtils.getOperationalData(readOnlyTransaction, childNodeRef);
            if (childNode != null) {

                // Gets node tables and iterate over it
                for (Iterator<Table> iterator2 = childNode.getTable().iterator(); iterator2.hasNext();) {

                    // Gets table id
                    TableKey tableKey = iterator2.next().getKey();
                    InstanceIdentifier<Table> tableRef = InstanceIdentifier
                            .create(Nodes.class).child(Node.class, childNodeKey)
                            .augmentation(FlowCapableNode.class).child(Table.class, tableKey);
                    Table table = OpenflowUtils.getOperationalData(readOnlyTransaction, tableRef);
                    if (table != null) {
                        if (table.getFlow() != null) {

                            // Gets table flows and iterate over it
                            for (Iterator<Flow> iterator3 = table.getFlow().iterator(); iterator3.hasNext();) {

                                FlowKey flowKey = iterator3.next().getKey();

                                InstanceIdentifier<Flow> flowRef = InstanceIdentifier
                                        .create(Nodes.class)
                                        .child(Node.class, childNodeKey)
                                        .augmentation(FlowCapableNode.class)
                                        .child(Table.class, tableKey)
                                        .child(Flow.class, flowKey);
                                Flow flow = OpenflowUtils.getOperationalData(readOnlyTransaction, flowRef);
                                if (flow != null) {
                                    Match match = flow.getMatch();

                                    if (match != null && match.getLayer4Match() != null
                                            && match.getLayer3Match() != null) {

                                        Layer3Match layer3Match = match.getLayer3Match();
                                        Layer4Match layer4Match = match.getLayer4Match();
                                        Ipv4Match ipMatch = (Ipv4Match) layer3Match;
                                        TcpMatch tcpMatch = (TcpMatch) layer4Match;

                                        // Add only mapped flows
                                        if (tcpMatch.getTcpDestinationPort() != null
                                                && ipMatch.getIpv4Destination() != null
                                                && ipMatch.getIpv4Source() != null) {

                                            if (flowKey.getId().getValue().startsWith("tcips")) {
                                                TcipsFlow stats = build(childNodeKey.getId().getValue().toString(),
                                                        flow);

                                                tcipsService.add(stats);
                                                LOG.debug("Got stats from {} - FLOW {}",
                                                        childNodeKey.getId().getValue(),
                                                        flowKey.getId().getValue());

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a new {@link TcipsFlow} instance using {@link Flow} values.
     * 
     * @param flow
     *            The {@link Flow} instance.
     */
    public static TcipsFlow build(String nodeName, Flow flow) {
        TcipsFlow tcipsFlow = new TcipsFlow();
        FlowStatisticsData data = flow.getAugmentation(FlowStatisticsData.class);
        if (null != data) {

            tcipsFlow.setNodeName(nodeName);

            FlowStatistics flosStatistics = data.getFlowStatistics();
            tcipsFlow.setPacketCount(flosStatistics.getPacketCount().getValue());
            tcipsFlow.setByteCount(flosStatistics.getByteCount().getValue());
            tcipsFlow.setDurationSeconds(flosStatistics.getDuration().getSecond().getValue());
            tcipsFlow.setDurationNanoSeconds(flosStatistics.getDuration().getNanosecond().getValue());

            Match flowMatch = flow.getMatch();
            if (flowMatch != null) {

                // EthernetMatch ethernetMatch = flowMatch.getEthernetMatch();
                // if (ethernetMatch != null &&
                // ethernetMatch.getEthernetSource() != null
                // && ethernetMatch.getEthernetDestination() != null) {
                //
                // tcipsFlow.setSrcMac(ethernetMatch.getEthernetSource().getAddress().getValue());
                // tcipsFlow.setDstMac(ethernetMatch.getEthernetDestination().getAddress().getValue());
                // } else {
                // LOG.warn("Creating a new TcipsMatch without ethernet
                // match.");
                // }

                Layer3Match layer3Match = flowMatch.getLayer3Match();
                if (layer3Match != null) {
                    if (layer3Match instanceof Ipv4Match) {

                        Ipv4Match ipMatch = (Ipv4Match) layer3Match;
                        tcipsFlow.setSrcIpv4(ipMatch.getIpv4Source().getValue());
                        tcipsFlow.setDstIpv4(ipMatch.getIpv4Destination().getValue());

                    }
                } else {
                    LOG.warn("Creating a new TcipsMatch without layer 4 match.");
                }

                Layer4Match layer4Match = flowMatch.getLayer4Match();
                if (layer4Match != null) {
                    if (layer4Match instanceof TcpMatch) {

                        TcpMatch tcpMatch = (TcpMatch) layer4Match;
                        tcipsFlow.setSrcPort(tcpMatch.getTcpSourcePort().getValue());
                        tcipsFlow.setDstPort(tcpMatch.getTcpDestinationPort().getValue());

                    } else if (layer4Match instanceof UdpMatch) {

                        UdpMatch udpMatch = (UdpMatch) layer4Match;
                        tcipsFlow.setSrcPort(udpMatch.getUdpSourcePort().getValue());
                        tcipsFlow.setDstPort(udpMatch.getUdpDestinationPort().getValue());

                    }
                } else {
                    LOG.warn("Creating a new TcipsMatch without layer 4 match.");
                }
            }
        }
        return tcipsFlow;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        this.stop = true;

    }
}
