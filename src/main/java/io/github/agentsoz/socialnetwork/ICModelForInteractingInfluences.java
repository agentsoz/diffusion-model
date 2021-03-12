package io.github.agentsoz.socialnetwork;

import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.Global;


import java.util.*;

import static io.github.agentsoz.socialnetwork.util.DataTypes.*;

public class ICModelForInteractingInfluences extends ICModel {


    // HashMap<String, Double> probMap;
    List<String> supportContent;
    List<String> competeContent;


    public ICModelForInteractingInfluences(SocialNetworkDiffusionModel sn, int step, double prob) {
        super(sn, step, prob);
        //    probMap = new HashMap<String, Double>();
        supportContent = new ArrayList<>();
        competeContent = new ArrayList<>();
    }


    public void initialise() {


        //instantiate adoptedContentList
        for (SocialAgent agent : getAgentMap().values()) {
            agent.initAdoptedContentList();
            agent.initExposedContentList();
        }

//        //register content type and contents
//        contentType = SNConfig.getContentType_ic();
//        for(String newContent: SNConfig.getContentsToRegisterForICModel()){
//            registerContentIfNotRegistered(newContent,DataTypes.LOCAL);
//        }

        // initalise seed
//        for(String content:this.contentList.keySet()){
//            initSeedBasedOnStrategy(content);
//        }

    }

    public void setSupportContentList(List<String> supportContent) {
        this.supportContent = supportContent;
    }

    public void setCompeteContentList(List<String> content) {
        this.competeContent = content;
    }

    public void initRandomSeed(int size, String newContent) {

        selectRandomSeed(size, newContent);
    }


    // @Override
    public void step() {


        this.currentStepActiveAgents.clear(); //clear previous step active agents.
        //     icDiffusion(DataTypes.COMBINE);

    }

    public void icDiffusion(HashMap<String, HashMap<String, Double>> allProbMap, boolean enableMemory, boolean singleAdoption) {

        HashMap<Integer, List<String>> currentStepExposures = new HashMap<Integer, List<String>>();

        if (!this.contentList.isEmpty()) { // if there are contents registered iterate over agents to receive msgs

            for (SocialAgent agent : getAgentMap().values()) { // for each agent

                //single adoption check
                if (singleAdoption && getAgentMap().get(agent.getID()).getAdoptedContentList().size() ==1) {
                    continue;
                }

//            ArrayList<String> contentList = agent.getAdoptedContentList();
                List<String> curStepExposedList = new ArrayList<String>();
                currentStepExposures.put(agent.getID(), curStepExposedList); // init new exposed list for current step

                List<Integer> neiIDs = new ArrayList<Integer>(agent.getLinkMap().keySet());

                for (int nid : neiIDs) {

                    for (String content : getAgentMap().get(nid).getAdoptedContentList()) {

                        //if the agent has already adopted, nothing to do with this content, try next one
                        if (getAgentMap().get(agent.getID()).getAdoptedContentList().contains(content)) {
                            continue;
                        }


                        // and nid->agent (onetime) exposure not done yet - posting from  a receive end now to accumilate all contents
                        if (!neighbourAlreadyExposed(nid, agent.getID(), content)) {
                            addExposureAttempt(nid, agent.getID(), content);

                            //if not already received before,add it. I do not consider multiple receivings of the same msg
                            if(!currentStepExposures.get(agent.getID()).contains(content)){
                                currentStepExposures.get(agent.getID()).add(content);
                            }
                            //update exposeCountMap
                            int count = this.dc.getExposedCountMap().get(content);
                            this.dc.getExposedCountMap().put(content, count + 1);


                        }
                    }


                }


            }
        }

        // now iteratve over agent map, and process activation  for all contents received/exposed,
        for (SocialAgent agent : getAgentMap().values()) { // for each agent

            ArrayList<String> curStepExposedContents = (ArrayList<String>) currentStepExposures.get(agent.getID());
            if (curStepExposedContents == null ||  (singleAdoption && agent.getAdoptedContentList().size()==1)) {
                // no processing required for this agent
                continue;
            }
            else if (!curStepExposedContents.isEmpty()) {
                if (!enableMemory) { // if no memory, use only current messages, one may get multiple attemepts for the same content.
                    processActivationForExposedContents(agent, curStepExposedContents, allProbMap,singleAdoption);
                } else { //now agents have memory

                    Set<String> exposedSet = new LinkedHashSet<>(agent.getExposedContentList()); //to avoid duplicates
                    exposedSet.addAll(curStepExposedContents);
                    ArrayList<String> combinedList = new ArrayList<>(exposedSet);
                    processActivationForExposedContents(agent, combinedList, allProbMap,singleAdoption);
                }

            }

        }


        logger.trace(" ic diffusion procecss ended...");
    }

