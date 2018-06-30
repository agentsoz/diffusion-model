package sn;

import bushfire.datacollection.ScenarioThreeData;
import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.SocialNetworkManager;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.SNUtils;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

// java xls reader

/* 
 *  setting the configurations:
 *  1. Use the  input xml file (hawkesbury.xml)
 *  2. Use setters in the SNConfig class and set the appropriate values.
 * 
 * 
 * Note -  when making adding/modifying existing functionalities, use the test dir to generate the results, much easier to se and 
 * analyase rather than opening new timestamp dirs each time.
 * 
 * Running and testing the model:
 * 1. Main -  configs will be set from the main config file - hawkesbury.xml
 * 2. LHS - configs will be initially taken from main config file, and lhs configs will be overwritten later.
 * same log file
 * 
 * IMP- for all network types, agent map should be generated with actual geo locations. Because, seeding
 * strategy agents near fire can be applied to ANY network.
 *  
 */

public class TestSNModel {


//	SocialNetworkManager sn_manager = new SocialNetworkManager();
	Random random = new Random();
	String dataFile = "sn-data"; 


	
	boolean printConfigFlag = false;
	
	String lhsDir = "./latin-hypercube-samples/";
	String xlFile = lhsDir + "s3-sn-samples.xls" ;
	
	// set the data dir and the timestamp dir
	String snDataDir="../sn_model_data/";
//	String timestampDir = snDataDir + new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss").format(new Date());
	
	//test directry
	String timestampDir = snDataDir + "/test";
			
	
	String rateFile = "rate-data";
	String activeIdFile =  timestampDir + "/active.txt";
	String inactiveIdFile = timestampDir + "/inactive.txt";
	
	//then set the log file into the timestamp dir
	String logFile = timestampDir + "/" + "snmodel.log";
	final Logger logger = Log.createLogger("", logFile);
	

	
	//	static functions for each run -  must run before the test cases execute
	@Before
	public void setTimestampDir()
	{ 
		// set main config file.
		SNUtils.setMainConfigFile();
		
		//String timestamp = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss").format(new Date());
		//	timestampDir = snDataDir + timestamp ; 
		new File(timestampDir).mkdir();

		//	log should be set after the timestamp dir is set
		logger.trace("sn dir : {} timestamp dir: {}",snDataDir, timestampDir);
		
	}
	
	//@Ignore
	@Test
	public void testDiffusionSeed() {
		SocialNetworkManager sn_manager = new SocialNetworkManager(SNUtils.getMainConfigFile());
		boolean configStatus = sn_manager.setupSNConfigs(); // set sn configs
		logger.debug("configurations status: {}", configStatus);
		randomAgentMap(sn_manager,1000, 10000);
		sn_manager.initSNModel();
		Assert.assertEquals(950,SNUtils.getLowCt());
		Assert.assertEquals(50,SNUtils.getMedCt());

	}
	
	@Ignore
	@Test
	public void testGenerateAgentMap(){
		SocialNetworkManager sn_manager = new SocialNetworkManager(SNUtils.getMainConfigFile());
		boolean configStatus = sn_manager.setupSNConfigs(); // set sn configs
		generateAgentMap(sn_manager,10);

		Assert.assertEquals(10,sn_manager.getAgentMap().size());
	}
	
	@Ignore
	@Test
	public void testSNModelLHS() {
	
		// IMPORTANT :  convert the .xls file to a csv file by save as sn-samples.csv 
		//network type = read from the main config 
		// nodes =  #agents
		// start sample end sample
		// iterations for each sample
		runWithLHSConfigsFromXLS(1000, 1,2,2); //38343
	}
	

//	@Ignore
	@Test
	public void testSNModelManual() {
	
		runWithMainConfigs(1,1000); //38343
	}
	
	/*Tested the LHS values in from the snlog file.
	 * Directly reads from the LHS xls file
	 * extracts a specific set of cells manually controlled, therefore if any change done to LHS this 
	 * method should be modified
	 */
	
