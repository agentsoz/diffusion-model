package io.github.agentsoz.socialnetwork.datacollection;

import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.SocialNetworkModel;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class LTModelDataCollector extends DataCollector{

    final static Logger logger = LoggerFactory.getLogger("");

//    private  int lowCt = 0,medCt=0,highCt=0; // social state counters


    private  TreeMap<Double, HashMap<String,Integer[]>> ltDiffSpread; // time, content, [inactive and active counts]

    public LTModelDataCollector() {

        ltDiffSpread = new TreeMap<Double, HashMap<String,Integer[]>>();
    }

    public int getFinalActiveAgents(String content){
        return this.ltDiffSpread.lastEntry().getValue().get(content)[1];
    }

    public int getFinalInactiveAgents(String content){
        return this.ltDiffSpread.lastEntry().getValue().get(content)[0];
    }

    public  void countLowMedHighAgents(SocialNetworkModel sn, List<String> contentList, double time) {

        HashMap<String,Integer[]> countMapForAllContents = new HashMap<String, Integer[]>() ;

        for(String content: contentList) { // for each content
            int l=0,m=0,h=0; // init counts

            for(SocialAgent agent:sn.getAgentMap().values()){

                String state = agent.getState(content);
                if(state.equals(DataTypes.LOW)) {
                    l++;
                }
                else if(state.equals(DataTypes.MEDIUM)) {
                    m++;
                }
                else if(state.equals(DataTypes.HIGH)){ //this is not used by the LT model
                    h++;
                }
            }

            logger.debug("Time: {} | INACTIVE agents: {}  | ACTIVE agents: {}",time, l,m);


            Integer[] counts = new Integer[2];
            counts[0] = l; counts[1] = m; // inactive, active
            countMapForAllContents.put(content,counts);
        }

        //finally , add countMap with the current time
        this.ltDiffSpread.put(time,countMapForAllContents);


    }



    public TreeMap<Double, HashMap<String,Integer[]>> getLtDiffSpread() {
        return ltDiffSpread;
    }

    public double getInactiveCount(double time,String content) {
        return this.ltDiffSpread.get(time).get(content)[0];
    }

    public double getActiveCount(double time,String content) {
        return this.ltDiffSpread.get(time).get(content)[1];
    }

    public  void writeSpreadDataToFile() {

        String fileName = SNConfig.getOutputFilePathOFLTModel();

        File file = new File(fileName); // create output directory if not exists
        if (!file.exists()) {
            if (file.getParentFile().mkdir()) {
                // logger.debug(" IC model data collection output dir created");
            }
        }

        logger.info("LT Model: creating diffusion output file: {} ", fileName);

        PrintWriter dataFile=null;
        try {
            if(dataFile == null) {
                dataFile = new PrintWriter(fileName, "UTF-8");

                double lastTimeStep = this.ltDiffSpread.lastKey(); // returns highest value stored
                Set<String> finalContentSet = this.ltDiffSpread.get(lastTimeStep).keySet();

                // write table header
                dataFile.print("time");
                for(String c: finalContentSet){ // print all contents at the end of the simulation
                    dataFile.print("\t" + c);
                }

                dataFile.println();
                for(Map.Entry<Double,HashMap<String, Integer[]>> entry: this.ltDiffSpread.entrySet()) {

                    double time = entry.getKey();
                    dataFile.print(time);
                    HashMap<String,Integer[]> stepSpreadCountMap = entry.getValue();
                    for(String con: finalContentSet) { // full content list

                        int count;
                        if(!stepSpreadCountMap.containsKey(con)) {
                            count = 0;
                        }
                        else{
                            count = stepSpreadCountMap.get(con)[1]; // get active agent count
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

}
