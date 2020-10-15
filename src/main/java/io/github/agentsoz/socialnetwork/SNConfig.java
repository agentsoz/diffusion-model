package io.github.agentsoz.socialnetwork;


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.github.agentsoz.socialnetwork.util.DataTypes;


public class SNConfig {

    private static final Logger logger = LoggerFactory.getLogger("");
    //	static Random rand =  Global.getRandom();
    static String networkLinksDir = "../sn_model_data/network_visuals/";
    private static String configFile = null;
    //    private static String defaultConfig = "./case_studies/hawkesbury/hawkesbury.xml";
    //logs
    private static String logFile = "./diffusion.log"; // default logfile, overwritten by the configuration file
    private static String logLevel = "d"; // default log level

    //default output file path, can be overwritten by the config
    private static String outFile_ic = "./diffusion_initial_config.out";
    private static String outFile_lt = "./diffusion_initial_config.out";
    private static String dynamicSeedFile = "./test.txt";

    //sn model
    private static String networkType = " ";
    private static ArrayList<String> diffusionModels;

    //random network
    private static boolean randNetNormalise = false;
    private static int randomNetAvgLinks = 0;

    //random regular network
    private static boolean randRegNetNormalise = false;
    private static int randRegNetAvgLinks = 0;

    //small-world network
    private static int swNetAvgLinks = 0;
    private static double swRewireProb = 0.0;
    private static double swNeiDistance = 0;
    private static boolean swNetNormalise = false;
    private static String agentCoordFile; // agent cooordinate file

    //ltmodel
    private static double seed_lt = 0;
    private static int diffTurn_lt = 0; // check designs for a detailed design
    private static String strategy_lt = " ";
    private static double meanLowPanicThreshold_lt = 0.0; // activation threshold
    private static double meanHighPanicThreshold = 0.0;
    private static String diffThresholdType_lt = " ";
    private static double standardDev_lt = 0.0;
    private static ArrayList<String> contentsToRegisterForLTModel;
    private static String contentType_lt;

    //CLTmodel
    private static double waitSeed = 0;
    private static double panicSeed = 0;
    private static double waitThreshold = 0.0;
    private static double panicThreshold = 0.0;
    private static ArrayList<String> contentsToRegisterForCLTModel;
    private static String contentType_clt;

    //ICModel
    private static double diffusionProbability_ic = 0.0;
    private static int diffTurn_ic = 0;
    private static String strategy_ic = " ";
    private static double seed_ic = 0;
    private static double standardDev_ic = 0.0;
    private static ArrayList<String> contentsToRegisterForICModel;
    private static String contentType_ic;

    // TestSNBDIModels
    private static double perSeed = 15;


    public static String getNetworkLinksDir() {

        File dir = new File(networkLinksDir);
        if (!dir.exists()) { // create networkLinksDir if does not exist
            dir.mkdir();
        }
        return networkLinksDir;
    }

//    public static String getDefaultConfigFile() {
//        return defaultConfig;
//    }

    public static String getConfigFile() {
        return configFile;
    }

    public static void setConfigFile(String string) {
        configFile = string;
    }

    // SN MODEL
    public static String getNetworkType() {
        return networkType;
    }

    public static void setNetworkType(String type) {
        networkType = type;
    }

    public static void addToDiffusionModelsList(String difmodel) {
        diffusionModels.add(difmodel);
    }

    public static ArrayList<String> getDiffusionModelsList() {
        return diffusionModels;
    }

    //logs
    public static void setLogFile(String filePath) {
        logFile = filePath;
    }

    public static String getLogFilePath() {
        return logFile;
    }

    public static String getLogLevel() {
        return logLevel;
    }

    public static void setLogLevel(String level) {
        logLevel = level;
    }

    //output file - LTModel
    public static void setOutputFileOfLTModel(String filePath) {
        outFile_lt = filePath;
    }

    public static String getOutputFilePathOFLTModel() {
        return outFile_lt;
    }

    public static void setOutputFileOfTheICModel(String filePath) {
        outFile_ic = filePath;
    }

    public static String getOutputFilePathOfTheICModel() {
        return outFile_ic;
    }

    public static String getDynamicSeedFile() {
        return dynamicSeedFile;
    }

    public static String getContentType_lt() {
        return contentType_lt;
    }

    //dynamic seed_lt file
    public static void setDynamicSeedFile(String filePath) {
        dynamicSeedFile = filePath;
    }

    // RANDOM NETWORK
    public static int getRandomNetAvgLinks() {
        return randomNetAvgLinks;
    }

