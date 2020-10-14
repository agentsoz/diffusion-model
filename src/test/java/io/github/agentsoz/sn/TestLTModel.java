package io.github.agentsoz.sn;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.socialnetwork.*;
import io.github.agentsoz.socialnetwork.util.Global;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.SNUtils;

public class TestLTModel {


	public static String testConfigFile = "case_studies/hawkesbury/test_LTModel.xml";
	SocialNetworkModel sn_manager = new SocialNetworkModel(testConfigFile); // init SNMan;
	HashMap<Integer,SocialAgent> agentmap = sn_manager.agentList;
	final Logger logger = LoggerFactory.getLogger("");
	LTModel ltModel;





	@Test
	//   @Ignore
	public void testConfigs(){
		SocialNetworkModel testSN = new SocialNetworkModel(testConfigFile);
		testSN.setupSNConfigsAndLogs();
		testSN.printSNModelconfigs();

		assertEquals(20.0, SNConfig.getSeed_lt(),0);
		assertEquals(3600,SNConfig.getDiffTurn_lt(),0);
		assertEquals("random",SNConfig.getStrategy_lt());
		assertEquals("guassian",SNConfig.getDiffusionThresholdType_lt());
//		assertEquals(0.16,SNConfig.getDiffusionProbability_ic(),0);
		assertEquals(0.089,SNConfig.getStandardDeviation_lt(),0);
		assertEquals(0.391,SNConfig.getMeanLowPanicThreshold_lt(),0.0);
		assertEquals(SNConfig.getOutputFilePathOFLTModel(),"./test/output/diffusion_testing_lt.out");

	}

	@Before
	public void setUpRandomAgentMap()
	{

		sn_manager.setupSNConfigsAndLogs();
		SNUtils.randomAgentMap(sn_manager,5,1000);
		logger.info("random agent map initialised: size {}", sn_manager.agentList.size());
		ltModel = new LTModel(2,3,sn_manager); // random values to create the model
	}
	

	
	// for both threshold types
	@Ignore
	@Test
	public void testInitialise() { 
		
	//	SNUtils.setMainConfigFile();
		SocialNetworkModel snModel = new SocialNetworkModel(testConfigFile);
		snModel.setupSNConfigsAndLogs();
		snModel.generateDiffusionModels(); // initialise is already run here


		LTModel model = (LTModel) snModel.getDiffModels()[0];
		model.printConfigParams();
		model.printthresholdMap();
		model.printContentValues();

	}
	

	
	//tested
	//@Ignore
	@Test
	public void testInitActiveAgentsAndLtDiffuse() { 

		// setup the network and update agentmap
		Network net = new Network();
		net.createLinkWithGivenWeight(0, 2, 1.0, agentmap); 
		net.createLinkWithGivenWeight(1, 2,0.0, agentmap);
		net.createLinkWithGivenWeight(2, 3, 1.0, agentmap);
		net.createLinkWithGivenWeight(3, 4, 1.0, agentmap);
		
		

		SNConfig.setDiffusionThresholdType_lt("random");


		SNConfig.getContentsToRegisterForLTModel().clear(); // clear contents in config file
		SNConfig.getContentsToRegisterForLTModel().add("test");
		ltModel.initialise(); // no diffusion seed, so initActiveAgents do not run

       	sn_manager.getAgentMap().get(0).setAsPartOfTheSeed("test",true);
		ltModel.updateContentValue("test",0, 1.0); // call after initialise as threhoslds are not mapped earlier
		ltModel.getSeedMap().get("test").put(0,1.0);

		ltModel.printthresholdMap();

		int turn = 1; 
		while (turn < 4) {
			logger.debug("turn: {}",turn);
			ltModel.ltDiffuse();
			ltModel.printContentValues();

			
			//logger.debug(" low: {} med: {} high: {}", ltModel.getLowPanicCount(),ltModel.getMedPanicCount(),ltModel.getHighPanicCount());
			if(turn == 3) {
				//ltModel.getDataCollector().countLowMedHighAgents(sn_manager,turn);
				ltModel.recordCurrentStepSpread(turn);
				assertEquals(1,ltModel.getDataCollector().getInactiveCount(3,"test"),1);
				assertEquals(4,ltModel.getDataCollector().getActiveCount(3,"test"),1);
			}
			turn++;
		}
		// turn1: 2-active(0,2), 3-inactive
		//turn2: 3-active(0,2,3), 2-inactive (1=0.0,4=0)
		//turn3: 4-active(0,2,3,4), 1-inactive (1=0.0)
		// network weights are not normalised, therefore panic values can exceed 1.0
	}

	
	//@Ignore
	@Test
	public void testIsActive() { 
		SocialAgent testAgent = agentmap.get(0);
		testAgent.setState("test",DataTypes.MEDIUM);
		assertEquals(true,ltModel.isActive("test",0));
	}
	
