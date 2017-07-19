/*
 * Copyright (c) 2017 Cassio Tatsch and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.db.command;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.service.command.CommandSession;

import com.ctatsch.tcips.db.FilteredFlow;
import com.ctatsch.tcips.db.GlobalFields;
import com.ctatsch.tcips.db.TcipsFlow;
import com.ctatsch.tcips.db.TcipsFlowService;

import lombok.Setter;

/**
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */
@Command(scope = "tcips", name = "list", description = "Lists all flows")
public class GetPortScanFlowsCommand implements Action {

    @Argument(index = 0, name = "Node name", required = true, description = "Node name", multiValued = false)
    String nodeName;

    @Setter
    private TcipsFlowService tcipsFlowService;

    @Override
    public Object execute(CommandSession session) throws Exception {
        Iterator<String> it = GlobalFields.BLOCKED_IPS.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }
        return null;
    }
}
