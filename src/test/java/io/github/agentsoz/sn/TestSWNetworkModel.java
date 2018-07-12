package io.github.agentsoz.sn;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SWNetwork;
import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.SocialNetworkManager;
import io.github.agentsoz.socialnetwork.util.SNUtils;
import io.github.agentsoz.socialnetwork.util.Log;

public class TestSWNetworkModel {

	/* FIXME generate sw network using actual geo locations
	 *  The x,y coords should be in meters (easting, northing)
	 *  tested testcases : 
	 *  higher distance with agents having no links
	 *  small area , 5000 agents, avg degree? exp =2 gen = 2.5 - good
	 *    agents -40,000 distance 10,000. -exp deg = 5 gen deg = 5.8
	 *    	why? because degree is also dependant on the x,y coords and distance range
	 * 
	 */
	
	String logFile =  SNUtils.getNetworkLinksDir() + "/" + "network-vis.log";
	final Logger logger = Log.createLogger("", logFile);

	
	SocialNetworkManager snManager = new SocialNetworkManager(SNUtils.getMainConfigFile());
	HashMap<Integer,SocialAgent> agentmap = snManager.agentList;
	Random random = new Random();
	
	double neiDistance = 0.8;
	double rewireProb = 0.0;
	int avgDegree = 3; 
	int numAgents = 2000;
	
	@Before
	//set the logger from the logger class
	public void setLogger()
	{
		// Create the logger
		//final Logger logger  = Log.createLogger("", Log.logFile);
	}
	
	

	//automatically generate a large agent map
	@Before
	public void setUpAgentMap()
	{

		
		//1. Random cords - agent x,y coords are in meters :  UTM uses meters from reference points
//		createAgentMap(20000, 5000);
		SNConfig.setConfigFile(SNUtils.getMainConfigFile());
		SNConfig.readConfig();
		SNConfig.setNetworkType(DataTypes.SMALL_WORLD);
		snManager.setupSNConfigs();
		SNUtils.createAgentMapUsingActualCoords(snManager, numAgents);


	}
	
	
	@Ignore
	@Test
	// to test the matrix without the agent map
	public void testSWNetwork()  
	{
		SWNetwork swNet = new SWNetwork(2.0, 5, 0.1);
		swNet.initialiseNeighbourLists(agentmap);
		swNet.genNeighbourhoodNetwork(agentmap);
		//swNet.verifyNetwork();
//		swNet.displayMatrix();
		swNet.rewireLinksToRandomAgent(agentmap);
		swNet.verifyNetwork();
//		swNet.displayMatrix();

		Assert.assertTrue(swNet.getRewiredLinksCount() > 0);
		Assert.assertEquals(5, swNet.getGenAvgDegree(),0.1); // checking generated avg degree

	}
	
	public void createAgentMap(int nodes, int cordRange) { 
		
		logger.trace("setting up random agent map.....");
		
		for(int id=0; id < nodes; id++) {
			int x = random.nextInt(cordRange);
			int y = random.nextInt(cordRange);
			snManager.createSocialAgent(Integer.toString(id));snManager.setCords(Integer.toString(id),x,y);

		}
				
		
	}
	
	@Ignore
	@Test
	// testing the updated agentmap
	/*TESTED
	 *  treemap order  incremental
	 *  rewireLinksToRandomAgent - no  neighbours ? neighbours == 0? tested
	 *  in rewire method - degree is not maintained, therefore agents may have higher degrees
	 */
	public void testUpdatedAgentMap()  
	{

		SocialNetworkManager sn = new SocialNetworkManager(SNUtils.getMainConfigFile());
		SNUtils.createAgentMapUsingActualCoords(sn, 100);


		SWNetwork swNet = new SWNetwork(2.0, 2, 1.0);
		swNet.initialiseNeighbourLists(sn.getAgentMap());
		swNet.genNeighbourhoodNetwork(sn.getAgentMap());
		swNet.verifyNetwork();
//		swNet.displayMatrix();
//		swNet.rewireLinksToOutsideDistAgent(agentmap);
		swNet.rewireLinksToRandomAgent(sn.getAgentMap());
		swNet.verifyNetwork();
//		swNet.displayMatrix();
		swNet.updateAgentMap(sn.getAgentMap());
		swNet.verifyUpdatedAgentList(sn.getAgentMap());
//		swNet.displayAgentMap(sn.getAgentMap());

		Assert.assertEquals(swNet.getNeihgbourLinkCount() * 2, swNet.getLinkCountInAgentMaps());
	}
	
	
	@Ignore
	@Test
	public void testNormalisedNetwork() {
		SocialNetworkManager sn = new SocialNetworkManager(SNUtils.getMainConfigFile());
		SNUtils.createAgentMapUsingActualCoords(sn, 100);

		SWNetwork swNet = new SWNetwork(4.0, 5, 1.0);
		SNConfig.setNormaliseRandNetwork(true);
		swNet.setupConfigs();
		swNet.genNetworkAndUpdateAgentMap(sn.getAgentMap());
		swNet.printNetworkWegihts(sn.getAgentMap());

		assertEquals(true,swNet.isProperlyNormalised(sn.getAgentMap()));
	}
	
	@Ignore
	@Test
	/*  test method of SN-Manager
	 *  tested 40,000 nodes =??
	 *  the probabililty can be tested :  if rewireProb = 0.25 && if tot links = 100, re-wired links = 25
	 */
	public void testSNManagerMethod()  
	{
		//method1 -  set configs from the config file
		SNUtils.setMainConfigFile();
		
//		snManager.setupSNConfigs(); 
		
		//method2 - set configs using setters -  should set after initSNModel as the configs are read again from the file in initSNmodel method
		SNConfig.setNetworkType(DataTypes.SMALL_WORLD);
		SNConfig.setSWNetNeiDistance(0.5);
		SNConfig.setSWNetAvgLinks(6);
		SNConfig.setSWNetRewireProb(1.0);
		
//		printAgentCoords();
		
		snManager.generateSocialNetwork();
//		snManager.initSWNetwork(2.0, 4, 0.25);

		Assert.assertEquals(snManager.getNetworkModel().getNeihgbourLinkCount() * 2, snManager.getNetworkModel().getLinkCountInAgentMaps());

	}
	
	public void printAgentCoords() {
		logger.debug("retreiving assigned corrdinates from the agentmap..");

		for(int i=0;i<agentmap.size(); i++) {
			logger.debug("x: {} y: {}",snManager.getAgentMap().get(i).getX(),snManager.getAgentMap().get(i).getY()); 

		}
	}
	
	
//	@Ignore
	@Test
	public void printNetworkLinksToFile(){
		
		SWNetwork swNet = new SWNetwork(neiDistance, avgDegree, rewireProb);
		swNet.initialiseNeighbourLists(agentmap);
		swNet.genNeighbourhoodNetwork(agentmap);
		swNet.verifyNetwork();
//		swNet.displayMatrix();
		swNet.rewireLinksToRandomAgent(agentmap);
		swNet.verifyNetwork();
		swNet.updateAgentMap(agentmap);
		swNet.verifyNeighbourDistances(agentmap, neiDistance);
		String fileName = SNUtils.getNetworkLinksDir() + "links.txt";
		swNet.writeNetworkLinksToFile(fileName);
	}
}
