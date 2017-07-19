/*
 * Copyright (c) 2017 Cassio Tatsch and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.producer;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctatsch.tcips.db.TcipsFlow;
import com.ctatsch.tcips.utils.KnownEtherType;
import com.ctatsch.tcips.utils.PacketUtils;

/**
 * Packet In handler.
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 */
public class PacketInHandlerImpl implements PacketProcessingListener {

    private static final Logger LOG = LoggerFactory.getLogger(PacketInHandlerImpl.class);

    /* Flow programmer instance */
    private FlowProgrammer flowProgrammer;

    /* Last packet in received, used to not process repeated packets */
    private final Map<Integer, Long> pktInBuffer;

    /* Timeout each packet should be saved in buffer */
    private int maxBufferTime;

    /* Number of received packets needed to purge packet in buffer */
    private int packetCountPurge;

    /* Number of packets received */
    private int packetCount;

    /**
     * Creates a new {@link PacketInHandlerImpl}, passing by argument
     * 
     * @param flowProgrammer
     *            a {@link FlowProgrammer} instance that will consume incoming
     *            packets.
     */
    public PacketInHandlerImpl(FlowProgrammer flowProgrammer) {
        this.flowProgrammer = flowProgrammer;

        maxBufferTime = 60000; // 60 seconds
        packetCountPurge = 100;
        packetCount = 0;
        pktInBuffer = new HashMap<>();

        LOG.info("PacketIn listener started");
    }

    /**
     * The handler function for IPv4 PktIn packets.
     *
     * @param packetIn
     *            The incoming packet.
     */
    @Override
    public void onPacketReceived(PacketReceived packetIn) {
        LOG.debug("PacketIn Received");
        if (packetIn == null) {
            return;
        }

        ++packetCount;
        // If number of packets receiver too high, purge buffer
        if (packetCount > packetCountPurge) {
            packetCount = 0;
            purgePktInBuffer();
        }

        final byte[] rawPacket = packetIn.getPayload();
        
        byte[] flags = PacketUtils.extractFlags(rawPacket);
        
        int x = (int) flags[0];
        
        if(x != 2){
            LOG.warn("Not SYN Packet, discarded.");
            return;
        }
        
        // Creates database instance
        TcipsFlow packetHeader = new TcipsFlow();
        packetHeader.setEtherType(PacketUtils.extractEtherTypeAsInt(rawPacket));
        packetHeader.setSrcIpv4(PacketUtils.extractSrcIpAsString(rawPacket) + "/32");
        packetHeader.setDstIpv4(PacketUtils.extractDstIpAsString(rawPacket) + "/32");
        packetHeader.setSrcMac(PacketUtils.extractSrcMacAsString(rawPacket));
        packetHeader.setDstMac(PacketUtils.extractDstMacAsString(rawPacket));
        packetHeader.setSrcPort((int) PacketUtils.extractSrcPortNumberAsShort(rawPacket));
        packetHeader.setDstPort((int) PacketUtils.extractDstPortNumberAsShort(rawPacket));
        LocalDateTime dt = LocalDateTime.now();
        packetHeader.setIncomeDateTime(dt);
        packetHeader.setPacketCount(BigInteger.ONE);
        packetHeader.setIncomeTime(Date.from(dt.atZone(ZoneId.systemDefault()).toInstant()));
        LOG.debug("Received from " + packetHeader.getSrcIpv4() + " to " + packetHeader.getDstIpv4() + " on port " + packetHeader.getDstPort());
        
        // Process only IPv4 packets
        if (!packetHeader.getEtherType().equals(KnownEtherType.Ipv4.getIntValue())) {
            LOG.warn("Not Ipv4 {}", packetHeader.getEtherType());
            return;
        }

        // Since all packets sent to SF are PktIn, only need to handle the first
        // one. In OpenFlow 1.5 we'll be able to do the PktIn on TCP Syn only.
        if (bufferPktIn(packetHeader)) {
            LOG.trace("Ipv4PacketInHandler PacketIn buffered");
            return;
        }

        // Get the metadata
        if (packetIn.getMatch() == null) {
            LOG.error("Ipv4PacketInHandler Cant get packet flow match {}", packetIn.toString());
            return;
        }

        // Process packet In
        this.flowProgrammer.processIncommingFlow(packetHeader, packetIn.getIngress().getValue());
    }

    /**
     * Decide if packets with the same src/dst IP have already been processed.
     * If they haven't been processed, store the IPs so they will be considered
     * processed.
     *
     * @param srcIpStr
     *            source IP
     * @param dstIpStr
     *            destination IP
     * @return {@code true} if the src/dst IP has already been processed,
     *         {@code false} otherwise
     */
    private boolean bufferPktIn(final TcipsFlow packetInHeader) {
        int key = packetInHeader.toString().hashCode();
        long currentMillis = System.currentTimeMillis();

        Long bufferedTime = pktInBuffer.get(key);

        // If the entry does not exist, add it and return false indicating the
        // packet needs to be processed
        if (bufferedTime == null) {
            // Add the entry
            pktInBuffer.put(key, currentMillis);
            return false;
        }

        // If the entry is old, update it and return false indicating the packet
        // needs to be processed
        if (currentMillis - bufferedTime.longValue() > maxBufferTime) {
            // Update the entry
            pktInBuffer.put(key, currentMillis);
            return false;
        }

        return true;
    }

    /**
     * Purge packets that have been in the PktIn buffer too long.
     */
    private void purgePktInBuffer() {
        long currentMillis = System.currentTimeMillis();
        Set<Integer> keySet = pktInBuffer.keySet();
        for (Integer key : keySet) {
            Long bufferedTime = pktInBuffer.get(key);
            if (currentMillis - bufferedTime.longValue() > maxBufferTime) {
                // This also removes the entry from the pktInBuffer map and
                // doesnt invalidate iteration
                keySet.remove(key);
            }
        }
    }
}