package io.github.agentsoz.socialnetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.agentsoz.socialnetwork.util.Utils;

public class RandomRegularNetwork extends Network{

	/*
	 * A ristricted random network -  degree distribution is ristricted.
	 * 
	 * Vertices V
	 * Degree k
	 * Number of vertices = V * k /2;
	 * 
	 */
	
	
	final Logger logger = LoggerFactory.getLogger("");
	
	public  ArrayList<ArrayList<Integer>> network;
	private int avgDegree;
	private int nodes=0;
	private int linkCount=0;
	private boolean normalise;
	
	
	
	public RandomRegularNetwork(int networkSize,int avgDegree) {
		this.nodes = networkSize;
		this.avgDegree=avgDegree;
		this.network = new ArrayList<ArrayList<Integer>>();
		
		logger.debug("RandomRegularNetwork constructor: nodes: {} | avg degree {}", this.nodes, this.avgDegree);
	}
	
	// initialising the id array list by assigning an arraylist as the neighbourlist for each id
	//  CALL THIS BEFORE genRandomRegularNet method
	public void initAgentArrayList() {
		
		for(int i=0; i< nodes; i++){
      	  ArrayList<Integer> neiList = new ArrayList<Integer>();
      	  network.add(i,neiList); 
		}
	}
	
	

    /**
     * Returns a uniformly random {@code k}-regular graph on {@code V} vertices
     * (not necessarily simple). The graph is simple with probability only about e^(-k^2/4),
     * which is tiny when k = 14.
     *
     * @param V the number of vertices in the graph
     * @param k degree of each vertex
     * @return a uniformly random {@code k}-regular graph on {@code V} vertices.
     * 
     * steps:
     * 1. create k copies of each node and add to an array
     * 2. shuffle the array.
     * 3. select consecutive pairs and add as neighbours
     * if  V*k is odd, the last element of the array will be left out.
     */
    public void genRandRegNetwork() {
    	int V = this.nodes;
    	int k = this.avgDegree;
    	
      //  if (V*k % 2 != 0) throw new IllegalArgumentException("Number of vertices * k must be even");

        // create k copies of each vertex
        int[] vertices = new int[V*k];
        for (int v = 0; v < V; v++) {
            for (int j = 0; j < k; j++) {
                vertices[v + V*j] = v;
            }
        }

        // pick a random perfect matching
        Utils.shuffle(vertices);
        logger.trace("shuffled array: {}", Arrays.toString(vertices));
        
        int numPairs; 
        if  (V*k % 2 != 0) { // odd
//        	numPairs = (int) Math.ceil( (double) V*k/2); // round to the lower integer
        	numPairs = V*k/2; // int/int rounded to the lower integer
        }
        else{ // even
        	numPairs = V*k/2;
        }
        
        
        logger.debug(" number of pairs: {}", numPairs);
        
        for (int i = 0; i < numPairs; i++) {
            addNeighbour(vertices[2*i], vertices[2*i + 1]);
        }
    }
    
    public void addNeighbour(int id1, int id2) {
    	
    	ArrayList<Integer> neiList_id1 = network.get(id1);
    	ArrayList<Integer> neiList_id2 = network.get(id2);
    	
    	// same agent id
    	if(id1 == id2) {
    		logger.debug(" {} {} cannot be linked -  same agent! ", id1,id2);
    		return;
    	}
    	
    	// already neighbours
    	else if(neiList_id1.contains(id2)) {
    		logger.debug(" {} and {} are already neighbours!", id1,id2);
    		return;
    	}
    	// all conditions sufficient to be linked 
    	else {
    		neiList_id1.add(id2);
    		neiList_id2.add(id1);
    		linkCount++;
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
					createLink(id, neiId, agentList);
				}
			}
			
		}

	   
	   public void verifyNetworkArraylist()
	   {
		 logger.debug("verifying stats of the internal arraylist...");  
		 logger.debug("Random Regular network size: {}  |  avg degree: {}", this.nodes, this.avgDegree);    
	     logger.debug("verification - expected tot links: {} | generated links: {} ",(this.nodes * this.avgDegree/2), linkCount);    
	   }
	   
	   // array -> arraylist -> agentmap
		@Override
		public void genNetworkAndUpdateAgentMap(HashMap<Integer,SocialAgent> agentList){
			
	    	logger.trace("generating a random network...");
	    	
	    	//1. setup configs
	    	setupConfigs();
	    	initAgentArrayList(); // initialise the arraylist to store agent ids and neighbours
	    	
	    	//2. gen network
			genRandRegNetwork();
//			displayArraylists();  
			updateAgentMap(agentList);
			verifyNetworkArraylist(); // verification method is important
			verifyUpdatedAgentList(agentList);
		//	printAgentList(agentList); //prints the ids - neighbours in agent map
			
			//3. normalise network
			if(this.normalise){
				normaliseLinkWeights(agentList);
			}
		//	printNetworkWegihts(agentList);
		}
	//	
		public void setupConfigs() {
			this.normalise = SNConfig.normaliseRandRegNetwork();
		}
		
		public void setNormaliseVariable(boolean var) {
			this.normalise = var;
		}
		
		
		public boolean getNormaliseVariable() {
			return this.normalise;
		}
}
