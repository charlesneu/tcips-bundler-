/*
 * Copyright (c) 2017 Cassio Tatsch and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.db.command;

import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.service.command.CommandSession;

import com.ctatsch.tcips.db.GlobalFields;

/**
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */
@Command(scope = "tcips", name = "resetScan", description = "Setup trigger")
public class ResetScanCommand implements Action  {

    @Override
    public Object execute(CommandSession session) throws Exception {
        GlobalFields.BLOCKED_IPS.clear();
        return null;
    }

}
