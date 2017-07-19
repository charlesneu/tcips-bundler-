/*
 * Copyright (c) 2017 Cassio Tatsch and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.db;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import lombok.Data;

/**
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */
@Entity
@Data
public class TcipsFlow {

    @Id
    @GeneratedValue
    private Integer id;
    private Integer etherType;

    /* Match */
    private String flowName;
    private String nodeName;
    private String inPort;
    private String outPort;
    private String srcIpv4;
    private String dstIpv4;
    private String srcMac;
    private String dstMac;
    private Integer srcPort;
    private Integer dstPort;
    @Temporal(TemporalType.TIMESTAMP)
    private Date incomeTime;

    @Transient
    private LocalDateTime incomeDateTime;
    private boolean consideredScan;

    /* Flow Stats */
    private Long durationSeconds;
    private Long durationNanoSeconds;
    private BigInteger packetCount;
    private BigInteger byteCount;
    private Integer reference;
    private boolean input;

    public TcipsFlow() {

    }

    public TcipsFlow(String nodeName) {
        this.nodeName = nodeName;
    }

    public boolean hasMatchFields() {
        return srcIpv4 != null && dstIpv4 != null && dstPort != null && srcPort != null;
    }

    public TcipsFlow getResponseFlow() {

        TcipsFlow packetHeader = new TcipsFlow();

        packetHeader.setNodeName(this.getNodeName());
        packetHeader.setEtherType(this.getEtherType());
        packetHeader.setSrcIpv4(this.getDstIpv4());
        packetHeader.setDstIpv4(this.getSrcIpv4());
        packetHeader.setSrcMac(this.getDstMac());
        packetHeader.setDstMac(this.getSrcMac());
        packetHeader.setSrcPort(this.getDstPort());
        packetHeader.setDstPort(this.getSrcPort());

        packetHeader.setInPort(this.getOutPort());
        packetHeader.setOutPort(this.getInPort());

        packetHeader.setIncomeDateTime(this.getIncomeDateTime());
        packetHeader.setIncomeTime(this.getIncomeTime());
        packetHeader.setReference(this.getReference());
        packetHeader.setInput(false);
        packetHeader.setPacketCount(BigInteger.ONE);
        packetHeader.setFlowName("tcipsflow-" + packetHeader.getReference() + "O");
        return packetHeader;

    }

    public LocalDateTime getIncomeTimeAsLocal() {

        return LocalDateTime.ofInstant(this.incomeTime.toInstant(), ZoneId.systemDefault());

    }
}
