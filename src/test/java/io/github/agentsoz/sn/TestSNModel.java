package io.github.agentsoz.sn;

import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.socialnetwork.ICModel;
import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SocialNetworkModel;
import io.github.agentsoz.socialnetwork.util.Global;
import io.github.agentsoz.socialnetwork.util.SNUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestSNModel {

//    String logFile = "./testSNModel.log";
//    final Logger logger = Log.createLogger("", logFile);

    @Test
    public void testSNModel() {

        Global.setRandomSeed(4711); // deterministic results for testing
        String testConfigFile="./case_studies/hawkesbury/test_ICModel.xml";
        DataServer ds1 = DataServer.getServer("TestServer1");



        SocialNetworkModel snModel = new SocialNetworkModel(testConfigFile,ds1);
        snModel.setupSNConfigsAndLogs();
        SNUtils.randomAgentMap(snModel, 1000, 1000);
        snModel.initWithoutSocialAgentsMap();

        // more diffusion model inits?
        ICModel ic = (ICModel) snModel.getDiffModels()[0];
        ic.initRandomSeed("contentX");
        ic.recordCurrentStepSpread(snModel.getDataServer().getTime());

        // run the diffusion process
        SNUtils.setEndSimTime(36000*8L);
        snModel.getDataServer().setTime(0.0);
        snModel.getDataServer().setTimeStep(SNConfig.getDiffTurn_ic()); // #FIXME hardcoded to ic model turn?
        while (snModel.getDataServer().getTime() <= SNUtils.getEndSimTime()) {

            snModel.getDiffModels()[0].doDiffProcess();// diffuseContent();
            snModel.getDataServer().stepTime();
            ICModel icModel = (ICModel) snModel.getDiffModels()[0];
            icModel.recordCurrentStepSpread(snModel.getDataServer().getTime());
        }

        snModel.finish();

    }

    @Ignore
    @Test
    public void testInitSocialAgentMap() {

        DataServer ds1 = DataServer.getServer("TestServer1");
        List<String> ids = Arrays.asList("1", "2", "3");


        SocialNetworkModel snModel = new SocialNetworkModel(SNConfig.getDefaultConfigFile(), ds1,ids);
        snModel.initSNModel();
        System.out.println(snModel.getAgentMap().keySet().toString());
        Assert.assertEquals(ids.size(), snModel.getAgentMap().size());

    }

    @Ignore
    @Test
    public void testgenSNModel() {

        DataServer ds2 = DataServer.getServer("TestServer2");
        List<String> ids = Arrays.asList("1", "2", "3");

        SocialNetworkModel snModel = new SocialNetworkModel(SNConfig.getDefaultConfigFile(), ds2,ids);
        snModel.initSNModel();

        SNUtils.setEndSimTime(7200L);
        while (snModel.getDataServer().getTime() <= SNUtils.getEndSimTime()) {
//            snModel.stepDiffusionProcess();
            snModel.getDataServer().stepTime();
        }


    }


}
