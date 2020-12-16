package io.github.agentsoz.sn;

import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.socialnetwork.*;
import io.github.agentsoz.socialnetwork.datacollection.ICModelDataCollector;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.SNUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;


import static io.github.agentsoz.socialnetwork.util.DataTypes.*;
import static org.junit.Assert.assertEquals;

public class TestCombiningInfluenceInteraction {

    public static String testConfigFile = "case_studies/hawkesbury/test_interaction_combine_influences.xml";





    @Test
    public void seedAB_SpreadABC() { //single adoption


        DataServer dataServer = DataServer.getInstance("test_ici1");
        SocialNetworkDiffusionModel sn = new SocialNetworkDiffusionModel(testConfigFile, dataServer);
        sn.setupSNConfigsAndLogs();
        SNUtils.randomAgentMap(sn, 10, 1000);

        //gen social network
        sn.generateSocialNetwork();
        sn.printSNModelconfigs();

        //create  ICIModel and add to diffusion models array
        ICModelForInteractingInfluences ici = new ICModelForInteractingInfluences(sn, SNConfig.getDiffTurn_ic(), SNConfig.getDiffusionProbability_ic());
        ici.initialise();

        ici.registerContentIfNotRegistered(CONTENT_A, DataTypes.LOCAL);
        ici.registerContentIfNotRegistered(CONTENT_B, DataTypes.LOCAL);
        ici.registerContentIfNotRegistered(CONTENT_C, DataTypes.LOCAL);

        //define interaction
//        String[] compContentList = {CONTENT_A, CONTENT_B, CONTENT_C};
//        ici.setCompeteContentList(Arrays.asList(compContentList));

        //set activation probabilities
        double probA = 0.5;
        double probB = 0.5;
        double probC = 0.5;

        //A,B
        double probAB_A = 0.2;
        double probAB_B = 0.2;
        double probAB_C = 0.4;
        double probAB_none = 0.2;

        double probBC_B = 0.3;
        double probBC_C = 0.4;
        double probBC_none = 0.3;

        //A,C
        double probAC_A = 0.5;
        double probAC_C = 0.5;
        double probAC_none = 0.2;

        HashMap<String,HashMap<String,Double>> probMap = createProbabilityMapForAllSituations(
                0.5,0.5,0.5,
                0.2,0.2,0.2,0.4,
                0.3,0.4,0.3,
                0.3,0.4,0.3
        );

        ici.initRandomSeed(20, CONTENT_A); // initialise a random seed for a specific content
        ici.initRandomSeed(20, CONTENT_B); // initialise a random seed for a specific content


        ici.recordCurrentStepSpread(0.0); //record seed spread

        //setup sim configs
        SNUtils.setEndSimTime(1 * 8);
        sn.getDataServer().setTime(0.0);
        sn.getDataServer().setTimeStep(1.0);

        while (sn.getDataServer().getTime() <= SNUtils.getEndSimTime()) {
            // sn.stepDiffusionProcess();
            ici.icDiffusion(probMap); //diffuseContent();
            sn.getDataServer().stepTime();
            ici.recordCurrentStepSpread(sn.getDataServer().getTime());
        }

        //end of simulation, now print to file
        ici.finish();
        ICModelDataCollector dc = new ICModelDataCollector();
        String outfile= "./test/output/diffusion_testing_interaction_combine_ic.out";
        ici.getDataCollector().writeSpreadDataToFile(outfile);

        assertEquals(sn.getAgentMap().size(), dc.getTotalInactiveAgents(sn) + dc.getAdoptedAgentCountForContent(sn,CONTENT_A) + dc.getAdoptedAgentCountForContent(sn,CONTENT_B) + dc.getAdoptedAgentCountForContent(sn,CONTENT_C));

    }

