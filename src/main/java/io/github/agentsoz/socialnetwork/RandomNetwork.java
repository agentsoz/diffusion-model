package io.github.agentsoz.socialnetwork;


/* ER network model


 * This class does not maintain any ids, ids and the indices should be matched when updating
 * This class is created to be independent from the agentlist, which makes i easier
 * to test the functionalities seprately.
 * The random network cannot be represented using a 2D array (i.e, an adj mat) because 
 * 40,000 * 40,000 is too large to be part of the memory.
 * Therefore, a 2D arraylist is maintained where the 1D arraylist contains the indices (which is mapped with the agent ids later)
 * and each index contains the arraylist of neighbours.
 * 
 * observation1: if the network size is small, generated avg degree may not be close to the expected. As the size  increase,
 * this number is close to the expected.
 * E.g. : nodes =40000: avg degree =2 -> expected links= 40,000
 * E.g. : nodes =40000: avg degree =3 -> expected links= 60,000
 * E.g. : nodes =40000: avg degree =4 -> expected links= 80,000
 * expected links increment from 20,000. There is a good variation between the networks?
 * 
 * What does controlling the avgDegree mean?
 * It only controls the number of links in the generaeted network and not actual degree of nodes, though the avg degree is
 * maintained.
 *
 * 
 * setting configs:
 * use the method in this class to set the configs and do not set configs anywhere else out of the class
 * 
 */
import java.util.ArrayList;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.agentsoz.socialnetwork.util.Global;


public class RandomNetwork extends Network {
	
	final Logger logger = LoggerFactory.getLogger("");
	
	public  ArrayList<ArrayList<Integer>> network;
	private int expAvgDgree;
	private int nodes=0;
	private double prob=0; // not always needed to generate a random network
	private int linkCount=0;
	private boolean normalise;
	
	/* constructor for G(n,p) type of ER network models 
	 * input: nodes(n) and avg degree(z) calculate the probability p based on that.
	 * expected number of links in the network with probability p =  1/2 * n * (n-1) * p = z* n/ 2
	 * total degree in the network = 1/2 * n * (n-1) * p * 2 (since each link has two sides)
	 * therefore, avg degree z= (n-1)p -> hence p = z/(n-1)
	 */
	public RandomNetwork(int networkSize,int avgDegree) {
		this.nodes = networkSize;
		this.expAvgDgree=avgDegree;
		this.prob = (double)avgDegree/ (double)(this.nodes -1 );
		this.network = new ArrayList<ArrayList<Integer>>();
		
		logger.debug("random network constructor: nodes: {} | avg degree {} | probability: {}", this.nodes, avgDegree, this.prob);
	}

	
	/* Only the indexes are used to generate the adj-matrix and not the actual ids
	 *  check all the pairs using indices and based on the independent probability assign a link.
	 * a single pair is considered only once
	 */
	public void genRandomNetwork() {
		if(this.nodes == 0 || this.prob == 0) { 
			logger.error("either the #nodes or the probability is not set: nodes: {} | prob : {}", this.nodes, this.prob);
			return ;
		}
		
	//	Random rand = Global.getRandom();
		// indices: 0 to nodes-1 
	      for(int i=0; i < this.nodes; i++) {
	    	  
        	  ArrayList<Integer> neighbours = new ArrayList<Integer>();
        	  network.add(i,neighbours); // neighbourlist of index (id) i
        	  
	          for(int j=0; j < this.nodes; j++) {
	        	  
	             if (i == j) // comparing the same node
	                continue;
	             else if (j < i) // already compared  || we only need to consider a triangle and not the rectangle 
	               continue; // 
	             else {
	                   if (Global.getRandom().nextDouble() <= prob) {  //uniformly distributed double random value between 0.0 and 1.0
	                	   logger.trace("i : {} j: {}", i,j);
	                      neighbours.add(j); // link established
	                   	  linkCount++;
	                   }
	             }
	          }
		
	      }
	}
	
