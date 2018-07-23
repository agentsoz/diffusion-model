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


    public static int getAdoptedAgentCountForContent(SocialNetworkManager sn, String content) {

        // non-adopted agents = totAgents - adoptedAgents
        int counter = 0;
        for(SocialAgent agent: sn.getAgentMap().values()) {
            if(agent.alreadyAdoptedContent(content)) {
                counter++;
            }

        }

        return counter;
    }

    public static Integer[] getAdoptedAgentIdArrayForContent(SocialNetworkManager sn, String content) {

        ArrayList<Integer> adoptedAgentIDList =  new ArrayList<Integer>();
        for(SocialAgent agent: sn.getAgentMap().values()) {
            if(agent.alreadyAdoptedContent(content)) {
                adoptedAgentIDList.add(agent.getID());
            }

        }

            return adoptedAgentIDList.toArray(new Integer[adoptedAgentIDList.size()]);
    }

    public static void writeICDiffusionOutputsToFile(TreeMap<Double, DiffusedContent> overallSpreadMap, String file) {

        PrintWriter  dataFile=null;
        try {
            if(dataFile == null) {
                dataFile = new PrintWriter(file, "UTF-8");


                //get all diffused contents
                Set<String> contentSet =  overallSpreadMap.lastEntry().getValue().getcontentSpreadMap().keySet();
                ArrayList<String> allContentList = new ArrayList<String>(contentSet);

                // write table header
                dataFile.print("Time");
                for (String content : allContentList) {
                    dataFile.print("\t");
                    dataFile.print(content);
                }

                dataFile.println("");
                for (double timestep : overallSpreadMap.keySet()) {

                    int[] countArr = new int[allContentList.size()]; // counters for all diffused content
                    int index = 0;
                    DiffusedContent dc = overallSpreadMap.get(timestep);

                    for (String content : allContentList) {
                        //get number of agents adopted the particular content
                        int contentCount = dc.getAdoptedAgentCountForContent(content);
                        countArr[index] = contentCount;
                        index++;
                    }

                    // writing count values
                    dataFile.print(timestep);
                    for (int count : countArr) {
                        dataFile.print("\t");
                        dataFile.print(count);

                    }


                }
            }
            System.out.println("Done");

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
}
