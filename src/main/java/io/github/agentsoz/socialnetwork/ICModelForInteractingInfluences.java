package io.github.agentsoz.socialnetwork;

import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.Global;


import java.util.*;

import static io.github.agentsoz.socialnetwork.util.DataTypes.*;

public class ICModelForInteractingInfluences extends ICModel {


    HashMap<String, Double> probMap;
    List<String> supportContent;
    List<String> competeContent;


    public ICModelForInteractingInfluences(SocialNetworkDiffusionModel sn, int step, double prob) {
        super(sn, step, prob);
        probMap = new HashMap<String, Double>();
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


    @Override
    public void step() {


        this.currentStepActiveAgents.clear(); //clear previous step active agents.
        icDiffusion(DataTypes.COMBINE);

    }

    public void icDiffusion(String funcType) {


        for (SocialAgent agent : getAgentMap().values()) { // for each agent
            ArrayList<String> contentList = agent.getAdoptedContentList();
            if (!contentList.isEmpty()) {
                for (String content : contentList) { // for each content
//                    if(this.contentList.get(content).equals(DataTypes.GLOBAL)) {
//                        continue; //  only consider local content types for  network diffusion
//                    }

                    int exposedCount = 0;
                    List<Integer> neiIDs = new ArrayList<Integer>(agent.getLinkMap().keySet());

                    //Integer[] neiIDs = (Integer[]) agent.getLinkMap().keySet().toArray();
                    for (int nid : neiIDs) { //for each neigbour

                     //   if (!getAgentMap().get(nid).alreadyAdoptedContent(content) && !neighbourAlreadyExposed(agent.getID(), nid, content)) {
                        //single adoption assumption
                           if (getAgentMap().get(nid).getAdoptedContentList().isEmpty() && !neighbourAlreadyExposed(agent.getID(), nid, content)) {

                            //check if any competitive ones are already adopted
                            if (hasAdoptedAnyCompetitiveContent(content, nid)) {
                                continue;
                            }

                            //competitive A,B influence diffusion with different probabilities
                            if (funcType.equals(COMPETITIVE)) {
                                spreadContentWithProbability(content, nid);
                            }
                            // competitive A,B and combined AB (C)
                            else if (funcType.equals(COMBINE)) {
                                spreadABAndCombinedWithProbability(content, nid);
                            } else if (funcType.equals(SPLIT)) {
                                spreadSplittedABWithProbability(content,nid);
                            }

                            addExposureAttempt(agent.getID(), nid, content);
                            exposedCount++;

                        }
                    }

                    //update exposeCountMap
                    int newCount = this.dc.getExposedCountMap().get(content) + exposedCount;
                    this.dc.getExposedCountMap().put(content, newCount);
                }


            }
        }

        logger.trace(" ic diffusion procecss ended...");
    }

    private void spreadSplittedABWithProbability(String content, int nid) {

        if(content.equals(CONTENT_C)) { //handle C
            String[] contentArr = {CONTENT_A,CONTENT_B};
            List<String> cList = Arrays.asList(contentArr);
            Collections.shuffle(cList);
            // shuffle and use to be unbiased
            spreadContentWithProbability(cList.get(0),nid);
            //if  not active for the above content, try the other content
            if(getAgentMap().get(nid).getAdoptedContentList().isEmpty()) {
                spreadContentWithProbability(cList.get(1),nid);
            }
        }
        else{ // handle A and B as stadard
            spreadContentWithProbability(content,nid);
        }


    }


    private void spreadABAndCombinedWithProbability(String content, int nid) {

        ArrayList<String> exposedList = getAgentMap().get(nid).getExposedContentList();
        exposedList.add(content); //exposed to content now, active or not

        if (exposedList.contains(CONTENT_A) && exposedList.contains(CONTENT_B)) {

            spreadContentWithProbability(CONTENT_C, nid);
        } else {
            spreadContentWithProbability(content, nid);
        }

    }

    private void spreadContentWithProbability(String content, int nid) {
        //get activation probability for each content

        if (Global.getRandom().nextDouble() <= getProbMap().get(content)) { //activation

            //probabilistic diffusion successful
            updateSocialState(nid, content);
            addActiveAgentToCurrenStepActiveAgentsList(nid, content);

        }
        if (getAgentMap().get(nid).getExposedContentList().contains(content)) {
            getAgentMap().get(nid).getExposedContentList().add(content);
        }

//        else { // inactive-exposure
//            exposedCount++;
//        }
    }

    private boolean hasAdoptedAnyCompetitiveContent(String content, int neiID) {
        boolean result = false;
        for (String cometitiveContent : this.competeContent) {
            if (this.competeContent.contains(content) && getAgentMap().get(neiID).getAdoptedContentList().contains(cometitiveContent)) {
                result = true;
            }
        }
        return result;
    }

    public HashMap<String, Double> getProbMap() {
        return probMap;
    }


}
