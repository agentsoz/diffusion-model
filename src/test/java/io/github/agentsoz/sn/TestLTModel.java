package sn;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Random;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.LTModel;
import io.github.agentsoz.socialnetwork.Network;
import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.SocialNetworkManager;
import io.github.agentsoz.socialnetwork.util.SNUtils;

public class TestLTModel {

	

	SocialNetworkManager sn_manager = new SocialNetworkManager(SNUtils.getMainConfigFile()); // init SNMan;
	HashMap<Integer,SocialAgent> agentmap = sn_manager.agentList;
	final Logger logger = LoggerFactory.getLogger("");
	LTModel ltModel;
	Random rand  =  new Random();
	
	
	@Before
	public void setUpRandomAgentMap()
	{
		SNConfig.setDiffusionType(DataTypes.ltModel);
		randomAgentMap(5,1000);
		logger.info("random agent map initialised: size {}", sn_manager.agentList.size());
		ltModel = new LTModel(2,3,sn_manager); // random values to create the model
	}
	
	public void randomAgentMap(int nodes, int cordRange) { 
		
		for(int id=0; id < nodes; id++) {
			int x = rand.nextInt(cordRange);
			int y = rand.nextInt(cordRange);
			sn_manager.createSocialAgent(Integer.toString(id));sn_manager.setCords(Integer.toString(id),x,y);
		}
	}
	
	// for both threshold types
	@Ignore
	@Test
	public void testInitialise() { 
		
		SNUtils.setMainConfigFile();
		sn_manager.setupSNConfigs();
		sn_manager.generateDiffModel(); // initialise is already run here

		
		sn_manager.getDiffModel().printConfigParams();
		sn_manager.getDiffModel().printthresholdMap();
		sn_manager.getDiffModel().printPanicValues();

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
		
		
//		agentmap.get(0).setState(DataTypes.HIGH);
//		agentmap.get(0).setPanicLevel(1.0);

		
		// set conifgs
		SNUtils.setMainConfigFile();
		SNConfig.setDiffusionThresholdType("random");
		
		ltModel.initialise(); // no diffusion seed, so initActiveAgents do not run
		ltModel.updatePanicValue(0, 1.0); // call after initialise as threhoslds are not mapped earlier
		sn_manager.getAgentMap().get(0).setIsSeedTrue();
		ltModel.printthresholdMap();

		int turn = 1; 
		while (turn < 4) {
			logger.debug("turn: {}",turn);
			ltModel.ltDiffuse();
			ltModel.printPanicValues();

			
			//logger.debug(" low: {} med: {} high: {}", ltModel.getLowPanicCount(),ltModel.getMedPanicCount(),ltModel.getHighPanicCount());
			if(turn == 2) {
				SNUtils.countLowMedHighAgents(sn_manager);
				assertEquals(2,SNUtils.getLowCt() /*ltModel.getLowPanicCount()*/,1);
				assertEquals(3,SNUtils.getMedCt() /*ltModel.getHighPanicCount()*/,1);
			}
			turn++;
		}
		// turn1: 2-active(0,2), 3-inactive
		//turn2: 3-active(0,2,3), 2-inactive (1=0.0,4=0)
		//turn3: 4-active(0,2,3,4), 1-inactive (1=0.0)
		// network weights are not normalised, therefore panic values can exceed 1.0
	}

	
	@Ignore
	@Test
	public void testIsActive() { 
		SocialAgent testAgent = agentmap.get(0);
		testAgent.setState(DataTypes.MEDIUM);
		assertEquals(true,ltModel.isActive(0)); 
	}
	
	//tested
	@Ignore
	@Test
	public void testConfigsFromFile() { 
		SNUtils.setMainConfigFile();
		sn_manager.setupSNConfigs();
		sn_manager.generateDiffModel();
		sn_manager.getDiffModel().initialise();
		sn_manager.getDiffModel().printConfigParams();
	}

	
//	@Ignore
	@Test
	public void testLowHighThresholds() {
		ltModel.assignGaussianDistThresholds(0, 0.2, 0, 0.1);
		ltModel.printthresholdMap();
	}
}
