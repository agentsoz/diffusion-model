package io.github.agentsoz.socialnetwork.datacollection;

import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.SocialNetworkManager;
import io.github.agentsoz.socialnetwork.util.DiffusedContent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class ICModelDataCollector {


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

        BufferedWriter bw = null;
        FileWriter fw = null;

        try {

            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);


            //get all diffused contents
            String[] allContentList = (String[]) overallSpreadMap.lastEntry().getValue().getcontentSpreadMap().keySet().toArray();
            // write table header
            bw.write("Time");
            for(String content: allContentList) {
                bw.write("\t");
                bw.write(content);
            }

            for(double timestep: overallSpreadMap.keySet()){

                int[] countArr = new int[allContentList.length];
                int index =0;
                DiffusedContent dc = overallSpreadMap.get(timestep);

                for(String content: allContentList) {
                    //get number of agents adopted the particular content
                    int contentCount = dc.getAdoptedAgentCountForContent(content);
                    countArr[index] = contentCount;
                    index++;
                }

                // writing count values
                bw.write("" +timestep);
                for(int count: countArr){
                    bw.write(count);

                }


            }

            System.out.println("Done");

        } catch (IOException e) {

            e.printStackTrace();

        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
