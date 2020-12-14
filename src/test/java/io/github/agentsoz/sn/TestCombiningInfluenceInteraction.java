package io.github.agentsoz.sn;

import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.socialnetwork.*;
import io.github.agentsoz.socialnetwork.datacollection.ICModelDataCollector;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.SNUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;


import static io.github.agentsoz.socialnetwork.util.DataTypes.*;
import static org.junit.Assert.assertEquals;

public class TestCombiningInfluenceInteraction {

    public static String testConfigFile = "case_studies/hawkesbury/test_interaction_combine_influences.xml";





    @Test
    public void seedAB_SpreadABC() { //single adoption


        DataServer dataServer = DataServer.getInstance("test_ici1");
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
        String[] compContentList = {CONTENT_A, CONTENT_B, CONTENT_C};
        ici.setCompeteContentList(Arrays.asList(compContentList));

        //set activation probabilities
        double probA = 0.5;
        double probB = 0.5;
        double probC = 0.5;
        ici.getProbMap().put(CONTENT_A, probA);
        ici.getProbMap().put(CONTENT_B, probB);
        ici.getProbMap().put(CONTENT_C, probC);



        ici.initRandomSeed(15, CONTENT_A); // initialise a random seed for a specific content
        ici.initRandomSeed(15, CONTENT_B); // initialise a random seed for a specific content
        //ici.initRandomSeed(15, CONTENT_B); // initialise a random seed for a specific content


        ici.recordCurrentStepSpread(0.0); //record seed spread

        //setup sim configs
        SNUtils.setEndSimTime(1 * 8);
        sn.getDataServer().setTime(0.0);
        sn.getDataServer().setTimeStep(1.0);

        while (sn.getDataServer().getTime() <= SNUtils.getEndSimTime()) {
            // sn.stepDiffusionProcess();
            ici.icDiffusion(COMBINE); //diffuseContent();
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
        String[] compContentList = {CONTENT_A, CONTENT_B, CONTENT_C};
        ici.setCompeteContentList(Arrays.asList(compContentList));

        //set activation probabilities
        double probA = 0.5;
        double probB = 0.5;
 //       double probC = 0.5;
        ici.getProbMap().put(CONTENT_A, probA);
        ici.getProbMap().put(CONTENT_B, probB);

        ici.initRandomSeed(10, CONTENT_C); // initialise a random seed for a specific content

        ici.recordCurrentStepSpread(0.0); //record seed spread

        //setup sim configs
        SNUtils.setEndSimTime(1 * 8);
        sn.getDataServer().setTime(0.0);
        sn.getDataServer().setTimeStep(1.0);

        while (sn.getDataServer().getTime() <= SNUtils.getEndSimTime()) {
            // sn.stepDiffusionProcess();

            ici.icDiffusion(SPLIT);
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

}
