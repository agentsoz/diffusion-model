package io.github.agentsoz.socialnetwork;

import io.github.agentsoz.dataInterface.DataClient;
import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.dataInterface.DataSource;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.DiffusionContent;
import io.github.agentsoz.socialnetwork.util.DiffusionDataContainer;
import io.github.agentsoz.socialnetwork.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import io.github.agentsoz.util.Time;



public class SocialNetworkDiffusionModel implements DataSource<SortedMap<Double, DiffusionDataContainer>>, DataClient<Object>  {



    Logger socialNetworkDiffusionLogger = null; //= LoggerFactory.getSocialNetworlDiffusionLogger("");

    public HashMap<Integer, SocialAgent> agentList = new HashMap<Integer, SocialAgent>();

    private Network network;
    private DiffModel[] diffModels;
    private String mainConfigFile;

    //variables used to manage dataserver API functions
    private final Logger logger = LoggerFactory.getLogger(SocialNetworkDiffusionModel.class);
    private static final String eConfigFile = "configFile";

    private DataServer dataServer;
    private double startTimeInSeconds = -1;
//    private SocialNetworkDiffusionModel snManager;
    private double lastUpdateTimeInMinutes = -1;
    private Time.TimestepUnit timestepUnit = Time.TimestepUnit.SECONDS;
 //   private String configFile = null;
    private List<String> agentsIds = null;

    private TreeMap<Double, DiffusionDataContainer> allStepsDiffusionData;

    Map<String, Set> localContentFromAgents;
    ArrayList<String> globalContentFromAgents;

    public SocialNetworkDiffusionModel(String configFile) {
        mainConfigFile = configFile;
    }

    public SocialNetworkDiffusionModel(String config, DataServer dataServer) {
        this.mainConfigFile = config;
        this.dataServer = dataServer;
    }

    public SocialNetworkDiffusionModel(String config, DataServer dataServer, List<String> ids) {
        this.mainConfigFile = config;
        this.dataServer = dataServer;
        this.agentsIds = ids;
    }

    public SocialNetworkDiffusionModel(Map<String, String> opts, DataServer dataServer, List<String> agentsIds) {
        parse(opts);
//        this.snManager = (configFile==null) ? null : new SocialNetworkDiffusionModel(configFile);
        this.localContentFromAgents = new HashMap<>();
        this.globalContentFromAgents =  new ArrayList<String>();
        this.allStepsDiffusionData =  new TreeMap<>();
        this.dataServer = dataServer;
        this.agentsIds = agentsIds;
    }

    public void init() {

        setupSNConfigsAndLogs();

        for (String id : agentsIds) {
            createSocialAgent(id); //populate agentmap
        }

        genNetworkAndDiffModels(); // gen network and diffusion models
        printSNModelconfigs();

//        //subscribe to diffusion data container from BDI side
        this.dataServer.subscribe(this, DataTypes.DIFFUSION_DATA_CONTAINDER_FROM_BDI);
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
        socialNetworkDiffusionLogger.trace(" social agent {} initialized ", id);
    }


    public void setCords(String id, double east, double north) {

        int agentID = Integer.parseInt(id);
        SocialAgent sAgent = agentList.get(agentID);
        if (sAgent == null) {
            socialNetworkDiffusionLogger.error("null agent found");
            return;
        }
        sAgent.setX(east);
        sAgent.setY(north);

        socialNetworkDiffusionLogger.trace("social agent {} start location initialised {} {}", agentID, sAgent.getX(), sAgent.getY());
    }

    public HashMap<Integer, SocialAgent> getAgentMap() {
        return agentList;
    }


    // initilises a network, diffusion model as specified in configurations
    public boolean genNetworkAndDiffModels() {
// separated setting up configurationa from this method
//    if(!setupSNConfigsAndLogs()) {
//		socialNetworkDiffusionLogger.error("Error in setting configurations");
//		return false;
//	}
        if (!generateSocialNetwork()) {
            socialNetworkDiffusionLogger.error("Error in generating network model");
            return false;
        } else if (!generateDiffusionModels()) {
            socialNetworkDiffusionLogger.error("Error in generating diffusion model");
            return false;
        } else {
            //SNConfig.printNetworkConfigs();
            //SNConfig.printDiffusionConfigs();
            socialNetworkDiffusionLogger.info("All SN model componants generated completely");
            return true;
        }


    }

