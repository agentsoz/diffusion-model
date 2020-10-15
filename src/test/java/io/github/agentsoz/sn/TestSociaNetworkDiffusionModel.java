package io.github.agentsoz.sn;

import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.socialnetwork.ICModel;
import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SocialNetworkDiffusionModel;
import io.github.agentsoz.socialnetwork.util.Global;
import io.github.agentsoz.socialnetwork.util.SNUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestSociaNetworkDiffusionModel {

//    String logFile = "./testSNModel.log";
//    final Logger logger = Log.createLogger("", logFile);
String testConfigFile="./case_studies/hawkesbury/test_IC_LT_Models.xml";






    @Ignore
    @Test
    public void testInitSocialAgentMap() {

        DataServer ds1 = DataServer.getInstance("TestServer1");
        List<String> ids = Arrays.asList("1", "2", "3");


        SocialNetworkDiffusionModel snModel = new SocialNetworkDiffusionModel(testConfigFile, ds1,ids);
        snModel.init();
        System.out.println(snModel.getAgentMap().keySet().toString());
        Assert.assertEquals(ids.size(), snModel.getAgentMap().size());

    }

    @Ignore
    @Test
    public void testgenSNModel() {

        DataServer ds2 = DataServer.getInstance("TestServer2");
        List<String> ids = Arrays.asList("1", "2", "3");

        SocialNetworkDiffusionModel snModel = new SocialNetworkDiffusionModel(testConfigFile, ds2,ids);
        snModel.init();

        SNUtils.setEndSimTime(7200L);
        while (snModel.getDataServer().getTime() <= SNUtils.getEndSimTime()) {
//            snModel.stepDiffusionProcess();
            snModel.getDataServer().stepTime();
        }


    }


}
