package io.github.agentsoz.socialnetwork.util;

//import bushfire.Config;
import io.github.agentsoz.socialnetwork.SocialNetworkModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.agentsoz.socialnetwork.Network;
import io.github.agentsoz.socialnetwork.SNConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SNUtils {


	static long  simTime = 0L;
	static long stepsize = 60L;
	static long  endSimTime = 61200L; // 8h=28800L, 61200 = 17 * 60min steps

	final static Logger logger = LoggerFactory.getLogger("");

	public static void setStepsize(long stepsize) {
		SNUtils.stepsize = stepsize;
	}

	public static void stepTime() {
		simTime = simTime +stepsize;
	}

//	public static boolean isDiffTurn(long simTime) {
//		boolean result;
//		result = (simTime % SNConfig.getDiffturn() == 0) || simTime == 1.0; // simtime ==1 is needed to start reasoning in the BDI side when seed is set.
//		logger.trace("isDiffTurn? {} {}",simTime, SNConfig.getDiffturn());
//		return result;
//	}

	public static long getSimTime() { 
		return simTime;
	}

	public static long getEndSimTime() { 
		return endSimTime;
	}

	public static void setEndSimTime(long newEndTime) {
		endSimTime = newEndTime;
	}
	
	public static void resetSimulationClock() { 
		simTime = 0L;
	}

	
	public static void readAndSetSNMainConfigs() { 
		//Config.setConfigFile(mainConfig);
		SNConfig.setConfigFile(SNConfig.getDefaultConfigFile());
		SNConfig.readConfig();
		logger.trace("setting SN main configs complete");
	}

	public static void readAndSetGivenSNMainConfig(String config) {
		//Config.setConfigFile(mainConfig);
		SNConfig.setConfigFile(config);
		SNConfig.readConfig();
		logger.trace("setting SN main configs complete");
	}

//	public static void setMainConfigFile() {
//		SNConfig.setConfigFile(SNConfig.getDefaultConfigFile());
//	}


	
//	public static void randomAgentMap(SocialNetworkModel sn_manager, int nodes, int cordRange) {
//		//Random  random = new Random();
//
//		for(int id=0; id < nodes; id++) {
//			int x = random.nextInt(cordRange);
//			int y = random.nextInt(cordRange);
//			sn_manager.createSocialAgent(Integer.toString(id));sn_manager.setCords(Integer.toString(id),x,y);
//		}
//
//
//	}
	public static void randomAgentMap(SocialNetworkModel sn_manager, int nodes, int cordRange) {

		for(int id=0; id < nodes; id++) {
			int x = Global.getRandom().nextInt(cordRange);
			int y = Global.getRandom().nextInt(cordRange);
			sn_manager.createSocialAgent(Integer.toString(id));sn_manager.setCords(Integer.toString(id),x,y);
		}
	}
	public static void createAgentMapUsingActualCoords(SocialNetworkModel snManager, int nodes) {
		int idCount=0; // restricts the number of agent ids extracted from the .txt file
		BufferedReader br = null;
		FileReader fr = null;

		logger.info("setting up agent map with actual coordinates.....");

		
		try {

			fr = new FileReader(SNConfig.getAgentCoordFile());
			br = new BufferedReader(fr);

			String sCurrentLine;
			String[] coordsArray;
			
			br = new BufferedReader(new FileReader(SNConfig.getAgentCoordFile()));

			while ( (sCurrentLine = br.readLine()) != null && idCount < nodes) {
				logger.trace(sCurrentLine);
				coordsArray = sCurrentLine.split("\t");
				logger.trace("id: {} x: {} y: {}",coordsArray[0],coordsArray[1],coordsArray[2]);
				snManager.createSocialAgent(coordsArray[0]);
				snManager.setCords(coordsArray[0],Double.parseDouble(coordsArray[1]),Double.parseDouble(coordsArray[2]));

//				id++;
				idCount++;
			}

		} catch (IOException e) {
			logger.error("IO exception:");
			e.printStackTrace();

		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}

	}
//	#FIXME diffusion turn hardcoded to IC model diffusion turn
    // reads timing of blocked percepts of the format (time,id), and store the agent ids to activate for diffusion step
    public static void readAndStoreDynamicSeed(HashMap<Integer,ArrayList<String>> seedMap) {
        int idCount=0; // restricts the number of agent ids extracted from the .txt file
        BufferedReader br = null;
        FileReader fr = null;

        logger.info("reading dynamic seed file and storing dynamic seed.....");

        HashMap<String,Integer> timedPercepts = new HashMap<String,Integer>(); // format id, time NOT  time, id. Because then can't have multiple ids of same time.
        try {

            fr = new FileReader(SNConfig.getDynamicSeedFile());
            br = new BufferedReader(fr);

            String sCurrentLine;
            String[] perceptsArray;

            br = new BufferedReader(new FileReader(SNConfig.getDynamicSeedFile()));

            while ( (sCurrentLine = br.readLine()) != null) { // store timed updates
                if(idCount >= 1) { // skipping the headers of the table
                   // logger.trace(sCurrentLine);
                    perceptsArray = sCurrentLine.split(",");
                    logger.trace("time: {}, id: {}",perceptsArray[0],perceptsArray[1]);
                    timedPercepts.put(perceptsArray[1], Integer.parseInt(perceptsArray[0])); // store the otherway round as id, time

                }
                idCount++;


            }

            // initialise seedMap, define a time interval start values
            int time = SNConfig.getDiffTurn_ic(); //#FIXME set to ic model config, make this generic, to set based on passed model arg



            while(time <= SNUtils.getEndSimTime() ) {  // initialise dynamic seed map
                    //logger.info("step: {}",time);
                    ArrayList<String> idList = new ArrayList<String>();
                    seedMap.put(time,idList);

                    time =  time + SNConfig.getDiffTurn_ic(); // increment the step


            }



            // check time intervals and create id lists for each diffusion step
            for(Map.Entry entry: timedPercepts.entrySet()){

            	String id = (String) entry.getKey();
            	int pTime = (int) entry.getValue();

                // re-initialising intervals
                int maxTime = SNConfig.getDiffTurn_ic(); // define a time interval to extract percept ids. Max start from diffusion step and min start from 0.
                int minTime = 0;

                while(maxTime<= SNUtils.getEndSimTime()) {

                    if(minTime < pTime && pTime <= maxTime) {
                        ArrayList<String> list = seedMap.get(maxTime); // get id list
                        list.add(id);
                        break; //found the time interval, exit while loop
                    }

                    // step time interval
                    maxTime = maxTime + SNConfig.getDiffTurn_ic();
                    minTime = minTime + SNConfig.getDiffTurn_ic();

                    // value is not within maximum range
//					if(maxTime == SNUtils.getEndSimTime()){
//						logger.error("time value {} outside maximum sim time range", pTime );
//					}
                }


            }

            // verificatoin
            logger.info("dynamic seeding pattern:");
            int ct = 0;
            for(int entry : seedMap.keySet()) { // print out final dynamic seed map
                logger.info("time: {} | id list: {}", entry, seedMap.get(entry).size());
                ct = ct + seedMap.get(entry).size();
            }
				// should all be  same
			logger.info("total seed read {} | total seed stored  {} | total sorted into diffusion step intervals: {}", idCount-1, timedPercepts.size(), ct);

        } catch (IOException e) {
            logger.error("IO exception:");
            e.printStackTrace();

        } finally {

            try {

                if (br != null)
                    br.close();

                if (fr != null)
                    fr.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }

        }

    }

	public static void createTestNetwork(String networkFile, SocialNetworkModel snman) {
		Network net =  new Network();
		File netFile = new File(networkFile);
		Scanner scan;
		try{
			scan = new Scanner(netFile);

			while(scan.hasNext())
			{
				net.createLinkWithGivenWeight(scan.nextInt(), scan.nextInt(), scan.nextDouble(), snman.getAgentMap());

			}
			scan.close();
			net.printAgentList(snman.getAgentMap()); // finally print
			net.printNetworkWegihts(snman.getAgentMap());
		}
		catch (FileNotFoundException e1) {
			e1.printStackTrace();
			logger.error("test sn file not found ");

		}catch (NullPointerException e) {
			e.printStackTrace();
			logger.error("Null pointer exception caught: {}", e.getMessage());
		}
	}






}