    public int isNeighbour(int node, int neighbour) 
    {
    	ArrayList<Integer> neighbourList = network.get(node);
    	if(neighbourList.contains(neighbour)) { 
    		return 1;
    	}
    	else 
    		return 0;
    }
    
    // if you use the socialNetworkDiffusionLogger : hard to get the adj matrix display in a nice way
    public void displayArraylists() { 
    	for (int i = 0; i < this.nodes; i++) { 
    		System.out.print("node: "+ i);
    		ArrayList<Integer> neighbours = network.get(i);
    		System.out.print(" neighbour size: "+ neighbours.size() + "|");
    		for (int j = 0; j <  neighbours.size(); j++) {
    			System.out.print(" "+ neighbours.get(j) + " " );
    		}
    		
    		 System.out.println();
    	}
    }
		// // if you use the socialNetworkDiffusionLogger : hard to get the adj matrix display in a nice way
	   public void displayMatrix()
	   {
           System.out.println(" adjacency matrix representation:: ");

           System.out.print("  ");

           for (int i = 0; i < this.nodes; i++)

               System.out.print(i + " ");

           System.out.println();



           for (int i = 0; i < this.nodes; i++) 

           {

               System.out.print(i + " ");

               for (int j = 0; j < this.nodes; j++)  {
            	   int link=isNeighbour(i, j);
                   System.out.print(link+ " ");
               }
               System.out.println();

           }
	   }
	
	   
		// random network info
	   // from genRandomNetwork, the neighborus are added to only one arraylist, therefore this method is wrong
	   // this method is considering class variables
	   public void verifyNetwork()
	   {
		   int ct0=0;int ct1=0;int ct2=0;int ct3=0;int ct4=0;int ct5=0;int ct6=0;int ct7=0;
		   
		 logger.debug("Random network size: {} | link probability: {}", this.nodes, this.prob);    
		 int expTotLinks= (int) (( this.nodes * (this.nodes -1) * this.prob ) /2) ;
	     logger.debug("verification - expected  #links: {} | generated #links: {} ", expTotLinks, linkCount);    
	     logger.debug("verification - expected avg degree: {} | generated avg degree: {} ", this.expAvgDgree, (double)(linkCount * 2)/ this.nodes );

	   }
	   /* 
	    * This function updates the agentMap from the 2D arraylist (network)
	    * 
	    */
		public void updateAgentMap(HashMap<Integer,SocialAgent> agentList)
		{
			ArrayList<Integer> idList = new ArrayList<Integer>(agentList.keySet());
			
			for (int index=0; index < network.size(); index++) { 
				int id = idList.get(index); // coresponding agent id
				ArrayList <Integer> neighbours = network.get(index); // in the network arraylist, index = id
				if (neighbours == null ) { 
					
					logger.debug(" no neighbours for{}", id);
					return ;
				}
				
				for (int j=0; j < neighbours.size(); j++) { 
					int neiIdIndex = neighbours.get(j); 
					int neiId = idList.get(neiIdIndex);
//					double newWeight = rand.nextDouble();
//					setLink(id, neiId,newWeight, agentList);
					createLink(id, neiId, agentList);
				}
			}
			
		}

		@Override
		public void genNetworkAndUpdateAgentMap(HashMap<Integer,SocialAgent> agentList){
			
	    	logger.trace("generating a random network...");
	    	
	    	//1. setup configs
	    	setupConfigs();
	    	
	    	//2. gen network
			genRandomNetwork();
			updateAgentMap(agentList);
//			displayAgentMap(agentList);
			verifyNetwork(); // verification method is important
			verifyUpdatedAgentList(agentList);
//			printAgentList(agentList); //prints the ids - neighbours in agent map
			
			//3. normalise network
			if(this.normalise){
				normaliseLinkWeights(agentList);
			}
		}

		//normalise link weights for each weight <=1
		public void setupConfigs() {
			this.normalise = SNConfig.normaliseRandNetwork();
		}

		
}
