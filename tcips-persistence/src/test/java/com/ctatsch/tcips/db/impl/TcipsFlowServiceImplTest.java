package com.ctatsch.tcips.db.impl;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Assert;
import org.junit.Test;

import com.ctatsch.tcips.db.FilteredFlow;
import com.ctatsch.tcips.db.GlobalFields;
import com.ctatsch.tcips.db.TcipsFlow;

/**
 * To test a persistence class we need to create and inject the EntityManager
 * and we need to manage the transaction by hand.
 */
public class TcipsFlowServiceImplTest {

    @Test
    public void testWriteRead() throws Exception {
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
        
        TcipsFlowServiceImpl tcipsFlowService = new TcipsFlowServiceImpl();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("tcipspu-test", System.getProperties());
        EntityManager em = emf.createEntityManager();
        tcipsFlowService.setEntityManager(em);

        TcipsFlow flo = new TcipsFlow("openflow:1");
        flo.setInput(true);
        flo.setConsideredScan(true);
        flo.setPacketCount(BigInteger.ONE);
        flo.setDstIpv4("192.168.0.0");
        flo.setSrcIpv4("10.0.0.1");
        flo.setIncomeTime(new Date());
        flo.setDstPort(12333);

        em.getTransaction().begin();
        tcipsFlowService.deleteAll();
        tcipsFlowService.add(flo);

        Thread.sleep(1000);
        TcipsFlow flo2 = new TcipsFlow("openflow:1");
        flo2.setInput(true);
        flo2.setConsideredScan(true);
        flo2.setPacketCount(BigInteger.ONE);
        flo2.setDstIpv4("192.168.0.0");
        flo2.setSrcIpv4("10.0.0.1");
        flo2.setDstPort(12336);
        flo2.setIncomeTime(new Date());
        tcipsFlowService.add(flo2);

        Thread.sleep(5000);
        TcipsFlow flo3 = new TcipsFlow("openflow:1");
        flo3.setInput(true);
        flo3.setConsideredScan(true);
        flo3.setPacketCount(BigInteger.ONE);
        flo3.setDstIpv4("192.168.0.0");
        flo3.setSrcIpv4("10.0.0.1");
        flo3.setDstPort(12334);
        flo3.setIncomeTime(new Date());
        tcipsFlowService.add(flo3);

        em.getTransaction().commit();

        TcipsFlow f = tcipsFlowService.getLast();
        Assert.assertNotNull(f);

        List<TcipsFlow> teste = tcipsFlowService.getFlowsByNodeDistinctDestinationHostsLow(GlobalFields.maxPackets,
                "openflow:1",
                Date.from(LocalDateTime.now().minusSeconds(GlobalFields.collectionInterval).atZone(ZoneId.systemDefault())
                        .toInstant()),
                GlobalFields.minHosts);
        System.out.println("TESTE1: " + teste.size());

        Date dt = Date.from(LocalDateTime.now().minusSeconds(5).atZone(ZoneId.systemDefault()).toInstant());
        System.out.println("UU: " + dt.toString());

        List<TcipsFlow> ffList = tcipsFlowService.getFlowsByNodeDistinctDestinationPortsLow(5, "openflow:1",
                dt, 1);

        for (TcipsFlow fa : ffList) {
            System.out.println("RES: " + fa.toString());
        }

        String lastIp = ffList.get(0).getSrcIpv4();
        LocalDateTime lastDt = ffList.get(0).getIncomeTimeAsLocal();
        int counter = 1;

        for (int i = 1; i < ffList.size(); i++) {
            TcipsFlow curr = ffList.get(i);

            if (!curr.getSrcIpv4().equals(lastIp)) {
                lastIp = curr.getSrcIpv4();
                lastDt = curr.getIncomeTimeAsLocal();
                counter = 0;
                System.out.println("Clear counter");
            }

            long diff = lastDt.until(curr.getIncomeTimeAsLocal(), ChronoUnit.SECONDS);
            System.out.println("DIFF: " + diff);
            if (diff <= 10) {
                counter++;

                if (counter >= 3) {
                    System.out.println("Block ip {}" + lastIp);
                }
            }

            System.out.println("Suspicious flow: " + curr.toString());
        }

        List<TcipsFlow> flows = tcipsFlowService.getAll();
        Assert.assertEquals(3, flows.size());
        Assert.assertEquals(new Integer(1), flows.get(0).getId());
        Assert.assertEquals("openflow:1", flows.get(0).getNodeName());
        
        tcipsFlowService.close();
    }

}
