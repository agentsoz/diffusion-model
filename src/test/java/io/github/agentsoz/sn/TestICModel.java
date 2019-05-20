package io.github.agentsoz.sn;

import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.socialnetwork.*;
import io.github.agentsoz.socialnetwork.datacollection.ICModelDataCollector;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.Global;
import io.github.agentsoz.socialnetwork.util.SNUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestICModel {

    public static String testConfigFile = "case_studies/hawkesbury/test_ICModel.xml";


    @Test
    @Ignore
    public void testConfigs(){
        SocialNetworkManager testSN = new SocialNetworkManager(testConfigFile);
        testSN.setupSNConfigsAndLogs();
        testSN.printSNModelconfigs();

        assertEquals(53.0, SNConfig.getSeed(),0);
        assertEquals(36000,SNConfig.getDiffturn(),0);
        assertEquals("random",SNConfig.getStrategy());
        assertEquals(0.16,SNConfig.getDiffProbability(),0);
        assertEquals(0.05,SNConfig.getStandardDeviation(),0);

    }

    @Test
    public void testRandomSeed() {

        SocialNetworkManager sn = new SocialNetworkManager(testConfigFile);
        sn.setupSNConfigsAndLogs();
        SNUtils.randomAgentMap(sn,80,1000);

        sn.genNetworkAndDiffModels();
        ICModel testIC = (ICModel) sn.getDiffModel();

        //test IC model configs
        testIC.registerContentIfNotRegistered("testContentX",DataTypes.LOCAL);
        testIC.selectRandomSeed(SNConfig.getSeed(),"testContentX");

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

        SocialNetworkManager sn = new SocialNetworkManager(testConfigFile);
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
        SocialNetworkManager sn = new SocialNetworkManager(testConfigFile);
        sn.setupSNConfigsAndLogs();
        SNUtils.randomAgentMap(sn, 100, 1000);

        sn.genNetworkAndDiffModels();

        ICModel icModel = (ICModel) sn.getDiffModel();

        for(int i = 0;i < 1000; i++) {
            double prob = icModel.getRandomDiffProbability();
            assertTrue(0.01 < prob && prob < 0.31);
        }

    }

    @Test
    public void testICDiffusion(){

        // SD = 0.05, p = 0.16
        Global.setRandomSeed(4711);
        SocialNetworkManager sn = new SocialNetworkManager(testConfigFile);
        sn.setupSNConfigsAndLogs();
        SNUtils.randomAgentMap(sn, 100, 1000);
        sn.genNetworkAndDiffModels();

        ICModel icModel = (ICModel) sn.getDiffModel();
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
        String outFile = "./test/output/diffusion.out";

        DataServer ds = DataServer.getServer("test"); //use a different dataserver for each test case, o.w mvn tests fail
        SNModel sn = new SNModel(testConfigFile,ds);
        sn.getSNManager().setupSNConfigsAndLogs();
        SNUtils.randomAgentMap(sn.getSNManager(), 100, 1000);

        sn.initSNModel();
        SNConfig.setDiffturn(60);
        SNConfig.setSeed(15);
        ICModel ic = (ICModel) sn.getSNManager().getDiffModel();
        ic.initRandomSeed("contentX"); // initialise a random seed for a specific content
        ic.initRandomSeed("contentY"); // initialise a random seed for a specific content

        ic.recordCurrentStepSpread(0.0); //record seed spread

        //setup sim configs
        SNUtils.setEndSimTime(3600*8);
        sn.getDataServer().setTime(0.0);
        sn.getDataServer().setTimeStep(SNConfig.getDiffturn());

        while(sn.getDataServer().getTime() <= SNUtils.getEndSimTime()) {
           // sn.stepDiffusionProcess();
            sn.getSNManager().diffuseContent();
            sn.getDataServer().stepTime();
            ic.recordCurrentStepSpread(sn.getDataServer().getTime());
        }

        //end of simulation, now print to file
        ic.finish();
        ICModelDataCollector dc = new ICModelDataCollector();
        ic.getDataCollector().writeSpreadDataToFile(outFile);

        //testing
        assertEquals(106, dc.getTotalInactiveAgents(sn.getSNManager()) + dc.getAdoptedAgentCountForContent(sn.getSNManager(),"contentX") + dc.getAdoptedAgentCountForContent(sn.getSNManager(),"contentY"));

    }

    // broadcast one global content, along with same x,y local contents
    @Test
    public void testConentBroadcast(){

        Global.setRandomSeed(4711); // deterministic results for testing
        String outFile = "./test/output/diffusion.out";

        DataServer ds = DataServer.getServer("test1"); // use a different dataserver for each test case, o.w mvn tests fail
        SNModel sn = new SNModel(testConfigFile,ds);
        sn.getSNManager().setupSNConfigsAndLogs();
        SNUtils.randomAgentMap(sn.getSNManager(), 100, 1000);

        sn.initSNModel();
        SNConfig.setDiffturn(60);
        SNConfig.setSeed(15);
        ICModel ic = (ICModel) sn.getSNManager().getDiffModel();

        ic.initRandomSeed("contentX"); // initialise a random seed for a specific content
        ic.initRandomSeed("contentY"); // initialise a random seed for a specific content

        ic.recordCurrentStepSpread(0.0); //record seed spread

        //setup sim configs
        SNUtils.setEndSimTime(3600*8);
        sn.getDataServer().setTime(0.0);
        sn.getDataServer().setTimeStep(SNConfig.getDiffturn());

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

            sn.getSNManager().diffuseContent();
            sn.getDataServer().stepTime();
            ic.recordCurrentStepSpread(sn.getDataServer().getTime());
        }

        //end of simulation, now print to file
        ic.finish();
        ICModelDataCollector dc = new ICModelDataCollector();
        ic.getDataCollector().writeSpreadDataToFile(outFile);

        assertEquals(24, dc.getAdoptedAgentCountForContent(sn.getSNManager(),"contentX"));
        assertEquals(27, dc.getAdoptedAgentCountForContent(sn.getSNManager(),"contentY"));
        assertEquals(ic.getAgentMap().size(), dc.getAdoptedAgentCountForContent(sn.getSNManager(),"evac-now"));
    }

}
