/*
 * Copyright (c) 2017 Cassio Tatsch and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.db.command;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.service.command.CommandSession;

import com.ctatsch.tcips.db.GlobalFields;
import com.ctatsch.tcips.db.TcipsFlow;
import com.ctatsch.tcips.db.TcipsFlowService;

import lombok.Setter;

/**
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */
@Command(scope = "tcips", name = "setup", description = "Setup trigger")
public class SetupCommand implements Action {

    @Argument(index = 0, name = "Interval", required = true, description = "Max nterval between flows", multiValued = false)
    Integer interval;
    
    @Argument(index = 1, name = "Max Packets", required = true, description = "Maximum number of packets", multiValued = false)
    Integer maxPackets;
    
    @Argument(index = 2, name = "Min Flows", required = true, description = "Minimum number of flows", multiValued = false)
    Integer weightLimit;

    @Override
    public Object execute(CommandSession session) throws Exception {
        GlobalFields.collectionInterval = interval;
        GlobalFields.maxPackets = maxPackets;
        GlobalFields.weightLimit = weightLimit;
        return null;
    }

}
