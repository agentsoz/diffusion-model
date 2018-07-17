package io.github.agentsoz.sn;

import io.github.agentsoz.socialnetwork.ICModel;
import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.SocialNetworkManager;
import io.github.agentsoz.socialnetwork.datacollection.ICModelDataCollector;
import io.github.agentsoz.socialnetwork.util.Global;
import io.github.agentsoz.socialnetwork.util.SNUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestICModel {

    public static String testConfigFile = "case_studies/hawkesbury/test_ICModel.xml";


    @Test
    public void testConfigs(){
        SocialNetworkManager testSN = new SocialNetworkManager(testConfigFile);
        testSN.setupSNConfigs();
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
        SNUtils.randomAgentMap(sn,80,1000);
        sn.setupSNConfigs();
        sn.genNetworkAndDiffModels();
        ICModel testIC = (ICModel) sn.getDiffModel();

        //test IC model configs
        testIC.registerNewContent("testContentX");
        testIC.selectRandomSeed(SNConfig.getSeed(),"testContentX");

       Assert.assertEquals(42, ICModelDataCollector.getAdoptedAgentsForContent(sn,"testContentX"));

       //test setSpecificSeed
        int[] testIds = {0,1,2};
        testIC.setSpecificSeed(testIds, "testContentY");
        Assert.assertEquals(3, ICModelDataCollector.getAdoptedAgentsForContent(sn,"testContentY"));

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

        icModel.registerNewContent("contentX");
        icModel.addExposureAttempt(1,2,"contentX");
        assertTrue(icModel.neighbourAlreadyExposed(1,2,"contentX"));
        Assert.assertFalse(icModel.neighbourAlreadyExposed(2,1,"contentX"));
    }

    @Test
    public void testRandomDiffProbabilityRange() {
        // SD = 0.05, p = 0.16
        Global.setRandomSeed(4711);
        SocialNetworkManager sn = new SocialNetworkManager(testConfigFile);
        SNUtils.randomAgentMap(sn, 100, 1000);
        sn.setupSNConfigs();
        sn.genNetworkAndDiffModels();

        ICModel icModel = (ICModel) sn.getDiffModel();

        for(int i = 0;i < 1000; i++) {
            double prob = icModel.getRandomDiffProbability();
            assertTrue(0.01 < prob && prob < 0.31);
        }

        icModel.registerNewContent("contentA");
        icModel.initSeedBasedOnStrategy();
        icModel.icDiffusion();
        int adoptedAgents = ICModelDataCollector.getAdoptedAgentsForContent(sn,"contentA");
        System.out.println(adoptedAgents);

    }
}