    public static void setRandomNetAvgLinks(int links) {
        randomNetAvgLinks = links;
    }

    public static boolean normaliseRandNetwork() {
        return randNetNormalise;
    }

    public static void setNormaliseRandNetwork(boolean res) {
        randNetNormalise = res;
    }

    //SW NETWORK setters and getters

    public static boolean normaliseSWNetwork() {
        return swNetNormalise;
    }

    public static void setNormaliseSWNetwork(boolean res) {
        swNetNormalise = res;
    }

    public static int getSWNetAvgLinks() {
        return swNetAvgLinks;
    }

    public static void setSWNetAvgLinks(int links) {
        swNetAvgLinks = links;
    }

    public static double getSWNetRewireProb() {
        return swRewireProb;
    }

    public static void setSWNetRewireProb(double prob) {
        swRewireProb = prob;
    }

    public static double getSWNetNeiDistance() {
        return swNeiDistance;
    }

    public static void setSWNetNeiDistance(double dist) {
        swNeiDistance = dist;
    }

    public static String getAgentCoordFile() {
        return agentCoordFile;
    }

    public static void setAgentCoordFile(String file) {
        agentCoordFile = file;
    }

    // RANDOM REGULAR NETWORK
    public static int getRandRegNetAvgLinks() {
        return randRegNetAvgLinks;
    }

    public static void setRandRegNetAvgLinks(int links) {
        randRegNetAvgLinks = links;
    }

    public static boolean normaliseRandRegNetwork() {
        return randRegNetNormalise;
    }

    public static void setNormaliseRandRegNetwork(boolean res) {
        randRegNetNormalise = res;
    }


    // LT MODEL
    public static double getSeed_lt() {
        return seed_lt;
    }

    public static void setSeed_lt(double sd) {
        seed_lt = sd;
    }

    public static int getDiffTurn_lt() {
        return diffTurn_lt;
    }

    public static void setDiffTurn_lt(int tn) {
        diffTurn_lt = tn * 60; // converting to seconds
    }

    public static double getMeanLowPanicThreshold_lt() {
        return meanLowPanicThreshold_lt;
    }

    public static void setMeanLowPanicThreshold_lt(double lowT) {
        meanLowPanicThreshold_lt = lowT;
    }


    public static double getMeanHighPanicThreshold() {
        return meanHighPanicThreshold;
    }

    public static void setMeanHighPanicThreshold(double highT) {
        meanHighPanicThreshold = highT;
    }

    public static String getDiffusionThresholdType_lt() {
        return diffThresholdType_lt;
    }

    public static void setDiffusionThresholdType_lt(String type) {
        diffThresholdType_lt = type;
    }

    public static double getStandardDeviation_lt() {
        return standardDev_lt;
    }

    public static String getStrategy_lt() {
        return strategy_lt;
    }

    public static void setStrategy_lt(String stra) {
        strategy_lt = stra;
    }

    public static ArrayList<String> getContentsToRegisterForLTModel() {
        return contentsToRegisterForLTModel;
    }

    public static void addContentsToRegisterForLTModel(String content) {
        contentsToRegisterForLTModel.add(content);
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

    public static ArrayList<String> getContentsToRegisterForCLTModel() {
        return contentsToRegisterForCLTModel;
    }

    public static void addContentsToRegisterForCLTModel(String content) {
        contentsToRegisterForCLTModel.add(content);
    }

    public static String getContentType_clt() {
        return contentType_clt;
    }


    //IC model getters and setters

    public static double getDiffusionProbability_ic() {
        return diffusionProbability_ic;
    }

    public static int getDiffTurn_ic() {
        return diffTurn_ic;
    }

    public static String getStrategy_ic() {
        return strategy_ic;
    }

    public static double getSeed_ic() {
        return seed_ic;
    }

    public static void setSeed_ic(int i) {
        seed_ic = i;
    }

    public static double getStandardDeviation_ic() {
        return standardDev_ic;
    }

    public static void setDiffturn_ic(int i) {
        diffTurn_ic = i * 60;
    }

    public static ArrayList<String> getContentsToRegisterForICModel() {
        return contentsToRegisterForICModel;
    }

    public static void addContentsToRegisterForICModel(String content) {
        contentsToRegisterForICModel.add(content);
    }

    public static String getContentType_ic() {
        return contentType_ic;
    }

    //TestSNBDIModels
    public static double getPerceptSeed() {
        return perSeed;
    }

    public static void setPerceptSeed(double sd) {
        perSeed = sd;
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
                            networkType = ntype;

                            String difModels = node.getAttributes().getNamedItem("diffusionModels").getNodeValue();
                            //comma seperated string, split and initialise diffusionModels arraylist
                            diffusionModels = new ArrayList(Arrays.asList(difModels.split(",")));

                            String cordFile = node.getAttributes().getNamedItem("coord_file").getNodeValue();
                            setAgentCoordFile(cordFile);

                            String logfile = node.getAttributes().getNamedItem("log_file").getNodeValue();
                            setLogFile(logfile);

                            String logLevel = node.getAttributes().getNamedItem("log_level").getNodeValue();
                            setLogLevel(logLevel);


                            String seedFile = node.getAttributes().getNamedItem("dynamic_seed_file").getNodeValue();
                            setDynamicSeedFile(seedFile);
                        } catch (Exception e) {
                            System.err
                                    .println("SNConfig: could not read from the node snModel " + e.getMessage());
                        }

                    }

