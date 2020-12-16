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

    public void icDiffusion(HashMap<String,HashMap<String,Double>> allProbMap) {

        HashMap<Integer,List<String>> currentStepExposures = new  HashMap<Integer, List<String>>();

        if (!this.contentList.isEmpty()) { // if there are contents registered iterate over agents to receive msgs

            for (SocialAgent agent : getAgentMap().values()) { // for each agent

                //single adoption assumption:
                if(!getAgentMap().get(agent.getID()).getAdoptedContentList().isEmpty()){
                    continue;
                }

//            ArrayList<String> contentList = agent.getAdoptedContentList();
            List<String> curStepExposedList =  new ArrayList<String>();
            currentStepExposures.put(agent.getID(),curStepExposedList); // init new exposed list for current step

                    List<Integer> neiIDs = new ArrayList<Integer>(agent.getLinkMap().keySet());

                    for (int nid : neiIDs) {

                        for (String content : getAgentMap().get(nid).getAdoptedContentList()) {

                               //if the agent has already adopted, nothing to do with this content, try next one
//                            if(getAgentMap().get(agent.getID()).getAdoptedContentList().contains(content)){
//                            continue;
//                        }


                            // if not already received before - remove this condition consider multiple exposure to same content
                            // and nid->agent exposure not done yet - posting from  a receive end now to accumilate all contents
                            if(!currentStepExposures.get(agent.getID()).contains(content) && !neighbourAlreadyExposed(nid, agent.getID(), content)) {
                                currentStepExposures.get(agent.getID()).add(content);
                                addExposureAttempt(nid,agent.getID(), content);

                                //update exposeCountMap
                                int count = this.dc.getExposedCountMap().get(content);
                                this.dc.getExposedCountMap().put(content, count+1);



                        }
                    }


                }


            }
        }

        // now iteratve over agent map, and process activation  for all contents received/exposed,
        for (SocialAgent agent : getAgentMap().values()) { // for each agent

        ArrayList<String> curStepExposedContents = (ArrayList<String>) currentStepExposures.get(agent.getID());
        if(curStepExposedContents == null){
            continue;
        }
        if(!curStepExposedContents.isEmpty()){
            processActivationForExposedContents( agent,curStepExposedContents, allProbMap);

        }

        }


            logger.trace(" ic diffusion procecss ended...");
    }

    private void processActivationForExposedContents( SocialAgent agent, ArrayList<String> curStepExposedContents, HashMap<String,HashMap<String,Double>> allProbMap) {

        HashMap<String,Double> probabilitiesForSituation = null;

        //check if any competitive ones are already adopted
        for(String content:curStepExposedContents) {
            if (hasAdoptedAnyCompetitiveContent(content, agent.getID())) {
                continue;
            }
        }
            if(curStepExposedContents.size() == 1){ // A,B or C
                    probabilitiesForSituation  = allProbMap.get(curStepExposedContents.get(0));

            }
            else if(curStepExposedContents.size() == 2){ // A,B or A,C or B,C
                if(curStepExposedContents.contains(CONTENT_A) && curStepExposedContents.contains(CONTENT_B)){
                    //A,B
                    probabilitiesForSituation  = allProbMap.get(CONTENTSAB);
                }
                else if(curStepExposedContents.contains(CONTENT_B) && curStepExposedContents.contains(CONTENT_C)){
                    //B,C
                    probabilitiesForSituation  = allProbMap.get(CONTENTSBC);

                }
                else if(curStepExposedContents.contains(CONTENT_A) && curStepExposedContents.contains(CONTENT_C)){
                    //A,C
                    probabilitiesForSituation  = allProbMap.get(CONTENTSAC);

                }

            }

            processProbabilityChoiceForContents(probabilitiesForSituation,curStepExposedContents,agent.getID());

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


    private void processProbabilityChoiceForContents(HashMap<String,Double> probMap, List<String> contentList, int id){

        if(probMap == null) {
            logger.error("empty probability map found for agent {} to process {}",id, contentList.toString());
            return;
        }
        //#tested separately, but only chosen one
        // not spreading is directly evaluated, but when contents are tested, indirectly it will consider not
        //spreading probability.
        for(String content: contentList){
            double spreadProbability = probMap.get(content);
            if(decideToSpreadOrNotBasedOnProbability(content,id,spreadProbability)){
                    break;
            }
        }

    }

    private boolean decideToSpreadOrNotBasedOnProbability(String content, int id, double prob) {

        boolean spread=false;
        if (Global.getRandom().nextDouble() <= prob) { //activation

            //probabilistic diffusion successful
            spread=true;
            updateSocialState(id, content);
            addActiveAgentToCurrenStepActiveAgentsList(id, content);

        }
        // spreading or not, add current exposure to the exposure list of all steps
        getAgentMap().get(id).getExposedContentList().add(content);
        return spread;
    }

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
