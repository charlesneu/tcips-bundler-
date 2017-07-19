/*
 * Copyright (c) 2017 Cassio Tatsch and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.utils;

/**
 * Ethernet types enum.
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */

public enum KnownEtherType {
    /**
     * Internet Protocol version 4 (IPv4)
     *
     */
    Ipv4(2048, "ipv4"),

    /**
     * Address Resolution Protocol (ARP)
     *
     */
    Arp(2054, "arp"),

    /**
     * Wake-on-LAN[3]
     *
     */
    WakeOnLan(2114, "wake-on-lan"),

    /**
     * IETF TRILL Protocol
     *
     */
    IetfTrill(8947, "ietf-trill"),

    /**
     * DECnet Phase IV
     *
     */
    DecnetPhaseIv(24579, "decnet-phase-iv"),

    /**
     * Reverse Address Resolution Protocol
     *
     */
    ReverseArp(32821, "reverse-arp"),

    /**
     * AppleTalk (Ethertalk)
     *
     */
    AppleTalk(32923, "apple-talk"),

    /**
     * AppleTalk Address Resolution Protocol (AARP)
     *
     */
    AppleTalkArp(33011, "apple-talk-arp"),

    /**
     * VLAN-tagged frame (IEEE 802.1Q) &amp; Shortest Path Bridging IEEE
     * 802.1aq[4]
     *
     */
    VlanTagged(33024, "vlan-tagged"),

    /**
     * IPX
     *
     */
    Ipx(33079, "ipx"),

    /**
     * IPX
     *
     */
    Ipx2(33080, "ipx2"),

    /**
     * QNX Qnet
     *
     */
    QnxQnet(33284, "qnx-qnet"),

    /**
     * Internet Protocol Version 6 (IPv6)
     *
     */
    Ipv6(34525, "ipv6"),

    /**
     * Ethernet flow control
     *
     */
    EthernetFlowControl(34824, "ethernet-flow-control"),

    /**
     * Slow Protocols (IEEE 802.3)
     *
     */
    SlowProtocols(34825, "slow-protocols"),

    /**
     * CobraNet
     *
     */
    CobraNet(34841, "cobra-net"),

    /**
     * MPLS unicast
     *
     */
    MplsUnicast(34887, "mpls-unicast"),

    /**
     * MPLS multicast
     *
     */
    MplsMulticast(34888, "mpls-multicast"),

    /**
     * PPPoE Discovery Stage
     *
     */
    PPPOverEthernetDiscovery(34915, "PPP-over-ethernet-discovery"),

    /**
     * PPPoE Session Stage
     *
     */
    PPPOverEthernetSession(34916, "PPP-over-ethernet-session"),

    /**
     * Jumbo Frames[2]
     *
     */
    Jumbo(34928, "jumbo"),

    /**
     * HomePlug 1.0 MME
     *
     */
    Homeplug(34939, "homeplug"),

    /**
     * EAP over LAN (IEEE 802.1X)
     *
     */
    EapOverLan(34958, "eap-over-lan"),

    /**
     * PROFINET Protocol
     *
     */
    Profinet(34962, "profinet"),

    /**
     * HyperSCSI (SCSI over Ethernet)
     *
     */
    HyperScsi(34970, "hyper-scsi"),

    /**
     * ATA over Ethernet
     *
     */
    AtaOverEthernet(34978, "ata-over-ethernet"),

    /**
     * EtherCAT Protocol
     *
     */
    Ethercat(34980, "ethercat"),

    /**
     * Provider Bridging (IEEE 802.1ad) &amp; Shortest Path Bridging IEEE
     * 802.1aq[5]
     *
     */
    ProviderBridging(34984, "provider-bridging"),

    /**
     * Ethernet Powerlink[citation needed]
     *
     */
    EthernetPowerlink(34987, "ethernet-powerlink"),

    /**
     * Link Layer Discovery Protocol (LLDP)
     *
     */
    Lldp(35020, "lldp"),

    /**
     * SERCOS III
     *
     */
    Sercos3(35021, "sercos-3"),

    /**
     * HomePlug AV MME[citation needed]
     *
     */
    HomeplugAvMme(35041, "homeplug-av-mme"),

    /**
     * Media Redundancy Protocol (IEC62439-2)
     *
     */
    MediaRedudancyProtocol(35043, "media-redudancy-protocol"),

    /**
     * MAC security (IEEE 802.1AE)
     *
     */
    MacSecurity(35045, "mac-security"),

    /**
     * Precision Time Protocol (IEEE 1588)
     *
     */
    PrecisionTimeProtocol(35063, "precision-time-protocol"),

    /**
     * IEEE 802.1ag Connectivity Fault Management (CFM) Protocol / ITU-T
     * Recommendation Y.1731 (OAM)
     *
     */
    ConnectivityFaultManagement(35074, "connectivity-fault-management"),

    /**
     * Fibre Channel over Ethernet (FCoE)
     *
     */
    FibreChannelOverEthernet(35078, "fibre-channel-over-ethernet"),

    /**
     * FCoE Initialization Protocol
     *
     */
    FibreChannelOverEthernetInitialization(35092, "fibre-channel-over-ethernet-initialization"),

    /**
     * RDMA over Converged Ethernet (RoCE)
     *
     */
    RmdaOverConvergedEthernet(35093, "rmda-over-converged-ethernet"),

    /**
     * High-availability Seamless Redundancy (HSR)
     *
     */
    HighAvailabilitySeamlessRedudancy(35119, "high-availability-seamless-redudancy"),

    /**
     * Ethernet Configuration Testing Protocol[6]
     *
     */
    EthernetConfigurationTestingProtocol(36864, "ethernet-configuration-testing-protocol"),

    /**
     * Q-in-Q
     *
     */
    QInQ(37120, "q-in-q"),

    /**
     * Veritas Low Latency Transport (LLT)[7] for Veritas Cluster Server
     *
     */
    VeritasLowLatency(51966, "veritas-low-latency");

    java.lang.String name;
    int value;
    private static final java.util.Map<java.lang.Integer, KnownEtherType> VALUE_MAP;

    static {
        final com.google.common.collect.ImmutableMap.Builder<java.lang.Integer, KnownEtherType> b = com.google.common.collect.ImmutableMap
                .builder();
        for (KnownEtherType enumItem : KnownEtherType.values()) {
            b.put(enumItem.value, enumItem);
        }

        VALUE_MAP = b.build();
    }

    private KnownEtherType(int value, java.lang.String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * Returns the name of the enumeration item as it is specified in the input
     * yang.
     *
     * @return the name of the enumeration item as it is specified in the input
     *         yang
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * @return integer value
     */
    public int getIntValue() {
        return value;
    }

    /**
     * @param valueArg
     * @return corresponding KnownEtherType item
     */
    public static KnownEtherType forValue(int valueArg) {
        return VALUE_MAP.get(valueArg);
    }
}