                    if (nodeName.equals("randomNetwork")) {
                        try {

                            String norm = node.getAttributes().getNamedItem("normalise").getNodeValue();
                            randNetNormalise = Boolean.parseBoolean(norm);

                            String links = node.getAttributes().getNamedItem("avg_links").getNodeValue();
                            randomNetAvgLinks = Integer.parseInt(links);

                        } catch (Exception e) {
                            System.err.println("SNConfig: WARNING: could not read from the node randomNetwork " + e.getMessage());
                        }

                    }

                    if (nodeName.equals("swNetwork")) {
                        try {

                            String norm = node.getAttributes().getNamedItem("normalise").getNodeValue();
                            swNetNormalise = Boolean.parseBoolean(norm);

                            String dist = node.getAttributes().getNamedItem("distance").getNodeValue();
                            swNeiDistance = Double.parseDouble(dist);

                            String links = node.getAttributes().getNamedItem("avg_links").getNodeValue();
                            swNetAvgLinks = Integer.parseInt(links);

                            String prob = node.getAttributes().getNamedItem("rewire_probability").getNodeValue();
                            swRewireProb = Double.parseDouble(prob);

//						String cordFile =  node.getAttributes().getNamedItem("coord_file").getNodeValue();
//						setAgentCoordFile(cordFile);

                        } catch (Exception e) {
                            System.err.println("SNConfig: WARNING: could not read from the node randomNetwork " + e.getMessage());
                        }

                    }