    public boolean setupSNConfigsAndLogs() {

        SNConfig.setConfigFile(mainConfigFile);

        if (!SNConfig.readConfig()) {
            socialNetworkDiffusionLogger.error("Failed to load SN configuration from '" + SNConfig.getConfigFile() + "'. Aborting");
            return false;
        } else {
            //all configurations set, so now create the log file.
            socialNetworkDiffusionLogger = Log.createLogger("", SNConfig.getLogFilePath());

            return true;
        }

    }

    //used in running lhs batch runs, to only set configs other than log, which will be created using a separate method.
    public boolean setupSNConfigs() {

        SNConfig.setConfigFile(mainConfigFile);
        boolean result = true;

        if (!SNConfig.readConfig()) {
            socialNetworkDiffusionLogger.error("Failed to load SN configuration from '" + SNConfig.getConfigFile() + "'. Aborting");
            result = false;
        }
        return result;
    }

    //used when logfile needs to shifted to different directories when executing lhs batch runs
    public void getSocialNetworlDiffusionLogger(String file) {

        SNConfig.setLogFile(file);
        socialNetworkDiffusionLogger = Log.getOrCreateLogger("", file);

    }

    public void printSNModelconfigs() {
        SNConfig.printNetworkConfigs();
        SNConfig.printDiffusionConfigs();
    }

