package io.github.agentsoz.socialnetwork.util;

import java.util.HashMap;

public class DiffusedInformation {

    // contenttype, active agents IDs
    HashMap<String,Integer[]> infoSpreadMap;

    public DiffusedInformation()
    {
        this.infoSpreadMap = new HashMap<String,Integer[]>();
    }

    public int getTotalDiffusionContents() {
        return infoSpreadMap.size();
    }


    public void setInfoSpreadMap(HashMap<String,Integer[]> currentSpreadMap) {

        this.infoSpreadMap = currentSpreadMap;
    }
}
