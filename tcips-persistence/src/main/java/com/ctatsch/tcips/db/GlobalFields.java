/*
 * Copyright (c) 2017 Cassio Tatsch and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.db;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;

/**
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */

public class GlobalFields {
    public static Integer collectionInterval;
    public static Integer scanInterval;
    public static Integer maxPackets;
    public static Integer weightLimit;
    public static Integer minHosts;
   
    public static Integer lowWeight;
    public static Integer highWeight;
    public static List<Integer> specialPorts;
    
    public static Set<String> BLOCKED_IPS = new HashSet<>();

    public static boolean isIpScan(String ip) {
        return BLOCKED_IPS.contains(ip);
    }
    
    
    
}
