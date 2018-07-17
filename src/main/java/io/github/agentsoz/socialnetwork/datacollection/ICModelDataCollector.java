package io.github.agentsoz.socialnetwork.datacollection;

import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.SocialNetworkManager;

public class ICModelDataCollector {


    public static int getAdoptedAgentsForContent(SocialNetworkManager sn, String content) {

        // non-adopted agents = totAgents - adoptedAgents
        int counter = 0;
        for(SocialAgent agent: sn.getAgentMap().values()) {
            if(agent.alreadyAdoptedContent(content)) {
                counter++;
            }

        }

        return counter;
    }
}
