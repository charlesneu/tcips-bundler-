/*
 * Copyright (c) 2017 Cassio Tatsch and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.db;

import java.util.Date;
import java.util.List;

/**
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */
public interface TcipsFlowService {

    void add(TcipsFlow tcipsFlow);

    void deleteAll();

    TcipsFlow getById(Integer id);

    List<TcipsFlow> getAll();
    
    List<TcipsFlow> getAllInput();

    TcipsFlow getByHeader(String nodeName, String srcIpv4, String dstIpv4, Integer srcPort, Integer dstPort);

    List<String> getDistinctNodes();

    List<TcipsFlow> getFlowsByNodeDistinctDestinationPortsLow(Integer packetCount, String nodeName, Date after, Integer minFlows);
    
    List<FilteredFlow> getFlowsByNodeDistinctDestinationPortsHigh(Integer packetCount, String nodeName);
    List<FilteredFlow> getFlowsByNodeDistinctDestinationHostsHigh(Integer packetCount, String nodeName);
    List<TcipsFlow> getFlowsByNodeDistinctDestinationHostsLow(Integer packetCount, String nodeName, Date after, Integer minHosts);
    TcipsFlow getLast();
}
