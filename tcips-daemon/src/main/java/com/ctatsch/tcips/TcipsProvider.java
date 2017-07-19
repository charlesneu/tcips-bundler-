/*
 * Copyright (c) 2017 Cássio Tatsch. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips;

import java.util.ArrayList;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctatsch.tcips.consumer.FlowStatsCollection;
import com.ctatsch.tcips.consumer.ScanAnalizer;
import com.ctatsch.tcips.db.GlobalFields;
import com.ctatsch.tcips.db.TcipsFlowService;
import com.ctatsch.tcips.producer.FlowCommitImpl;
import com.ctatsch.tcips.producer.FlowProgrammer;
import com.ctatsch.tcips.producer.PacketInHandlerImpl;

import lombok.Setter;

/**
 * Tcips Bundler Activator.
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */
public class TcipsProvider implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(TcipsProvider.class);

    private final DataBroker dataBroker;

    private NotificationService notificationService;
    private FlowStatsCollection statsCollection;
    private TcipsFlowService tcipsFlowService;
    private SalFlowService salFlowService;
    ScanAnalizer fv;

    public TcipsProvider(final DataBroker dataBroker, NotificationService notificationService,
            SalFlowService salFlowService, TcipsFlowService tcipsFlowService) {
        this.dataBroker = dataBroker;
        this.notificationService = notificationService;
        this.salFlowService = salFlowService;
        this.tcipsFlowService = tcipsFlowService;
    }

    public void init() {

        //Default setings
        GlobalFields.collectionInterval = 10;
        GlobalFields.scanInterval = 3;
        GlobalFields.maxPackets = 5;
        GlobalFields.lowWeight = 3;
        GlobalFields.highWeight = 5;
        GlobalFields.weightLimit = 15;
        GlobalFields.minHosts = 3;
       

        GlobalFields.specialPorts = new ArrayList<>();
        GlobalFields.specialPorts.add(80);
        GlobalFields.specialPorts.add(23);
        GlobalFields.specialPorts.add(25);

        notificationService.registerNotificationListener(
                new PacketInHandlerImpl(
                        new FlowProgrammer(dataBroker,
                                new FlowCommitImpl(salFlowService, tcipsFlowService, dataBroker))));

        statsCollection = new FlowStatsCollection(dataBroker, tcipsFlowService);
        new Thread(statsCollection).start();
        fv = new ScanAnalizer(tcipsFlowService);
        new Thread(fv).start();

    }

    @Override
    public void close() {
        try {
            fv.close();
            statsCollection.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}