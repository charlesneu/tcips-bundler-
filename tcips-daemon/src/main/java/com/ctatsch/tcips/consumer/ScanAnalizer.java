/*
 * Copyright (c) 2017 Cassio Tatsch and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.consumer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctatsch.tcips.db.GlobalFields;
import com.ctatsch.tcips.db.TcipsFlow;
import com.ctatsch.tcips.db.TcipsFlowService;

/**
 * Verifies possible attacks based on database data.
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */
public class ScanAnalizer implements Runnable, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ScanAnalizer.class);

    private TcipsFlowService tcipsService;
    private boolean stop = false;

    public ScanAnalizer(TcipsFlowService tcipsService) {
        this.tcipsService = tcipsService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        while (!stop) {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
            }

            List<String> nodeList = tcipsService.getDistinctNodes();

            for (String node : nodeList) {
                LOG.trace("Verifying hosts scanner");
                verifyScan(tcipsService.getFlowsByNodeDistinctDestinationHostsLow(GlobalFields.maxPackets,
                        node,
                        Date.from(LocalDateTime.now().minusSeconds(GlobalFields.collectionInterval)
                                .atZone(ZoneId.systemDefault()).toInstant()),
                        GlobalFields.minHosts), false);

                LOG.trace("Verifying port scanner");
                verifyScan(tcipsService.getFlowsByNodeDistinctDestinationPortsLow(GlobalFields.maxPackets,
                        node,
                        Date.from(LocalDateTime.now().minusSeconds(GlobalFields.collectionInterval)
                                .atZone(ZoneId.systemDefault()).toInstant()),
                        GlobalFields.minHosts), true);

            }
        }
    }

    private void verifyScan(List<TcipsFlow> ffList, boolean portScan) {

        if (ffList.size() > 0) {

            String lastIp = ffList.get(0).getSrcIpv4();
            LocalDateTime lastDt = ffList.get(0).getIncomeTimeAsLocal();
            int ports = 5;
            int hosts = 1;

            for (int i = 1; i < ffList.size(); i++) {
                TcipsFlow curr = ffList.get(i);

                if (GlobalFields.BLOCKED_IPS.contains(curr.getSrcIpv4())) {
                    LOG.debug("IP {} already blocked", curr.getSrcIpv4());
                    continue;
                }

                if (!curr.getSrcIpv4().equals(lastIp)) {
                    lastIp = curr.getSrcIpv4();
                    lastDt = curr.getIncomeTimeAsLocal();
                    hosts = 1;
                    ports = 5;
                    LOG.debug("Clear counter");
                }

                long diff = lastDt.until(curr.getIncomeTimeAsLocal(), ChronoUnit.SECONDS);
                if (diff <= 10) {

                    if (portScan) {
                        if (GlobalFields.specialPorts.contains(curr.getDstPort())) {
                            ports += GlobalFields.highWeight;
                        } else {
                            ports += GlobalFields.lowWeight;
                        }
                    } else {
                        hosts++;
                    }

                    if (ports >= GlobalFields.weightLimit || hosts >= GlobalFields.minHosts) {
                        LOG.info("Sum Ports {}, Hosts {}", ports, hosts);
                        LOG.warn("Source IP {} detected as {}", lastIp, portScan ? "Port Scan" : "Host Scan");
                        GlobalFields.BLOCKED_IPS.add(lastIp);
                    }
                }

                LOG.debug("Suspicious flow: {} - {}", curr.getSrcIpv4(), curr.getDstIpv4());
            }
        }
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
