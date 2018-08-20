package io.github.agentsoz.sn;

import static org.junit.Assert.*;


import java.util.HashMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.RandomNetwork;
import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.SocialNetworkManager;
import io.github.agentsoz.socialnetwork.util.SNUtils;


public class TestRandomNetworkModel {

	final Logger logger = LoggerFactory.getLogger("");
	
	SocialNetworkManager snManager = new SocialNetworkManager(SNConfig.getDefaultConfigFile());
	HashMap<Integer,SocialAgent> agentmap = snManager.agentList;

	@Ignore
//	@Before  // Regardless of @Ignore, this method will run
	public void setUp()
	{
		logger.info("setting up to test Random network model.....");
		
		//agent x,y coords are in meters :  UTM uses meters from reference points
		snManager.createSocialAgent("0");snManager.setCords("0", 0, 0);
		snManager.createSocialAgent("9");snManager.setCords("9", 2000, 2000); // 2.82
		snManager.createSocialAgent("8");snManager.setCords("8", 3000, 3000); // 4.24
		snManager.createSocialAgent("7");snManager.setCords("7", 5000, 5000);  //7.07
		snManager.createSocialAgent("6");snManager.setCords("6", 7000, 7000);
		
		
	}
	
//	@Ignore
	@Before
	//automatically generate a large agent map
	public void setUpRandomAgentMap()
	{
		logger.trace("setting up random agent map.....");
	

		//agent x,y coords are in meters :  UTM uses meters from reference points
		
		SNUtils.randomAgentMap(snManager,5, 100000);
		
	}
	
	@Ignore
	@Test
	// test the adjacency matrix generated and other properties of the random network
	// as a matrix (before updating to the agentMap)
	public void testRandomNetworkModel()  
	{
		RandomNetwork randNet = new RandomNetwork(5,5);
		randNet.genRandomNetwork();
	//	randNet.displayMatrix();
		randNet.displayArraylists();
		randNet.verifyNetwork();
	}
	
	
	@Ignore
	@Test
	// test the agentmap
	public void testUpdateAgentMap()  
	{
		//int[][] arr =  new int[40000][40000];
		RandomNetwork randNet = new RandomNetwork(agentmap.size(),2);
		randNet.genNetworkAndUpdateAgentMap(agentmap);
	}
	
//	@Ignore
	@Test
	/*  test method of SN-Manager
	 *  tested 40,000 nodes = avg degree -ok degrees from 0 to 12. takes  around 3-4mins for the whole process
	 * 
	 */
	public void testSNManagerMethod()  
	{
		// method1
		//SNUtils.setMainConfigFile();
		snManager.setupSNConfigs();
		
		//method2
		SNConfig.setNetworkType(DataTypes.RANDOM);
		SNConfig.setRandomNetAvgLinks(6);
		SNConfig.setNormaliseRandNetwork(false);
		
		snManager.generateSocialNetwork();
	}
	
	@Ignore
	@Test
	public void testNormalisedNetwork() {
		RandomNetwork randNet = new RandomNetwork(5,3);
		SNConfig.setNormaliseRandNetwork(true);
		randNet.setupConfigs();
		randNet.genNetworkAndUpdateAgentMap(agentmap);
		randNet.printNetworkWegihts(agentmap);
		assertEquals(true,randNet.isProperlyNormalised(agentmap));
	}
	

}
