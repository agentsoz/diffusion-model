package agentoz.socialnetwork;

import agentoz.socialnetwork.util.DataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import agentoz.socialnetwork.util.Global;
import agentoz.socialnetwork.util.SNUtils;
import io.github.agentsoz.dataInterface.DataClient;
import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.dataInterface.DataSource;

import java.util.HashMap;

/*

 * Configs:
 * SN model has the separate config
 * steps:
 * 	create the agentList : createSocialAgent -> setCord 
 *  initialise the social network
 *  for each diff turn -> process diffusion
 *
 *  Whenever there is a change in agent states, SN Manager should  publish to the dataserver, inorder for the changes to take effect
 *  in BDI and MATSim systems. Currently there are two such functions: Seeding and diff process.
 *  Updates of these two functions are published to the dataserver at the application side.
 */

public class SocialNetworkManager{

	final Logger logger = LoggerFactory.getLogger("");

	public HashMap<Integer, SocialAgent> agentList = new HashMap<Integer, SocialAgent>();

	protected DataServer dataServer;
    Network network;
    DiffModel diffModel;
	private String mainConfigFile;


	public SocialNetworkManager(String configFile) { // overwrite default configuration
		mainConfigFile = configFile;
	}
        
    public void createSocialAgent(String id)
    {
    	int agentID = Integer.parseInt(id);  //use this t initialise agents in SN side

		if(SNConfig.getDiffusionType().equals(DataTypes.ltModel)) {
			agentList.put(agentID, new SocialAgent(agentID)); // LT Model social agent constructor.
			logger.trace(" social agent {} initialized ", id);
		}
		else if(SNConfig.getDiffusionType().equals(DataTypes.CLTModel)) {
			agentList.put(agentID, new SocialAgent(agentID, DataTypes.MEDIUM)); // CLT Model social agent constructor.
		}
    	 
    }
        
//    public void setExecType(String type) {
//    	this.execType = type;
//    }
    
    public void setCords(String id, double east, double north) {

		int agentID = Integer.parseInt(id);
    	SocialAgent sAgent = agentList.get(agentID);
    	if(sAgent == null) {
    		logger.error("null agent found");
    		return;
		}
    	sAgent.setX(east);
    	sAgent.setY(north);

		logger.trace("social agent {} start location initialised {} {}",agentID, sAgent.getX(), sAgent.getY());
    }
   
    public  HashMap<Integer, SocialAgent> getAgentMap () {
    	return agentList;
    }
    
    
	/*
	 * method : main method that initialises a social network model 
	 * first method that runs from the BDI side.
	 * 
	 * note : the diffusion process will execute only if this methods return true.
	 * therefore check the conditions for initialising the social network and return false
	 * 
	 * note: when testing network generation, it cannot use the agentmap from haw_pop xml file?
	 * class variables of SNmanager class are not tested
	 * 
	 * note: after updating the agent map, the social network object is not needed. 
	 * 
	 * SN model is successfully initialised if all three statuses return true.
	 */
    
    public boolean initSNModel()
    {

    boolean configStatus = 	setupSNConfigs();

	//SNConfig.printNetworkConfigs();
	//SNConfig.printDiffusionConfigs();
	
	    //2.gen network
    boolean networkStatus =  generateSocialNetwork();
	    
	    //3. gen diff model
    boolean diffStatus =   generateDiffModel();
	    
    if (configStatus && networkStatus && diffStatus) {
    	
    	logger.info("All SN model componants generated completely");
    	return true;
    }
    else
    	return false;
	    
    }
    
    public boolean setupSNConfigs(){

    	SNConfig.setConfigFile(mainConfigFile);
    	
		if (!SNConfig.readConfig()) {
			logger.error("Failed to load SN configuration from '"+SNConfig.getConfigFile()+"'. Aborting");
			return false;
		}
		else { 
			//1. print the configs
//			if(execType.equals(DataTypes.SN_BDI)) {
//				SNConfig.printNetworkConfigs();
//				SNConfig.printDiffusionConfigs();
//			}
			return true;
		}
    }

