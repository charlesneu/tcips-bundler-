/*
 * Copyright (c) 2017 Cassio Tatsch and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.producer;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.drop.action._case.DropActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctatsch.tcips.db.GlobalFields;
import com.ctatsch.tcips.db.TcipsFlow;
import com.ctatsch.tcips.db.TcipsFlowService;
import com.ctatsch.tcips.utils.KnownEtherType;
import com.ctatsch.tcips.utils.OpenflowUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.CheckedFuture;

/**
 * Provides methods to create and manipulate flows.
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */
public class FlowCommitImpl {

    private static final Logger LOG = LoggerFactory.getLogger(FlowCommitImpl.class);

    private TcipsFlowService tcipsFlowService;
    private final SalFlowService salFlowService;
    private final DataBroker dataBroker;

    /* Timeout if no packet is available */
    private final Integer idleTimeout = 20;
    /* Timeout even the packet is available */
    private final Integer hardTimeout = 0;

    private Integer reference;

    /**
     * Creates a new instance of {@link FlowCommitImpl}.
     * 
     * @param salFlowService
     *            an interface for the YANG RPCs.
     * @param tcipsFlowService
     *            an interface for the tcips persistence service.
     */
    public FlowCommitImpl(SalFlowService salFlowService, TcipsFlowService tcipsFlowService, DataBroker dataBroker) {
        this.salFlowService = salFlowService;
        this.tcipsFlowService = tcipsFlowService;
        this.dataBroker = dataBroker;
        TcipsFlow tmp = tcipsFlowService.getLast();
        this.reference = tmp != null ? tmp.getReference() : 0;
    }

    /**
     * Creates a new temporary flow, used by tcips daemon to get information
     * about port scan attacks.
     * 
     * @param nodeId
     *            the node instance identifier.
     * @param inNodeConnectorIId
     *            the in port instance identifier.
     * @param outNodeConnectorIId
     *            the out port instance identifier.
     * @param flowTableId
     *            the table flow id.
     * @param flowPriority
     *            the flow property.
     * @param tcipsFlow
     *            the packet header information.
     */
    public void addTempFlow(InstanceIdentifier<Node> nodeId, InstanceIdentifier<NodeConnector> inNodeConnectorIId,
            InstanceIdentifier<NodeConnector> outNodeConnectorIId, Short flowTableId, int flowPriority,
            TcipsFlow tcipsFlow) {
        InstanceIdentifier<Table> tableId = getTableInstanceId(nodeId, flowTableId);

        // Return pack
        Uri inputPort = inNodeConnectorIId.firstKeyOf(NodeConnector.class).getId();
        Uri outputPort = outNodeConnectorIId.firstKeyOf(NodeConnector.class).getId();
        tcipsFlow.setNodeName(nodeId.firstKeyOf(Node.class).getId().getValue().toString());
        tcipsFlow.setInPort(inputPort.getValue().toString());
        tcipsFlow.setOutPort(outputPort.getValue().toString());
        tcipsFlow.setInput(true);
        tcipsFlow.setReference(reference++);
        tcipsFlow.setFlowName("tcipsflow-" + tcipsFlow.getReference() + "I");

        boolean shouldDrop = GlobalFields.isIpScan(tcipsFlow.getSrcIpv4());

        InstanceIdentifier<Flow> flowId = getFlowInstanceId(tableId, tcipsFlow.getFlowName());
        boolean result = writeFlowToController(nodeId, tableId, flowId,
                createOutputControllerFlow(tcipsFlow, outputPort, (short) 0, 60000, shouldDrop));

        if (result) {
            tcipsFlowService.add(tcipsFlow);
            LOG.info("Added flow {} in {}ms", tcipsFlow.getFlowName(),
                    tcipsFlow.getIncomeDateTime().until(LocalDateTime.now(), ChronoUnit.MILLIS));
            // Send pack
            TcipsFlow tcipsResponseFlow = tcipsFlow.getResponseFlow();
            outputPort = outNodeConnectorIId.firstKeyOf(NodeConnector.class).getId();
            InstanceIdentifier<Flow> flowId2 = getFlowInstanceId(tableId,
                    tcipsResponseFlow.getFlowName());
            result = writeFlowToController(nodeId, tableId, flowId2,
                    createOutputControllerFlow(tcipsResponseFlow, inputPort, (short) 0, 60000, shouldDrop));
            if (result) {
                tcipsFlowService.add(tcipsResponseFlow);
                LOG.info("Added flow {} in {}ms", tcipsResponseFlow.getFlowName(),
                        tcipsResponseFlow.getIncomeDateTime().until(LocalDateTime.now(), ChronoUnit.MILLIS));
            }
        }
    }

