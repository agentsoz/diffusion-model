package io.github.agentsoz.socialnetwork.util;

import java.util.HashMap;

public class DiffusedInformation {

    // contenttype, active agents IDs
    HashMap<String,Integer[]> infoSpread;

    DiffusedInformation()
    {
        this.infoSpread = new HashMap<String,Integer[]>();
    }

    public int getTotalDiffusionContents() {
        return infoSpread.size();
    }


}
