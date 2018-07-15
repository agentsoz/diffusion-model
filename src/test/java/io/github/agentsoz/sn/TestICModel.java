package io.github.agentsoz.sn;

import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SocialNetworkManager;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
        assertEquals(0.0015,SNConfig.getStandardDeviation(),0);

    }

}
