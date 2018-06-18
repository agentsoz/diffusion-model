package socialnetwork;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Random;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import socialnetwork.util.Global;

public  class Network{

/* This class contains the generic functionalities of the network class such as updating the agentmap, get neighbouring agents within a distance range
 *  This class can be used as an extension for other network types
 *  
 *  Modifications:
 *  alreadylinked (commented) -> alreadyLinked
 *  setLink (commented) -> createLink
 */

final Logger logger = LoggerFactory.getLogger("");

public  static ArrayList<SocialLink> linkList = new  ArrayList<SocialLink>(); // NOT USED PRACTICALLY
public static int neighbourLinksCount = 0;

public static String type = "normalise"; // normalise (sum weights = 1),equal

Random rand = Global.getRandom();
	/**
     *  initialises the social agents by mapping agent Ids from the BDI system
	 * @return 
	 *  
     */
//generic function to get a random function out of an arraylist
	public  int randomID(ArrayList<Integer> list) {
		int randID = list.get(rand.nextInt(list.size())); // exclusive of size
		return randID;
	}	

	public HashMap<Integer,SocialAgent> initialiseSocialAgentsFromFile(File dataset)
	{
		
		    Scanner scan;
		    HashMap<Integer,SocialAgent> agents = new HashMap<Integer,SocialAgent>();
		    try {
		        scan = new Scanner(dataset);

		        while(scan.hasNext())
		        {	          
		        	SocialAgent agent = new SocialAgent(scan.nextInt(),scan.nextDouble(),scan.nextDouble() );
		        	agents.put(agent.getId(),agent);
		        }
		        scan.close();

		    } catch (FileNotFoundException e1) {
		            e1.printStackTrace();
		    }
		  
			return agents;
		
	}
	
public void genNetworkAndUpdateAgentMap(HashMap<Integer,SocialAgent> agentList){}

    /* function:get the agents inside a particular distance range
	 * input: dist in km since euclidean function returns in km
	 * agent x,y coords are in meters :  UTM uses meters from reference points
	 *  */
    public ArrayList<Integer> getNeighbouringAgents( int id, HashMap<Integer,SocialAgent> agents,double distRange)
    {
    	SocialAgent currentAgent = agents.get(id);
    	ArrayList<Integer> neighbours = new ArrayList<Integer>();
    	 for (SocialAgent agent :  agents.values()) 
    	{   		 
    		if (currentAgent.getId() != agent.getId() && (euclideanDistance(currentAgent.getX(),currentAgent.getY(),agent.getX(),agent.getY()) <= distRange)   ) {
                neighbours.add(agent.getId());
            }
    	}
//    	System.out.println(neighbours.toString());
    	return neighbours;
    	 
    	 
    }
    
    /*
     *when the network is initialised, this function is used to create the links.
     *Adds the neighbour to both agents. Do not use this function when modifying a weight of a single neighbour.
     */
	public void createLink(int agent1_Id, int agent2_Id, HashMap<Integer,SocialAgent> agentList)
	{
		
		double weight = rand.nextDouble();
		logger.trace("generated weight: {} ", weight);
		
		SocialAgent agent1 = agentList.get(agent1_Id);
		SocialAgent agent2 = agentList.get(agent2_Id);
		
		//link exists in both agents
		if(agent1.alreadyLinked(agent2_Id) && agent2.alreadyLinked(agent1_Id)) { 
			logger.trace("abort createLink - link already exists (both sides): {} {}", agent1_Id,agent2_Id);
			return ;
		}
		//link exists in one agent
		else if(agent1.alreadyLinked(agent2_Id) || agent2.alreadyLinked(agent1_Id)) { 
			logger.trace("abort createLink - link already exists (single side): {} {}", agent1_Id,agent2_Id);
			return ;
		}
		//link doesn't exist in either or both agents
		else{
			neighbourLinksCount++;
			agent1.addNeighbourOrModifyWeight(agent2_Id, weight);
			agent2.addNeighbourOrModifyWeight(agent1_Id, weight);
		}
	}
	
