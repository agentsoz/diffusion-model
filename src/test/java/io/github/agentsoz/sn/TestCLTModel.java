package io.github.agentsoz.sn;

import io.github.agentsoz.socialnetwork.CLTModel;
import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SocialNetworkModel;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.Global;
import io.github.agentsoz.socialnetwork.util.SNUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class TestCLTModel {

    public static String testConfigFile = "case_studies/hawkesbury/testOverallConfig_hawkesbury.xml";
    String cltDiffNetFile = "test_data/clt-diffusion-network.txt";
    final Logger logger = LoggerFactory.getLogger("");
    SocialNetworkModel testSN =  new SocialNetworkModel(testConfigFile);
    CLTModel cltModel;

    @Before
    public void setConfigs() {

        testSN.setupSNConfigsAndLogs();
        SNUtils.createAgentMapUsingActualCoords(testSN,5); // generate agent map
        logger.debug("test agent map size: {}",testSN.getAgentMap().size());


        cltModel = new CLTModel(SNConfig.getWaitSeed(),
                SNConfig.getPanicSeed(),
                SNConfig.getDiffTurn_lt(),
                testSN);
        cltModel.setupDiffConfigs();

        //set random seed
        Global.getRandom().setSeed(4711);
    }

    @Test
    public void testSNConifgs(){



        SNConfig.printDiffusionConfigs();

        assertEquals("lt", SNConfig.getDiffusionModelsList().get(0)); // #FIXME
        assertEquals("sw", SNConfig.getNetworkType());

        assertEquals(7200,SNConfig.getDiffTurn_lt());
        assertEquals(0.01,SNConfig.getStandardDeviation_lt(),0);
        assertEquals(DataTypes.PROBILITY,SNConfig.getStrategy_lt());
        assertEquals(DataTypes.GAUSSIAN,SNConfig.getDiffusionThresholdType_lt());
        assertEquals(13.12,SNConfig.getSeed_lt(),0);

        assertEquals(0.353,SNConfig.getMeanLowPanicThreshold_lt(),0);
       //   assertEquals(0.751,SNConfig.getMeanHighPanicThreshold(),0); // this is not read from the configs.

        assertEquals(25.32,SNConfig.getPanicSeed(),0);
        assertEquals(50.45,SNConfig.getWaitSeed(),0);
        assertEquals(0.244,SNConfig.getWaitThreshold(),0);
        assertEquals(0.576,SNConfig.getPanicThreshold(),0);


    }
    @Ignore
    @Test
    public void testWaitPanicAgentThresholds() {
        //calculate expected boundary values for both thresholds


        double waitlb = SNConfig.getWaitThreshold() - 3 * SNConfig.getStandardDeviation_lt();
        double waitub = SNConfig.getWaitThreshold() + 3 * SNConfig.getStandardDeviation_lt();

        logger.debug(" wait threshold boundaries: {}  - {}", waitlb,waitub);
        double paniclb = SNConfig.getPanicThreshold() - 3 * SNConfig.getStandardDeviation_lt();
        double panicub = SNConfig.getPanicThreshold() + 3 * SNConfig.getStandardDeviation_lt();

        logger.debug(" panic threshold boundaries: {}  - {}", paniclb,panicub);

        for(int i=0;i<1;i++) { //test iteratively
            cltModel.assignThresholds();
           // cltModel.printthresholdMap();

            assertTrue(waitlb <= cltModel.getAgentContentTreshold(0,DataTypes.WAIT) && cltModel.getAgentContentTreshold(0,DataTypes.WAIT) <= waitub);
            assertTrue(paniclb <= cltModel.getAgentContentTreshold(0,DataTypes.PANIC) && cltModel.getAgentContentTreshold(0,DataTypes.PANIC) <= panicub);
        }



    }

    @Ignore
    @Test
    public void testDistanceBasedProbabilities() {
        //frirefront-------------------------farEnd

        // checking Panic based probabilities
        double fireFrontProb = cltModel.getContentBasedProbability(0,280000, DataTypes.PANIC);
        double midDistfireFrontProb = cltModel.getContentBasedProbability(0,295000, DataTypes.PANIC);
        double farDistfireFrontProb = cltModel.getContentBasedProbability(0,310000, DataTypes.PANIC);

        Assert.assertEquals(0.75,fireFrontProb,0.0);
        Assert.assertEquals(0.5,midDistfireFrontProb,0.01);
        Assert.assertEquals(0.25,farDistfireFrontProb,0.01);

        //checking wait based probabilities
        double farEndWaitProb = cltModel.getContentBasedProbability(0,310000, DataTypes.WAIT);
        double midDistWaitProb = cltModel.getContentBasedProbability(0,295000, DataTypes.WAIT);
        double fireFrontWaitProb = cltModel.getContentBasedProbability(0,280000, DataTypes.WAIT);

        Assert.assertEquals(0.75,farEndWaitProb,0.01);
        Assert.assertEquals(0.5,midDistWaitProb,0.01);
        Assert.assertEquals(0.25,fireFrontWaitProb,0.01);

        //logger.trace("val {}",fireFrontWaitProb);
    }
    @Ignore
    @Test
    // random agent ids, random panic values (> respective thresholds)
    public void testInitialise() {

        cltModel.initialise();
        cltModel.printthresholdMap();
        cltModel.printSeedMaps();
        for(int id: testSN.getAgentMap().keySet()) { // no agent should be in both seed maps!
            assertFalse( cltModel.getSeedMap(DataTypes.PANIC).containsKey(id) && cltModel.getSeedMap(DataTypes.WAIT).containsKey(id) );

        }

        cltModel.printAgentContentValues();
    }

    @Ignore
    @Test
    // the social network used for this test case is an extended network with more links and agents than the one used for Lt model case.
    //Check the designs for the manual execution hardcopy of this process.
    public void testcompetivitveDiffusion() {


        SocialNetworkModel cltSNmanager = new SocialNetworkModel(testConfigFile);
        cltSNmanager.setupSNConfigsAndLogs(); // set sn configs
        SNUtils.randomAgentMap(cltSNmanager,9,1000); // cant use the actual cords as they do not match with the
        SNUtils.createTestNetwork(cltDiffNetFile,cltSNmanager); // create the test network

        CLTModel testCLT = new CLTModel(SNConfig.getWaitSeed(),
                SNConfig.getPanicSeed(),
                SNConfig.getDiffTurn_lt(),
                cltSNmanager);
        testCLT.setupDiffConfigs();

        testCLT.assignFixedThresholds(0.25,0.25); //set thresholds
       // testCLT.printthresholdMap();
        testCLT.manualSeedSet(DataTypes.WAIT,2, 0.8); // st manual seed - agent content levels are not updated
        testCLT.manualSeedSet(DataTypes.PANIC,6,0.7);
        checkSeedValues(testCLT);


       // testCLT.printSeedMaps();

        testCLT.doDiffProcess();
       // checkSeedValues(testCLT);
        checkTurn1ContentValues(testCLT);
        testCLT.printAgentContentValues();


        testCLT.doDiffProcess();
        checkTurn2ContentValues(testCLT);

        testCLT.doDiffProcess();
        checkTurn3ContentValues(testCLT);

        //print final agent values and adopted content
        testCLT.printAgentContentValues();
    }

    public void checkSeedValues(CLTModel testModel){
        Assert.assertEquals(0.8,testModel.getAgentMap().get(2).getContentlevel(DataTypes.WAIT),0.00);
        Assert.assertEquals(0.7,testModel.getAgentMap().get(6).getContentlevel(DataTypes.PANIC),0.00); //seed  values same

//        logger.info("optCount: {} waitCount: {} panicCount: {}",testModel.getOptCount(),testModel.getWaitCount(),testModel.getPanicCount());
    }

    public void checkTurn1ContentValues(CLTModel testModel) {

        Assert.assertEquals(0.2,testModel.getAgentMap().get(1).getContentlevel(DataTypes.WAIT),0.00); // agent 1

        Assert.assertEquals(0.3,testModel.getAgentMap().get(3).getContentlevel(DataTypes.WAIT),0.00); // agent 3
        Assert.assertEquals(DataTypes.WAIT,testModel.getAgentMap().get(3).getActivatedContentType());

        Assert.assertEquals(0.3,testModel.getAgentMap().get(4).getContentlevel(DataTypes.PANIC),0.00); //agent 4
        Assert.assertEquals(DataTypes.PANIC,testModel.getAgentMap().get(4).getActivatedContentType());

        Assert.assertEquals(0.1,testModel.getAgentMap().get(5).getContentlevel(DataTypes.PANIC),0.00); //agent 4
        //agent 7 -  random activation to wait, equal content levels
        Assert.assertEquals(0.3,testModel.getAgentMap().get(7).getContentlevel(DataTypes.WAIT),0.00); //agent7
        Assert.assertEquals(0.3,testModel.getAgentMap().get(7).getContentlevel(DataTypes.PANIC),0.00);
        Assert.assertEquals(DataTypes.PANIC,testModel.getAgentMap().get(7).getActivatedContentType());

 //       logger.info("optCount: {} waitCount: {} panicCount: {}",testModel.getOptCount(),testModel.getWaitCount(),testModel.getPanicCount());
    }

    public void checkTurn2ContentValues(CLTModel testModel) {

        Assert.assertEquals(0.0,testModel.getAgentMap().get(0).getContentlevel(DataTypes.PANIC),0.00); //agent 0
        Assert.assertEquals(0.0,testModel.getAgentMap().get(0).getContentlevel(DataTypes.PANIC),0.00);

        Assert.assertEquals(0.3,testModel.getAgentMap().get(1).getContentlevel(DataTypes.WAIT),0.01); // agent 1
        Assert.assertEquals(0.2,testModel.getAgentMap().get(1).getContentlevel(DataTypes.PANIC),0.00);
        Assert.assertEquals(DataTypes.WAIT,testModel.getAgentMap().get(1).getActivatedContentType());

        Assert.assertEquals(1.0,testModel.getAgentMap().get(2).getContentlevel(DataTypes.WAIT),0.00); //agent 2
        Assert.assertEquals(0.3,testModel.getAgentMap().get(2).getContentlevel(DataTypes.PANIC),0.00); //agent 2
        Assert.assertEquals(DataTypes.PANIC,testModel.getAgentMap().get(2).getActivatedContentType()); // agent converted to panic

        Assert.assertEquals(0.3,testModel.getAgentMap().get(3).getContentlevel(DataTypes.WAIT),0.00); // agent 3
        Assert.assertEquals(0.5,testModel.getAgentMap().get(3).getContentlevel(DataTypes.PANIC),0.00);

        Assert.assertEquals(0.5,testModel.getAgentMap().get(4).getContentlevel(DataTypes.WAIT),0.00); // agent 4
        Assert.assertEquals(0.3,testModel.getAgentMap().get(4).getContentlevel(DataTypes.PANIC),0.00);

        Assert.assertEquals(0.1,testModel.getAgentMap().get(5).getContentlevel(DataTypes.PANIC),0.00); //agent 5
        Assert.assertEquals(0.1,testModel.getAgentMap().get(5).getContentlevel(DataTypes.WAIT),0.00);

        Assert.assertEquals(1.0,testModel.getAgentMap().get(6).getContentlevel(DataTypes.PANIC),0.00); //agent 6
        Assert.assertEquals(0.0,testModel.getAgentMap().get(6).getContentlevel(DataTypes.WAIT),0.00);

        //agent 7 -  no difference - same as turn 1

        Assert.assertEquals(0.5,testModel.getAgentMap().get(8).getContentlevel(DataTypes.PANIC),0.00); //agent 8
        Assert.assertEquals(DataTypes.PANIC,testModel.getAgentMap().get(8).getActivatedContentType());

//        logger.info("optCount: {} waitCount: {} panicCount: {}",testModel.getOptCount(),testModel.getWaitCount(),testModel.getPanicCount());
    }

    public void checkTurn3ContentValues(CLTModel testModel) {
        Assert.assertEquals(0.5,testModel.getAgentMap().get(0).getContentlevel(DataTypes.PANIC),0.00); //agent 0
        Assert.assertEquals(0.2,testModel.getAgentMap().get(0).getContentlevel(DataTypes.WAIT),0.00);
        Assert.assertEquals(DataTypes.PANIC,testModel.getAgentMap().get(0).getActivatedContentType());

        Assert.assertEquals(0.8,testModel.getAgentMap().get(3).getContentlevel(DataTypes.PANIC),0.00); //agent 3
        Assert.assertEquals(0.1,testModel.getAgentMap().get(3).getContentlevel(DataTypes.WAIT),0.00);
        Assert.assertEquals(DataTypes.PANIC,testModel.getAgentMap().get(3).getActivatedContentType());

        Assert.assertEquals(0.2,testModel.getAgentMap().get(4).getContentlevel(DataTypes.WAIT),0.00); //agent 4
        Assert.assertEquals(1.0,testModel.getAgentMap().get(4).getContentlevel(DataTypes.PANIC),0.00);


        Assert.assertEquals(DataTypes.INACTIVE,testModel.getAgentMap().get(5).getActivatedContentType()); //agent 5

        //same as turn2: agent 8, agent 7,2,6

 //       logger.info("optCount: {} waitCount: {} panicCount: {}",testModel.getOptCount(),testModel.getWaitCount(),testModel.getPanicCount());
    }

}
