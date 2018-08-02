package io.github.agentsoz.socialnetwork.datacollection;

import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.SocialNetworkManager;
import io.github.agentsoz.socialnetwork.util.DiffusedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class ICModelDataCollector {

    final static Logger logger = LoggerFactory.getLogger("");
    TreeMap<Double, HashMap<String, Integer>> icDiffSpread = new TreeMap<Double, HashMap<String, Integer>>();

    public  ICModelDataCollector() {
    }

    public void collectCurrentStepSpreadData(SocialNetworkManager sn, ArrayList<String> currentContentList, double time) {

        HashMap<String, Integer> currentSpreadCountMap = new HashMap<String, Integer>();
        for(String content: currentContentList) {
            int numAdoptions = getAdoptedAgentCountForContent(sn,content);
            currentSpreadCountMap.put(content,numAdoptions);
        }

        this.icDiffSpread.put(time,currentSpreadCountMap); // finally store timed hashmap

    }
    public  int getAdoptedAgentCountForContent(SocialNetworkManager sn, String content) {

        // non-adopted agents = totAgents - adoptedAgents
        int counter = 0;
        for(SocialAgent agent: sn.getAgentMap().values()) {
            if(agent.alreadyAdoptedContent(content)) {
                counter++;
            }

        }

        return counter;
    }

    public  Integer[] getAdoptedAgentIdArrayForContent(SocialNetworkManager sn, String content) {

        ArrayList<Integer> adoptedAgentIDList =  new ArrayList<Integer>();
        for(SocialAgent agent: sn.getAgentMap().values()) {
            if(agent.alreadyAdoptedContent(content)) {
                adoptedAgentIDList.add(agent.getID());
            }

        }

            return adoptedAgentIDList.toArray(new Integer[adoptedAgentIDList.size()]);
    }


        public  void writeSpreadDataToFile(String file) {

        PrintWriter  dataFile=null;
        try {
            if(dataFile == null) {
                dataFile = new PrintWriter(file, "UTF-8");

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

                        dataFile.print("\t\t" + count);

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

//    public static void writeICDiffusionOutputsToFile(TreeMap<Double, DiffusedContent> overallSpreadMap, String file) {
//
//        PrintWriter  dataFile=null;
//        try {
//            if(dataFile == null) {
//                dataFile = new PrintWriter(file, "UTF-8");
//
//
//                //get all diffused contents
//                Set<String> contentSet =  overallSpreadMap.lastEntry().getValue().getcontentSpreadMap().keySet();
//                ArrayList<String> allContentList = new ArrayList<String>(contentSet);
//
//                // write table header
//                dataFile.print("Time");
//                for (String content : allContentList) {
//                    dataFile.print("\t");
//                    dataFile.print(content);
//                }
//
//                dataFile.println("");
//                for (double timestep : overallSpreadMap.keySet()) {
//
//                    int[] countArr = new int[allContentList.size()]; // counters for all diffused content
//                    int index = 0;
//                    DiffusedContent dc = overallSpreadMap.get(timestep);
//
//                    for (String content : allContentList) {
//                        //get number of agents adopted the particular content
//                        int contentCount = dc.getAdoptedAgentCountForContent(content);
//                        countArr[index] = contentCount;
//                        index++;
//                    }
//
//                    // writing count values
//                    dataFile.print(timestep);
//                    for (int count : countArr) {
//                        dataFile.print("\t");
//                        dataFile.print(count);
//
//                    }
//
//
//                }
//            }
//            System.out.println("Done");
//
//        } catch (FileNotFoundException e) {
//                // TODO Auto-generated catch block
//                logger.debug(" datafile path not found: {}", e.getMessage());
//                e.printStackTrace();
//            } catch (UnsupportedEncodingException e) {
//                // TODO Auto-generated catch block
//                logger.debug(" datafile - UnsupportedEncodingException : {}", e.getMessage());
//                e.printStackTrace();
//            }finally {
//                dataFile.close();
//            }
//    }
}
