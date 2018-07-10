package io.github.agentsoz.socialnetwork;



import java.io.FileInputStream;
import java.util.Random;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.Global;


/**
 * 
 * Configuration class of the sn model. This is needed:
 * 1. sn configs are separated from the other configs
 * 2. useful when running only the social network model 
 *
 *config parameters are printed in this class and not in respective network/diff model class.
 *Because, if something went wrong during instantiation of network/diff model the configs will not be printed.
 */
public class SNConfig {

	private static final Logger logger = LoggerFactory.getLogger("");
	
	
	private static String configFile = null;
	static Random rand =  Global.getRandom();
	
	//sn model
	private static String networkType = " " ;
	private static String diffusionType = " " ;
	
	//random network
	private static boolean randNetNormalise = false ;
	private static int randomNetAvgLinks = 0 ;
	
	//random regular network
	private static boolean randRegNetNormalise = false ;
	private static int randRegNetAvgLinks = 0 ;
	
	//small-world network
	private static int swNetAvgLinks = 0 ;
	private static double swRewireProb = 0.0 ;
	private static double  swNeiDistance = 0 ;
	private static boolean swNetNormalise = false ;
	private static String agentCoordFile; // agent cooordinate file
	
	//ltmodel
	private static double seed = 0 ;
	private static int diffTurn = 0 ; // check designs for a detailed design
	private static String strategy = " " ;
	private static double meanLowPanicThreshold = 0.0 ; // activation threshold
	private static double meanHighPanicThreshold = 0.0 ;
	private static String diffThresholdType = " " ;
	private static double standardDev = 0.0 ;

	//CLTmodel
	private static double waitSeed = 0 ;
	private static double panicSeed = 0 ;
	private static double waitThreshold = 0.0 ;
	private static double panicThreshold = 0.0 ;

	// TestSNBDIModels
	private static double perSeed = 15 ;


	public static void setConfigFile(String string) {
		configFile = string;
	}
	
	public static String getConfigFile() {
		return configFile;
	}
	
	// SN MODEL
	public static String getNetworkType() {
		return networkType;
	}
	
	public static void setNetworkType(String type) {
		 networkType = type;
	}
	
	public static void setDiffusionType(String difmodel) {
		 diffusionType=difmodel;
	}
	
	public static String getDiffusionType() {
		return diffusionType;
	}
	
	// RANDOM NETWORK
	public static int getRandomNetAvgLinks() {
		return randomNetAvgLinks;
	}
	
	public static void setRandomNetAvgLinks(int links) {
		 randomNetAvgLinks =  links;
	}
	
	public static boolean normaliseRandNetwork() {
		return randNetNormalise;
	}
	
	public static void setNormaliseRandNetwork(boolean res) {
		randNetNormalise=res;
	}
	
	//SW NETWORK setters and getters
	
	public static boolean normaliseSWNetwork() {
		return swNetNormalise;
	}
	
	public static void setNormaliseSWNetwork(boolean res) {
		swNetNormalise=res;
	}
	
	public static int getSWNetAvgLinks() {
		return swNetAvgLinks;
	}
	
	public static void setSWNetAvgLinks(int links) {
		 swNetAvgLinks=links;
	}
	
	public static double getSWNetRewireProb() {
		return swRewireProb;
	}
	
	public static void setSWNetRewireProb(double prob) {
		 swRewireProb=prob;
	}
	
	public static double getSWNetNeiDistance() {
		return swNeiDistance;
	}
	
	public static void setSWNetNeiDistance(double dist) {
		 swNeiDistance=dist;
	}

	public static void setAgentCoordFile(String file) {
		agentCoordFile =  file;
	}

	public static String getAgentCoordFile(){
		return agentCoordFile;
	}

	
	// RANDOM REGULAR NETWORK
	public static int getRandRegNetAvgLinks() {
		return randRegNetAvgLinks;
	}
	
	public static void setRandRegNetAvgLinks(int links) {
		randRegNetAvgLinks =  links;
	}
	
	public static boolean normaliseRandRegNetwork() {
		return randRegNetNormalise;
	}
	
	public static void setNormaliseRandRegNetwork(boolean res) {
		randRegNetNormalise=res;
	}
	
	
	
	// LT MODEL
	public static double getSeed() {
		return seed;
	}
	
	public static void setSeed(double sd) {
		 seed=sd;
	}
	
	public static void setDiffturn(int tn) {
		 diffTurn=tn * 60; // converting to seconds
	}
	
