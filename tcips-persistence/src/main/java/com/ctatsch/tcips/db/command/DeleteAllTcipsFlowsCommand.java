/*
 * Copyright (c) 2017 Cassio Tatsch and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.db.command;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.service.command.CommandSession;

import com.ctatsch.tcips.db.TcipsFlowService;

import lombok.Setter;

@Command(scope = "tcips", name = "deleteAll", description = "Delete all flows")
public class DeleteAllTcipsFlowsCommand implements Action {

    @Setter
    private TcipsFlowService tcipsFlowService;

    @Override
    public Object execute(CommandSession session) throws Exception {
        tcipsFlowService.deleteAll();
        return null;
    }

}
