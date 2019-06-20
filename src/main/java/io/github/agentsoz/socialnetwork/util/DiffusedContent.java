package io.github.agentsoz.socialnetwork.util;

import java.util.ArrayList;
import java.util.HashMap;

public class DiffusedContent {

    // contenttype, active agents IDs
    HashMap<String,ArrayList<String>> oneStepSpreadMap;

    public DiffusedContent()
    {
        this.oneStepSpreadMap = new HashMap<String,ArrayList<String>>();
    }

    public int getTotalDiffusionContents() {
        return oneStepSpreadMap.size();
    }

    public int getAdoptedAgentCountForContent(String c) {
        if (this.oneStepSpreadMap.containsKey(c)) {

            return this.oneStepSpreadMap.get(c).size();
        }
        else{
            return 0; // no agent has adopted the content
        }
    }
    public void setContentSpreadMap(HashMap<String,ArrayList<String>> currentSpreadMap) {

        this.oneStepSpreadMap = currentSpreadMap;
    }

    public HashMap<String,ArrayList<String>> getcontentSpreadMap() {
        return this.oneStepSpreadMap;
    }


}