	// same method as createLink but weight is an input
	public void createLinkWithGivenWeight(int agent1_Id, int agent2_Id, double weight, HashMap<Integer,SocialAgent> agentList)
	{
				
		SocialAgent agent1 = agentList.get(agent1_Id);
		SocialAgent agent2 = agentList.get(agent2_Id);
		
		//link exists in both agents
		if(agent1.alreadyLinked(agent2_Id) && agent2.alreadyLinked(agent1_Id)) { 
			logger.trace("abort createLink - link already exists (both sides): {} {}", agent1_Id,agent2_Id);
			return ;
		}
		//link exists in one agent
		else if(agent1.alreadyLinked(agent2_Id) || agent2.alreadyLinked(agent1_Id)) { 
			logger.trace("abort createLink - link already exists (single side): {} {}", agent1_Id,agent2_Id);
			return ;
		}
		//link doesn't exist in either or both agents
		else{
			neighbourLinksCount++;
			agent1.addNeighbourOrModifyWeight(agent2_Id, weight);
			agent2.addNeighbourOrModifyWeight(agent1_Id, weight);
		}
	}
    
    /* OLRDER METHIOD TO CREATE LINKS
     * creates a new social link instance and set weight
     * updates the social agent's link set
     * since this method is called by interating through a hashmap, where each link is included in two different places,
     * its not appropriate to claim alreadyLinked as an error or a warning?
     */
	public void setLink(int agent1_Id, int agent2_Id, double weight,HashMap<Integer,SocialAgent> agentList)
	{
		//link already exists -  even if a
//		if(alreadylinked(agent1_Id,agent2_Id,agentList)) 
		if(alreadyLinked(agent1_Id,agent2_Id,agentList)) 
		{
			logger.trace("abort setLink - link already exists: {} {}", agent1_Id,agent2_Id);
			return ;
		}
		
		// link doesn't exist
		else
		{
			//new link creation		
			SocialLink newlink = new SocialLink(agent1_Id,agent2_Id);
			
			newlink.setLinkWeight(weight); // modify the link weight
			
			SocialLink refLink1 = newlink;
			SocialLink refLink2 = newlink;
			
			
			SocialAgent agent1 = agentList.get(agent1_Id);			
			agent1.addLink(agent2_Id, refLink1);
			
			SocialAgent agent2 = agentList.get(agent2_Id);			
			agent2.addLink(agent1_Id, refLink2);
			
			Network.linkList.add(newlink);
		}
		
	}
	
	/*
	 * 
	 * type variable is defined in this class as a class variable
	 */
	public void normaliseLinkWeights(HashMap<Integer,SocialAgent> agentList) {
		
		 
		if(type.equals("normalise")) { //different link weights, sum is always less than or equal to one
			logger.info("normalising network - sum of weights less than or equal to 1");
			 for (SocialAgent agent: agentList.values()) {
				 
				 if(agent.getSumWeights() > 1) { 
					 agent.normaliseWeights();
				 }
			 }
		}
		
		else if(type.equals("equal")) { // all neighbours have equal weights, at all times, sum to 1
			logger.info("normalising network - equal neighbour weights");
			 for (SocialAgent agent: agentList.values()) {

				 HashMap<Integer,Double> neiList = agent.getLinkMap();
				 double weight = 1.0/ neiList.size();
				 for(int neiId: neiList.keySet()) {
					 agent.addNeighbourOrModifyWeight(neiId,weight);
				 }
			 }
		}

	}
	
	public boolean isProperlyNormalised(HashMap<Integer,SocialAgent> agentList) {
		boolean res = true;
		 for (SocialAgent agent: agentList.values()) {
			 if(agent.getSumWeights() > 1) { 
				 logger.error("agent {} sum of weights exceeds 1", agent.getID());
				 	res = false;
			 }
		 }
		  return res;
	}
	
	
	/* For the agent link map - neiId, weight
	 * If agent1 contains id2 in the linkset? &&
	 * If agent2 contains id1 in the linkset?
	 * then return true
	 */
	public boolean alreadyLinked(int id1, int id2, HashMap<Integer,SocialAgent> agentList)
	{
		SocialAgent agent1 = agentList.get(id1);
		boolean link1To2 = agent1.alreadyLinked(id2);
		
		
		SocialAgent agent2 = agentList.get(id2);
		boolean link2To1 = agent2.alreadyLinked(id1);

        return link1To2 && link2To1;
		
	}
	
