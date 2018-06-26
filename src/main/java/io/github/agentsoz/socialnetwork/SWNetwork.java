package io.github.agentsoz.socialnetwork;


import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
/* SW Network Model' - Watts-Strogatz model
 

 * Input : agentList - with x,y coords of the agents.
 *  Typically, SW MOdel is generated in two steps: 
 *  1. randomly connect each node to K number of nearest neighbours (K=avg degree -  yes, but also depends on the distance range)
 *  2. With probability P, for each link - re-wire one side of the link with any random node (no duplicate neighbours or self links)
 *  	
 *  A SW Network model can be controlled by these three parameters: K, distance range and P
 *  
 * if K=high P=low -> higher inter connections within SWs (strong small worlds) | less interconnections between small worlds
 * if P=high -> higher  intra connections between small worlds
 * 		When p is really large, the network will be close to a ER random network
 * 
 * neighbourhood generation -> rewire  links -> update agent map
 * in rewire process, some agents get a higher degree by 1, and some decreases their degree by 1,
 * so if you have avgDeg =2, then there will be agents with no links.
 * 
 * The implementation should be able to generate different shapes of SW networks  controlled by K and P.
 *  distance:  should define what we call as a neighourhood. The problem would be that the  degree of a node will be controlled by both distance and avg degree (or probability)
 *  
 * expAvgDegree Vs genAvgDegree ??
 * variable used =  linkCount 
 * linkCount is increased when within distance links are created. 
 * When changing them based on a probability, the total #links do not change. 
 * so we do not count those links. But for them implementation,  
 * Also, probability is independent from the #links
 * 
 * distance and the expAvgDegree determine the #links or the degree of an agent
 * 
 * #links:
 * generated degree  remains same with the neighbourhood method and the re-wired method 
 * the number of links created depends on:  size of the 2d space agents are situated, distance range and avg degree
 * you can do a few test cases to understand which parameter affects the most. maybe avgdegree?
 * #links is independent from the re-wire probability.
 * 
 * probability: 
 * this balances the neighbourhood network and the number of interconnections between small worlds. 
 * 
 * setting configs:
 * use the method in this class to set the configs and do not set configs anywhere else out of the class
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SWNetwork extends Network {
	
	final Logger logger = LoggerFactory.getLogger("");
	
	public  HashMap<Integer,ArrayList<Integer>> network;
	private int expAvgDgree;
	private double dist=0;
	private double prob=0; // beta - determines if a generated link inside neghbourhood should be re-wired randomly across small-world
	private int linkCount=0;
	private int rewiredLinksCount=0;
	private boolean normalise;
	
	// check conditions are different in checAddNeighbours for different methods : genNei and rewire
	// String below is used to identify the method
	String rewireMethod = "rewire"; // used for checkingAddNeighbours
	String genNhoodMethod = "nhood";
	
	/* constructor for neighbourhood based SW network models 
	 * input: distance, average degree, probability of re-wiring a link to a random node - input in km
	 * expected number of links in the network with probability p =  1/2 * n * (n-1) * p
	 * total degree in the network = 1/2 * n * (n-1) * p * 2 (since each link has two sides)
	 * therefore, avg degree z= (n-1)p -> hence p = z/(n-1)
	 * 
	 * 
	 */
	public SWNetwork(double distance,int avgDegree, double prob) {
		this.dist = distance; // km
		this.expAvgDgree=avgDegree;
		this.prob = prob;
		this.network = new HashMap<Integer,ArrayList<Integer>>();
		
		logger.info("SW network : distance: {} | avg degree {} | probability: {}", this.dist, avgDegree, this.prob);
	}

	// instantiate an arrylist for each agent and add it to the network map
	public void initialiseNeighbourLists(HashMap<Integer,SocialAgent> agentmap) { 
		
		for(int id: agentmap.keySet()) {
			ArrayList<Integer> neighbourList = new ArrayList<Integer>();
			network.put(id, neighbourList);
		}
 
	}
	/* 
	 * expDegree:
	 * 	this function is used  by two functions
	 * 		1.rewire : number of neighbours is not controlled. threfore some agents may have large degrees. but avgDegrre of netow
	 * 				 will be the same : -link -> +link
	 * 		2. in distance based  neighbourhood genaeration :  
	 * 				for this an agent will  not have a degree exceeding avgDegree. checked using if conditions in the function as well

	 */		
	
	public boolean checkAddConditions(int id1, int id2, String method) { 
		
		boolean check;
		
		ArrayList<Integer> id1List = network.get(id1);
		ArrayList<Integer> id2List = network.get(id2);

		// rewire functions first condition is not checked 
		// == : max #links an agent can have is the degree
		if(!method.equals(this.rewireMethod) && (id1List.size() >= this.expAvgDgree || id2List.size() >= this.expAvgDgree)) { 
			logger.trace("check failed - neighbour lists exceeds the avg degree");
            check = false;
		}
		// already contains in the arraylists
		else if (id1List.contains(id2) && id2List.contains(id1)) {
				logger.trace("check failed - id {} and {} already exists in the neighbour lists", id1, id2);
				check = false;
		}
		else if (id1 == id2) {
			logger.trace("check failed - cannot add the same agent as neighbours : id1-{} id2-{}", id1, id2);
			check = false;
	}
		// all checks passed
		else {
			check = true;
		}

		
		return check;



}
	
	// add each id to its neighbourList
	/* 
	 * note :  when ever you are trying to link two agents, make sure to the ids to both arraylists,
	 * this is the concept of the Network class functions.
	 */
	public boolean addToNeighbourLists(int id1, int id2) { 
		
			boolean linked;
			
			ArrayList<Integer> id1List = network.get(id1);
			ArrayList<Integer> id2List = network.get(id2);

//			// neighbour lists exceeds degree
//			if(id1List.size() > this.expAvgDgree || id2List.size() > this.expAvgDgree) { 
//				logger.debug("neighbour lists exceeds the avg degree");;
//				linked = false;
//			}
//			// already contains in the arraylists
//			else if (id1List.contains(id2) && id2List.contains(id1)) {
//					logger.debug("id {} and {} already exists in the neighbour lists", id1, id2);
//					linked = false;
//			}
//			// al conditions checked, link the ids
//			else {
				id1List.add(id2);
				id2List.add(id1);
				linkCount++;
				linked = true ;

//			}
			
			return linked;

	}
	
	// remove ids from the neighbour lists
	// remove from both lists
	public void removeFromNeighbourLists(int id1, int id2) { 
		
		ArrayList<Integer> id1List = network.get(id1);
		ArrayList<Integer> id2List = network.get(id2);

		//add each id
		int indexOf2 = id1List.indexOf(id2);
		int indexOf1 = id2List.indexOf(id1);
		
		if(indexOf2 == -1) { 
			logger.error("error while removing: cant find id {} in {} neighbourlist : ", id2, id1);
			return ;
		}
		if(indexOf1 == -1) { 
			logger.error("error while removing: cant find id {} in {} neighbourlist : ", id1, id2);
			return ;
		}
		
		id1List.remove(indexOf2);
		id2List.remove(indexOf1);
		linkCount--;

}
	
	/*  Three types can occur:
	 * 1. no agents around - abort
	 * 2. agents around <= degree - add all the agents
	 * 3. agents around > degree -
	 *   get agentsWithinDist list
	 *   get random agent - add / try to add - remove agent from list
	 *   do the above steps until the list is not empty
	 *   
	 * note :  when ever you are trying to link two agents, make sure to the ids to both arraylists,
	 * this is the concept of the Network class functions.
	 * initialize the network first - initNeighbourlists method
	 */
	public void genNeighbourhoodNetwork(HashMap<Integer,SocialAgent> agentmap) {
	
		logger.info("starting neighbourhood creation process..........");
	// condition check for the method	
		if(agentmap.size() == 0) { 
			logger.error("agentlist empty! It has to be initialised before generating the SW network");
			return ;
		}
		else { 
		
			for(int id: agentmap.keySet()) {

				ArrayList<Integer> neighbours = network.get(id);
				ArrayList<Integer> agentsWithinDist = getNeighbouringAgents(id,agentmap,this.dist);
				logger.trace(" agent {} | agents within the distance range: {}", id ,agentsWithinDist.toString());
					// condition1 - no agents within the distance range
				if (agentsWithinDist.isEmpty()) { 
					logger.trace("agent {} doesn't have any other agents within the distance",id);
					continue;
				} 
				// if the neighbours is already full -> continue
				else if (neighbours.size() >= this.expAvgDgree) { 
					logger.trace("agent {} already has the needed neighbours, skipping..",id);
					continue;
				} 
				
				// condition - not empty but have less agents than the expAvgDegree within the distance 
				else if(agentsWithinDist.size() < this.expAvgDgree && neighbours.size() < this.expAvgDgree) { 
					logger.trace("agentID: {} | condition 2", id);

						// add all agents in the list if the following conditions match:
				      for(int i=0; i <agentsWithinDist.size(); i++) {
				    	  
				    	  // if agent-id neighbour list is complete - break
				    	  if(neighbours.size() >= this.expAvgDgree) { 
				    		  break;
				    	  }
				    	 
				    	  int distAgentId =  agentsWithinDist.get(i);				    	  

				    	  // check conditions and link the two agents
				    	  if(checkAddConditions(id,distAgentId, this.genNhoodMethod)) { 
					    	  addToNeighbourLists(id,distAgentId);

				    	  }
				      }
				}
				else { 
					logger.trace("agentID: {} | condition 3", id);
					// condition3 - agentsWithinDist  > expAvgDegree and neighbourSize < degree 
					//therefore randomly select ids until neighboursize = degree
					//int selected = 0;
					// this process is expected to reach the neighbour size when done
					
					int neededIds = this.expAvgDgree - neighbours.size();
					// two conditions : 
					// in some cases, most agents in agentsWithinDist can be linked fully
					// and they may not be enough agents to complete the neighbour list. 
					while(agentsWithinDist.size() > 0) { 
						if(neededIds == 0) {
							break;
						}
						int randId = randomID(agentsWithinDist);
						if(checkAddConditions(id,randId,this.genNhoodMethod) && addToNeighbourLists(id, randId)) { 
							neededIds--;
						}
						
						//remove the considered agent id
						int index = agentsWithinDist.indexOf(randId);
						logger.trace(" removing id {} from within distance list", randId);
						agentsWithinDist.remove(index);
					}

				}

				logger.trace("agentID: {} : neighbourhood - size: {} | nodes: {}",id, neighbours.size(),neighbours.toString());
			}
		
		}
	}
   public ArrayList<Integer> getAgentsOutsideDist(int id, HashMap<Integer,SocialAgent> agentmap/*,ArrayList<Integer> agentsWithinDist*/) { 
	   
	  	 ArrayList<Integer> agentIds = new ArrayList<Integer>(agentmap.keySet());

  	   @SuppressWarnings("unchecked")
  	   ArrayList<Integer> tempAgentIds =  (ArrayList<Integer>) agentIds.clone();
  	   
  	   //remove1 considering agent id
		   int ind = tempAgentIds.indexOf(id);
		   tempAgentIds.remove(ind);
  	   
  	   
  	   //remove2 - all the agents within the distance
  	   //(some neighbours is a subset of agentsWithinDist)
	ArrayList<Integer> agentsWithinDist = getNeighbouringAgents(id,agentmap,this.dist);
  	   for(int withinId: agentsWithinDist) { 
  		   
  		   if(tempAgentIds.contains(withinId))  {
  			   int index = tempAgentIds.indexOf(withinId);
  			   tempAgentIds.remove(index);
  		   }
  		   else{
  			   logger.error("agent id {} in agentsWithinDist does not exist in the cloned agent map id list");
  		   }
  	   } 
  	  	   //remove3 - all the agents in the neighbours list
  	  	   // randomly linked agents are not removed from the above agentsWithinDist list
  	   		// i.e., neighbours added during the re-wiring process
  		 ArrayList<Integer> neiList = this.network.get(id);
  	  	   for(int nId: neiList) { 
  	  		   
  	  		   if(tempAgentIds.contains(nId))  {
  	  			   int in = tempAgentIds.indexOf(nId);
  	  			   tempAgentIds.remove(in);
  	  		   }
  	  		   // most agents are already removed from withinDist list
  		   
  	  	   }
  	  	   
  	   return tempAgentIds;
   }
   
   
	/* loop over each created link and with a probabiliity, re-wire it with a random node
	 * 
	 *  reWire links to an id totally outside the distance range.
	 *  for 40,000 nodes -  it takes around 1 - 2h
	 *  travesing the outsideagent arraylists is expensive in terms of time
	 *  so this method is not prefered
	 */
	//rather than a node in the neighbourhood
  public void rewireLinksToOutsideDistAgent(HashMap<Integer,SocialAgent> agentmap) { 
	  
		logger.info("starting link re-wiring process..........");
		logger.trace("agentmap ids: {} ..........", agentmap.keySet().toString());
		// don' use the network - because in add the network gets modified
		// and the same agent is conidered twice
	  for(int id: agentmap.keySet()) {
//		  logger.debug("AGENT {}", id);
		 // int id = (int) entry.getKey();
		  ArrayList<Integer> neighbours = this.network.get(id);
		  //a copy of the original neighbours to loop over as add/remove changes the neighbours arraylist
		  ArrayList<Integer> tempNeighbours = (ArrayList<Integer>) neighbours.clone();
		  
		  // get agents ouside the distance range, whom can be randomly linked
		//  ArrayList<Integer> agentsWithinDist = getNeighbouringAgents(id,agentmap,this.dist);
		  ArrayList<Integer> agentsOutSideDist = getAgentsOutsideDist(id,agentmap); //  removed withinAgents from the params
		  
		  // for each neighbour/link in neighbours
		  for(int i=0; i <tempNeighbours.size(); i++)  {

			  // even if the probablity check passes, for some agnts,
			  //there may be no other random agent out of withinDist list and already neighbours.
			  	
			  	// first check !! if no agents then there is no point executing th
			    // this function
			  	if (agentsOutSideDist.isEmpty()) {
			  		logger.debug(" agent {} : no agents left outside the distance to re-wire", id);
			  		break;
			  	}
			  	
			  	// have random agents - probability check
				 if (rand.nextDouble() <= this.prob) {  
					 
					 
					 int existingId = tempNeighbours.get(i);
					   logger.trace("agentID: {} : links before re-wire: {}",id, neighbours.toString());
			      	   logger.trace("link to be re-wired: {}-{} ", id,existingId);
			      	   
		      		   int randid = randomID(agentsOutSideDist);
		      		   
		      		   if(checkAddConditions(id,randid,this.rewireMethod)) { 
			      			 removeFromNeighbourLists(id,existingId);
			    			   int index = agentsOutSideDist.indexOf(randid);
			    			   agentsOutSideDist.remove(index);
			      			   
			      			 addToNeighbourLists(id,randid);
			      			
			      			  logger.trace(" link re-wired : {}-{}", id,randid);
			      			  logger.trace("agentID: {} : links afer re-wire: {}",id, neighbours.toString());
			      			  this.rewiredLinksCount++;
		      		   }
		      			 
					 }
		  }

			 
	  }

  }
  
  /*
   * Rewire links to any random agent - within distance or outside distance
   * no duplicates :
   * 			agent 1 neighbours: 4,5,6 
   * 			remove 4 linkwith 2 -> 2,5,6
   * 			then next time remove 5 and link with 4 ?? - duplicate link 
   * No need this function if p=0 
   * 
   */
  public void rewireLinksToRandomAgent(HashMap<Integer,SocialAgent> agentmap) { 
	  
	  if(this.prob == 0.0) { // p=0 just the neighbourhood network
		  return;
	  }
	  
	  //  for the agents to be iterated from lowest -> highest -  so that we cut off double checking the same link
		Map<Integer,SocialAgent> treeAgentMap = new TreeMap<Integer,SocialAgent>(agentmap);
		ArrayList<Integer> agentIds = new ArrayList<Integer>(agentmap.keySet());
		
		logger.info("starting link re-wiring process..........");
		logger.trace("treemap ids: {} ..........", treeAgentMap.keySet().toString());
		// don' use the network - because in add the network gets modified
		// and the same agent is conidered twice
	  for(int id: treeAgentMap.keySet()) {
		  logger.trace("AGENT {}", id);
		 // int id = (int) entry.getKey();
		  ArrayList<Integer> neighbours = this.network.get(id);
		  
		  if(neighbours.size() == 0) { // agent doesn't have neighbours at all
			  logger.trace("agent {} | no neighbours");
			  continue;
		  }
		  //a copy of the original neighbours to loop over as add/remove changes the neighbours arraylist
		  ArrayList<Integer> tempNeighbours = (ArrayList<Integer>) neighbours.clone();
		  
		   logger.trace("agentID: {} : links before re-wire: {}",id, neighbours.toString());

		  // for each neighbour/link in neighbours
		  for(int i=0; i <tempNeighbours.size(); i++)  {

			  // even if the probablity check passes, for some agnts,
			  //are there agents  in the agentmap available to be linked with this agent?
			  if(checkNewNeighbourPossiblity(agentmap,tempNeighbours,neighbours)) { 
				
				  //  probability check
					 if (rand.nextDouble() <= this.prob) {
						 boolean selected = false;
						 
						 int existingId = tempNeighbours.get(i);
						 //check - if existingId < id - already considered
						 if (existingId < id) { 
							 logger.trace(" considered earlier: {}-{}", existingId, id);
							 continue;
						 }
				      	   logger.trace("links before re-wire: {}-{} ", id,existingId);
				      	   
				      	   while(!selected) { 
				      		   int randid = randomID(agentIds);
				      		   if(checkAddConditions(id,randid,this.rewireMethod)) { 
				      			   selected = true;
				      			   	removeFromNeighbourLists(id,existingId);
					      			 addToNeighbourLists(id,randid);
					      			
					      			  logger.trace(" old link: {}-{} new link: {}-{}",id,existingId, id,randid);
					      			  this.rewiredLinksCount++;
				      		   }
				      	   }
					 }
						 
			  }
		  }
			  logger.trace("agentID: {} : rewired links: {}",id, neighbours.toString());

		  
	  }
			  	
			  	

}
  /*
   * this method checks wether an agent can be linked with other agents as neighbours or not.
   * merge oldNeighbours and newNeighbours (current) into hashmap - overwite duplicate agents
   * if hashmap size + 1 < agentmap size -> can be linked
   */
  public boolean checkNewNeighbourPossiblity(HashMap<Integer,SocialAgent> agentmap, ArrayList<Integer> oldList,  ArrayList<Integer> newList) { 
	  boolean check = false;
	  HashMap<Integer,Integer> allNeigbours = new HashMap<Integer,Integer>();
	  for(int oid: oldList) { 
		  allNeigbours.put(oid, 0);
	  }
	  for(int nid: newList) { 
		  allNeigbours.put(nid, 0);
	  }
	  
	  // +1 is for the agent of the neighbours
	  if(allNeigbours.size() + 1 < agentmap.size()) { 
		  check = true;
	  }
	  
	  return check;
  }
  
	// displays the SW network as an adjacency matrix representation
   // before updating it to the agentmap
  //do not log this method , then the matrix doesn't display correctly
  public void displayMatrix()
  {
		Map<Integer,ArrayList<Integer>> treeNet = new TreeMap<Integer,ArrayList<Integer>>(this.network);

		
      System.out.println(" adjacency matrix representation: uppper traingle ");

      System.out.print("  ");

      for (int id : treeNet.keySet())

          System.out.print(id + " ");

      System.out.println();



      for (Map.Entry entry : treeNet.entrySet()) 

      {
    	  int id = (int) entry.getKey();
    	  ArrayList<Integer> neighbours = (ArrayList<Integer>) entry.getValue();
    	  
          System.out.print(id + " ");

          for (int otherId : treeNet.keySet())  {
       	   int linked = 0; 
       	   //because  otherAgentId < id means already checked  ; = -> ignore
       	   if(neighbours.contains(otherId) && otherId > id) { 
       		   linked = 1 ;
       	   }
              System.out.print(linked+ " ");
          }
          System.out.println();

      }
  }
  
		// random network info
  // if you call this method after re-wiring - the degree distribution is the same as updated agent maps degree dist.
  // cant remove deg dist part because for the genneghbourhood network it is usefull
	   public void verifyNetwork()
	   {
		   int ct0=0;int ct1=0;int ct2=0;int ct3=0;int ct4=0;int ct5=0;int ct6=0;int ct7=0;
		   int nullCount = 0;
		   
		 logger.info("SW network size: {} | link probability: {}", this.network.size(), this.prob);    
//		 int expTotLinks= (int) (( this.nodes * (this.nodes -1) * this.prob ) /2) ;
//	     logger.debug("verfication1 - expected  #links: {} | generated #links: {} ", expTotLinks, linkCount)
		 logger.info("verfication1 - total #links: {} | rewired #links {}", this.linkCount, this.rewiredLinksCount);     
	     logger.info("verfication2 - expected avg degree: {} | generated avg degree: {} ", this.expAvgDgree, getGenAvgDegree() /* (double)(linkCount * 2)/  this.network.size() */ );
	     logger.info("verfication3 - degree distribution:"); 
	    
    	for (int i = 0; i <  this.network.size(); i++) { 
    		ArrayList<Integer> neighbours = network.get(i);
    		if(neighbours == null) {
				nullCount++;
    		}
    		else {
    			
       		 int size = neighbours.size() ;
       		 if (size == 0) { ct0++; }
       		 if (size == 1) { ct1++; }
       		 if (size == 2) { ct2++; }
       		 if (size == 3) { ct3++; }
       		 if (size == 4) { ct4++; }
       		 if (size == 5) { ct5++; }
       		 if (size == 6) { ct6++; }
       		 if (size > 6) { ct7++; }
       		 
       		 
    		}
		}
    	logger.info("degree  #nodes");
    	logger.info("0 \t {}",ct0);
    	logger.info("1 \t {}", ct1);
    	logger.info("2 \t {}",ct2);
    	logger.info("3 \t {}",ct3);
    	logger.info("4 \t {}",ct4);
    	logger.info("5 \t {}",ct5);
    	logger.info("6 \t {}",ct6);
    	logger.info("6> \t {}",ct7);

    	logger.warn(" {} agents has a null neighbour list", nullCount);


	   }

	   public double getGenAvgDegree() {
		   return (double)(this.linkCount * 2)/  this.network.size();
	   }

	   public int getRewiredLinksCount() {

  			return this.rewiredLinksCount;
	   }
	   /* 
	    * This function updates the agentMap from the 2D arraylist (network)
	    * TESTED - the re-wired adj matrix display and the updated agent map display is the same. 
	    */
		public void updateAgentMap(HashMap<Integer,SocialAgent> agentList)
		{
			// for all agents in the agent list. agent ids are equal in 
			// both network and agent  hashmaps
			for (int agentId : agentList.keySet()) { 
				ArrayList <Integer> neighbours = network.get(agentId);
				if (neighbours == null ) { 
					
					logger.error(" agent {} has a null neighbour list", agentId);
					continue ;
				}
				
				for (int j=0; j < neighbours.size(); j++) { 
					int neiId = neighbours.get(j);
//					double newWeight = rand.nextDouble();
//					setLink(agentId, neiId,newWeight, agentList);
					createLink(agentId, neiId, agentList);
				}
			}
			
		}

		@Override
		public void genNetworkAndUpdateAgentMap(HashMap<Integer,SocialAgent> agentList){
			
	    	logger.trace("generating a small world network...");
	    	
	    	// 1. setup configs
	    	setupConfigs();
	    	
	    	//2. gen network
			initialiseNeighbourLists(agentList);
			genNeighbourhoodNetwork(agentList);
			verifyNetwork(); // print this only if u want check the degree distribution of the neighbourhood network - other paramters the same
//			displayMatrix();
			rewireLinksToRandomAgent(agentList);

			updateAgentMap(agentList);
//			displayMatrix();
//			displayAgentMap(this.agentList);
			verifyNetwork();
			verifyUpdatedAgentList(agentList);
			
			if(this.prob == 0.0) { // if neighbourhood network - with no rewired links
				verifyNeighbourDistances(agentList,this.dist);
			}
			//3. normalise network
			if(this.normalise){
				normaliseLinkWeights(agentList);
			}
		}

		
		public void setupConfigs() {
			this.normalise = SNConfig.normaliseSWNetwork();
		}
		
		public void writeNetworkLinksToFile(String file) {
			
			
			 PrintWriter  dataFile=null;
				try {
					if(dataFile == null) { 
						dataFile = new PrintWriter(file, "UTF-8");
						dataFile.println("from \t to");
						
						for(Map.Entry id : network.entrySet()) {
							int i = (int) id.getKey();
							
							ArrayList<Integer> neiList = (ArrayList<Integer>) id.getValue();
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
