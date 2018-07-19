package io.github.agentsoz.socialnetwork;

import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.Global;
import io.github.agentsoz.socialnetwork.util.Utils;

import java.util.*;

public class ICModel extends DiffModel{

    private double meanDiffProbability;
    private ArrayList<String> contentList;
    private HashMap<String,ArrayList<String>> exposedMap;

    public ICModel(SocialNetworkManager sn, int step, double prob) {

        this.snManager = sn;
        this.diffStep = step;
        this.meanDiffProbability = prob;

        // instatiate contentList and exposedMap
        this.contentList = new ArrayList<String>();
        this.exposedMap =  new HashMap<String,ArrayList<String>>();

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


    public void registerContentIfNotRegistered(String newContent){

        if(!this.contentList.contains(newContent)) {

            // add new content type to contentList and exposed map
            this.contentList.add(newContent);
            this.exposedMap.put(newContent,new ArrayList<String>());

            logger.info("content {} registered in the IC model",newContent);
            return ;
        }

    }

    public void initSeedBasedOnStrategy() {
        if (SNConfig.getStrategy().equals(DataTypes.RANDOM)) {
            selectRandomSeed(SNConfig.getSeed(), this.contentList.get(0));
        }
    }

    public void selectRandomSeed(double seedPercentage, String content) {

        int numOfSeedAgents = getNumAgentsForSeed(seedPercentage);

        int selected = 0 ;
        List<Integer> idList = new ArrayList<Integer>(getAgentMap().keySet());
        Collections.shuffle(idList);

        while( selected < numOfSeedAgents) {

            int id = idList.get(selected);
            updateSocialState(id,content);
            selected++;

        }

        logger.info("ICModel - random seed: set {} agents for content {}", selected,content);


    }

    @Override
    public void updateSocialStatesFromBDIPercepts(Object data) {

        logger.debug("ICModel: updating social states based on BDI percepts");
        HashMap<String,Integer []> perceptMap = (HashMap<String,Integer []>) data ;

        for( Map.Entry entry: perceptMap.entrySet()) {

            String content = (String) entry.getKey();
            int[] agentIds = (int[]) entry.getValue();

            //register content if not registered
            registerContentIfNotRegistered(content);

            //adopt content
            setSpecificSeed(agentIds,content);
        }

    }

    public void setSpecificSeed(int[] idArray, String content) {

        for(int id:idArray) {
            updateSocialState(id,content);
        }
    }


    public void icDiffusion() {

        for(SocialAgent agent: getAgentMap().values()) { // for each agent
            ArrayList<String> contentList = agent.getAdoptedContentList();
            if(!contentList.isEmpty()) {
                for(String content: contentList) { // for each content

                    List<Integer> neiIDs = new ArrayList<Integer>(agent.getLinkMap().keySet());

                    //Integer[] neiIDs = (Integer[]) agent.getLinkMap().keySet().toArray();
                    for(int nid: neiIDs) { //for each neigbour

                        if(!getAgentMap().get(nid).alreadyAdoptedContent(content) && !neighbourAlreadyExposed(agent.getID(),nid,content)) {

                            if(Global.getRandom().nextDouble() <= getRandomDiffProbability()) {

                                //probabilistic diffusion successful
                                updateSocialState(nid,content);

                            }

                            addExposureAttempt(agent.getID(),nid,content);
                        }
                    }
                }


            }
        }
    }

    public double getRandomDiffProbability() {
        return Utils.getRandomGaussionWithinThreeSD(SNConfig.getStandardDeviation(),getMeanDiffProbability());
    }
    public void addExposureAttempt(int nodeID, int neighbourID, String content) {

        if(!this.contentList.contains(content) || !this.exposedMap.containsKey(content)) {
            logger.error("content {} not registered properly in the IC model", content);
            return ;
        }

        String directedLinkID = String.valueOf(nodeID).concat(String.valueOf(neighbourID));
      //  logger.info("linkID: {}",directedLinkID);

        ArrayList<String> attemptList =  this.exposedMap.get(content);
        if(attemptList.contains(directedLinkID)) {
            logger.warn("Exposure attempt for content {} already exists: node {} neighbour {}", content, nodeID,neighbourID);
            return;
        }

        attemptList.add(directedLinkID);


    }
    public boolean neighbourAlreadyExposed(int nodeID, int neighbourID, String content) {

        String directedLinkID = String.valueOf(nodeID).concat(String.valueOf(neighbourID));
      //  logger.info("linkID: {}",directedLinkID);

        ArrayList<String> attemptList =  this.exposedMap.get(content);
        if(attemptList.contains(directedLinkID))
        {
            return true;
        }
        else{

            return false;
        }
    }


    public void updateSocialState(int id, String content) {

       SocialAgent agent =  getAgentMap().get(id);
       agent.adoptContent(content);


    }
}
