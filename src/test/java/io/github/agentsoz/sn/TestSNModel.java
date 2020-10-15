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

public class TestSNModel {

//    String logFile = "./testSNModel.log";
//    final Logger logger = Log.createLogger("", logFile);
String testConfigFile="./case_studies/hawkesbury/test_ICModel.xml";

    @Test
    public void testSNModel() {

        Global.setRandomSeed(4711); // deterministic results for testing
        String testConfigFile="./case_studies/hawkesbury/test_ICModel.xml";
        DataServer ds1 = DataServer.getInstance("TestServer1");


        SocialNetworkDiffusionModel snModel = new SocialNetworkDiffusionModel(testConfigFile,ds1);

        snModel.setupSNConfigsAndLogs();
        SNConfig.getContentsToRegisterForICModel().clear();
        SNConfig.getContentsToRegisterForICModel().add("contentX");
        SNConfig.printNetworkConfigs();
        SNConfig.printDiffusionConfigs();

        SNUtils.randomAgentMap(snModel, 1000, 1000);
        //snModel.initWithoutSocialAgentsMap();
        snModel.generateSocialNetwork();
        snModel.generateDiffusionModels();
       // snModel.getDiffModels()[0]. initialise();

        // more diffusion model inits?
        ICModel ic = (ICModel) snModel.getDiffModels()[0];
       // ic.initRandomSeed("contentX");
        ic.recordCurrentStepSpread(snModel.getDataServer().getTime());

        // run the diffusion process
        SNUtils.setEndSimTime(36000*8L);
        snModel.getDataServer().setTime(0.0);
        snModel.getDataServer().setTimeStep(SNConfig.getDiffTurn_ic());
        while (snModel.getDataServer().getTime() <= SNUtils.getEndSimTime()) {

            ic.step();// diffuseContent();
            snModel.getDataServer().stepTime();
            ic.recordCurrentStepSpread(snModel.getDataServer().getTime());
        }

        snModel.finish();
        Assert.assertEquals(666, ic.getDataCollector().getAdoptedAgentCountForContent(snModel,"contentX"));
        Assert.assertEquals(641, ic.getDataCollector().getExposedAgentCountForContent("contentX"));

    }

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
