package io.github.agentsoz.socialnetwork;

import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.socialnetwork.util.Log;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/*
 *  Whenever there is a change in agent social states, SN Manager should  publish to the dataserver, inorder for the changes to take effect
 *  in BDI and MATSim systems. Currently there are two such functions: Seeding and diff process.
 *  Updates of these two functions are published to the dataserver to be received at the application side.
 */

public class SocialNetworkModel {

    private DataServer dataServer;
    private List<String> agentsIds = null;

    Logger logger = null; //= LoggerFactory.getLogger("");

    public HashMap<Integer, SocialAgent> agentList = new HashMap<Integer, SocialAgent>();

    private Network network;
    private DiffModel[] diffModels;
    private String mainConfigFile;


    public SocialNetworkModel(String configFile) {
        mainConfigFile = configFile;
    }

    public SocialNetworkModel(String config, DataServer dataServer) {
        this.mainConfigFile = config;
        this.dataServer = dataServer;
    }

    public SocialNetworkModel(String config, DataServer dataServer, List<String> ids) {
        this.mainConfigFile = config;
        this.dataServer = dataServer;
        this.agentsIds = ids;
    }



    public void initSNModel() {

        setupSNConfigsAndLogs();

        for (String id : agentsIds) {
            createSocialAgent(id); //populate agentmap
        }

        genNetworkAndDiffModels(); // gen network and diffusion models
        printSNModelconfigs();
//
//        //subscribe to BDI data updates
//        this.dataServer.subscribe(this, DataTypes.DIFFUSION_DATA_CONTAINDER_FROM_BDI); #FIXME
    }

    public void initWithoutSocialAgentsMap() { // init SN model with already populated social agent map


        setupSNConfigsAndLogs(); //setup configs and create log first

        genNetworkAndDiffModels(); // gen network and diffusion models
        printSNModelconfigs();

        //subscribe to BDI data updates
        //  this.dataServer.subscribe(this, DataTypes.DIFFUSION_DATA_CONTAINDER_FROM_BDI); #FIXME

    }

    public void createSocialAgent(String id) {
        int agentID = Integer.parseInt(id);
        this.agentList.put(agentID, new SocialAgent(agentID)); // LT Model constructor.
        logger.trace(" social agent {} initialized ", id);
    }


    public void setCords(String id, double east, double north) {

        int agentID = Integer.parseInt(id);
        SocialAgent sAgent = agentList.get(agentID);
        if (sAgent == null) {
            logger.error("null agent found");
            return;
        }
        sAgent.setX(east);
        sAgent.setY(north);

        logger.trace("social agent {} start location initialised {} {}", agentID, sAgent.getX(), sAgent.getY());
    }

    public HashMap<Integer, SocialAgent> getAgentMap() {
        return agentList;
    }


    // initilises a network, diffusion model as specified in configurations
    public boolean genNetworkAndDiffModels() {
// separated setting up configurationa from this method
//    if(!setupSNConfigsAndLogs()) {
//		logger.error("Error in setting configurations");
//		return false;
//	}
        if (!generateSocialNetwork()) {
            logger.error("Error in generating network model");
            return false;
        } else if (!generateDiffusionModels()) {
            logger.error("Error in generating diffusion model");
            return false;
        } else {
            //SNConfig.printNetworkConfigs();
            //SNConfig.printDiffusionConfigs();
            logger.info("All SN model componants generated completely");
            return true;
        }


    }

    public boolean setupSNConfigsAndLogs() {

        SNConfig.setConfigFile(mainConfigFile);

        if (!SNConfig.readConfig()) {
            logger.error("Failed to load SN configuration from '" + SNConfig.getConfigFile() + "'. Aborting");
            return false;
        } else {
            //all configurations set, so now create the log file.
            logger = Log.createLogger("", SNConfig.getLogFilePath());

            return true;
        }

    }

    //used in running lhs batch runs, to only set configs other than log, which will be created using a separate method.
    public boolean setupSNConfigs() {

        SNConfig.setConfigFile(mainConfigFile);
        boolean result = true;

        if (!SNConfig.readConfig()) {
            logger.error("Failed to load SN configuration from '" + SNConfig.getConfigFile() + "'. Aborting");
            result = false;
        }
        return result;
    }

    //used when logfile needs to shifted to different directories when executing lhs batch runs
    public void getLogger(String file) {

        SNConfig.setLogFile(file);
        logger = Log.getOrCreateLogger("", file);

    }

    public void printSNModelconfigs() {
        SNConfig.printNetworkConfigs();
        SNConfig.printDiffusionConfigs();
    }

