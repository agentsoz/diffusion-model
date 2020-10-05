package io.github.agentsoz.sn;

import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.socialnetwork.ICModel;
import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SocialNetworkManager;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.SNUtils;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class TestDyanmicSeedForICModel {

    public static String testConfigFile = "./case_studies/hawkesbury/test_ICModel_dynamic_seed.xml";
    HashMap<Integer, ArrayList<String>> dynamicSeedMap;

/*
differences with random seeding:
at t=0, no one is active.
percepts input file, set seeding strategy to dynamic.
 */

// #FIXME this test fails in maven command line.
 //   @Test
    public void testDyanmicSeed() {
        DataServer ds = DataServer.getServer("test100"); // use a different dataserver for each test case, o.w mvn tests fail
        SocialNetworkManager sn = new SocialNetworkManager(testConfigFile);
        sn.setupSNConfigsAndLogs();
        sn.printSNModelconfigs();
        SNUtils.randomAgentMap(sn,50000,1000);

        // setting up the dynamic seed
        dynamicSeedMap = new HashMap<Integer,ArrayList<String>>(); // init dynamic seed map
        SNUtils.readAndStoreDynamicSeed(dynamicSeedMap);


        sn.genNetworkAndDiffModels();
        ICModel testIC = (ICModel) sn.getDiffModel();

        //test IC model configs
        testIC.registerContentIfNotRegistered("blockageInfo",DataTypes.LOCAL);

        //setup sim configs
        SNUtils.setEndSimTime(3600*17); // 61200 secs
        ds.setTime(0.0);
        testIC.recordCurrentStepSpread(0.0); //record seed spread
        ds.setTimeStep(SNConfig.getDiffTurn_ic());

        while(ds.getTime() <= SNUtils.getEndSimTime()) {

            if(ds.getTime() % SNConfig.getDiffTurn_ic() == 0 && ds.getTime() > 0){ // avoid ds = 0.0

                ArrayList<String> idListForStep = dynamicSeedMap.get((int)ds.getTime());
                if( idListForStep != null && idListForStep.size() != 0){ // if there are agents that need to be activated
                    HashMap<String,String[]> perceptData = new HashMap<String, String[]>();
                    perceptData.put("blockageInfo", idListForStep.toArray(new String[idListForStep.size()]));
                    testIC.updateSocialStatesFromLocalContent(perceptData);// assign dynamic seeding first

                }
            }


            sn.diffuseContent();
            testIC.recordCurrentStepSpread(ds.getTime());
            ds.stepTime();

        }

        testIC.finish();
        testIC.getDataCollector().writeSpreadDataToFile();

        assertEquals(452, testIC.getDataCollector().getAdoptedAgentCountForContent(sn,"blockageInfo"));
        assertEquals(49548, testIC.getDataCollector().getTotalInactiveAgents(sn));


    }

}
