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

import javax.xml.crypto.Data;


public class SocialNetworkDiffusionModel implements DataSource<SortedMap<Double, DiffusionDataContainer>>, DataClient<Object> {


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

    /*
        content_Type, Object:
        ICModel Object is of <String content, Set>  type // string values
        LTModel Object is of <String content, Map<String,Double>> // numerical values
     */
    Map<String, Object> localContentsFromBDIAgents;
    HashMap<String, String[]> globalContentsFromBDIAgents; // content_type, contents

    public SocialNetworkDiffusionModel(String configFile) {
        mainConfigFile = configFile;
    }

    public SocialNetworkDiffusionModel(String config, DataServer dataServer) {
        this.mainConfigFile = config;
        this.dataServer = dataServer;
        this.localContentsFromBDIAgents = new HashMap<String,Object>();
        this.globalContentsFromBDIAgents = new HashMap<String, String[]>();
        this.allStepsDiffusionData = new TreeMap<>();
    }

    public SocialNetworkDiffusionModel(String config, DataServer dataServer, List<String> ids) {
        this.localContentsFromBDIAgents = new HashMap<String,Object>();
        this.globalContentsFromBDIAgents = new HashMap<String, String[]>();
        this.allStepsDiffusionData = new TreeMap<>();
        this.mainConfigFile = config;
        this.dataServer = dataServer;
        this.agentsIds = ids;
    }

    public SocialNetworkDiffusionModel(Map<String, String> opts, DataServer dataServer, List<String> agentsIds) {
        parse(opts);
//        this.snManager = (configFile==null) ? null : new SocialNetworkDiffusionModel(configFile);
        this.localContentsFromBDIAgents = new HashMap<String,Object>();
        this.globalContentsFromBDIAgents = new HashMap<String, String[]>();
        this.allStepsDiffusionData = new TreeMap<>();
        this.dataServer = dataServer;
        this.agentsIds = agentsIds;
    }