    public boolean generateSocialNetwork() {

        if (this.agentList.size() < 2) {
            logger.warn("only {} social agent in the list, too small for diffusion", this.agentList.size());
            //return false;
        }

        NetworkFactory netFactory = new NetworkFactory();
        network = netFactory.getNetwork(SNConfig.getNetworkType(), this.agentList);
        if (network == null) {
            logger.error("network generation failed, null network ");
            return false;
        } else {
            network.genNetworkAndUpdateAgentMap(this.agentList);
            logger.info(" network generation complete");
            return true;
        }


    }

    public boolean generateDiffusionModels() {

        this.diffModels = new DiffModel[SNConfig.getDiffusionModelsList().size()];

        //DiffModelFactory diffFactory =  new DiffModelFactory();
        int i = 0;

        for (String modelName : SNConfig.getDiffusionModelsList()) {

            DiffModel model = DiffModelFactory.getDiffusionModel(modelName, this);
            if (model == null) {
                logger.error("diffusion model generation failed, null diffusion model found");
                return false;
            } else {
                // now initialise the model
               // model.registerContentIfNotRegistered("default", DataTypes.LOCAL);
                model.initialise();
//                model.recordCurrentStepSpread(dataServer.getTime());
                diffModels[i] = model;
                i++;
                logger.info(" {} diffusion model generation complete ", modelName);

            }

        }
        return true;

    }

public void stepDiffusionModels(double time) {
        for (DiffModel model: diffModels){
            if(model.getTimeForNextStep() == time) {
                model.step();
                model.recordCurrentStepSpread(time);
                model.setTimeForNextStep();
            }
        }
}

public double getShortestTimeOfAllDiffusionModels(){
        List<Integer> timeSteps = new ArrayList<Integer>();
        for(DiffModel model:diffModels) {
                timeSteps.add(model.getDiffStep());
            }


        return (double) Collections.min(timeSteps);
}

    /*
     * The main method that controls the diffusion process.
     * Used by SN and SN-BDI and SN-BDI-ABM componants, so includes general functions,
     * any specific functions (e.g. publish data to dataserver, write diffusion data)
     * should be called in the application side.
     *
     * This method includes the constraint of stepsize check before the diffusion model steps. diffuse() method does not have any restrictions
     */
//    public boolean processDiffusion(long time)
//    {
//    	logger.trace("processDiffusion method...");
//    	 if(diffModels.equals(null)) {
//    		 logger.error("Diffusion model not set, aborting");
//    		 return false;
//    	 }
//
//    	else if(diffModels.isDiffTurn(time)) {
//    		logger.debug("SNManger started executing diffusion process at {}..",time);
//        	diffModels.preDiffProcess(); // e.g. external diffusion
//        	diffModels.step();
//        	diffModels.postDiffProcess(time); // e.g. data collection , send data to BDI side
//
//
    //SN_BDI execution
//        	if(this.execType.equals(DataTypes.SN_BDI)) {
//
//        		///publish data
//        		//this.dataServer.publish(DataTypes.PANIC_DATA_UPDATES,(Object) null); // just a message no data is passed
//        		publishSNDataUpdate(); // pass the adc to this method
//
//        	}
//
//        	return true;
//    	}
//    	else{
//    	 	return false;
//		 }
//
//	}

//	public void diffuseContent() {
//		if(diffModels.equals(null)) {
//			logger.error("Diffusion model not set, aborting");
//			return ;
//		}
//		else{
//			diffModels.step();
//		}
//
//
//	}

//	public String getAgentState(int id) {
//		SocialAgent agent = agentList.get(id);
//		String s = agent.getState();
//		return s;
//	}

    public void finish() {
        // cleaning

        if (this == null) { // return if the diffusion model is not executed
            return;
        }
        for (DiffModel model : diffModels) {
//            if (model instanceof ICModel) {

            //terminate diffusion model and output diffusion data

            model.finish();
            model.getDataCollector().writeSpreadDataToFile();
//            }
        }
    }

    public DiffModel[] getDiffModels() {
        return this.diffModels;
    }

    public Network getNetworkModel() {
        return this.network;
    }

    public DataServer getDataServer() {
        return dataServer;
    }

    //hook a specific diffusion model with SN Manager
    public void setTestDiffModel(DiffModel dModel) {
        this.diffModels[0] = dModel;
    }


    // generate agentmap with starting from a specified id.
//	public void createAgentMapWithSpecifiedId(int startId, int numAgents) {
//
//    	int maxEasting = 10000;
//    	int maxNorthing = 10000;
//    	for(int i=startId; i<=numAgents; i++) {
//    		createSocialAgent(Integer.toString(startId));
//    		setCords(Integer.toString(startId), Global.getRandom().nextInt(maxEasting), Global.getRandom().nextInt(maxNorthing));
//		}
//	}
//
}
