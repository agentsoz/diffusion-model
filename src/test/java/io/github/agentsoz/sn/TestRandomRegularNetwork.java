package io.github.agentsoz.sn;

import java.util.HashMap;

import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.Log;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

import io.github.agentsoz.socialnetwork.RandomRegularNetwork;
import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.SocialNetworkManager;
import io.github.agentsoz.socialnetwork.util.SNUtils;

public class TestRandomRegularNetwork {

	SocialNetworkManager snManager = new SocialNetworkManager(SNUtils.getMainConfigFile());
	HashMap<Integer,SocialAgent> agentmap = snManager.agentList;
	int testNodes = 10;
	int testDegree = 2;

	String logFile =  SNUtils.getNetworkLinksDir() + "/" + "network-vis.log";
	final Logger logger = Log.createLogger("", logFile);
	
//	@Ignore
	@Before
	//automatically generate a large agent map
	public void setUpRandomAgentMap()
	{
		logger.trace("setting up random agent map.....");
	

		//agent x,y coords are in meters :  UTM uses meters from reference points
		SNConfig.setDiffusionType(DataTypes.ltModel);
		SNUtils.createAgentMapUsingActualCoords(snManager, testNodes);
//		SNUtils.randomAgentMap(snManager, testNodes, 1000);
		
	}
	
	@Ignore
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
	
//	@Ignore
	@Test
	public void printNetworkLinksToFile(){
		RandomRegularNetwork randRegNet = new RandomRegularNetwork(testNodes,testDegree);
		randRegNet.initAgentArrayList();
		randRegNet.genRandRegNetwork();
		randRegNet.displayArraylists();
		randRegNet.verifyNetworkArraylist();
		String fileName = SNUtils.getNetworkLinksDir() + "links.txt";
		randRegNet.writeNetworkLinksToFile(fileName, randRegNet.network);
	}
	
}