                    if (nodeName.equals("randRegNetwork")) {
                        try {

                            String norm = node.getAttributes().getNamedItem("normalise").getNodeValue();
                            randRegNetNormalise = Boolean.parseBoolean(norm);

                            String links = node.getAttributes().getNamedItem("avg_links").getNodeValue();
                            randRegNetAvgLinks = Integer.parseInt(links);


                        } catch (Exception e) {
                            System.err.println("SNConfig: WARNING: could not read from the node randomRegularNetwork " + e.getMessage());
                        }

                    }
                    if (nodeName.equals("ic") && getDiffusionModelsList().contains(DataTypes.icModel)) {
                        try {

                            Element eElement = (Element) node;

                            String turn = eElement.getElementsByTagName("step_size").item(0).getTextContent();
                            diffTurn_ic = Integer.parseInt(turn) * 60;

                            strategy_ic = eElement.getElementsByTagName("seeding_strategy").item(0).getTextContent();

                            String dseed = eElement.getElementsByTagName("seeding_strategy").item(0).getAttributes().getNamedItem("seed").getNodeValue();
                            seed_ic = Double.parseDouble(dseed);


                            String prob = eElement.getElementsByTagName("diffusion_probability").item(0).getTextContent();
                            diffusionProbability_ic = Double.parseDouble(prob);

                            String sd = eElement.getElementsByTagName("diffusion_probability").item(0).getAttributes().getNamedItem("sigma").getNodeValue();
                            standardDev_ic = Double.parseDouble(sd);

                            String oFile = eElement.getElementsByTagName("out_file").item(0).getTextContent();
                            setOutputFileOfTheICModel(oFile);

                            String icContents = eElement.getElementsByTagName("contents").item(0).getTextContent();

                            //if string is empty,  insntantiate empty list
                            contentsToRegisterForICModel = (icContents.isEmpty()) ? new ArrayList<String>() : new ArrayList(Arrays.asList(icContents.split(",")));

                            String icContentType = eElement.getElementsByTagName("contents").item(0).getAttributes().getNamedItem("type").getNodeValue();
                            contentType_ic = icContentType;

                        } catch (Exception e) {
                            System.err.println("SNConfig: ERROR while reading IC config: " + e.getMessage());
                        }
//						return true;
                    }
                    if (nodeName.equals("lt") && getDiffusionModelsList().contains(DataTypes.ltModel)) {
                        try {

                            String dseed = node.getAttributes().getNamedItem("diff_seed").getNodeValue();
                            seed_lt = Double.parseDouble(dseed);

                            String turn = node.getAttributes().getNamedItem("diff_turn").getNodeValue();
                            diffTurn_lt = Integer.parseInt(turn) * 60;

                            strategy_lt = node.getAttributes().getNamedItem("strategy").getNodeValue();

                            String meanLow = node.getAttributes().getNamedItem("mean_act_threshold").getNodeValue();
                            meanLowPanicThreshold_lt = Double.parseDouble(meanLow);

                            //String meanHigh = node.getAttributes().getNamedItem("mean_high_threshold").getNodeValue();
                            //meanHighPanicThreshold  = Double.parseDouble(meanHigh);
                            meanHighPanicThreshold = meanLowPanicThreshold_lt * 2;

                            String type = node.getAttributes().getNamedItem("thresholdType").getNodeValue();
                            diffThresholdType_lt = type;

                            String sd = node.getAttributes().getNamedItem("standard_deviation").getNodeValue();
                            standardDev_lt = Double.parseDouble(sd);

                            String lt_oFile = node.getAttributes().getNamedItem("out_file").getNodeValue();
                            setOutputFileOfLTModel(lt_oFile);

                            String contents = node.getAttributes().getNamedItem("contents").getNodeValue();
                            //if string is empty,  insntantiate empty list
                            contentsToRegisterForLTModel = (contents.isEmpty()) ? new ArrayList<String>() : new ArrayList(Arrays.asList(contents.split(",")));

                            String contentType = node.getAttributes().getNamedItem("content_type").getNodeValue();
                            contentType_lt = contentType;

                            // CLT model parameters
//                            String ps = node.getAttributes().getNamedItem("panicSeed").getNodeValue();
//                            panicSeed = Double.parseDouble(ps);
//
//                            String ws = node.getAttributes().getNamedItem("waitSeed").getNodeValue();
//                            waitSeed = Double.parseDouble(ws);
//
//                            String wt = node.getAttributes().getNamedItem("waitT").getNodeValue();
//                            waitThreshold = Double.parseDouble(wt);
//
//                            String pt = node.getAttributes().getNamedItem("panicT").getNodeValue();
//                            panicThreshold = Double.parseDouble(pt);

                        } catch (Exception e) {
                            System.err.println("SNConfig: WARNING: could not read from the node lt " + e.getMessage());
                        }
                    }
                    if (nodeName.equals("clt") && getDiffusionModelsList().contains(DataTypes.CLTModel)) {
                        try {

                            String dseed = node.getAttributes().getNamedItem("diff_seed").getNodeValue();
                            seed_lt = Double.parseDouble(dseed);

                            String turn = node.getAttributes().getNamedItem("diff_turn").getNodeValue();
                            diffTurn_lt = Integer.parseInt(turn) * 60;

                            strategy_lt = node.getAttributes().getNamedItem("strategy").getNodeValue();

                            String meanLow = node.getAttributes().getNamedItem("mean_act_threshold").getNodeValue();
                            meanLowPanicThreshold_lt = Double.parseDouble(meanLow);

                            //String meanHigh = node.getAttributes().getNamedItem("mean_high_threshold").getNodeValue();
                            //meanHighPanicThreshold  = Double.parseDouble(meanHigh);
                            meanHighPanicThreshold = meanLowPanicThreshold_lt * 2;

                            String type = node.getAttributes().getNamedItem("thresholdType").getNodeValue();
                            diffThresholdType_lt = type;

                            String sd = node.getAttributes().getNamedItem("standard_deviation").getNodeValue();
                            standardDev_lt = Double.parseDouble(sd);

                            String lt_oFile = node.getAttributes().getNamedItem("out_file").getNodeValue();
                            setOutputFileOfLTModel(lt_oFile);

                            // CLT model parameters
                            String ps = node.getAttributes().getNamedItem("panicSeed").getNodeValue();
                            panicSeed = Double.parseDouble(ps);

                            String ws = node.getAttributes().getNamedItem("waitSeed").getNodeValue();
                            waitSeed = Double.parseDouble(ws);

                            String wt = node.getAttributes().getNamedItem("waitT").getNodeValue();
                            waitThreshold = Double.parseDouble(wt);

                            String pt = node.getAttributes().getNamedItem("panicT").getNodeValue();
                            panicThreshold = Double.parseDouble(pt);

                            String contents = node.getAttributes().getNamedItem("contents").getNodeValue();
                            //if string is empty,  insntantiate empty list
                            contentsToRegisterForLTModel = (contents.isEmpty()) ? new ArrayList<String>() : new ArrayList(Arrays.asList(contents.split(",")));

                            String contentType = node.getAttributes().getNamedItem("content_type").getNodeValue();
                            contentType_clt = contentType;

                        } catch (Exception e) {
                            System.err.println("SNConfig: WARNING: could not read from the node lt " + e.getMessage());
                        }

                    }


                }
            }
        } catch (Exception e) {
            System.err.println("SNConfig: ERROR while reading config: " + e.getMessage());
        }

        return true;
    }

    public static void printNetworkConfigs() {

        logger.info("sn model: network {} | diffusion model/s: {}", getNetworkType(), getDiffusionModelsList().toString());
        logger.info("LogFile: path {} | level {}", getLogFilePath(), getLogLevel());
        logger.info("Dyanmic seed file: path {} ", getDynamicSeedFile());

        if (networkType.equals(DataTypes.RANDOM)) {
            logger.info(" RANDOM network configs:");
            logger.info("normalise network = {}", normaliseRandNetwork());
            logger.info("average links = {}", getRandomNetAvgLinks());
        }

        if (networkType.equals(DataTypes.SMALL_WORLD)) {
            logger.info(" SMALL WORLD network configs:");
            logger.info("normalise network = {}", normaliseSWNetwork());
            logger.info("neighbour distance = {}", getSWNetNeiDistance());
            logger.info("average links = {}", getSWNetAvgLinks());
            logger.info("rewire probability = {}", getSWNetRewireProb());
            logger.info("agent coordinates file = {}", getAgentCoordFile());
        }

        if (networkType.equals(DataTypes.RANDOM_REGULAR)) {
            logger.info(" RANDOM-REGULAR network configs:");
            logger.info("normalise network = {}", normaliseRandRegNetwork());
            logger.info("average links = {}", getRandRegNetAvgLinks());
        }
    }


    public static void printDiffusionConfigs() {

        for (String model : diffusionModels) {

            if (model.equals(DataTypes.ltModel)) {
                logger.info(" LT MODEL configs:");
                logger.info("content type: {}", getContentType_lt());
                logger.info("influences to diffuse: {}", getContentsToRegisterForLTModel().toString());
                logger.info("diffusion seed = {}", getSeed_lt());
                logger.info("diffusion turn = {}", getDiffTurn_lt());
                logger.info("diffusion strategy = {}", getStrategy_lt());
                logger.info("mean Low Panic Threshold = {}", getMeanLowPanicThreshold_lt());
//				socialNetworkDiffusionLogger.info("mean High Panic Threshold = {}", getMeanHighPanicThreshold());
                logger.info(" diffusion threshold generation type = {}", getDiffusionThresholdType_lt());
                logger.info("standard deviation = {}", getStandardDeviation_lt());
                logger.info("percept seed  = {}", getPerceptSeed());
                logger.info("output file(LT MOdel): {}", getOutputFilePathOFLTModel());




            }
            if (model.equals(DataTypes.CLTModel)) {
                logger.info(" CLT MODEL configs:");
                logger.info("diffusion turn = {}", getDiffTurn_lt());
                logger.info("content type: {}", getContentType_clt());
                logger.info("diffusion strategy_lt = {}", getStrategy_lt());
                logger.info(" diffusion threshold generation type = {}", getDiffusionThresholdType_lt());
                logger.info("standard deviation = {}", getStandardDeviation_lt());
                logger.info("wait seed = {}", getWaitSeed());
                logger.info("panic seed = {}", getPanicSeed());
                logger.info("wait threshold = {}", getWaitThreshold());
                logger.info("panic threshold = {}", getPanicThreshold());
                logger.info("output file(CLT MOdel): {}", getOutputFilePathOFLTModel());

            }
            if (model.equals(DataTypes.icModel)) {
                logger.info(" IC MODEL configs:");
                logger.info("influences to diffuse: {}", getContentsToRegisterForICModel().toString());
                logger.info("content type: {}", getContentType_ic());
                logger.info("diffusion seed = {}", getSeed_ic());
                logger.info("diffusion turn = {}", getDiffTurn_ic());
                logger.info("diffusion strategy = {}", getStrategy_ic());
                logger.info("diffusion probability = {}", getDiffusionProbability_ic());
                logger.info("standard deviation = {}", getStandardDeviation_ic());
                logger.info("output file(IC MOdel): {}", getOutputFilePathOfTheICModel());

            }

        }

    }


}
