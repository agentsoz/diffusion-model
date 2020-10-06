package io.github.agentsoz.socialnetwork;

import io.github.agentsoz.socialnetwork.datacollection.ICModelDataCollector;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.Global;
import io.github.agentsoz.socialnetwork.util.Utils;

import java.util.*;
/*
    IC Model should handle  and pass back String array of agent ids
    current  handling methods:
    updateSocialStatesFromBDIPercepts()
    getLatestDiffusionUpdates()
 */
public class ICModel extends DiffModel{

    private double meanDiffProbability;
    private HashMap<String,String> contentList; // content, type (local/ global)
    private ArrayList<String> diffusedGlobalContent; // to ensure global content are diffused just one time.
    private HashMap<String,ArrayList<String>> attemptedLinksMap;
    private ICModelDataCollector dc;
    private HashMap<String, ArrayList<String>> currentStepActiveAgents =  new HashMap<String, ArrayList<String>>();

    public ICModel(SocialNetworkModel sn, int step, double prob) {

        this.snManager = sn;
        this.diffStep = step;
        this.meanDiffProbability = prob;

        // instatiate contentList and exposedMap
        this.contentList = new HashMap<String,String>();
        this.diffusedGlobalContent = new ArrayList<String>();
        this.attemptedLinksMap =  new HashMap<String,ArrayList<String>>();
        this.dc = new ICModelDataCollector();

    }

    public double getMeanDiffProbability() {
        return meanDiffProbability;
    }

    @Override
    public void initialise() {


        //instantiate adoptedContentList
        for(SocialAgent agent: getAgentMap().values()) {
            agent.initAdoptedContentList();
        }

    }

    public void initRandomSeed(String newContent) {

        registerContentIfNotRegistered(newContent,DataTypes.LOCAL);
        selectRandomSeed(SNConfig.getSeed_ic(), newContent);
    }

    public void registerContentIfNotRegistered(String newContent, String type){

        if(!this.contentList.keySet().contains(newContent)) {

            // applicable for both local and global contents
            this.contentList.put(newContent,type);

            if(type.equals(DataTypes.LOCAL)) { //  exposed applicable for local contents
                this.attemptedLinksMap.put(newContent,new ArrayList<String>());
                this.dc.getExposedCountMap().put(newContent,0);

                logger.trace("IC model: exposed counters initialised for content {} ",newContent);

            }


            logger.info("IC model: registered {} content type {}", type, newContent);
            return ;
        }

    }

    public void initSeedBasedOnStrategy(String content) {
        if (SNConfig.getStrategy_ic().equals(DataTypes.RANDOM)) {
            selectRandomSeed(SNConfig.getSeed_ic(), content);
        }

    }

    public void selectRandomSeed(double seedPercentage, String content) {

        int numOfSeedAgents = getNumAgentsForSeed(seedPercentage);

        int selected = 0 ;
        List<Integer> idList = new ArrayList<Integer>(getAgentMap().keySet());
        Collections.shuffle(idList,Global.getRandom()); // provide the random object for deterministic behaviour for testing

        while( selected < numOfSeedAgents) {

            int id = idList.get(selected);
            updateSocialState(id,content);
            selected++;

        }

        logger.info("ICModel - random seed: set {} agents for content {}", selected,content);

    }


    // set seed/state from external model, use only for global content types
    public void updateSocialStatesFromGlobalContent(Object data) {

        logger.info("ICModel: broadcasting global messages to social agents");
        ArrayList<String> contents = (ArrayList<String>) data ;

        for( String content: contents) {

            // if not diffused, diffuse content
            if(!this.diffusedGlobalContent.contains(content)) {

                //register first
                registerContentIfNotRegistered(content, DataTypes.GLOBAL);

                logger.info("diffusing global content: {} ", content);
                //get ids of all agents to broadcast message
                Integer[] intIdArray = getAgentMap().keySet().toArray(new Integer[getAgentMap().size()]);
                setSpecificSeed(intIdArray, content);

                //finally add to diffused contents
                this.diffusedGlobalContent.add(content);
            }
        }

    }


    // set seed/state from external model, use only for local content types
    public void updateSocialStatesFromLocalContent(Object data) {

        logger.debug("ICModel: updating social states from local contents");
        HashMap<String,String []> perceptMap = (HashMap<String,String []>) data ;

        for( Map.Entry entry: perceptMap.entrySet()) {

            String content = (String) entry.getKey();
            String[] agentIds = (String[]) entry.getValue();

            //register content if not registered
            registerContentIfNotRegistered(content,DataTypes.LOCAL);

            logger.trace("content: {} id list: {}", content, agentIds);

            //convert the String array to Integer
            Integer[] intIdArray = new Integer[agentIds.length];
            for(int i =0; i < agentIds.length; i++) {
                intIdArray[i] = Integer.parseInt(agentIds[i]);
            }

            setSpecificSeed(intIdArray,content);
        }

        logger.debug("social states updated");
    }

    public void setSpecificSeed(Integer[] idArray, String content) {

        for(Integer id:idArray) {
            updateSocialState(id,content);
        }
    }


    @Override
    public void doDiffProcess() {


        this.currentStepActiveAgents.clear(); //clear previous step active agents.
        icDiffusion();

    }

