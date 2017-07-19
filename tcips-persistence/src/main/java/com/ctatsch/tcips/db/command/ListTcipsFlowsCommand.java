/*
 * Copyright (c) 2017 Cassio Tatsch and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.db.command;

import java.util.List;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.service.command.CommandSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctatsch.tcips.db.TcipsFlow;
import com.ctatsch.tcips.db.TcipsFlowService;

import lombok.Setter;

/**
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */
@Command(scope = "tcips", name = "list", description = "Lists all flows")
public class ListTcipsFlowsCommand implements Action {

    @Setter
    private TcipsFlowService tcipsFlowService;

    @Override
    public Object execute(CommandSession session) throws Exception {
        List<TcipsFlow> flows = tcipsFlowService.getAll();

        StringBuilder sb = new StringBuilder();
        sb.append("id | ")
                .append("etherType | ")
                .append("flowName | ")
                .append("nodeName | ")
                .append("inPort | ")
                .append("outPort | ")
                .append("srcIpv4 | ")
                .append("dstIpv4 | ")
                .append("srcMac | ")
                .append("dstMac | ")
                .append("srcPort | ")
                .append("dstPort | ")
                .append("incomeTime | ")
                .append("consideredScan | ")
                .append("durationSeconds | ")
                .append("durationNanoSeconds | ")
                .append("packetCount | ")
                .append("byteCount | ")
                .append("reference | ")
                .append("input | ");

        for (TcipsFlow flow : flows) {
            sb.append(flow.getId()).append(" | ")
                    .append(flow.getFlowName()).append(" | ")
                    .append(flow.getNodeName()).append(" | ")
                    .append(flow.getSrcIpv4()).append(" | ")
                    .append(flow.getDstIpv4()).append(" | ")
                    .append(flow.getSrcPort()).append(" | ")
                    .append(flow.getDstPort()).append(" | ")
                    .append(flow.getIncomeTime()).append(" | ")
                    .append(flow.getDurationSeconds()).append(" | ")
                    .append(flow.getDurationNanoSeconds()).append(" | ")
                    .append(flow.getPacketCount()).append(" | ")
                    .append(flow.getByteCount()).append(" | ")
                    .append(flow.getReference()).append(" | ")
                    .append(flow.isInput() ? "in" : "out").append(" | ").append("\n");

        }
        System.out.println(sb.toString());
        return null;
    }

}
