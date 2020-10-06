package io.github.agentsoz.sn;

import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SocialNetworkModel;
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
        SNUtils.randomAgentMap(testSN,10,1000);
        testSN.genNetworkAndDiffModels();
        testSN.printSNModelconfigs();

        //IC model
        assertEquals(53.0, SNConfig.getSeed_ic(),0);
        assertEquals(36000,SNConfig.getDiffTurn_ic(),0);
        assertEquals("random",SNConfig.getStrategy_ic());
        assertEquals(0.16,SNConfig.getDiffusionProbability_ic(),0);
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


}