	public void runWithLHSConfigsFromXLS(int nodes, int start, int end, int iterations) { 
		// startsample endsample #iteratiaons
		
		logger.info("Runnig SN Model with LHS configs");
		
		
		SNUtils.readAndSetSNMainConfigs(); // sets the main config file and reads the configs 
		
        int indent = 4 ; // there is gap of 5, therefore the exact index of start should be +4
	    File inputWorkbook = new File(xlFile);
	    Workbook w;
	      
	        try {
	            w = Workbook.getWorkbook(inputWorkbook);
	            
	            Sheet sheet = w.getSheet(3); // Get the LHS sheet
	            
	            //print LHS parameter ranges
	            logger.info("extracted ranges from LHS xls file:");
	           	  for (int col = 1; col < 5; col++) { 
	           		  	int row = 1;
	              		 Cell lowRange = sheet.getCell(col, row);
	              		 Cell highRange = sheet.getCell(col, row+1);
	              		 Cell  paramName = sheet.getCell(col, row+3);
	                     logger.info("param:{} | Range: {} - {}",paramName.getContents(), lowRange.getContents(), highRange.getContents() );
	           	  
              }
	   

	                for (int row = start+ indent; row <= end + indent; row++) {
	                	  //for (int col = 1; col < 5; col++) { 
	                	int col = 1; // values between col 1,2,3,4 (ignore col 0)
	                    String links = sheet.getCell(col, row).getContents();
	                    String turn = sheet.getCell(col+1, row).getContents();
	                    String seed = sheet.getCell(col+2, row).getContents();
	                    String lowT = sheet.getCell(col+3, row).getContents();
//	                    Cell highT = sheet.getCell(col+4, row);
	                    
	                    logger.debug(",{},{},{},{},0,0,0 ", links,turn,seed,lowT);

	            		logger.info("Sample: {}",(row - indent));
	            		String sampleDir = createAndgetSampleDir(row - indent);
	             		for(int counter=0; counter<iterations;counter++) { // iterations
	            			
	            			logger.info("iteration: {}", counter);
	            			   
	            			SocialNetworkManager sn_manager = new SocialNetworkManager(SNUtils.getMainConfigFile());
	            			//1.set sn configs
	            			boolean configStatus = sn_manager.setupSNConfigs(); // 
	            			overwriteConfigsWithLHSConfigs(links,turn,seed, lowT); // overwrite lhs configs
	            			
							sn_manager.printSNModelconfigs(); // print configs after LHS overwrite

	            			//2. generate the agent map
	            			generateAgentMap(sn_manager,nodes);
	            		//	randomAgentMap(sn_manager,nodes, 10000);
	            			
	            			boolean netStatus = sn_manager.generateSocialNetwork();
	            			boolean diffStatus = sn_manager.generateDiffModel();
	            			
	            			
	            			if(!configStatus || !netStatus || !diffStatus) {
	        					logger.error("initialisation falied: config {} network {} diffmodel {} - aborting",
	        							configStatus,netStatus,diffStatus);
	        					System.exit(-1);
	            			}

							ScenarioThreeData itrDataColMaps = new ScenarioThreeData();

							runSimulation(sn_manager);  // #FIXME : count the data and add this information.
	            			
	            			// write data to file
							itrDataColMaps.writePanicCountData(getFileName(dataFile,counter,sampleDir));
//	            			ScenarioThreeData.writePanicRateData(getFileName(rateFile,counter,sampleDir));
	            			
	            			// reset setup
							itrDataColMaps.clearDataArrayLists();
	            			SNUtils.resetSimulationClock();
	            			
	                		
	                	}
	            		
	            } 
	        } catch (BiffException | IOException e) {
				logger.error("error in reading LHS File: {} ", e.getMessage());
	            e.printStackTrace();
	        }
	        

	}
	

	
	
	
	
	/*
	 * tested print configs of one sn manager
	 */
	
	public void runWithMainConfigs(int iterations, int nodes) {
		
		logger.info("Runnig SN Model with Main configs");
		
		String sampleDir = createAndgetSampleDir(1);
		
		
		for(int counter=0; counter<iterations;counter++) {
			logger.debug("sn model iteration: {}", counter);
			SocialNetworkManager sn_manager = new SocialNetworkManager(SNUtils.getMainConfigFile());
			sn_manager.setupSNConfigs();
			generateAgentMap(sn_manager,nodes);
			
			//randomAgentMap(sn_manager,40000,100000);
		//	sn_manager.setExecType(DataTypes.SN_BDI); // to print the configs inside initSNModel()
			sn_manager.initSNModel();

			ScenarioThreeData itrDataColMaps  = new ScenarioThreeData();

			runSimulation(sn_manager); // #FIXME : count the data and add this information.
			
			// write data to file
			itrDataColMaps.writePanicCountData(getFileName(dataFile,counter,sampleDir));
//			ScenarioThreeData.writePanicRateData(getFileName(rateFile,counter,timestampDir));

			// reset setup
			itrDataColMaps.clearDataArrayLists();
			SNUtils.resetSimulationClock();
		}
		
		
	}
	

	
	// overwrite the SNConfigs with the configs  generaterd from LHS
	public void overwriteConfigsWithLHSConfigs(String avgLinks, String turn, String seed, String lowT)
	{
		// setting up configs
		logger.info("overwriting configs using LHS configs.....");
		
		if(SNConfig.getNetworkType().equals(DataTypes.RANDOM)) {
			SNConfig.setRandomNetAvgLinks(Integer.parseInt(avgLinks));

		}
		else if(SNConfig.getNetworkType().equals(DataTypes.SMALL_WORLD)) {
			SNConfig.setSWNetAvgLinks(Integer.parseInt(avgLinks));

		}
		else if(SNConfig.getNetworkType().equals(DataTypes.RANDOM_REGULAR)) {
			SNConfig.setRandRegNetAvgLinks(Integer.parseInt(avgLinks));

		}
		
		SNConfig.setDiffturn(Integer.parseInt(turn));
		SNConfig.setSeed(Double.parseDouble(seed));
		SNConfig.setMeanLowPanicThreshold(Double.parseDouble(lowT));
	//	SNConfig.setMeanHighPanicThreshold(Double.parseDouble(highT));
		
	}

