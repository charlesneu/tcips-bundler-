/*
 * Copyright (c) 2017 Cassio Tatsch and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.ctatsch.tcips.db.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TemporalType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctatsch.tcips.db.FilteredFlow;
import com.ctatsch.tcips.db.TcipsFlow;
import com.ctatsch.tcips.db.TcipsFlowService;

/**
 * 
 * @author Cassio Tatsch (tatschcassio@gmail.com)
 *
 */
public class TcipsFlowServiceImpl implements TcipsFlowService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(TcipsFlowServiceImpl.class);

    private EntityManager em;

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ctatsch.tcips.db.TcipsFlowService#add(com.ctatsch.tcips.db.TcipsFlow)
     */
    @Override
    public void add(TcipsFlow tcipsFlow) {
        try {

            TcipsFlow t = getByHeader(tcipsFlow.getNodeName(), tcipsFlow.getSrcIpv4(), tcipsFlow.getDstIpv4(),
                    tcipsFlow.getSrcPort(), tcipsFlow.getDstPort());
            if (t != null) {
                t.setDurationNanoSeconds(tcipsFlow.getDurationNanoSeconds());
                t.setDurationSeconds(tcipsFlow.getDurationSeconds());
                t.setPacketCount(tcipsFlow.getPacketCount());
                t.setByteCount(tcipsFlow.getByteCount());
                em.merge(t);
            } else {
                em.persist(tcipsFlow);
                em.flush();
            }

        } catch (Exception e) {
            LOG.error("Failed to save flow - {}", e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ctatsch.tcips.db.TcipsFlowService#deleteAll()
     */
    @Override
    public void deleteAll() {
        em.createQuery("delete from TcipsFlow").executeUpdate();
        em.flush();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ctatsch.tcips.db.TcipsFlowService#getAll()
     */
    @Override
    public List<TcipsFlow> getAll() {
        try {
            return em.createQuery("select t from TcipsFlow t", TcipsFlow.class).getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public TcipsFlow getLast() {
        try {
            return em.createQuery("select t from TcipsFlow t ORDER BY t.id desc", TcipsFlow.class).setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<TcipsFlow> getAllInput() {
        try {
            return em.createQuery("select t from TcipsFlow t WHERE t.input is true", TcipsFlow.class).getResultList();
        } catch (NoResultException e) {
            return Collections.emptyList();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ctatsch.tcips.db.TcipsFlowService#getById()
     */
    @Override
    public TcipsFlow getById(Integer id) {
        try {
            return em.createQuery("select t from TcipsFlow t where t.id = :id", TcipsFlow.class)
                    .setParameter("id", id).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ctatsch.tcips.db.TcipsFlowService#getById()
     */
    @Override
    public TcipsFlow getByHeader(String nodeName, String srcIpv4, String dstIpv4, Integer srcPort, Integer dstPort) {

        try {
            return em.createQuery("select t from TcipsFlow t "
                    + "where t.nodeName = :nodeName "
                    + "and t.srcIpv4 = :srcIpv4 "
                    + "and t.dstIpv4 = :dstIpv4 "
                    + "and t.srcPort = :srcPort "
                    + "and t.dstPort = :dstPort", TcipsFlow.class)
                    .setParameter("nodeName", nodeName)
                    .setParameter("srcIpv4", srcIpv4)
                    .setParameter("dstIpv4", dstIpv4)
                    .setParameter("srcPort", srcPort)
                    .setParameter("dstPort", dstPort)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getDistinctNodes() {

        try {
            return em.createNativeQuery("select distinct nodeName from tcipsflow").getResultList();
        } catch (NoResultException e) {
            LOG.error("No result for getDistinctNodes");
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TcipsFlow> getFlowsByNodeDistinctDestinationPortsLow(Integer packetCount, String nodeName, Date after,
            Integer minFlows) {

        try {

            return em.createNativeQuery(""
                    + "select * from "
                    + "(select srcipv4, dstipv4, count(distinct dstport) as qnt "
                    + "from tcipsflow "
                    + "where input is true "
                    + "and packetcount < ? "
                    + "and nodename = ? "
                    + "and incometime > ?"
                    + "group by srcipv4, dstipv4) as s1 "
                    + "join tcipsflow t1 on t1.srcipv4 = s1.srcipv4 "
                    + "and t1.dstipv4 = s1.dstipv4 "
                    + "and s1.qnt >= ? "
                    + "and t1.incometime > ? "
                    + "and packetcount < ? "
                    + "and nodename = ? "
                    + "order by t1.srcipv4, t1.dstipv4, t1.incomeTime", TcipsFlow.class)
                    .setParameter(1, packetCount)
                    .setParameter(2, nodeName)
                    .setParameter(3, after, TemporalType.TIMESTAMP)
                    .setParameter(4, minFlows)
                    .setParameter(5, after, TemporalType.TIMESTAMP)
                    .setParameter(6, packetCount)
                    .setParameter(7, nodeName)
                    .getResultList();

        } catch (Exception e) {
            LOG.error("{}", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<FilteredFlow> getFlowsByNodeDistinctDestinationPortsHigh(Integer packetCount, String nodeName) {

        try {
            @SuppressWarnings("unchecked")
            List<Object[]> list = em.createNativeQuery(""
                    + "select srcipv4, dstipv4, count(distinct dstport) "
                    + "from tcipsflow "
                    + "where input is true "
                    + "and packetcount >= ? "
                    + "and nodename = ? "
                    + "group by srcipv4, dstipv4")
                    .setParameter(1, packetCount)
                    .setParameter(2, nodeName)
                    .getResultList();

            List<FilteredFlow> ffList = new ArrayList<>();
            for (Object[] obj : list) {
                FilteredFlow ff = new FilteredFlow();

                ff.setSrcIp((String) obj[0]);
                ff.setDstIp((String) obj[1]);
                // ff.setDstPort((Integer) obj[2]);

                ff.setCount(((Number) obj[2]).longValue());
                ffList.add(ff);
            }
            return ffList;

        } catch (Exception e) {
            LOG.error("{}", e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TcipsFlow> getFlowsByNodeDistinctDestinationHostsLow(Integer packetCount, String nodeName,
            Date after, Integer minHosts) {

        try {

            return em.createNativeQuery(""
                    + "select * from "
                    + "(select srcipv4, dstport, count(distinct dstipv4) as qnt "
                    + "from tcipsflow "
                    + "where input is true "
                    + "and packetcount < ? "
                    + "and nodename = ? "
                    + "and incometime > ?"
                    + "group by srcipv4, dstport) as s1 "
                    + "join tcipsflow t1 on t1.srcipv4 = s1.srcipv4 "
                    + "and t1.dstport = s1.dstport "
                    + "and s1.qnt >= ? "
                    + "and t1.incometime > ? "
                    + "and t1.packetcount < ? "
                    + "and t1.nodename = ? "
                    + "order by t1.srcipv4, t1.dstport, t1.incomeTime", TcipsFlow.class)
                    .setParameter(1, packetCount)
                    .setParameter(2, nodeName)
                    .setParameter(3, after, TemporalType.TIMESTAMP)
                    .setParameter(4, minHosts)
                    .setParameter(5, after, TemporalType.TIMESTAMP)
                    .setParameter(6, packetCount)
                    .setParameter(7, nodeName)
                    .getResultList();

        } catch (Exception e) {
            LOG.error("{}", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<FilteredFlow> getFlowsByNodeDistinctDestinationHostsHigh(Integer packetCount, String nodeName) {

        try {
            @SuppressWarnings("unchecked")
            List<Object[]> list = em.createNativeQuery(""
                    + "select srcipv4, dstport, count(distinct dstipv4) "
                    + "from tcipsflow "
                    + "where input is true "
                    + "and packetcount >= ? "
                    + "and nodename = ? "
                    + "group by srcipv4, dstport")
                    .setParameter(1, packetCount)
                    .setParameter(2, nodeName)
                    .getResultList();

            List<FilteredFlow> ffList = new ArrayList<>();
            for (Object[] obj : list) {
                FilteredFlow ff = new FilteredFlow();

                ff.setSrcIp((String) obj[0]);
                ff.setDstIp((String) obj[1]);
                // ff.setDstPort((Integer) obj[2]);

                ff.setCount(((Number) obj[2]).longValue());
                ffList.add(ff);
            }
            return ffList;

        } catch (Exception e) {
            LOG.error("{}", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void close() throws Exception {
        if (this.em != null && this.em.isOpen()) {
            this.em.close();
        }
    }

}
