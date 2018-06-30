package sn;

import java.util.HashMap;
import java.util.Random;

import io.github.agentsoz.socialnetwork.util.DataTypes;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.agentsoz.socialnetwork.Network;
import io.github.agentsoz.socialnetwork.RandomRegularNetwork;
import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SWNetwork;
import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.SocialNetworkManager;
import io.github.agentsoz.socialnetwork.util.SNUtils;

public class TestNormaliseWeights {

	final Logger logger = LoggerFactory.getLogger("");
	
	SocialNetworkManager snManager = new SocialNetworkManager(SNUtils.getMainConfigFile());
	HashMap<Integer,SocialAgent> agentmap = snManager.agentList;
	Network net = new Network();
	Random random = new Random();
	int nodes = 5;
	int degree = 3;
	double swDist = 1.0;
	double swProb =  0.0; // neighbourhood network
	
//	@Ignore
	@Before
	//automatically generate a large agent map
	public void setUpRandomAgentMap()
	{
		logger.trace("setting up random agent map.....");
	

		//agent x,y coords are in meters :  UTM uses meters from reference points
		SNConfig.setDiffusionType(DataTypes.ltModel);
		createAgentMap(nodes, 1000);
		

		
		
	}
	@Ignore
	@Test
	//automatically generate a large agent map
	public void testNormaliseWeightsWithRandomRegularNetwork()
	{
		RandomRegularNetwork randNet = new RandomRegularNetwork(nodes,degree);
		SNConfig.setNormaliseRandRegNetwork(true); // IMPORTANT
		randNet.genNetworkAndUpdateAgentMap(agentmap);

		randNet.displayAgentMap(agentmap);
		
		
		//print values
		randNet.printNetworkWegihts(agentmap);
//		logger.debug(" normalise network? {}",randNet.getNormaliseVariable());
	}
	
	
//	@Ignore
	@Test
	//automatically generate a large agent map
	public void testNormaliseWeightsWithSmallWorldNetwork()
	{
		SWNetwork swNet = new SWNetwork(swDist,degree,swProb);
		SNConfig.setNormaliseSWNetwork(true);// IMPORTANT
		swNet.genNetworkAndUpdateAgentMap(agentmap);

		swNet.displayAgentMap(agentmap);
		
		
		//print values
		swNet.printNetworkWegihts(agentmap);
//		logger.debug(" normalise network? {}",randNet.getNormaliseVariable());
	}
	
	@Ignore
	@Test
	//automatically generate a large agent map
	public void testNormaliseWeightsWithGivenNetwork()
	{
		net.createLinkWithGivenWeight(1, 0,0.5, agentmap);
		net.createLinkWithGivenWeight(1, 2,0.5, agentmap);
		net.createLinkWithGivenWeight(1, 3,0.5, agentmap);
		net.createLinkWithGivenWeight(4, 3,1.0, agentmap);
		
		net.normaliseLinkWeights(agentmap);
		net.displayAgentMap(agentmap);
		
		
		//print values
		net.printNetworkWegihts(agentmap);
	}
	
	public void createAgentMap(int nodes, int cordRange) { 
		
		for(int id=0; id < nodes; id++) {
			int x = random.nextInt(cordRange);
			int y = random.nextInt(cordRange);
			snManager.createSocialAgent(Integer.toString(id));snManager.setCords(Integer.toString(id),x,y);

		}
	}
}


