package io.github.agentsoz.socialnetwork.datacollection;

import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.SocialNetworkManager;
import io.github.agentsoz.socialnetwork.util.DataTypes;

public class LTModelDataCollector {

    private static int lowCt = 0,medCt=0,highCt=0; // social state counters

    public static void countLowMedHighAgents(SocialNetworkManager sn) {

        int l=0,m=0,h=0;
        for(SocialAgent agent:sn.getAgentMap().values()){

            String state = agent.getState();
            if(state.equals(DataTypes.LOW)) {
                l++;
            }
            else if(state.equals(DataTypes.MEDIUM)) {
                m++;
            }
            else if(state.equals(DataTypes.HIGH)){
                h++;
            }
        }

        setHighCount(h);
        setMedCount(m);
        setLowCount(l);


    }


    public static void setLowCount(int l) {
        lowCt = l;
    }

    public static void setMedCount(int m) {
        medCt = m;
    }

    public static void setHighCount(int h) {
        highCt = h;
    }

    public static int getLowCt() {
        return lowCt;
    }

    public static int getMedCt() {
        return medCt;
    }

    public static int getHighCt() {
        return highCt;
    }
}
