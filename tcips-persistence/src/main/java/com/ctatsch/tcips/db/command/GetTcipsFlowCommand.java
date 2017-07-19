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
import org.apache.felix.gogo.commands.Argument;
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
@Command(scope = "tcips", name = "get", description = "Lists all flows")
public class GetTcipsFlowCommand implements Action {

    @Setter
    private TcipsFlowService tcipsFlowService;

    @Argument(index = 0, name = "Node id", required = true, description = "Node id", multiValued = false)
    Integer id;

    @Override
    public Object execute(CommandSession session) throws Exception {
        TcipsFlow flow = tcipsFlowService.getById(id);
        System.out.println("Flow - " + flow.toString());
        return null;
    }

}