    public void icDiffusion() {

        logger.trace("starting ic diffusion procecss...");
        for(SocialAgent agent: getAgentMap().values()) { // for each agent
            ArrayList<String> contentList = agent.getAdoptedContentList();
            if(!contentList.isEmpty()) {
                for(String content: contentList) { // for each content
                    if(this.contentList.get(content).equals(DataTypes.GLOBAL)) {
                        continue; //  only consider local content types for  network diffusion
                    }

                    int exposedCount = 0;
                    List<Integer> neiIDs = new ArrayList<Integer>(agent.getLinkMap().keySet());

                    //Integer[] neiIDs = (Integer[]) agent.getLinkMap().keySet().toArray();
                    for(int nid: neiIDs) { //for each neigbour

                        if(!getAgentMap().get(nid).alreadyAdoptedContent(content) && !neighbourAlreadyExposed(agent.getID(),nid,content)) {

                            if(Global.getRandom().nextDouble() <= getRandomDiffProbability()) { //activation

                                //probabilistic diffusion successful
                                updateSocialState(nid,content);
                                addActiveAgentToCurrenStepActiveAgentsList(nid,content);

                            }
                            else{ // inactive-exposure
                                exposedCount++;
                            }

                            addExposureAttempt(agent.getID(),nid,content);

                        }
                    }

                    //update exposeCountMap
                    int newCount = this.dc.getExposedCountMap().get(content) + exposedCount;
                    this.dc.getExposedCountMap().put(content,newCount);
                }


            }
        }

        logger.trace(" ic diffusion procecss ended...");
    }

    private void addActiveAgentToCurrenStepActiveAgentsList(int agentid, String content) {

            ArrayList<String> idList = this.currentStepActiveAgents.get(content);
            if(idList == null) {
                idList =  new ArrayList<String>();
                this.currentStepActiveAgents.put(content,idList);
            }

            idList.add(String.valueOf(agentid));

    }

    public double getRandomDiffProbability() {
        return Utils.getRandomGaussionWithinThreeSD(SNConfig.getStandardDeviation_ic(),getMeanDiffProbability());
    }

    public void addExposureAttempt(int nodeID, int neighbourID, String content) {

        if(!this.contentList.keySet().contains(content) || !this.attemptedLinksMap.containsKey(content)) {
            logger.error("content {} not registered properly in the IC model", content);
            return ;
        }

        if(this.contentList.get(content).equals(DataTypes.GLOBAL)) { // exposure attempts are not needed for global content
            return ;
        }

        String directedLinkID = String.valueOf(nodeID).concat(String.valueOf(neighbourID));
      //  logger.info("linkID: {}",directedLinkID);

        ArrayList<String> attemptList =  this.attemptedLinksMap.get(content);
        if(attemptList.contains(directedLinkID)) {
            logger.warn("Exposure attempt for content {} already exists: node {} neighbour {}", content, nodeID,neighbourID);
            return;
        }

        attemptList.add(directedLinkID);


    }


    public boolean neighbourAlreadyExposed(int nodeID, int neighbourID, String content) {

        String directedLinkID = String.valueOf(nodeID).concat(String.valueOf(neighbourID));
      //  logger.info("linkID: {}",directedLinkID);

        if(this.attemptedLinksMap.get(content) == null) {
            logger.error("no attempted links map found");
        }
        ArrayList<String> attemptList =  this.attemptedLinksMap.get(content);
        if(attemptList.contains(directedLinkID))
        {
            return true;
        }
        else {

            return false;
        }
    }

//    public int getTotExposedAgents(){
//        int ct=0;
//        for(ArrayList<String> attemptLists: this.exposedMap.values()) {
//            ct=ct+ attemptLists.size();
//        }
//        return ct;
//    }

    public HashMap<String, ArrayList<String>> getLatestDiffusionUpdates() {

        return this.currentStepActiveAgents;

//        HashMap<String, String[]> latestSpread =  new HashMap<String, String[]>();
//        for(String content: this.contentList.keySet()) { // want to send both global and local content types for reasoning.
//
//           Integer[] contentArray =  this.dc.getAdoptedAgentIdArrayForContent(snManager,content);
//
//           //convert Integer[] to  String[] and pass back to the BDI model
//            String[] strIdArray = new String[contentArray.length];
//            for(int i =0; i < contentArray.length; i++) {
//                strIdArray[i] = String.valueOf(contentArray[i]);
//            }
//
//           latestSpread.put(content,strIdArray);
//        }
//            return latestSpread;
    }


    public void updateSocialState(int id, String content) {

       SocialAgent agent =  getAgentMap().get(id);
       agent.adoptContent(content);


    }

    public void recordCurrentStepSpread(double timestep) { // recording total active agents

        this.dc.collectCurrentStepSpreadData(this.snManager,this.contentList.keySet(),timestep);
    }

    public ICModelDataCollector getDataCollector() {
        return this.dc;
    }

    public void finish(){
        logger.info("total number of inactive agents: {} ", this.dc.getTotalInactiveAgents(snManager));

        for(Map.Entry entry : contentList.entrySet()) {
            String content = (String) entry.getKey();
            String type = (String) entry.getValue();
            logger.info(" Content {} : type: {} active agents= {} | exposed agents {}", content,type, this.dc.getAdoptedAgentCountForContent(snManager,content), this.dc.getExposedAgentCountForContent(content));
        }

    }
}
