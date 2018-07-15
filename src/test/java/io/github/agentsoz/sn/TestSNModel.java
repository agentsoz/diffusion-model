package io.github.agentsoz.sn;

import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SNModel;
import io.github.agentsoz.socialnetwork.util.Log;
import io.github.agentsoz.socialnetwork.util.SNUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

public class TestSNModel {

    String logFile = "./testSNModel.log";
    final Logger logger = Log.createLogger("", logFile);

    //  @Ignore
    @Test
    public void testInitSocialAgentMap() {

        DataServer ds1 = DataServer.getServer("TestServer1");
        List<String> ids = Arrays.asList("1", "2", "3");


        SNModel snModel = new SNModel(SNConfig.getDefaultConfigFile(), ds1);
        snModel.initSocialAgentMap(ids);
        System.out.println(snModel.getSNManager().getAgentMap().keySet().toString());
        Assert.assertEquals(ids.size(), snModel.getSNManager().getAgentMap().size());

    }

   // @Ignore
    @Test
    public void testgenSNModel() {

        DataServer ds2 = DataServer.getServer("TestServer2");
        List<String> ids = Arrays.asList("1", "2", "3");

        SNModel snModel = new SNModel(SNConfig.getDefaultConfigFile(), ds2);
        snModel.initSocialAgentMap(ids);

        snModel.initSNModel();

        SNUtils.setEndSimTime(7200L);
        while (snModel.getDataServer().getTime() <= SNUtils.getEndSimTime()) {
            snModel.stepDiffusionProcess();
            snModel.getDataServer().stepTime();
        }


    }


}