    public boolean generateSocialNetwork() {

        if (this.agentList.size() < 2) {
            socialNetworkDiffusionLogger.warn("only {} social agent in the list, too small for diffusion", this.agentList.size());
            //return false;
        }

        NetworkFactory netFactory = new NetworkFactory();
        network = netFactory.getNetwork(SNConfig.getNetworkType(), this.agentList);
        if (network == null) {
            socialNetworkDiffusionLogger.error("network generation failed, null network ");
            return false;
        } else {
            network.genNetworkAndUpdateAgentMap(this.agentList);
            socialNetworkDiffusionLogger.info(" network generation complete");
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
                socialNetworkDiffusionLogger.error("diffusion model generation failed, null diffusion model found");
                return false;
            } else {
                // now initialise the model
               // model.registerContentIfNotRegistered("default", DataTypes.LOCAL);
                model.initialise();
//                model.recordCurrentStepSpread(dataServer.getTime());
                diffModels[i] = model;
                i++;
                socialNetworkDiffusionLogger.info(" {} diffusion model generation complete ", modelName);

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

public double getShortestTimeStepOfAllDiffusionModels(){
        List<Integer> timeSteps = new ArrayList<Integer>();
        for(DiffModel model:diffModels) {
                timeSteps.add(model.getDiffStep());
            }


        return (double) Collections.min(timeSteps);
}

    public List<DiffModel> getDiffusionModelsToStepForCurrentTime(double time){
       List<DiffModel> models = new ArrayList<DiffModel>();
        for(DiffModel model:diffModels) {
            if(model.getTimeForNextStep() == time) {
                models.add(model);
            }

        }

        return models;
    }

//	public void diffuseContent() {
//		if(diffModels.equals(null)) {
//			socialNetworkDiffusionLogger.error("Diffusion model not set, aborting");
//			return ;
//		}
//		else{
//			diffModels.step();
//		}
//
//
//	}



    public void finish() {
        // cleaning

        if (this == null) { // return if the diffusion model is not executed
            return;
        }
        for (DiffModel model : diffModels) {
              //terminate diffusion model and output diffusion data
            model.finish();
            model.getDataCollector().writeSpreadDataToFile();
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


    //----------- Data server functionality methods ------------------

    private void parse(Map<String, String> opts) {
        if (opts == null) {
            return;
        }
        for (String opt : opts.keySet()) {
            logger.info("Found option: {}={}", opt, opts.get(opt));
            switch(opt) {
                case DataTypes.eGlobalStartHhMm:
                    String[] tokens = opts.get(opt).split(":");
                    setStartHHMM(new int[]{Integer.parseInt(tokens[0]),Integer.parseInt(tokens[1])});
                    break;
                case eConfigFile:
                    mainConfigFile = opts.get(opt); // social network model configuration file
                    break;
                default:
                    logger.warn("Ignoring option: " + opt + "=" + opts.get(opt));
            }
        }
    }


    public void setStartHHMM(int[] hhmm) {
        startTimeInSeconds = Time.convertTime(hhmm[0], Time.TimestepUnit.HOURS, timestepUnit)
                + Time.convertTime(hhmm[1], Time.TimestepUnit.MINUTES, timestepUnit);
    }

    protected void stepDiffusionModelsAndUpdateDataContainer(DiffusionDataContainer dataContainer, double timestep) {
//        snManager.diffuseContent(); // step the diffusion model
            stepDiffusionModels(timestep);
        for(DiffModel model: diffModels) {
            if (model instanceof ICModel) {
                ICModel icModel = (ICModel) model;
                //icModel.recordCurrentStepSpread((int) timestep); this is done in stepModels()

                HashMap<String, ArrayList<String>> latestUpdate = icModel.getLatestDiffusionUpdates();
                if (!latestUpdate.isEmpty()) {
                    logger.info("received updates fromt the IC model");
                    for (Map.Entry<String, ArrayList<String>> contents : latestUpdate.entrySet()) {
                        String content = contents.getKey();
                        ArrayList<String> agentIDs = contents.getValue();
                        logger.info("agents activated for content {} at time {} are: {}", content, (int) timestep, agentIDs.toString());

                        for (String id : agentIDs) { // for each agent create a DiffusionContent and put content type and parameters
                            //   DiffusionContent content = dataContainer.getOrCreateDiffusedContent(id);
                            String[] params = {content}; //#FIXME fix dummy_content below
                            dataContainer.putContentToContentsMapFromDiffusionModel(id, "dummy_content", params);

                            //  content.getContentsMapFromDiffusionModel().put(contentType,params );
                        }
                    }

                }

            }
            else if(model instanceof LTModel){
                //#TODO
            }

        }


    }

    @Override
    public SortedMap<Double, DiffusionDataContainer> sendData(double timestep, String dataType) {

        double currentTimeInMinutes = Time.convertTime(timestep, timestepUnit, Time.TimestepUnit.MINUTES); // current time in minutes
        Double nextTime = timestep + getShortestTimeStepOfAllDiffusionModels();

        // create data structure to store current step contents and params
        DiffusionDataContainer currentStepDataContainer =  new DiffusionDataContainer();


        if (nextTime != null) {
            dataServer.registerTimedUpdate(DataTypes.DIFFUSION_DATA_CONTAINER_FROM_DIFFUSION_MODEL, this, nextTime);
            // update the model with any new messages form agents
            ICModel icModel = (ICModel) getDiffModels()[0]; //#FIXME remove hardcoded
            //#FIXME how to get which diffusion model for a given influence?
            if (!localContentFromAgents.isEmpty()) { // update local content
                Map<String, String[]> map = new HashMap<>();
                for (String key : localContentFromAgents.keySet()) {
                    Object[] set = localContentFromAgents.get(key).toArray(new String[0]);
                    String[] newSet = new String[set.length];
                    for (int i = 0; i < set.length; i++) {
                        newSet[i] = (String)set[i];
                    }
                    map.put(key,newSet);
                    logger.info(String.format("At time %.0f, total %d agents received content %s from BDI Model.", timestep, newSet.length, key));
                    logger.info("Agents spreading content are: {}", Arrays.toString(newSet));
                }
                icModel.updateSocialStatesFromLocalContent(map);
            }

            if(!globalContentFromAgents.isEmpty()) { // update global contents

                logger.info("Global content received to spread: {}", globalContentFromAgents.toString());
                icModel.updateSocialStatesFromGlobalContent(globalContentFromAgents);

            }

            // step the models
            stepDiffusionModelsAndUpdateDataContainer(currentStepDataContainer,currentTimeInMinutes);

            //now put the current step data container to all steps data map
            if(!currentStepDataContainer.getDiffusionDataMap().isEmpty()){
                this.allStepsDiffusionData.put(currentTimeInMinutes, currentStepDataContainer);
            }



            // clear the contents
            globalContentFromAgents.clear();
            localContentFromAgents.clear();

        }

        //+1 to avoid returning empty map for diffusion data for first step (toKey = fromKey)
        SortedMap<Double, DiffusionDataContainer> periodicDiffusionData =   allStepsDiffusionData.subMap(lastUpdateTimeInMinutes,currentTimeInMinutes+1);
        lastUpdateTimeInMinutes = currentTimeInMinutes;

        return (currentStepDataContainer.getDiffusionDataMap().isEmpty()) ? null : periodicDiffusionData;

    }

    @Override
    public void receiveData(double time, String dataType, Object data) { // data package from the BDI side

        switch (dataType) {
            case DataTypes.DIFFUSION_DATA_CONTAINDER_FROM_BDI: // update Diffusion model based on BDI updates

                DiffusionDataContainer dataContainer = (DiffusionDataContainer) data;

                if (!(data instanceof DiffusionDataContainer)) {
                    logger.error("received unknown data: " + data.toString());
                    break;
                }

                for (Map.Entry entry : dataContainer.getDiffusionDataMap().entrySet()) {

                    String agentId = (String) entry.getKey();
                    DiffusionContent dc = (DiffusionContent) entry.getValue();

                    //process local contents from the BDI model
                    if(!dc.getContentsMapFromBDIModel().isEmpty()){
                        for(String localContent: dc.getContentsMapFromBDIModel().keySet()){
                            String[] contents = (String[]) dc.getContentsMapFromBDIModel().get(localContent);
                            String content = contents[0];
                            // do something with parameters

                            logger.debug("Agent {} received content {} of type {} ",agentId,content,localContent);
                            Set<String> agents = (localContentFromAgents.containsKey(content)) ? localContentFromAgents.get(content) :
                                    new HashSet<>();
                            agents.add(agentId);
                            localContentFromAgents.put(content, agents);
                        }
                    }


                    //process global (broadcast) contents from BDI model
                    if(!dc.getBroadcastContentsMapFromBDIModel().isEmpty()){
                        for(String globalContent: dc.getBroadcastContentsMapFromBDIModel().keySet()){
                            logger.debug("received global content " + globalContent);
                            if(!globalContentFromAgents.contains(globalContent)) {
                                globalContentFromAgents.add(globalContent);
                            }
                            String[] params = (String[])dc.getBroadcastContentsMapFromBDIModel().get(globalContent);
                            // do something with parameters

                        }
                    }


                    //process SN actions
                    if(!dc.getSnActionsMapFromBDIModel().isEmpty()){
                        for(String action: dc.getSnActionsMapFromBDIModel().keySet()){
                            Object[] params = dc.getSnActionsMapFromBDIModel().get(action);
                            // do something with parameters
                        }
                    }
                }

                break;
            default:
                throw new RuntimeException("Unknown data type received: " + dataType);
        }
    }

    /**
     * Set the time step unit for this model
     * @param unit the time step unit to use
     */
    void setTimestepUnit(Time.TimestepUnit unit) {
        timestepUnit = unit;
    }

    public void start() {
        if (this != null) {
            init();
            setTimestepUnit(Time.TimestepUnit.MINUTES);
            dataServer.registerTimedUpdate(DataTypes.DIFFUSION_DATA_CONTAINER_FROM_DIFFUSION_MODEL, this, Time.convertTime(startTimeInSeconds, Time.TimestepUnit.SECONDS, timestepUnit));
        } else {
            logger.warn("started but will be idle forever!!");
        }
    }

    /**
     * Start publishing data
     * @param hhmm an array of size 2 with hour and minutes representing start time
     */
    public void start(int[] hhmm) {
        double startTimeInSeconds = Time.convertTime(hhmm[0], Time.TimestepUnit.HOURS, Time.TimestepUnit.SECONDS)
                + Time.convertTime(hhmm[1], Time.TimestepUnit.MINUTES, Time.TimestepUnit.SECONDS);
        dataServer.registerTimedUpdate(DataTypes.DIFFUSION_DATA_CONTAINER_FROM_DIFFUSION_MODEL, this, startTimeInSeconds);
    }



}