	public void runSimulation(SocialNetworkManager sn_m)  //test the data file and the parameters used
	{
		logger.info("TestSNModel : testing the diffusion process..... ");
//		sn_m.generateSocialNetwork();
		while(SNUtils.getSimTime() <= SNUtils.getEndSimTime()) { // looping through the simulation time

			sn_m.processDiffusion(SNUtils.getSimTime());
//			sn_m.getDiffModel().printPanicValues();
			

			
			SNUtils.stepTime();
			
		}
		

	}
	/*For ALL networks, use createAgentMapUsingActualCords method
	 */
	public void generateAgentMap(SocialNetworkManager sn_manager, int nodes) {
//		if(SNConfig.getNetworkType().equals(DataTypes.RANDOM)) { 
//			logger.trace("generating agentmap with random coords...");
//			SNUtils.randomAgentMap(sn_manager, nodes, 10000);
//		}
//		else if(SNConfig.getNetworkType().equals(DataTypes.SMALL_WORLD)) { 
			logger.debug("generating agentmap of {} agents", nodes);
			SNUtils.createAgentMapUsingActualCoords(sn_manager, nodes);
//		}
	}
	
	public void randomAgentMap(SocialNetworkManager sn_manager, int nodes, int cordRange) { 
		
		for(int id=0; id < nodes; id++) {
			int x = random.nextInt(cordRange);
			int y = random.nextInt(cordRange);
			sn_manager.createSocialAgent(Integer.toString(id));sn_manager.setCords(Integer.toString(id),x,y);
		}
				
		
	}
	
	// create sample dir for LHS configs and return the file path so that the data files can be placed inside the dir
	public String createAndgetSampleDir(int sample)
	{ 
		//timestamp
		
		String sampleDir; 
		sampleDir= timestampDir + "/" + "sample" + Integer.toString(sample) ; 
		logger.trace("sample dir : {} ",sampleDir);
		new File(sampleDir).mkdir();
		return sampleDir;
		
	}
	
	//static functions for each run
	public String getFileName(String fileType, int fileCounter, String fileDir) { 
		String filepath = fileDir + "/" + fileType + Integer.toString(fileCounter) + ".txt"; 
		return filepath;
	}
	// used only for the runWithLHSConfigsFromCSV method
	public String setLHSfile(String network) { 
		String filepath = lhsDir + "sn-samples.csv"; 
		return filepath;
	}
	
	
	/* Specify the AGents manually in this method!!
	 * Seed - from config
	 * this function does not generate the social network. 
	 * Generates the agentmap and then gen the diffsion model.
	 * Tested active inactive data files with 10 nodes.
	 */
	@Ignore
	@Test
	public void writeSeedDataFile() {
		int nodes = 38343; //38343
		logger.info("testing inactive active agent id files..");
		
			
			SocialNetworkManager sn_manager = new SocialNetworkManager(SNUtils.getMainConfigFile());
			sn_manager.setupSNConfigs();
			SNConfig.printDiffusionConfigs();
			generateAgentMap(sn_manager,nodes);
			
			sn_manager.generateDiffModel();	// no need of the network to initialise the seed?
			writeActiveInactiveAgentsToFile(sn_manager);
	}
	