	public static int getDiffturn() {
		return diffTurn;
	}

	public static double getMeanLowPanicThreshold() {
		return meanLowPanicThreshold;
	}
	
	public static void setMeanLowPanicThreshold(double lowT) {
		 meanLowPanicThreshold = lowT;
	}

	
	public static double getMeanHighPanicThreshold() {
		return meanHighPanicThreshold;
	}
	
	public static void setMeanHighPanicThreshold(double highT) {
		 meanHighPanicThreshold = highT;
	}
	
	public static void setDiffusionThresholdType(String type) {
		 diffThresholdType=type;
	}
	
	public static String getDiffusionThresholdType() {
		return diffThresholdType;
	}

	public static double getStandardDeviation() {
		return standardDev;
	}
	
	public static void setStrategy(String stra) {
		strategy = stra ; 
	}
	
	public static String getStrategy() {
		return strategy;
	}
	
	//CLT model specifics
	public static double getWaitSeed() {
		return waitSeed;
	}
	public static double getPanicSeed() {
		return panicSeed;
	}
	public static double getWaitThreshold() {
		return waitThreshold;
	}
	public static double getPanicThreshold() {
		return panicThreshold;
	}


	//TestSNBDIModels
	public static double getPerceptSeed() {
		return perSeed;
	}

	public static void setPerceptSeed(double sd) {
		perSeed=sd;
	}

	public static boolean readConfig() {
		if (configFile == null) {
			logger.error("SNConfig: No configuration file given");
			return false;
		}
		logger.info("SNCONFIG: Loading configuration from '" + configFile + "'");
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new FileInputStream(configFile));

