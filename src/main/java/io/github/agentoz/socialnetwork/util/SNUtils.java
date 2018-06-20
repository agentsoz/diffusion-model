package io.github.agentoz.socialnetwork.util;

//import bushfire.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.agentoz.socialnetwork.Network;
import io.github.agentoz.socialnetwork.SNConfig;
import io.github.agentoz.socialnetwork.SocialNetworkManager;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class SNUtils {


	static long  simTime = 0L;
	static long stepsize = 60L;
	static long  endSimTime = 61200L; // 8h=28800L, 61200 = 17 * 60min steps
	static String homeLocations = "./input-data/homeLocations.txt";
	static String depTimesFile = "./input-data/opt-dep.txt";
	static String mainConfig = "./case_studies/hawkesbury/hawkesbury.xml";
	static String networkLinksDir = "../sn_model_data/network_visuals/";
	final static Logger logger = LoggerFactory.getLogger("");

	
	
	public static void stepTime() { 
		simTime = simTime +stepsize;
	}

	public static boolean isDiffTurn(long simTime) {
		boolean result;
		result = (simTime % SNConfig.getDiffturn() == 0) || simTime == 1.0; // simtime ==1 is needed to start reasoning in the BDI side when seed is set.
		logger.trace("isDiffTurn? {} {}",simTime, SNConfig.getDiffturn());
		return result;
	}

	public static long getSimTime() { 
		return simTime;
	}

	public static String getDepTimeFile() {
		return depTimesFile;
	}
	
	public static long getEndSimTime() { 
		return endSimTime;
	}
	
	public static void resetSimulationClock() { 
		simTime = 0L;
	}
	public static String getNetworkLinksDir() {
		return networkLinksDir;
	}
	
	public static void readAndSetSNMainConfigs() { 
		//Config.setConfigFile(mainConfig);
		SNConfig.setConfigFile(mainConfig);
		SNConfig.readConfig();
		logger.trace("setting SN main configs complete");
	}
	
//	public static void setMainConfigFile() {
//		Config.setConfigFile(mainConfig);
//	}

	public static String getMainConfigFile() {
		return mainConfig;
	}
	
	public static void randomAgentMap(SocialNetworkManager sn_manager, int nodes, int cordRange) { 
		Random  random = new Random();
		
		for(int id=0; id < nodes; id++) {
			int x = random.nextInt(cordRange);
			int y = random.nextInt(cordRange);
			sn_manager.createSocialAgent(Integer.toString(id));sn_manager.setCords(Integer.toString(id),x,y);
		}
				
		
	}
	public static void createAgentMapUsingActualCoords(SocialNetworkManager snManager, int nodes) { 
//		int id = 0; // NO need , id is already in the homelocations file
		int idCount=0; // restricts the number of agent ids selected from the .txt file
		BufferedReader br = null;
		FileReader fr = null;

		logger.info("setting up agent map with actual coordinates.....");

		
		try {

			fr = new FileReader(homeLocations);
			br = new BufferedReader(fr);

			String sCurrentLine;
			String[] coordsArray;
			
			br = new BufferedReader(new FileReader(homeLocations));

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

	public static void createTestNetwork(String networkFile, SocialNetworkManager snman) {
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
