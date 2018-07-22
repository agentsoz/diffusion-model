package io.github.agentsoz.socialnetwork.datacollection;

import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.SocialNetworkManager;

import java.util.ArrayList;

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
}