    /**
     * Creates and returns a {@link Flow} object that will be persisted on data
     * store.
     * 
     * @param tcipsFlow
     *            the packet header information.
     * @param outputUri
     *            the output port uri.
     * @param tableId
     *            the table id.
     * @param priority
     *            the flow priority.
     * @param shouldDrop
     *            {@code true} if packet should be dropped, {@code false}
     *            otherwise.
     * @return a new {@link Flow} instance.
     */
    private Flow createOutputControllerFlow(TcipsFlow tcipsFlow, Uri outputUri, Short tableId, int priority,
            boolean shouldDrop) {

        // start building flow
        FlowBuilder outputFlow = new FlowBuilder(); //
        outputFlow.setTableId(tableId); //
        outputFlow.setFlowName(tcipsFlow.getFlowName());

        // use its own hash code for id.
        outputFlow.setId(new FlowId(Integer.toString(outputFlow.hashCode())));

        MatchBuilder match = new MatchBuilder();
        match.setEthernetMatch(new EthernetMatchBuilder()
                .setEthernetType(new EthernetTypeBuilder()
                        .setType(new EtherType(Long.valueOf(KnownEtherType.Ipv4.getIntValue()))).build())
                .build());

        match.setIpMatch(new IpMatchBuilder().setIpProtocol((short) 6).build());

        if (shouldDrop) {
            match.setLayer3Match(new Ipv4MatchBuilder()
                    .setIpv4Source(new Ipv4Prefix(tcipsFlow.getSrcIpv4())).build());
        } else {
            match.setLayer3Match(new Ipv4MatchBuilder()
                    .setIpv4Destination(new Ipv4Prefix(tcipsFlow.getDstIpv4()))
                    .setIpv4Source(new Ipv4Prefix(tcipsFlow.getSrcIpv4())).build());

            match.setLayer4Match(new TcpMatchBuilder()
                    .setTcpDestinationPort(new PortNumber(tcipsFlow.getDstPort()))
                    .setTcpSourcePort(new PortNumber(tcipsFlow.getSrcPort())).build());
        }
        List<Action> actions = new ArrayList<Action>();
        // actions.add(getSendToControllerAction());
        // actions.add(getNormalAction());

        if (shouldDrop) {
            actions.add(getDropAction());
        } else {
            actions.add(getSendToOutputAction(outputUri));
        }

        // Create an Apply Action
        ApplyActions applyActions = new ApplyActionsBuilder() //
                .setAction(ImmutableList.copyOf(actions)) //
                .build();

        // Wrap our Apply Action in an Instruction
        Instruction applyActionsInstruction = new InstructionBuilder() //
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()//
                        .setApplyActions(applyActions) //
                        .build()) //
                .build();

        List<Instruction> instr = new ArrayList<>();
        instr.add(applyActionsInstruction);

        // Put our Instruction in a list of Instructions
        outputFlow
                .setMatch(match.build()) //
                .setInstructions(new InstructionsBuilder() //
                        .setInstruction(ImmutableList.of(applyActionsInstruction)) //
                        .build()) //
                .setPriority(priority) //
                .setBufferId(OFConstants.OFP_NO_BUFFER) //
                .setHardTimeout(hardTimeout) //
                .setIdleTimeout(idleTimeout) //
                .setCookie(new FlowCookie(BigInteger.valueOf(OpenflowUtils.FLOW_COOKIE_INC.getAndIncrement())))
                .setFlags(new FlowModFlags(false, false, false, false, false));

