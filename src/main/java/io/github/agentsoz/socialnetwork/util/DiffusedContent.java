package io.github.agentsoz.socialnetwork.util;

import java.util.HashMap;

public class DiffusedContent {

    // contenttype, active agents IDs
    HashMap<String,Integer[]> oneStepSpreadMap;

    public DiffusedContent()
    {
        this.oneStepSpreadMap = new HashMap<String,Integer[]>();
    }

    public int getTotalDiffusionContents() {
        return oneStepSpreadMap.size();
    }

    public int getAdoptedAgentCountForContent(String c) {
        if (this.oneStepSpreadMap.containsKey(c)) {

            return this.oneStepSpreadMap.get(c).length;
        }
        else{
            return 0; // no agent has adopted the content
        }
    }
    public void setContentSpreadMap(HashMap<String,Integer[]> currentSpreadMap) {

        this.oneStepSpreadMap = currentSpreadMap;
    }

    public HashMap<String,Integer[]> getcontentSpreadMap() {
        return this.oneStepSpreadMap;
    }


}
