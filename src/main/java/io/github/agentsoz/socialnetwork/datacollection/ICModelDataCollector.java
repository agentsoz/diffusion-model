package io.github.agentsoz.socialnetwork.datacollection;

import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.SocialNetworkDiffusionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class ICModelDataCollector extends DataCollector{

    final static Logger logger = LoggerFactory.getLogger("");
    TreeMap<Double, HashMap<String, Integer>> icDiffSpread;
    HashMap<String, Integer> exposedCountMap; // for each content, number of agents that was exposed to the content (inactive-exposed)

    public  ICModelDataCollector() {
        this.exposedCountMap = new HashMap<String, Integer>();
        this.icDiffSpread = new TreeMap<Double, HashMap<String, Integer>>();
    }


    public void collectCurrentStepSpreadData(SocialNetworkDiffusionModel sn, Collection<String> currentContentList, double time) {

        HashMap<String, Integer> currentSpreadCountMap = new HashMap<String, Integer>();
        for(String content: currentContentList) {
            int numAdoptions = getAdoptedAgentCountForContent(sn,content);
            currentSpreadCountMap.put(content,numAdoptions);
        }

        this.icDiffSpread.put(time,currentSpreadCountMap); // finally store timed hashmap

    }

    public  int getTotalInactiveAgents(SocialNetworkDiffusionModel sn) {

        // total inactive agents, both inactive-exposed and inactive-unexposed
        int counter = 0;
        for(SocialAgent agent: sn.getAgentMap().values()) {
            if(agent.getAdoptedContentList().isEmpty()) {
                counter++;
            }

        }

        return counter;
    }

    public  int getExposedAgentCountForContent(String content) {
        if(!this.getExposedCountMap().containsKey(content)) { // global content
            logger.info("content type {} not found in exposed count map, probably global content, returning 0", content);
            return 0;
        }
        else{
            return this.getExposedCountMap().get(content); // local content
        }
    }

    public  int getAdoptedAgentCountForContent(SocialNetworkDiffusionModel sn, String content) {

        // non-adopted agents = totAgents - adoptedAgents
        int counter = 0;
        for(SocialAgent agent: sn.getAgentMap().values()) {
            if(agent.alreadyAdoptedContent(content)) {
                counter++;

                if(agent.getAdoptedContentList().size() ==2){
                    logger.info("TWO CONTENTS agent: {} contents: {} ", agent.getID(),agent.getAdoptedContentList().toString());
                }
            }



        }

        return counter;
    }

    public  Integer[] getAdoptedAgentIdArrayForContent(SocialNetworkDiffusionModel sn, String content) {

        ArrayList<Integer> adoptedAgentIDList =  new ArrayList<Integer>();
        for(SocialAgent agent: sn.getAgentMap().values()) {
            if(agent.alreadyAdoptedContent(content)) {
                adoptedAgentIDList.add(agent.getID());
            }

        }

            return adoptedAgentIDList.toArray(new Integer[adoptedAgentIDList.size()]);
    }

        //version1:  output to a given to file path
        public  void writeSpreadDataToFile(String fileName) {

            File file = new File(fileName); // create output directory if not exists
            if (!file.exists()) {
                if (file.getParentFile().mkdir()) {
                    logger.debug(" IC model data collection output dir created");
                }
            }

            logger.info("creating diffusion output file: {} ", fileName);

        PrintWriter  dataFile=null;
        try {
            if(dataFile == null) {
                dataFile = new PrintWriter(fileName, "UTF-8");

                double lastTimeStep = this.icDiffSpread.lastKey(); // returns highest value stored
                Set<String> finalContentSet = this.icDiffSpread.get(lastTimeStep).keySet();

                // write table header
                dataFile.print("time");
                for(String c: finalContentSet){
                    dataFile.print("\t" + c);
                }

                dataFile.println();
                for(Map.Entry<Double,HashMap<String, Integer>> entry: this.icDiffSpread.entrySet()) {

                    double time = entry.getKey();
                    dataFile.print(time);
                    HashMap<String,Integer> stepSpreadCountMap = entry.getValue();
                    for(String con: finalContentSet) { // full content list

                        int count;
                        if(!stepSpreadCountMap.containsKey(con)) {
                            count = 0;
                        }
                        else{
                            count = stepSpreadCountMap.get(con);
                        }

                        dataFile.print("\t" + count);

                    }

                    dataFile.println();

                }

            }

        } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                logger.debug(" datafile path not found: {}", e.getMessage());
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                logger.debug(" datafile - UnsupportedEncodingException : {}", e.getMessage());
                e.printStackTrace();
            }finally {
                dataFile.close();
            }

        }

    // version2: use the output file specified in the SNConfig.
    public  void writeSpreadDataToFile() {

        String fileName = SNConfig.getOutputFilePathOfTheICModel();

        File file = new File(fileName); // create output directory if not exists
        if (!file.exists()) {
            if (file.getParentFile().mkdir()) {
               // logger.debug(" IC model data collection output dir created");
            }
        }

        logger.info("IC Model: creating diffusion output file: {} ", fileName);

        PrintWriter  dataFile=null;
        try {
            if(dataFile == null) {
                dataFile = new PrintWriter(fileName, "UTF-8");

                double lastTimeStep = this.icDiffSpread.lastKey(); // returns highest value stored
                Set<String> finalContentSet = this.icDiffSpread.get(lastTimeStep).keySet();

                // write table header
                dataFile.print("time");
                for(String c: finalContentSet){
                    dataFile.print("\t" + c);
                }

                dataFile.println();
                for(Map.Entry<Double,HashMap<String, Integer>> entry: this.icDiffSpread.entrySet()) {

                    double time = entry.getKey();
                    dataFile.print(time);
                    HashMap<String,Integer> stepSpreadCountMap = entry.getValue();
                    for(String con: finalContentSet) { // full content list

                        int count;
                        if(!stepSpreadCountMap.containsKey(con)) {
                            count = 0;
                        }
                        else{
                            count = stepSpreadCountMap.get(con);
                        }

                        dataFile.print("\t" + count);

                    }

                    dataFile.println();

                }

            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            logger.debug(" datafile path not found: {}", e.getMessage());
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            logger.debug(" datafile - UnsupportedEncodingException : {}", e.getMessage());
            e.printStackTrace();
        }finally {
            dataFile.close();
        }

    }

    public HashMap<String, Integer> getExposedCountMap() {
        return exposedCountMap;
    }


}
