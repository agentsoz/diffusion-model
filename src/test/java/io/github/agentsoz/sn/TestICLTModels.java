package io.github.agentsoz.sn;

import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.socialnetwork.DiffModel;
import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SocialNetworkDiffusionModel;
import io.github.agentsoz.socialnetwork.datacollection.ICModelDataCollector;
import io.github.agentsoz.socialnetwork.datacollection.LTModelDataCollector;
import io.github.agentsoz.socialnetwork.util.Global;
import io.github.agentsoz.socialnetwork.util.SNUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestICLTModels {

    public static String testConfigFile = "case_studies/hawkesbury/test_IC_LT_Models.xml";

    @Test
    public void testConfigs(){
        SocialNetworkDiffusionModel testSN = new SocialNetworkDiffusionModel(testConfigFile);
        testSN.setupSNConfigsAndLogs();
//        SNUtils.randomAgentMap(testSN,100,1000);
//        testSN.genNetworkAndDiffModels();
        testSN.printSNModelconfigs();

        //IC model
        assertEquals(5.0, SNConfig.getSeed_ic(),0);
        assertEquals(3600,SNConfig.getDiffTurn_ic(),0);
        assertEquals("random",SNConfig.getStrategy_ic());
        assertEquals(0.7,SNConfig.getDiffusionProbability_ic(),0);
        assertEquals(0.05,SNConfig.getStandardDeviation_ic(),0);

        //LT model
        assertEquals(20.0, SNConfig.getSeed_lt(),0);
        assertEquals(7200,SNConfig.getDiffTurn_lt(),0);
        assertEquals("random",SNConfig.getStrategy_lt());
        assertEquals("guassian",SNConfig.getDiffusionThresholdType_lt());
//		assertEquals(0.16,SNConfig.getDiffusionProbability_ic(),0);
        assertEquals(0.025,SNConfig.getStandardDeviation_lt(),0);
        assertEquals(0.2,SNConfig.getMeanLowPanicThreshold_lt(),0.0);

    }

    @Test
    public void testMultipleDiffusionModelsSingleInstance_WithDS(){

        Global.setRandomSeed(4711); // deterministic results for testing

        DataServer ds = DataServer.getInstance("multiple"); //use a different dataserver for each test case, o.w mvn tests fail

        SocialNetworkDiffusionModel testSN = new SocialNetworkDiffusionModel(testConfigFile,ds);
        testSN.setupSNConfigsAndLogs();
        testSN.printSNModelconfigs();
        SNUtils.randomAgentMap(testSN,100,1000);
        testSN.genNetworkAndDiffModels();



        SNUtils.setEndSimTime(3600*8);
        testSN.getDataServer().setTime(0.0);
  //      double shortestStep = testSN.getShortestTimeStepOfAllDiffusionModels();
        testSN.getDataServer().setTimeStep(30);


//        for(DiffModel model:testSN.getDiffModels()){
//            model.recordCurrentStepSpread(testSN.getDataServer().getTime());
//            model.setTimeForNextStep();
//        }
        testSN.getDataServer().stepTime();

        while(testSN.getDataServer().getTime() <= SNUtils.getEndSimTime()) {
            // sn.stepDiffusionProcess();
            testSN.stepDiffusionModels(testSN.getDataServer().getTime()); //diffuseContent();
            testSN.getDataServer().stepTime();

        }


       testSN.finish(); // stop models properly, write data outputs

        ICModelDataCollector ic_dc = (ICModelDataCollector) testSN.getDiffModels()[0].getDataCollector();
        assertEquals(100, ic_dc.getTotalInactiveAgents(testSN) + ic_dc.getAdoptedAgentCountForContent(testSN,"content_mult_x_ic"));
        assertEquals(93,  ic_dc.getAdoptedAgentCountForContent(testSN,"content_mult_x_ic"));

        LTModelDataCollector lt_dc = (LTModelDataCollector) testSN.getDiffModels()[1].getDataCollector();
        assertEquals(99,  lt_dc.getFinalActiveAgents("content_mult_x_lt"));

        /*
        time	content_mult_ic
        0.0		5
        3600.0		19
        7200.0		36
        10800.0		47
        14400.0		60
        18000.0		76
        21600.0		82
        25200.0		92
        28800.0		93
         */

        /*
        time	content_mult_lt
    0.0		20
    7200.0		51
    14400.0		78
    21600.0		96
    28800.0		99
         */
    }


    @Test
    public void testMultipleDiffusionModelsMultipleInstances_WithDS(){

        Global.setRandomSeed(4711); // deterministic results for testing

        DataServer ds = DataServer.getInstance("multiple2"); //use a different dataserver for each test case, o.w mvn tests fail

        SocialNetworkDiffusionModel testSN = new SocialNetworkDiffusionModel(testConfigFile,ds);
        testSN.setupSNConfigsAndLogs();
        SNUtils.randomAgentMap(testSN,100,1000);
        testSN.genNetworkAndDiffModels();

        // add new content instances
        SNConfig.addContentsToRegisterForICModel("content_mult_y_ic");
        SNConfig.addContentsToRegisterForLTModel("content_mult_y_lt");
        for (DiffModel model: testSN.getDiffModels()){
            model.initialise(); // initialise again
        }

        testSN.printSNModelconfigs(); // finally print now



        SNUtils.setEndSimTime(3600*8);
        testSN.getDataServer().setTime(0.0);
//        double shortestStep = testSN.getShortestTimeStepOfAllDiffusionModels();
        testSN.getDataServer().setTimeStep(30);


//        for(DiffModel model:testSN.getDiffModels()){
//            model.recordCurrentStepSpread(testSN.getDataServer().getTime());
//            model.setTimeForNextStep();
//        }
        testSN.getDataServer().stepTime();

        while(testSN.getDataServer().getTime() <= SNUtils.getEndSimTime()) {
            // sn.stepDiffusionProcess();
            testSN.stepDiffusionModels(testSN.getDataServer().getTime()); //diffuseContent();
            testSN.getDataServer().stepTime();

        }


        testSN.finish(); // stop models properly, write data outputs

        ICModelDataCollector ic_dc = (ICModelDataCollector) testSN.getDiffModels()[0].getDataCollector();
        assertEquals(90, ic_dc.getAdoptedAgentCountForContent(testSN,"content_mult_x_ic"));
        assertEquals(76,  ic_dc.getAdoptedAgentCountForContent(testSN,"content_mult_y_ic"));

        LTModelDataCollector lt_dc = (LTModelDataCollector) testSN.getDiffModels()[1].getDataCollector();
        assertEquals(100,  lt_dc.getFinalActiveAgents("content_mult_x_lt"));
        assertEquals(100,  lt_dc.getFinalActiveAgents("content_mult_y_lt"));

       /*
       time	content_mult_y_lt	content_mult_x_lt
        0.0		20		34
        7200.0		54		80
        14400.0		79		98
        21600.0		94		100
        28800.0		100		100
        */

       /*
        time	content_mult_x_ic	content_mult_y_ic
0.0		9		5
3600.0		27		21
7200.0		46		55
10800.0		62		76
14400.0		72		93
18000.0		79		96
21600.0		80		96
25200.0		80		96
28800.0		80		96
        */
    }
}