    public void init() {

        setupSNConfigsAndLogs();

        for (String id : agentsIds) {
            createSocialAgent(id); //populate agentmap
        }
        printSNModelconfigs();
        genNetworkAndDiffModels(); // gen network and diffusion models


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
    public void createSocialNetworlDiffusionLogger(String file) {

        SNConfig.setLogFile(file);
        socialNetworkDiffusionLogger = Log.getOrCreateLogger("", file);

    }

    public Logger getSocialNetworkDiffusionLogger(){
        return socialNetworkDiffusionLogger;
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
                model.initialise(); //#FIXME seed contents are not passed to the BDI side.
                model.recordCurrentStepSpread(dataServer.getTime());
                model.setTimeForNextStep(); // after seeding increase timestep
                diffModels[i] = model;
                i++;
                socialNetworkDiffusionLogger.info(" {} diffusion model generation complete ", modelName);

            }

        }
        return true;

    }

    public void stepDiffusionModels(double time) {
        for (DiffModel model : diffModels) {
            if (model.getTimeForNextStep() == time) {
                if(model instanceof ICModel){
                    logger.info("IC model is stepping at {}", time);
                }
                if(model instanceof LTModel){
                    logger.info("LT model is stepping at {}", time);
                }
                model.step();
                model.recordCurrentStepSpread(time);
                model.setTimeForNextStep(); // this is initially set at start(), after seeding
            }
        }
    }

    public double getEarliestTimeForNextStep() {
        List<Double> timesForNextStep = new ArrayList<Double>();
        for (DiffModel model : diffModels) {
            timesForNextStep.add(model.getTimeForNextStep());
        }


        return  Collections.min(timesForNextStep);
    }

    public List<DiffModel> getDiffusionModelsToStepForCurrentTime(double time) {
        List<DiffModel> models = new ArrayList<DiffModel>();
        for (DiffModel model : diffModels) {
            if (model.getTimeForNextStep() == time) {
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

    public LTModel getLTModel() {
        DiffModel model = null;
        for(DiffModel m: diffModels){
            if(m instanceof LTModel){
                model = m;
            }
        }
        return (LTModel) model;
    }

    public ICModel getICModel() {
        DiffModel model = null;
        for(DiffModel m: diffModels){
            if(m instanceof ICModel){
                model = m;
            }
        }
        return (ICModel) model;
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
            switch (opt) {
                case DataTypes.eGlobalStartHhMm:
                    String[] tokens = opts.get(opt).split(":");
                    setStartHHMM(new int[]{Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1])});
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
        stepDiffusionModels(timestep); // step models

        for (DiffModel model : diffModels) { // get updates, returns empty if the model has not stepped in this timestep
            if (model instanceof ICModel) {
                ICModel icModel = (ICModel) model;
                //icModel.recordCurrentStepSpread((int) timestep); this is done in stepModels()

                HashMap<String, ArrayList<String>> latestUpdate = icModel.getLatestDiffusionUpdates();
                if (!latestUpdate.isEmpty()) {
                    logger.info("received updates from the IC model");
                    for (Map.Entry<String, ArrayList<String>> contents : latestUpdate.entrySet()) {
                        String content = contents.getKey();
                        ArrayList<String> agentIDs = contents.getValue();
                        logger.info("agents activated for content {} at time {} are: {}", content, (int) timestep, agentIDs.toString());

                        for (String id : agentIDs) { // for each agent create a DiffusionContent and put content type and parameters
                            //   DiffusionContent content = dataContainer.getOrCreateDiffusedContent(id);
                            String[] params = {content};
                            dataContainer.putContentToContentsMapFromDiffusionModel(id, DataTypes.INFORMATION, params);

                            //  content.getContentsMapFromDiffusionModel().put(contentType,params );
                        }
                    }

                }

            } else if (model instanceof LTModel) {

                LTModel ltModel = (LTModel) model;
                Map<String, HashMap<String, Double>> latestUpdate = ltModel.getLatestDiffusionUpdates();
                if (!latestUpdate.isEmpty()) {
                    logger.info("received updates from the LT model");
                    for (Map.Entry<String, HashMap<String, Double>> update : latestUpdate.entrySet()) {
                        String content = update.getKey();
                        HashMap<String, Double> contentLevels = update.getValue();
                        logger.info("received content levels for {} agents for content {} at time {}.", contentLevels.size(), content, (int) timestep);

                        for (String id : contentLevels.keySet()) { // for each agent create a DiffusionContent and put content type and parameters
                            //   DiffusionContent content = dataContainer.getOrCreateDiffusedContent(id);
                            Object[] params = {content, contentLevels.get(id)};
                            dataContainer.putContentToContentsMapFromDiffusionModel(id, DataTypes.INFLUENCE, params);
                        }
                    }
                }

            }

        }


    }

    @Override
    public SortedMap<Double, DiffusionDataContainer> sendData(double timestep, String dataType) {

//        double currentTimeInMinutes = Time.convertTime(timestep, timestepUnit, Time.TimestepUnit.SECONDS);
        //#FIXME  reducing 1 as dataserver checks this: (Double)this.timedUpdates.firstKey() < this.time.
        // As in our model eligible diffusion models here are selected by getNExtTimeForStep == time
        double time = timestep -1;
        // create data structure to store current step contents and params
        DiffusionDataContainer currentStepDataContainer = new DiffusionDataContainer();
//
//        Double nextTime = (timestep -1 )+ getShortestTimeStepOfAllDiffusionModels();
//        if (nextTime != null) {
//            dataServer.registerTimedUpdate(DataTypes.DIFFUSION_DATA_CONTAINER_FROM_DIFFUSION_MODEL, this, nextTime);

        List<DiffModel> diffModels = getDiffusionModelsToStepForCurrentTime(time);
        List<String> localContentTypesProcessed = new ArrayList<String>(); // used to clear local data structures at the end.
        List<String> globalContentTypesProcessed = new ArrayList<String>(); // used to clear global data structures at the end.

        logger.info("{} diffusion models are stepping at time {}: ", diffModels.size(),time);
        for (DiffModel model : diffModels) {

                // update the model with any new contents/ social states from BDI agents
                if (!localContentsFromBDIAgents.isEmpty()) {

                    for (String contentType : localContentsFromBDIAgents.keySet()) {

                        if (contentType.equals(DataTypes.INFORMATION) && (model instanceof ICModel)) {
                            localContentTypesProcessed.add(DataTypes.INFORMATION);
                            ICModel icModel = (ICModel) model;
                            HashMap<String, Set<String>> adoptedAgentSet = (HashMap<String, Set<String>>) localContentsFromBDIAgents.get(contentType);

                            Map<String, String[]> map = new HashMap<>(); //create a copy
                            for (String content : adoptedAgentSet.keySet()) { // process each content

                                Object[] set = adoptedAgentSet.get(content).toArray();
                                String[] newSet = new String[set.length];
                                for (int i = 0; i < set.length; i++) { // copy values
                                    newSet[i] = (String) set[i];
                                }

                                map.put(content, newSet);
                                logger.info(String.format("At time %.0f, total %d agents received content %s from BDI Model.", timestep, newSet.length, content));
                                logger.info("Agents spreading content are: {}", Arrays.toString(newSet));
                            }
                            icModel.updateSocialStatesFromLocalContent(map);
                        }

                        if (contentType.equals(DataTypes.INFLUENCE) && (model instanceof LTModel)) {
                            localContentTypesProcessed.add(DataTypes.INFLUENCE);
                            LTModel ltModel = (LTModel) model;

                            HashMap<String, HashMap<String, Double>> agentValueMapForContents = (HashMap<String, HashMap<String, Double>>) localContentsFromBDIAgents.get(contentType);
                            Map<String, HashMap<String, Double>> newContentMap = new HashMap<String, HashMap<String, Double>>(); //create a copy
                            for (String content : agentValueMapForContents.keySet()) { // process contents
                                HashMap<String, Double> newValueMap = new HashMap<String, Double>();
                                HashMap<String, Double> contentValuesForAgents = agentValueMapForContents.get(content);
                                for (String id : contentValuesForAgents.keySet()) {
                                    newValueMap.put(id, contentValuesForAgents.get(id));
                                }
                                newContentMap.put(content, newValueMap);
                            }
                            //update LT model
                            ltModel.updateSocialStatesFromLocalContent(newContentMap);
                        }

                    }
                    // now update global contents using the selected diffusion models
                    if (!globalContentsFromBDIAgents.isEmpty()) {

                        logger.info("Global content received to spread: {}", globalContentsFromBDIAgents.toString());

                        for (String contentType : globalContentsFromBDIAgents.keySet()) {
                            if (contentType.equals(DataTypes.INFORMATION) && (model instanceof ICModel)) {
                                globalContentTypesProcessed.add(DataTypes.INFORMATION);
                                String[] globalContents = globalContentsFromBDIAgents.get(contentType);
                                ICModel icModel = (ICModel) model;
                                icModel.updateSocialStatesFromGlobalContent(globalContents);

                            }

                            if (contentType.equals(DataTypes.INFLUENCE) && (model instanceof LTModel)) {
                                globalContentTypesProcessed.add(DataTypes.INFLUENCE);
                                String[] globalContents = globalContentsFromBDIAgents.get(contentType);
                                LTModel icModel = (LTModel) model;
                                icModel.updateSocialStatesFromGlobalContent(globalContents);

                            }

                        }

                    }
                }
            }

           if(!diffModels.isEmpty()) {


               // step the models
               stepDiffusionModelsAndUpdateDataContainer(currentStepDataContainer, time);

               //now put the current step data container to all steps data map
               if (!currentStepDataContainer.getDiffusionDataMap().isEmpty()) {
                   this.allStepsDiffusionData.put(time, currentStepDataContainer);
               }


               // clear the contents of only content types that are processed, another model may have received
               //updates from the BDI model, waiting to step in future
               for(String type: localContentTypesProcessed){
                   localContentsFromBDIAgents.remove(type);
               }
               for(String type: globalContentTypesProcessed){
                   globalContentsFromBDIAgents.remove(type);
               }



//        }


           }

        // register time for next update
        double nextTime = getEarliestTimeForNextStep();
        dataServer.registerTimedUpdate(DataTypes.DIFFUSION_DATA_CONTAINER_FROM_DIFFUSION_MODEL, this, nextTime);

        //+1 to avoid returning empty map for diffusion data for first step (toKey = fromKey)
        SortedMap<Double, DiffusionDataContainer> periodicDiffusionData = allStepsDiffusionData.subMap(lastUpdateTimeInMinutes, time + 1);
        lastUpdateTimeInMinutes = time;
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
                    if (!dc.getContentsMapFromBDIModel().isEmpty()) {
                        for (String localContentType : dc.getContentsMapFromBDIModel().keySet()) {

                            if (localContentType.equals(DataTypes.INFORMATION)) {
                                String[] contents = (String[]) dc.getContentsMapFromBDIModel().get(localContentType);
                                String content = contents[0]; // expected params: content (instance)
                                logger.debug("Agent {} received content {} of type {} ", agentId, content, localContentType);

                                Map<String, Set> icModelMap = (localContentsFromBDIAgents.containsKey(localContentType)) ?
                                        (Map<String, Set>) localContentsFromBDIAgents.get(localContentType) : new HashMap<String, Set>();
                                Set<String> agents = (icModelMap.containsKey(content)) ? icModelMap.get(content) :
                                        new HashSet<>();
                                agents.add(agentId);
                                icModelMap.put(content, agents);
                                localContentsFromBDIAgents.put(localContentType, icModelMap);
                            }
                            if (localContentType.equals(DataTypes.INFLUENCE)) {
                                Object[] contents = (Object[]) dc.getContentsMapFromBDIModel().get(localContentType);
                                String content = (String) contents[0]; // expected params: content (instance), contentValue
                                double value = (double) contents[1];
                                logger.debug("Agent {} received value {} for content {} of type {} ", agentId, value, content, localContentType);

                                Map<String, Map<String, Double>> MapForContentType = (localContentsFromBDIAgents.containsKey(localContentType)) ?
                                        (Map<String, Map<String, Double>>) localContentsFromBDIAgents.get(localContentType) : new HashMap<String, Map<String, Double>>();
                                Map<String, Double> MapForContent = (MapForContentType.containsKey(content)) ?
                                        (Map<String, Double>) MapForContentType.get(content) : new HashMap<String, Double>();
                                MapForContent.put(agentId, value);
                                MapForContentType.put(content, MapForContent);
                                localContentsFromBDIAgents.put(localContentType, MapForContentType);
                            }

                        }
                    }
                    //process global (broadcast) contents from BDI model
                    // required content_type, global content
                    if (!dc.getBroadcastContentsMapFromBDIModel().isEmpty()) {
                        for (String globalContentType : dc.getBroadcastContentsMapFromBDIModel().keySet()) {
                            String[] contentList = (String[]) dc.getBroadcastContentsMapFromBDIModel().get(globalContentType);
                            logger.debug("Agent {} received global contents {} of type {} ", agentId, contentList.toString(), globalContentType);
                            globalContentsFromBDIAgents.put(globalContentType, contentList);

//                            for (String globalContent : contentList) {
//                                if (!globalContentsFromBDIAgents.containsKey(globalContentType)) {
//                                    globalContentsFromBDIAgents.put(globalContentType, globalContent);
//                                }
//                            }

                        }
                    }
                    //process SN actions
                    if (!dc.getSnActionsMapFromBDIModel().isEmpty()) {
                        for (String action : dc.getSnActionsMapFromBDIModel().keySet()) {
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
     *
     * @param unit the time step unit to use
     */
    public void setTimestepUnit(Time.TimestepUnit unit) {
        timestepUnit = unit;
    }

    public void start() {
        if (this != null) {
            init();
            setTimestepUnit(Time.TimestepUnit.SECONDS);
            for (DiffModel model : diffModels) {
                model.setTimeForNextStep();
            }
            dataServer.registerTimedUpdate(DataTypes.DIFFUSION_DATA_CONTAINER_FROM_DIFFUSION_MODEL, this, Time.convertTime(startTimeInSeconds, Time.TimestepUnit.SECONDS, timestepUnit));
            logger.trace("registered time update ");
        } else {
            logger.warn("started but will be idle forever!!");
        }
    }

    /**
     * Start publishing data
     *
     * @param hhmm an array of size 2 with hour and minutes representing start time
     */
    public void start(int[] hhmm) {
        double startTimeInSeconds = Time.convertTime(hhmm[0], Time.TimestepUnit.HOURS, Time.TimestepUnit.SECONDS)
                + Time.convertTime(hhmm[1], Time.TimestepUnit.MINUTES, Time.TimestepUnit.SECONDS);
        dataServer.registerTimedUpdate(DataTypes.DIFFUSION_DATA_CONTAINER_FROM_DIFFUSION_MODEL, this, startTimeInSeconds);
    }


}