	/*
	 * If agent1 contains id2 in the linkset? &&
	 * If agent2 contains id1 in the linkset?
	 * then return true
	 */
//	public boolean alreadylinked(int agent1_Id, int agent2_Id, HashMap<Integer,SocialAgent> agentList)
//	{
//		SocialAgent agent1 = agentList.get(agent1_Id);
//		if(agent1 == null) {
//			logger.error("agent {} not found in the agent map, aborting" ,agent1_Id);
//			return false;
//		}
//		
//		HashMap<Integer,SocialLink> agent1_links = agent1.getLinkSet();
//		
//		
//		SocialAgent agent2 = agentList.get(agent2_Id);
//		if(agent2 == null) {
//			logger.error("agent {} not found in the agent map, aborting" ,agent2_Id);
//			return false;
//		}
//		
//		HashMap<Integer,SocialLink> agent2_links = agent2.getLinkSet();
//		
//		boolean link1Exists = false;
//		boolean link2Exists = false;
//		
//
//		SocialLink link1=agent1.getLink(agent2_Id);
//		if(link1 != null)
//			link1Exists = true;
//
//		SocialLink link2=agent2.getLink(agent1_Id);
//		if(link2 != null)
//			link2Exists = true;
//		
//				
//
//		if(link1Exists && link2Exists) // if both link refernces exists
//		{
//			return true;
//		}
//		else
//		{
//			return false; // one or both link references do not exist
//		}
//		
//	}
	


    
    /*input : agent x,y are in meters
     *output euclidean distance in km
     */
    public double euclideanDistance(double x1, double y1, double x2, double y2)
    {
    	return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2))/1000;
    } 
    

    
    public void printAgentList(HashMap<Integer,SocialAgent> agentList)
    {
    	 System.out.println("Number of Social Agents instantiated: "+ agentList.size()); 
    	
//    	    
    		for(SocialAgent agent :agentList.values())
    		
    		{
    			logger.debug("id {}  | neighbour Ids {}",agent.getID(), agent.getLinkedNeighbourIDs());
    			
    		}
    		
    		System.out.println("\n"); 
    }
    
	public void printNetworkWegihts(HashMap<Integer,SocialAgent> agentList )
	{
		for (SocialAgent agent :agentList.values())
		{
			agent.printWeights();
		}
	}
	
	// prints the agent map as an adjacency matrix
	public void displayAgentMap(HashMap<Integer,SocialAgent> agentList)  {
		
		// first, we need to convert the agentmap to treemap to order the map. 
		// This ordering is needed for printing the agent map and distinguishing
		// links that is already considered
		Map<Integer,SocialAgent> treeAgentMap = new TreeMap<Integer,SocialAgent>(agentList);
// 	   logger.debug("treemap size: {}", treeAgentMap.size());
 	   
           System.out.println(" agent map:: ");

           System.out.print("  ");

           for (int id : treeAgentMap.keySet()) 

               System.out.print(id + " ");

           System.out.println();



           for (int id : treeAgentMap.keySet()) 

           {
        	   SocialAgent agent = treeAgentMap.get(id);
        	   HashMap<Integer,SocialLink> links = agent.getLinkSet();
        	   
               System.out.print(id + " ");

               for (int otherAgentId : treeAgentMap.keySet() )  {
            	   int linked = 0; 
            	   //because  otherAgentId < id means already checked  ; = -> ignore
//            	   if(alreadylinked(id,otherAgentId,agentList) && otherAgentId > id) { 
            	   if(alreadyLinked(id,otherAgentId,agentList) && otherAgentId > id) { 
            		   linked = 1 ;
            	   }
                   System.out.print(linked+ " ");
               }
               System.out.println();

           }
	}
	
	   /* This method is the verification from the update of adjacency matrix to the 
	    *  agentMap. Should be logged.
	    */
		public void verifyUpdatedAgentList(HashMap<Integer,SocialAgent> agentList)  {
			int linkCountInAgentMaps = 0 ; // counts the #links  in the agent map 
			int alreadyLinkedCount = 0 ; // given two agents are they both exist in each other's link maps
			logger.info("verifying the updated agentmap.....");
//			logger.debug("verification - #links in the  linkList: {} ", this.links.size());  neighbourLinksCount
			logger.info("verification - #links added to the agentList: {} ",neighbourLinksCount);  
			
	           for (int id: agentList.keySet()) 
	           {
	        	   SocialAgent agent = agentList.get(id);
	        	   linkCountInAgentMaps =  linkCountInAgentMaps + agent.getLinkMapSize();
	        	//   HashMap<Integer,SocialLink> links = agent.getLinkSet();
	        	   
	        	   
	               for (int otherAgentId : agentList.keySet() )  {
	            	   if(alreadyLinked(id,otherAgentId,agentList)) { 
	            		   alreadyLinkedCount++ ;
	            	   }
	            	   
	               }
	        	   
	        	   
	        	   
	           }
	           // these two counts should be equal, which are counted in two different ways
				logger.info("verification - total link count in agentmaps : {} ", linkCountInAgentMaps);
				logger.info("verification - alreadylinkedCount (check social agent neighbours) : {} ", alreadyLinkedCount);
				
				logger.info("verification - degree distribution");
				calcDegDist(agentList);

		}
		
		/**
		 * Checks the distance between each pair of neighbours in the agentmap with the desired distance range
		 * Required ONLY for SWNetwork with p=0.0 -> a neighbourhood network
		 * This method should be called AFTER updating the agentmap
		 * @param agentList
		 */
		public void verifyNeighbourDistances(HashMap<Integer,SocialAgent> agentList, double maxDist) {
			for(SocialAgent agent: agentList.values()) {
				HashMap<Integer,Double> neiLinks = agent.getLinkMap();
				for(Integer neiId: neiLinks.keySet()) { // for all neighbours
					double neiX = agentList.get(neiId).getX();
					double neiY = agentList.get(neiId).getY();
					double actualDist = euclideanDistance(agent.getX(),agent.getY(),neiX,neiY) ;
				//	logger.trace("neihours: {} {} distance: {}", agent.getID(),neiId,actualDist);
					if(actualDist > maxDist) {
						logger.error("found neighbours {} {} with distance higher ({}) than the max distance ({})", agent.getID(), neiId,actualDist, maxDist);
					}
						
				}
			}
		}
		
		public void calcDegDist(HashMap<Integer,SocialAgent> agentList) { 
			
			int maxDegree = 0;
			for(SocialAgent agent: agentList.values()) { // step1 - get the highest degree
				if(agent.getLinkMapSize() > maxDegree) { 
//					maxDegree = agent.getLinkSet().size();
					maxDegree = agent.getLinkMapSize();
				}
			}
			
			int[] degArr = new int[maxDegree+1]; // initialising the array to store degrees considering 0 as well
			
			for(SocialAgent agent: agentList.values()) { //for all agents get linkset - increment the degcount
				//	int deg = agent.getLinkSet().size();
					int deg = agent.getLinkMapSize();
					int degCount = degArr[deg]; // assume that this value is zero initially
					degCount++;
					degArr[deg] = degCount;
					
			}
			
			//printing the degArr array
			for(int degCt=0;degCt < degArr.length ; degCt++) {
				
				logger.debug("degree: {} | agent count: {}",degCt,degArr[degCt]);
			}
		}
    

		
		public void writeNetworkLinksToFile(String file, ArrayList<ArrayList<Integer>> network) {
						
					
			 PrintWriter  dataFile=null;
				try {
					if(dataFile == null) { 
						dataFile = new PrintWriter(file, "UTF-8");
						dataFile.println("from \t to");
						
						for(int i=0;i<network.size();i++){
							
							ArrayList<Integer> neiList = network.get(i);
							for(int nei=0;nei<neiList.size();nei++){
								dataFile.println(i + "\t" + neiList.get(nei));
							}
							
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
