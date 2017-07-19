/*
 * Copyright (c) 2017 Cassio Tatsch and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.db;

import java.math.BigInteger;

import lombok.Data;

/**
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */
@Data
public class FilteredFlow {

    
    String srcIp;
    String dstIp;
    Integer dstPort;
    Long count;
}