    private void processActivationForExposedContents(SocialAgent agent, ArrayList<String> exposedContents, HashMap<String, HashMap<String, Double>> allProbMap,boolean singleAdoption) {

        HashMap<String, Double> probabilitiesForSituation = null;

        //check if any competitive ones are already adopted

        for (String content : exposedContents) {
                // check for any previous contradictory adoptions
            if (hasAdoptedAnyCompetitiveContent(content, agent.getID())) {
                return;
            }
        }
        //identify the probabilities based on the situation
            if (exposedContents.size() == 1) { // A or B
                probabilitiesForSituation = allProbMap.get(exposedContents.get(0));

//            } else if (exposedContents.size() == 1 && exposedContents.get(0).equals(CONTENT_C)) { // C
//                probabilitiesForSituation = allProbMap.get(CONTENT_C);

            } else if (exposedContents.size() == 2) { // A,B or A,C or B,C
                if (exposedContents.contains(CONTENT_A) && exposedContents.contains(CONTENT_B)) {
                    //A,B
                    probabilitiesForSituation = allProbMap.get(CONTENTSAB);
                } else if (exposedContents.contains(CONTENT_B) && exposedContents.contains(CONTENT_C)) {
                    //B,C
                    probabilitiesForSituation = allProbMap.get(CONTENTSBC);

                } else if (exposedContents.contains(CONTENT_A) && exposedContents.contains(CONTENT_C)) {
                    //A,C
                    probabilitiesForSituation = allProbMap.get(CONTENTSAC);

                }

            }
            else if (exposedContents.size() == 3){
                probabilitiesForSituation = allProbMap.get(CONTENTSABC);

            }
            // probability map set, now evaluate the probabilities
            processProbabilityChoiceForContents(probabilitiesForSituation, exposedContents, agent.getID(),singleAdoption);
//        }

//            //competitive A,B influence diffusion with different probabilities
//            if (func.equals(COMPETITIVE)) {
//                decideToSpreadOrNotBasedOnProbability(content, agent.getID());
//            }
//            // competitive A,B and combined AB (C)
//            else if (func.equals(COMBINE)) {
//                spreadABAndCombinedWithProbability(content, agent.getID());
//            } else if (func.equals(SPLIT)) {
//                spreadSplittedABWithProbability(content,agent.getID());
//            }


    }

//    private void spreadSplittedABWithProbability(String content, int nid) {
//
//        if(content.equals(CONTENT_C)) { //handle C
//            String[] contentArr = {CONTENT_A,CONTENT_B};
//            List<String> cList = Arrays.asList(contentArr);
//            Collections.shuffle(cList);
//            // shuffle and use to be unbiased
//            decideToSpreadOrNotBasedOnProbability(cList.get(0),nid);
//            //if  not active for the above content, try the other content
//            if(getAgentMap().get(nid).getAdoptedContentList().isEmpty()) {
//                decideToSpreadOrNotBasedOnProbability(cList.get(1),nid);
//            }
//        }
//        else{ // handle A and B as stadard
//            decideToSpreadOrNotBasedOnProbability(content,nid);
//        }
//
//
//    }


//    private void spreadABAndCombinedWithProbability(String content, int nid) {
//
//        ArrayList<String> exposedList = getAgentMap().get(nid).getExposedContentList();
////        exposedList.add(content); //exposed to content now, active or not
//
//        if (exposedList.contains(CONTENT_A) && exposedList.contains(CONTENT_B)) {
//
//            decideToSpreadOrNotBasedOnProbability(CONTENT_C, nid);
//        } else {
//            decideToSpreadOrNotBasedOnProbability(content, nid);
//        }
//
//    }

 //probability choice model
    private void processProbabilityChoiceForContents(HashMap<String, Double> probMap, List<String> contentList, int id, boolean singleAdoption) {

        if (probMap == null) {
            logger.error("empty probability map found for agent {} to process {}", id, contentList.toString());
            return;
        }

        double randomProb = Global.getRandom().nextDouble();
        double lower=0.0,upper=0.0;

        for (String content : probMap.keySet()) {
            double spreadProbability = probMap.get(content);
            upper = upper + spreadProbability;
            if(lower < randomProb && randomProb <= upper) {
                if(!content.equals(NOSPREAD)){
                    updateSocialState(id, content);
                    addActiveAgentToCurrenStepActiveAgentsList(id, content);
                }

            break; // no more evaluation
            }

            lower = upper;

        }

    }


//    private boolean decideToSpreadOrNotBasedOnProbability(String content, int id, double prob) {
//
//        boolean spread = false;
//        if (Global.getRandom().nextDouble() <= prob) { //activation
//
//            //probabilistic diffusion successful
//            spread = true;
//            updateSocialState(id, content);
//            addActiveAgentToCurrenStepActiveAgentsList(id, content);
//
//        } else {
//            // if not spreading, add current exposure to the exposure list of all steps
//            getAgentMap().get(id).getExposedContentList().add(content);
//        }
//
//        return spread;
//    }

    private boolean hasAdoptedAnyCompetitiveContent(String content, int neiID) {
        boolean result = false;
        if (!this.competeContent.isEmpty() || !(this.competeContent == null)) {
            for (String cometitiveContent : this.competeContent) {
                if (this.competeContent.contains(content) && getAgentMap().get(neiID).getAdoptedContentList().contains(cometitiveContent)) {
                    result = true;
                }
            }
        }
        return result;
    }

//    public HashMap<String, Double> getProbMap() {
//        return probMap;
//    }


}