	/* 
	 * Generates two text files each having active and inactive agent ids 
	 * file names = active inactive, created in timestamp dir (not in sample dir).
	 */
	public void writeActiveInactiveAgentsToFile(SocialNetworkManager snmanager) {
		
		HashMap<Integer,SocialAgent> agentmap = snmanager.getAgentMap();
		List<Integer> inactive =  new ArrayList<Integer>();
		List<Integer> active =  new ArrayList<Integer>();
		
		PrintWriter  activeFile=null;
		PrintWriter  inactiveFile=null;
		for(SocialAgent agent: agentmap.values()) {
			if(agent.isActive()) {
				active.add(agent.getID());
			}
			else {
				inactive.add(agent.getID());
			}
		}
		
			try {
				if(activeFile == null) { // writing active agent ids
					activeFile = new PrintWriter(activeIdFile, "UTF-8");
					activeFile.println("activeIds");
					
					for(int id: active){
						activeFile.println(id);
					}
				}

				if(inactiveFile == null) { // writing inactive agent ids
					inactiveFile = new PrintWriter(inactiveIdFile, "UTF-8");
					inactiveFile.println("inactiveIds");
					
					for(int id: inactive){
						inactiveFile.println(id);
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
				inactiveFile.close();
				activeFile.close();
			}
	}
	
	
	
	
	
	
	
	
	
	
	
	//************************************* Obsolete*****************************
	/*
	 * 
	 * tested - read files of the the two network types 
	 * tested - inputs with extracting config parameters based on start and end samples
	 * tested - printconfigs of both network types
	 * This method reads from a csv file where the user needs to manually convert the xls into csv.
*/
//	public void runWithLHSConfigsFromCSV(int nodes, int start, int end, int iterations) {
//		// startsample endsample #iteratiaons
//
//		logger.info("Runnig SN Model with LHS configs");
//
//
//		SNUtils.readAndSetSNMainConfigs(); // sets the main config file and reads the configs
//
//		String csvFile = setLHSfile(SNConfig.getNetworkType());
//
//        BufferedReader br = null;
//        String configLine = "";
//        String cvsSplitBy = ",";
//        int indent = 5 ; // there is gap of 5 between input start/end paramters and line needed
//        int lineNo = 0;
//
//        try {
//
//        	br = new BufferedReader(new FileReader(csvFile));
//            while ((configLine = br.readLine()) != null) {
//            	lineNo++;
//            	if((start + indent) <= lineNo && lineNo <= (end + indent)) { //sample level
//            		logger.debug(configLine);
//            		//set dir
//            		String sampleDir = createAndgetSampleDir(lineNo - indent);
//            		List<String> paramList = Arrays.asList(configLine.split(","));
//            		String links = paramList.get(1);
//            		String turn = paramList.get(2);
//            		String seed = paramList.get(3);
//
//            		String lowT = paramList.get(4);
//            		String highT = paramList.get(5); //Thigh can be calculated by Tlow.
//
//            		logger.trace(" extracted LHS configs: avglinks: {} | turn: {} | seed: {} | lowT {} | highT: {}",
//            				links,turn, seed, lowT);
//
//            		logger.info("Sample: {}",(lineNo - indent));
//            		for(int counter=0; counter<iterations;counter++) { // iterations
//
//        			logger.info("iteration: {}", counter);
//
//        			SocialNetworkManager sn_manager = new SocialNetworkManager();
//        			//1.set sn configs
//        			boolean configStatus = sn_manager.setupSNConfigs(); //
//        			overwriteConfigsWithLHSConfigs(links,turn,seed, lowT); // overwrite lhs configs
//
//        			SNConfig.printNetworkConfigs();
//        			SNConfig.printDiffusionConfigs(); // print configs again to check if there is an issue
//
//        			//2. generate the agent map
//        			generateAgentMap(sn_manager,nodes);
//        		//	randomAgentMap(sn_manager,nodes, 10000);
//
//        			boolean netStatus = sn_manager.generateSocialNetwork();
//        			boolean diffStatus = sn_manager.generateDiffModel();
//
//
//        			if(!configStatus || !netStatus || !diffStatus) {
//    					logger.error("initialisation falied: config {} network {} diffmodel {} - aborting",
//    							configStatus,netStatus,diffStatus);
//    					System.exit(-1);
//        			}
//
//						runSimulation(sn_manager);
//
//        			// write data to file
//        			ScenarioThreeData.writePanicCountData(getFileName(dataFile,counter,sampleDir));
////        			ScenarioThreeData.writePanicRateData(getFileName(rateFile,counter,sampleDir));
//
//        			// reset setup
//        			ScenarioThreeData.clearDataArrayLists();
//        			SNUtils.resetSimulationClock();
//
//            	}
//
//            	}
//            }
//
///////
//		}catch(Exception ioe) {
//			logger.error("error in reading LHS File: {} ", ioe.getMessage());
//		    ioe.printStackTrace();
//		}
//
//
//	}
}
