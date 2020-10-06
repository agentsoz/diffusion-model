package io.github.agentsoz.sn;

import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.socialnetwork.DiffModel;
import io.github.agentsoz.socialnetwork.ICModel;
import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SocialNetworkModel;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.SNUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestICLTModels {

    public static String testConfigFile = "case_studies/hawkesbury/test_IC_LT_Models.xml";

    @Test
    //   @Ignore
    public void testConfigs(){
        SocialNetworkModel testSN = new SocialNetworkModel(testConfigFile);
        testSN.setupSNConfigsAndLogs();
        SNUtils.randomAgentMap(testSN,100,1000);
        testSN.genNetworkAndDiffModels();
        testSN.printSNModelconfigs();

        //IC model
        assertEquals(13.0, SNConfig.getSeed_ic(),0);
        assertEquals(1800,SNConfig.getDiffTurn_ic(),0);
        assertEquals("random",SNConfig.getStrategy_ic());
        assertEquals(0.5,SNConfig.getDiffusionProbability_ic(),0);
        assertEquals(0.05,SNConfig.getStandardDeviation_ic(),0);

        //LT model
        assertEquals(30.0, SNConfig.getSeed_lt(),0);
        assertEquals(3600,SNConfig.getDiffTurn_lt(),0);
        assertEquals("random",SNConfig.getStrategy_lt());
        assertEquals("guassian",SNConfig.getDiffusionThresholdType_lt());
//		assertEquals(0.16,SNConfig.getDiffusionProbability_ic(),0);
        assertEquals(0.025,SNConfig.getStandardDeviation_lt(),0);
        assertEquals(0.3,SNConfig.getMeanLowPanicThreshold_lt(),0.0);

    }

    @Test
    public void testMultipleDiffusionProcessesWithDS(){

        DataServer ds = DataServer.getServer("multiple"); //use a different dataserver for each test case, o.w mvn tests fail

        SocialNetworkModel testSN = new SocialNetworkModel(testConfigFile,ds);
        testSN.setupSNConfigsAndLogs();
        SNUtils.randomAgentMap(testSN,100,1000);
        testSN.genNetworkAndDiffModels();
        testSN.printSNModelconfigs();

        ICModel icModel  = (ICModel) testSN.getDiffModels()[0];
        icModel.registerContentIfNotRegistered("contentA", DataTypes.LOCAL);
        icModel.initSeedBasedOnStrategy("contentA");

        SNUtils.setEndSimTime(3600*8);
        testSN.getDataServer().setTime(0.0);
        testSN.getDataServer().setTimeStep(SNConfig.getDiffTurn_ic());

        icModel.recordCurrentStepSpread(testSN.getDataServer().getTime());
        while(testSN.getDataServer().getTime() <= SNUtils.getEndSimTime()) {
            // sn.stepDiffusionProcess();
            testSN.stepDiffusionModels(testSN.getDataServer().getTime()); //diffuseContent();
            testSN.getDataServer().stepTime();
            icModel.recordCurrentStepSpread(testSN.getDataServer().getTime());
        }

        for(DiffModel model:testSN.getDiffModels()) {
            model.finish();
        }
        icModel.getDataCollector().writeSpreadDataToFile();

    }
}
