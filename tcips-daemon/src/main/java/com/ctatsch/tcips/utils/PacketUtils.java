/*
 * Copyright (c) 2017 Cassio Tatsch and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities to extract data from packets.
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */
public class PacketUtils {

    private static final Logger LOG = LoggerFactory.getLogger(PacketUtils.class);

    private static final int OFFSET_MAC_DST = 0;
    private static final int OFFSET_MAC_SRC = 6;
    private static final int OFFSET_ETH_TYPE = 12;
    private static final int OFFSET_IP_SRC = 26;
    private static final int OFFSET_IP_DST = 30;
    private static final int OFFSET_PORT_SRC = 34;
    private static final int OFFSET_PORT_DST = 36;
    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    public static byte[] extractFlags(byte[] rawPacket) {
        return Arrays.copyOfRange(rawPacket, 47, 48);
    }
    /**
     * @param payload
     * @return destination MAC address
     */
    public static byte[] extractDstMac(byte[] rawPacket) {
        return Arrays.copyOfRange(rawPacket, OFFSET_MAC_DST, OFFSET_MAC_DST + 6);
    }

    public static String extractDstMacAsString(byte[] rawPacket) {
        return convertRawMacToString(extractDstMac(rawPacket));
    }

    /**
     * @param payload
     * @return source MAC address
     */
    public static byte[] extractSrcMac(byte[] payload) {
        return Arrays.copyOfRange(payload, OFFSET_MAC_SRC, OFFSET_MAC_SRC + 6);
    }

    public static String extractSrcMacAsString(byte[] rawPacket) {
        return convertRawMacToString(extractSrcMac(rawPacket));
    }

    /**
     * @param rawMac
     * @return {@link MacAddress} wrapping string value, baked upon binary MAC
     *         address
     */
    public static MacAddress convertRawMacToMacAddress(byte[] rawMac) {
        return new MacAddress(convertRawMacToString(rawMac));
    }

    public static String convertRawMacToString(byte[] rawMac) {
        StringBuilder r = new StringBuilder(rawMac.length * 2 + 6);
        for (byte b : rawMac) {
            r.append(":");
            r.append(HEX_DIGITS[(b >> 4) & 0xF]);
            r.append(HEX_DIGITS[(b & 0xF)]);
        }
        return r.substring(1);
    }

    /**
     * Given a raw packet, return the EtherType.
     * 
     * @param rawPacket
     *            the raw packet.
     * @return ether type.
     */
    public static byte[] extractEtherType(byte[] rawPacket) {
        return Arrays.copyOfRange(rawPacket, OFFSET_ETH_TYPE, OFFSET_ETH_TYPE + 2);
    }

    /**
     * Given a raw packet, return the EtherType.
     *
     * @param rawPacket
     *            packet
     * @return etherType
     */
    public static int extractEtherTypeAsInt(final byte[] rawPacket) {
        return packInt(extractEtherType(rawPacket));
    }

    /**
     * Given a raw packet, return the SrcIp.
     *
     * @param rawPacket
     *            packet
     * @return srcIp String
     */
    public static String extractSrcIpAsString(final byte[] rawPacket) {
        final byte[] ipSrcBytes = Arrays.copyOfRange(rawPacket, OFFSET_IP_SRC, OFFSET_IP_SRC + 4);
        String pktSrcIpStr = null;
        try {
            pktSrcIpStr = InetAddress.getByAddress(ipSrcBytes).getHostAddress();
        } catch (UnknownHostException e) {
            LOG.error("Exception getting Src IP address [{}]", e.getMessage(), e);
        }
        return pktSrcIpStr;
    }

    /**
     * Given a raw packet, return the DstIp.
     *
     * @param rawPacket
     *            packet    
     * @return dstIp String
     */
    public static String extractDstIpAsString(final byte[] rawPacket) {
        final byte[] ipDstBytes = Arrays.copyOfRange(rawPacket, OFFSET_IP_DST, OFFSET_IP_DST + 4);
        String pktDstIpStr = null;
        try {
            pktDstIpStr = InetAddress.getByAddress(ipDstBytes).getHostAddress();
        } catch (UnknownHostException e) {
            LOG.error("Exception getting Dst IP address [{}]", e.getMessage(), e);
        }
        return pktDstIpStr;
    }

    /**
     * @param payload
     * @return TCP source Port Number
     */
    public static byte[] extractSrcPortNumber(byte[] rawPacket) {
        return Arrays.copyOfRange(rawPacket, OFFSET_PORT_SRC, OFFSET_PORT_SRC + 2);
    }

    public static int extractSrcPortNumberAsShort(byte[] rawPacket) {
        return packInt(extractSrcPortNumber(rawPacket));
    }

    /**
     * @param payload
     * @return TCP destination Port Number
     */
    public static byte[] extractDstPortNumber(byte[] payload) {
        return Arrays.copyOfRange(payload, OFFSET_PORT_DST, OFFSET_PORT_DST + 2);
    }

    public static int extractDstPortNumberAsShort(byte[] rawPacket) {
        return packInt(extractDstPortNumber(rawPacket));
    }

    /**
     * Simple internal utility function to convert from a 2-byte array to a int.
     *
     * @param bytes
     *            byte array
     * @return the bytes packed into a short
     */
    public static int packInt(byte[] bytes) {
        return bytes[0] << 8 & 0xFF00 | bytes[1] & 0xFF;
    }

}