	//tested
	@Ignore
	@Test
	public void testConfigsFromFile() { 
		//SNUtils.setMainConfigFile();
		sn_manager.setupSNConfigsAndLogs();
		sn_manager.generateDiffusionModels();
		sn_manager.getDiffModels()[0].initialise();
		sn_manager.getDiffModels()[0].printConfigParams();
	}

	
	@Ignore
	@Test
	public void testLowHighThresholds() {
		ltModel.registerContentIfNotRegistered("test2",DataTypes.LOCAL);
		ltModel.assignGaussianDistThresholds("test2",0, 0.2, 0, 0.1);
		ltModel.printthresholdMap();
	}

	@Test
	public void testWriteFile(){ // for multiple infleunces

		Global.setRandomSeed(4711); // deterministic results for testing
		//  String outFile = "./test/output/diffusion.out";

		DataServer ds = DataServer.getServer("test4"); //use a different dataserver for each test case, o.w mvn tests fail
		SocialNetworkModel sn = new SocialNetworkModel(testConfigFile,ds);
		sn.setupSNConfigsAndLogs();
		SNUtils.randomAgentMap(sn, 100, 1000);

		sn.initWithoutSocialAgentsMap();
		//SNConfig.setDiffTurn_lt(60);
		//SNConfig.setSeed_lt(20);
		//SNConfig.printDiffusionConfigs();
		LTModel lt = (LTModel) sn.getDiffModels()[0];
		//lt.initRandomSeed("default");
		//lt.initRandomSeed("contentX"); // initialise a random seed for a specific content
		//lt.initRandomSeed("contentY"); // initialise a random seed for a specific content

		lt.recordCurrentStepSpread(0.0); //record seed spread

		//setup sim configs
		SNUtils.setEndSimTime(3600*8);
		sn.getDataServer().setTime(0.0);
		sn.getDataServer().setTimeStep(SNConfig.getDiffTurn_lt());

		while(sn.getDataServer().getTime() <= SNUtils.getEndSimTime()) {
			// sn.stepDiffusionProcess();
			lt.step(); //diffuseContent();
			sn.getDataServer().stepTime();
			lt.recordCurrentStepSpread(sn.getDataServer().getTime());

		}

		//end of simulation, now print to file
		lt.finish();
		//ICModelDataCollector dc = new ICModelDataCollector();
		lt.getDataCollector().writeSpreadDataToFile();

		//verify total agent count, then specific active agent count for each content
		//assertEquals(106, dc.getTotalInactiveAgents(sn) + dc.getAdoptedAgentCountForContent(sn,"contentX") + lt.getDataCollector().getAdoptedAgentCountForContent(sn,"contentY"));
		assertEquals(66,  lt.getDataCollector().getActiveCount(sn.getDataServer().getTime(),"contentX"),0);
		assertEquals(67,  lt.getDataCollector().getActiveCount(sn.getDataServer().getTime(),"contentY"),0);
		assertEquals(33, lt.getDataCollector().getInactiveCount(sn.getDataServer().getTime(),"contentY"),0);

	}
}
