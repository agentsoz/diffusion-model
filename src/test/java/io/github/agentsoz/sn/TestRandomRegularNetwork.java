package io.github.agentsoz.sn;

import java.util.HashMap;

import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SocialNetworkDiffusionModel;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.Log;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

import io.github.agentsoz.socialnetwork.RandomRegularNetwork;
import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.util.SNUtils;
import org.slf4j.LoggerFactory;

public class TestRandomRegularNetwork {

	String testConfigFile="./case_studies/hawkesbury/hawkesbury.xml";
	SocialNetworkDiffusionModel snManager = new SocialNetworkDiffusionModel(testConfigFile);
	HashMap<Integer,SocialAgent> agentmap = snManager.agentList;
	int testNodes = 10;
	int testDegree = 2;

//	String logFile =  SNConfig.getNetworkLinksDir() + "/" + "network-vis.log";
	final Logger logger = LoggerFactory.getLogger("");
	
//	@Ignore
	@Before
	//automatically generate a large agent map
	public void setUpRandomAgentMap()
	{
		logger.trace("setting up random agent map.....");
	

		//agent x,y coords are in meters :  UTM uses meters from reference points
//		SNConfig.setConfigFile(SNUtils.getMainConfigFile());
//		SNConfig.readConfig();
		snManager.setupSNConfigsAndLogs();
		SNConfig.setNetworkType(DataTypes.RANDOM_REGULAR);
		SNConfig.addToDiffusionModelsList(DataTypes.ltModel);
		SNUtils.createAgentMapUsingActualCoords(snManager, testNodes);
//		SNUtils.randomAgentMap(snManager, testNodes, 1000);
		
	}
	
//	@Ignore
	@Test
	// test the adjacency matrix generated and other properties of the random network
	// as a matrix (before updating to the agentMap)
	public void testRandomRegularNetworkModel()  
	{
		//int[][] arr =  new int[40000][40000];
		RandomRegularNetwork randRegNet = new RandomRegularNetwork(testNodes,testDegree);
		randRegNet.initAgentArrayList();
		randRegNet.genRandRegNetwork();
//		randRegNet.displayMatrix();
		randRegNet.displayArraylists();
		randRegNet.verifyNetworkArraylist();
	}
	
	// update agent map test
	@Ignore
	@Test
	public void testUpdateAgentMap()  
	{
		RandomRegularNetwork randRegNet = new RandomRegularNetwork(testNodes,testDegree);
		// direct check the method
		randRegNet.genNetworkAndUpdateAgentMap(agentmap);
		
		// customised version

//		randRegNet.initAgentArrayList();
//		randRegNet.genRandRegNetwork();
//		randRegNet.displayArraylists();
//		randRegNet.verifyNetworkArraylist();
//		randRegNet.updateAgentMap(agentmap);
//		randRegNet.verifyUpdatedAgentList(agentmap);
	}
	
	@Ignore
	@Test
	public void printNetworkLinksToFile(){
		RandomRegularNetwork randRegNet = new RandomRegularNetwork(testNodes,testDegree);
		randRegNet.initAgentArrayList();
		randRegNet.genRandRegNetwork();
		randRegNet.displayArraylists();
		randRegNet.verifyNetworkArraylist();
		String fileName = SNConfig.getNetworkLinksDir() + "links.txt";
		randRegNet.writeNetworkLinksToFile(fileName, randRegNet.network);
	}
	
}