    public void printSNModelconfigs() {
		SNConfig.printNetworkConfigs();
		SNConfig.printDiffusionConfigs();
	}
    public boolean generateSocialNetwork()
    {
    	
    	if(this.agentList.size() < 2) {
		logger.error("sn initialisation falied: size of agent list is too small: {}  exiting..",this.agentList.size() );
		return false;
    	}
    	
    	NetworkFactory netFactory =  new NetworkFactory();
    	network = netFactory.getNetwork(SNConfig.getNetworkType(), this.agentList);
    	if(network == null) {
        	logger.error("network generation failed, null network found");
        	return false;
    	}
    	else {
    		network.genNetworkAndUpdateAgentMap(this.agentList);
            logger.info(" network generation complete");
            return true;
    	}


    }

    public boolean generateDiffModel()
    {
    	
    	DiffModelFactory diffFactory =  new DiffModelFactory();
    	diffModel = diffFactory.getDiffusionModel(SNConfig.getDiffusionType(), this);
    	if(diffModel == null) {
        	logger.error("diffusion model generation failed, null model found");
        	return false;
    	}
    	else {
    		// other initialisation methods
    		diffModel.initialise(); 
            logger.info(" diffusion model initialisation complete"); // ready to start diffusion process
            return true;
    	}


    }
    

    
    /*
     * The main method that controls the diffusion process.
     * This process should include the standard diffusion functions, anything external to the standard
     * functions (e.g. publish data to dataserver, write diffusion data)
     * should be executed in the application side.
     */
    public boolean processDiffusion(long time)
    {
    	logger.trace("processDiffusion method...");
    	 if(diffModel.equals(null)) {
    		 logger.error("Diffusion model not set, aborting");
    		 return false;
    	 }

    	else if(diffModel.isDiffTurn(time)) {
    		logger.debug("SNManger started executing diffusion process at {}..",time);
//        	diffModel.preDiffProcess(); // e.g. external diffusion
        	diffModel.doDiffProcess(time);
        	diffModel.postDiffProcess(time); // e.g. data collection , send data to BDI side

        	
        	//SN_BDI execution
//        	if(this.execType.equals(DataTypes.SN_BDI)) {
//
//        		///publish data
//        		//this.dataServer.publish(DataTypes.PANIC_DATA_UPDATES,(Object) null); // just a message no data is passed
//        		publishSNDataUpdate(); // pass the adc to this method
//
//        	}

        	return true;
    	}
    	else{
    	 	return false;
		 }

	}


	public String getAgentState(int id) {
		SocialAgent agent = agentList.get(id);
		String s = agent.getState();
		return s;
	}

	public void setDataServer(DataServer ds) {
		this.dataServer = ds;
	}

	public HashMap<Integer,Double> getCurrentStepDiffusionData() {

		HashMap<Integer,Double> data =  new HashMap<Integer, Double>();
		data.put(1,0.5); // #FIXME implement the Diffusion data update

    	return data;
	}

	public DiffModel getDiffModel() { 
		return this.diffModel;
	}

    //hook a preconfigured diffusion model with SN Manager
	public void setTestDiffModel( DiffModel dModel) {
		this.diffModel = dModel;
	}

	public void publishSNDataUpdate() {
		this.dataServer.publish(DataTypes.DIFFUSION, "sn-data");
	}

	// generate agentmap with starting from a specified id.
	public void createAgentMapWithSpecifiedId(int startId, int numAgents) {

    	int maxEasting = 10000;
    	int maxNorthing = 10000;
    	for(int i=startId; i<=numAgents; i++) {
    		createSocialAgent(Integer.toString(startId));
    		setCords(Integer.toString(startId), Global.getRandom().nextInt(maxEasting), Global.getRandom().nextInt(maxNorthing));
		}
	}
	
}