			NodeList nl = doc.getDocumentElement().getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if (node instanceof Element) {
					String nodeName = node.getNodeName();
					logger.trace("found node " + nodeName);
					
					if (nodeName.equals("snModel")) { 
						try {

		
						String ntype = node.getAttributes().getNamedItem("networkType").getNodeValue();
						networkType  = ntype;
						
						String dtype = node.getAttributes().getNamedItem("diffusionType").getNodeValue();
						diffusionType  = dtype;
						
						
						}
						catch (Exception e) {
							System.err
									.println("WARNING: could not read from the node snModel "	+ e.getMessage());
						}
						
					}
					
					if (nodeName.equals("randomNetwork")) {
						try {

						String norm = node.getAttributes().getNamedItem("normalise").getNodeValue();
						randNetNormalise  = Boolean.parseBoolean(norm);
						
						String links = node.getAttributes().getNamedItem("avg_links").getNodeValue();
						randomNetAvgLinks  = Integer.parseInt(links);

						}
						catch (Exception e) {
							System.err.println("SNConfig: WARNING: could not read from the node randomNetwork "	+ e.getMessage());
						}
						
					}
					
					if (nodeName.equals("swNetwork")) {
						try {

						String norm = node.getAttributes().getNamedItem("normalise").getNodeValue();
						swNetNormalise  = Boolean.parseBoolean(norm);
							
						String dist = node.getAttributes().getNamedItem("distance").getNodeValue();
						swNeiDistance  = Double.parseDouble(dist);
						
						String links = node.getAttributes().getNamedItem("avg_links").getNodeValue();
						swNetAvgLinks  = Integer.parseInt(links);

						String prob = node.getAttributes().getNamedItem("rewire_probability").getNodeValue();
						swRewireProb  = Double.parseDouble(prob);

						String cordFile =  node.getAttributes().getNamedItem("coord_file").getNodeValue();
						setAgentCoordFile(cordFile);
						
						}
						catch (Exception e) {
							System.err.println("SNConfig: WARNING: could not read from the node randomNetwork "	+ e.getMessage());
						}
						
					}
					
					if (nodeName.equals("randRegNetwork")) {
						try {

						String norm = node.getAttributes().getNamedItem("normalise").getNodeValue();
						randRegNetNormalise  = Boolean.parseBoolean(norm);
						
						String links = node.getAttributes().getNamedItem("avg_links").getNodeValue();
						randRegNetAvgLinks  = Integer.parseInt(links);

						}
						catch (Exception e) {
							System.err.println("SNConfig: WARNING: could not read from the node randomRegularNetwork "	+ e.getMessage());
						}
						
					}
					
					if (nodeName.equals("diffModel")) {
						try {

						String dseed = node.getAttributes().getNamedItem("diff_seed").getNodeValue();
						seed  = Double.parseDouble(dseed);
						
						String turn = node.getAttributes().getNamedItem("diff_turn").getNodeValue();
						diffTurn  = Integer.parseInt(turn) * 60;

						strategy = node.getAttributes().getNamedItem("strategy").getNodeValue();
						
						String meanLow = node.getAttributes().getNamedItem("mean_act_threshold").getNodeValue();
						meanLowPanicThreshold  = Double.parseDouble(meanLow);
						
						//String meanHigh = node.getAttributes().getNamedItem("mean_high_threshold").getNodeValue();
						//meanHighPanicThreshold  = Double.parseDouble(meanHigh);
						meanHighPanicThreshold = meanLowPanicThreshold * 2;
						
						String type = node.getAttributes().getNamedItem("thresholdType").getNodeValue();
						diffThresholdType  = type;
						
						String sd = node.getAttributes().getNamedItem("standard_deviation").getNodeValue();
						standardDev  = Double.parseDouble(sd);

						String ps = node.getAttributes().getNamedItem("panicSeed").getNodeValue();
						panicSeed =  Double.parseDouble(ps);

						String ws = node.getAttributes().getNamedItem("waitSeed").getNodeValue();
						waitSeed =  Double.parseDouble(ws);

						String wt = node.getAttributes().getNamedItem("waitT").getNodeValue();
						waitThreshold =  Double.parseDouble(wt);

						String pt = node.getAttributes().getNamedItem("panicT").getNodeValue();
						panicThreshold =  Double.parseDouble(pt);

						}
						catch (Exception e) {
							System.err.println("SNConfig: WARNING: could not read from the node diffModel "	+ e.getMessage());
						}
						
					}
					

				}
			}
		} catch (Exception e) {
			System.err.println("SNConfig: ERROR while reading config: " + e.getMessage());
		}
	
		return true;
	}
	
	public static  void printNetworkConfigs() {
		
		logger.info("sn model: network {} | diffusion model {}", getNetworkType(), getDiffusionType());
		if(networkType.equals(DataTypes.RANDOM)) {
			logger.info(" RANDOM network configs:");
			logger.info("normalise network = {}", normaliseRandNetwork());
			logger.info("average links = {}", getRandomNetAvgLinks());
		}
		
		if(networkType.equals(DataTypes.SMALL_WORLD)) {
			logger.info(" SMALL WORLD network configs:");
			logger.info("normalise network = {}", normaliseSWNetwork());
			logger.info("neighbour distance = {}", getSWNetNeiDistance());
			logger.info("average links = {}", getSWNetAvgLinks());
			logger.info("rewire probability = {}", getSWNetRewireProb());
			logger.info("agent coordinates file = {}",getAgentCoordFile());
		}
		
		if(networkType.equals(DataTypes.RANDOM_REGULAR)) {
			logger.info(" RANDOM-REGULAR network configs:");
			logger.info("normalise network = {}", normaliseRandRegNetwork());
			logger.info("average links = {}", getRandRegNetAvgLinks());
		}
	}
	
	
	public static void printDiffusionConfigs() {
		if(diffusionType.equals(DataTypes.ltModel)) {
			logger.info(" LT MODEL configs:");
			logger.info("diffusion seed = {}", getSeed());
			logger.info("diffusion turn = {}", getDiffturn());
			logger.info("diffusion strategy = {}", getStrategy());
			logger.info("mean Low Panic Threshold = {}", getMeanLowPanicThreshold());
			logger.info("mean High Panic Threshold = {}", getMeanHighPanicThreshold());
			logger.info(" diffusion threshold generation type = {}", getDiffusionThresholdType());
			logger.info("standard deviation = {}", getStandardDeviation());
			logger.info("percept seed = {}", getPerceptSeed());
			
		}
		if (diffusionType.equals(DataTypes.CLTModel)) {
			logger.info("diffusion turn = {}", getDiffturn());
			logger.info("diffusion strategy = {}", getStrategy());
			logger.info(" diffusion threshold generation type = {}", getDiffusionThresholdType());
			logger.info("standard deviation = {}", getStandardDeviation());
			logger.info("wait seed = {}", getWaitSeed());
			logger.info("panic seed = {}", getPanicSeed());
			logger.info("wait threshold = {}", getWaitThreshold());
			logger.info("panic threshold = {}", getPanicThreshold());
		}
	}
	
	
}