        return outputFlow.build();
    }

    /**
     * Creates and returns a SEND_TO_CONTROLLER action. Each flow will be
     * redirected to the controller.
     * 
     * @return a SEND_TO_CONTROLLER action.
     */
    private Action getSendToControllerAction() {
        Action sendToController = new ActionBuilder()
                .setOrder(0)
                .setKey(new ActionKey(0))
                .setAction(new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder()
                                .setMaxLength(0xffff)
                                .setOutputNodeConnector(new Uri(OutputPortValues.CONTROLLER.toString()))
                                .build())
                        .build())
                .build();
        return sendToController;
    }

    /**
     * Creates and returns a NORMAL action, An action for legacy switch
     * compatibility.
     * 
     * @return a NORMAL action.
     */
    private Action getNormalAction() {
        Action normal = new ActionBuilder()
                .setOrder(0)
                .setKey(new ActionKey(0))
                .setAction(new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder()
                                .setMaxLength(0xffff)
                                .setOutputNodeConnector(new Uri(OutputPortValues.NORMAL.toString()))
                                .build())
                        .build())
                .build();
        return normal;
    }

    /**
     * Creates and returns a OUTPUT action, a respective flow will be redirected
     * to the output port passed by argument.
     * 
     * @return a OUTPUT action.
     */
    private Action getSendToOutputAction(Uri outputUri) {
        Action sendToController = new ActionBuilder()
                .setOrder(0)
                .setKey(new ActionKey(0))
                .setAction(new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder()
                                .setMaxLength(0xffff)
                                .setOutputNodeConnector(outputUri)
                                .build())
                        .build())
                .build();
        return sendToController;
    }

    /**
     * Creates a drop action. A respective flow will be dropped.
     * 
     * @return a drop action.
     */
    private static Action getDropAction() {
        Action drop = new ActionBuilder()
                .setOrder(0)
                .setKey(new ActionKey(0))
                .setAction(new DropActionCaseBuilder()
                        .setDropAction(new DropActionBuilder()
                                .build())
                        .build())
                .build();
        return drop;
    }

    /**
     * Send flow to the controller data store using YANG RPC.
     * 
     * @param nodeInstanceId
     *            the node instance identifier.
     * @param tableInstanceId
     *            the table instance identifier.
     * @param flowPath
     *            the flow instance identifier.
     * @param flow
     *            the flow object.
     * @return {@code true} if the flow was saved, {@code false} otherwise.
     */
    private boolean writeFlowToController(InstanceIdentifier<Node> nodeInstanceId,
            InstanceIdentifier<Table> tableInstanceId,
            InstanceIdentifier<Flow> flowPath,
            Flow flow) {
        LOG.trace("Adding flow to node {}", nodeInstanceId.firstKeyOf(Node.class).getId().getValue());
        final AddFlowInputBuilder builder = new AddFlowInputBuilder(flow);
        builder.setNode(new NodeRef(nodeInstanceId));
        builder.setFlowRef(new FlowRef(flowPath));
        builder.setFlowTable(new FlowTableRef(tableInstanceId));
        builder.setTransactionUri(new Uri(flow.getId().getValue()));

        Future<RpcResult<AddFlowOutput>> rpcResult = salFlowService.addFlow(builder.build());

        try {
            // Não necessário esperar retorno, o mesmo demora devido a espera do
            // BARRIER
            RpcResult<AddFlowOutput> res = rpcResult.get(3, TimeUnit.SECONDS);
            return res.isSuccessful();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("Failed to write flow {}", e);
        }

        return true;

    }

    @Deprecated
    private boolean writeFlowToController2(InstanceIdentifier<Node> nodeInstanceId,
            InstanceIdentifier<Table> tableInstanceId,
            InstanceIdentifier<Flow> flowPath,
            Flow flow) {

        LOG.warn("W1");
        LOG.trace("Adding flow to node {}", nodeInstanceId.firstKeyOf(Node.class).getId().getValue());

        WriteTransaction modification = dataBroker.newWriteOnlyTransaction();
        // modification.put(LogicalDatastoreType.CONFIGURATION, nodeInstanceId,
        // true);
        modification.put(LogicalDatastoreType.CONFIGURATION, flowPath, flow, true);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        try {
            commitFuture.get(); // TODO: Make it async (See bug 1362)
            LOG.debug("Transaction success for write of Flow {}", flow.getFlowName());
            return true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            modification.cancel();
            return false;
        }

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

}
