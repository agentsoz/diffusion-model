package io.github.agentsoz.sn;

import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.socialnetwork.*;
import io.github.agentsoz.socialnetwork.datacollection.ICModelDataCollector;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.Global;
import io.github.agentsoz.socialnetwork.util.SNUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestICModel {

    public static String testConfigFile = "case_studies/hawkesbury/test_ICModel.xml";


    @Test
 //   @Ignore
    public void testConfigs(){
        SocialNetworkDiffusionModel testSN = new SocialNetworkDiffusionModel(testConfigFile);
        testSN.setupSNConfigsAndLogs();
        testSN.printSNModelconfigs();

        assertEquals(53.0, SNConfig.getSeed_ic(),0);
        assertEquals(36000,SNConfig.getDiffTurn_ic(),0);
        assertEquals("random",SNConfig.getStrategy_ic());
        assertEquals(0.16,SNConfig.getDiffusionProbability_ic(),0);
        assertEquals(0.05,SNConfig.getStandardDeviation_ic(),0);
        assertEquals(SNConfig.getOutputFilePathOfTheICModel(),"./test/output/diffusion_testing_ic.out");


    }

    @Test
    public void testRandomSeed() {

        SocialNetworkDiffusionModel sn = new SocialNetworkDiffusionModel(testConfigFile);
        sn.setupSNConfigsAndLogs();
        SNUtils.randomAgentMap(sn,80,1000);

        sn.genNetworkAndDiffModels();
        ICModel testIC = (ICModel) sn.getDiffModels()[0];

        //test IC model configs
        testIC.registerContentIfNotRegistered("testContentX",DataTypes.LOCAL);
        testIC.selectRandomSeed(SNConfig.getSeed_ic(),"testContentX");

        ICModelDataCollector dc = new ICModelDataCollector(); // data collector
        Assert.assertEquals(42, dc.getAdoptedAgentCountForContent(sn,"testContentX"));

       //test setSpecificSeed
        Integer[] testIds = {0,1,2};
        testIC.setSpecificSeed(testIds, "testContentY");
        Assert.assertEquals(3, dc.getAdoptedAgentCountForContent(sn,"testContentY"));

    }


    @Test
    public void testAdoptedContent(){

        SocialAgent agent =  new SocialAgent(1);
        agent.adoptContent("testContent1");
        assertTrue(agent.alreadyAdoptedContent("testContent1"));
        Assert.assertFalse(agent.alreadyAdoptedContent("testContent2"));
    }

    @Test
    public void testExposureAttmept(){

        SocialNetworkDiffusionModel sn = new SocialNetworkDiffusionModel(testConfigFile);
        ICModel icModel = new ICModel(sn, 30, 0.0);

        icModel.registerContentIfNotRegistered("contentX",DataTypes.LOCAL);
        icModel.addExposureAttempt(1,2,"contentX");
        assertTrue(icModel.neighbourAlreadyExposed(1,2,"contentX"));
        Assert.assertFalse(icModel.neighbourAlreadyExposed(2,1,"contentX"));
    }

    @Test
    public void testRandomDiffProbabilityRange() {
        // SD = 0.05, p = 0.16
        Global.setRandomSeed(4711);
        SocialNetworkDiffusionModel sn = new SocialNetworkDiffusionModel(testConfigFile);
        sn.setupSNConfigsAndLogs();
        SNUtils.randomAgentMap(sn, 100, 1000);

        sn.genNetworkAndDiffModels();

        ICModel icModel = (ICModel) sn.getDiffModels()[0];

        for(int i = 0;i < 1000; i++) {
            double prob = icModel.getRandomDiffProbability();
            assertTrue(0.01 < prob && prob < 0.31);
        }

    }

    @Test
    public void testICDiffusion(){

        // SD = 0.05, p = 0.16
        Global.setRandomSeed(4711);
        SocialNetworkDiffusionModel sn = new SocialNetworkDiffusionModel(testConfigFile);
        sn.setupSNConfigsAndLogs();
        SNUtils.randomAgentMap(sn, 100, 1000);
        sn.genNetworkAndDiffModels();

        ICModel icModel = (ICModel) sn.getDiffModels()[0];
        icModel.registerContentIfNotRegistered("contentA",DataTypes.LOCAL);
        icModel.initSeedBasedOnStrategy("contentA");
        icModel.icDiffusion();

        ICModelDataCollector dc = new ICModelDataCollector();
        int adoptedAgents = dc.getAdoptedAgentCountForContent(sn,"contentA");
        Assert.assertEquals(64,adoptedAgents);

    }

    @Test
    public void testWriteFile(){

        Global.setRandomSeed(4711); // deterministic results for testing
      //  String outFile = "./test/output/diffusion.out";

        DataServer ds = DataServer.getInstance("test"); //use a different dataserver for each test case, o.w mvn tests fail
        SocialNetworkDiffusionModel sn = new SocialNetworkDiffusionModel(testConfigFile,ds);
        sn.setupSNConfigsAndLogs();
        SNUtils.randomAgentMap(sn, 100, 1000);


        sn.initWithoutSocialAgentsMap();
        SNConfig.setDiffturn_ic(60);
        SNConfig.setSeed_ic(15);

        ICModel ic = (ICModel) sn.getDiffModels()[0];
        ic.registerContentIfNotRegistered("contentX",DataTypes.LOCAL);
        ic.registerContentIfNotRegistered("contentY",DataTypes.LOCAL);
        ic.initRandomSeed("contentX"); // initialise a random seed for a specific content
        ic.initRandomSeed("contentY"); // initialise a random seed for a specific content

        ic.recordCurrentStepSpread(0.0); //record seed spread

        //setup sim configs
        SNUtils.setEndSimTime(3600*8);
        sn.getDataServer().setTime(0.0);
        sn.getDataServer().setTimeStep(SNConfig.getDiffTurn_ic());

        while(sn.getDataServer().getTime() <= SNUtils.getEndSimTime()) {
           // sn.stepDiffusionProcess();
            sn.getDiffModels()[0].step(); //diffuseContent();
            sn.getDataServer().stepTime();
            ic.recordCurrentStepSpread(sn.getDataServer().getTime());
        }

        //end of simulation, now print to file
        ic.finish();
        ICModelDataCollector dc = new ICModelDataCollector();
        ic.getDataCollector().writeSpreadDataToFile();

        //verify total agent count, then specific active agent count for each content
        assertEquals(106, dc.getTotalInactiveAgents(sn) + dc.getAdoptedAgentCountForContent(sn,"contentX") + dc.getAdoptedAgentCountForContent(sn,"contentY"));
        assertEquals(24,  dc.getAdoptedAgentCountForContent(sn,"contentX"));
        assertEquals(27, dc.getAdoptedAgentCountForContent(sn,"contentY"));

    }

    // broadcast one global content, along with same x,y local contents
    @Test
    public void testConentBroadcast(){

        Global.setRandomSeed(4711); // deterministic results for testing
     //   String outFile = "./test/output/diffusion.out";

        DataServer ds = DataServer.getInstance("test1"); // use a different dataserver for each test case, o.w mvn tests fail
        SocialNetworkDiffusionModel sn = new SocialNetworkDiffusionModel(testConfigFile,ds);
        sn.setupSNConfigsAndLogs();
        SNUtils.randomAgentMap(sn, 100, 1000);

        sn.initWithoutSocialAgentsMap();
        SNConfig.setDiffturn_ic(60);
        SNConfig.setSeed_ic(15);
        ICModel ic = (ICModel) sn.getDiffModels()[0];

        ic.registerContentIfNotRegistered("contentX",DataTypes.LOCAL);
        ic.registerContentIfNotRegistered("contentY",DataTypes.LOCAL);
        ic.initRandomSeed("contentX"); // initialise a random seed for a specific content
        ic.initRandomSeed("contentY"); // initialise a random seed for a specific content

        ic.recordCurrentStepSpread(0.0); //record seed spread

        //setup sim configs
        SNUtils.setEndSimTime(3600*8);
        sn.getDataServer().setTime(0.0);
        sn.getDataServer().setTimeStep(SNConfig.getDiffTurn_ic());

        while(sn.getDataServer().getTime() <= SNUtils.getEndSimTime()) {
            // sn.stepDiffusionProcess();

            if( sn.getDataServer().getTime() == 3600*4) {
                ic.registerContentIfNotRegistered("evac-now",DataTypes.GLOBAL);
                ArrayList<String> globalcontentArr = new ArrayList<String>();
                globalcontentArr.add("evac-now");
                ic.updateSocialStatesFromGlobalContent(globalcontentArr);
            }
            if( sn.getDataServer().getTime() == 3600*6) { // checking if sent again from BDI side, what will happen
                ic.registerContentIfNotRegistered("evac-now",DataTypes.GLOBAL);
                ArrayList<String> globalcontents = new ArrayList<String>();
                globalcontents.add("evac-now");
                ic.updateSocialStatesFromGlobalContent(globalcontents);
            }

            sn.getDiffModels()[0].step(); //diffuseContent();
            sn.getDataServer().stepTime();
            ic.recordCurrentStepSpread(sn.getDataServer().getTime());
        }

        //end of simulation, now print to file
        ic.finish();
        ICModelDataCollector dc = new ICModelDataCollector();
        ic.getDataCollector().writeSpreadDataToFile();

        assertEquals(24, dc.getAdoptedAgentCountForContent(sn,"contentX"));
        assertEquals(27, dc.getAdoptedAgentCountForContent(sn,"contentY"));
        assertEquals(ic.getAgentMap().size(), dc.getAdoptedAgentCountForContent(sn,"evac-now"));
    }


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

}