    @Test
    public void seedC_SpreadAB() {


        DataServer dataServer = DataServer.getInstance("test_ici");
        SocialNetworkDiffusionModel sn = new SocialNetworkDiffusionModel(testConfigFile, dataServer);
        sn.setupSNConfigsAndLogs();
        SNUtils.randomAgentMap(sn, 1000, 1000);

        //gen social network
        sn.generateSocialNetwork();
        sn.printSNModelconfigs();

        //create  ICIModel and add to diffusion models array
        ICModelForInteractingInfluences ici = new ICModelForInteractingInfluences(sn, SNConfig.getDiffTurn_ic(), SNConfig.getDiffusionProbability_ic());
        ici.initialise();

        ici.registerContentIfNotRegistered(CONTENT_A, DataTypes.LOCAL);
        ici.registerContentIfNotRegistered(CONTENT_B, DataTypes.LOCAL);
        ici.registerContentIfNotRegistered(CONTENT_C, DataTypes.LOCAL);

        //define interaction
   //     String[] compContentList = {CONTENT_A, CONTENT_B, CONTENT_C};
   //     ici.setCompeteContentList(Arrays.asList(compContentList));

        //get  probability choice map
        HashMap<String,HashMap<String,Double>> probMap2 = createProbabilityMapForAllSituations(
                0.5,0.5,0.5,
                0.2,0.2,0.2,0.4,
                0.3,0.4,0.3,
                0.3,0.4,0.3
        );


        ici.initRandomSeed(10, CONTENT_C); // initialise a random seed for a specific content

        ici.recordCurrentStepSpread(0.0); //record seed spread

        //setup sim configs
        SNUtils.setEndSimTime(1 * 8);
        sn.getDataServer().setTime(0.0);
        sn.getDataServer().setTimeStep(1.0);

        while (sn.getDataServer().getTime() <= SNUtils.getEndSimTime()) {
            // sn.stepDiffusionProcess();

            ici.icDiffusion(probMap2);
            sn.getDataServer().stepTime();
            ici.recordCurrentStepSpread(sn.getDataServer().getTime());
        }

        //end of simulation, now print to file
        ici.finish();
        ICModelDataCollector dc = new ICModelDataCollector();
        String outfile= "./test/output/diffusion_testing_interaction_split_ic.out";
        ici.getDataCollector().writeSpreadDataToFile(outfile);

        assertEquals(sn.getAgentMap().size(), dc.getTotalInactiveAgents(sn) + dc.getAdoptedAgentCountForContent(sn,CONTENT_A) + dc.getAdoptedAgentCountForContent(sn,CONTENT_B) + dc.getAdoptedAgentCountForContent(sn,CONTENT_C));

    }

    /*
    Test: values are initailised correctly for 6 situations.
     */

    public HashMap<String,HashMap<String,Double>> createProbabilityMapForAllSituations(
            /* String A, */ double probA, //A
           /* String B, */ double probB, // B
           /* String C,*/  double probC, // C
           /* String AB_A,*/ double probAB_A, // A,B
           /* String AB_B,*/ double probAB_B,
           /* String AB_C,*/ double probAB_C,
           /* String AB_none,*/ double probAB_none,
           /* String AC_A,*/ double probAC_A, //A,C
           /* String AC_C,*/ double probAC_C,
            /*String AC_none,*/ double probAC_none,
           /* String BC_A,*/ double probBC_B, //B,C
           /* String BC_C,*/ double probBC_C,
           /* String BC_none,*/ double probBC_none
    ){
        HashMap<String,HashMap<String,Double>> probMapForAllSituations = new HashMap<String,HashMap<String,Double>>();

        //contentA
        HashMap<String,Double> contentAProbMap = new HashMap<String,Double>();
        contentAProbMap.put(CONTENT_A,probA);
        probMapForAllSituations.put(CONTENT_A,contentAProbMap);

        //contentB
        HashMap<String,Double> contentBProbMap = new HashMap<String,Double>();
        contentBProbMap.put(CONTENT_B,probB);
        probMapForAllSituations.put(CONTENT_B,contentBProbMap);

        //content C
        HashMap<String,Double> contentCProbMap = new HashMap<String,Double>();
        contentCProbMap.put(CONTENT_C,probC);
        probMapForAllSituations.put(CONTENT_C,contentCProbMap);

        //A,B
        HashMap<String,Double> contentABProbMap = new HashMap<String,Double>();
        contentABProbMap.put(CONTENT_A,probAB_A);
        contentABProbMap.put(CONTENT_B,probAB_B);
        contentABProbMap.put(CONTENT_C,probAB_C);
        contentABProbMap.put(NOSPREAD,probAB_none);
        probMapForAllSituations.put(CONTENTSAB,contentABProbMap);

        //A,C
        HashMap<String,Double> contentACProbMap = new HashMap<String,Double>();
        contentACProbMap.put(CONTENT_A,probAC_A);
        contentACProbMap.put(CONTENT_C,probAC_C);
        contentACProbMap.put(NOSPREAD,probAC_none);
        probMapForAllSituations.put(CONTENTSAC,contentACProbMap);

        //B,C
        HashMap<String,Double> contentBCProbMap = new HashMap<String,Double>();
        contentBCProbMap.put(CONTENT_B,probBC_B);
        contentBCProbMap.put(CONTENT_C,probBC_C);
        contentBCProbMap.put(NOSPREAD,probBC_none);
        probMapForAllSituations.put(CONTENTSBC,contentBCProbMap);

        return probMapForAllSituations;


    }

}
