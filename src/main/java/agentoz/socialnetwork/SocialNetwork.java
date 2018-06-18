package agentoz.socialnetwork;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import agentoz.socialnetwork.util.Global;

public class SocialNetwork{

/* This class contains the first implementation of the social network with neighbours, family members and friends.
 * 
 * */
private static double neighbourWeight=0.4;
private static double familyWeight=0.3;
private static double friendWeight=0.3;

final Logger logger = LoggerFactory.getLogger("");

public  ArrayList<SocialLink> linkList = new  ArrayList<SocialLink>();
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
	

	public void assignFamilies(int maxFamilySize, int min_familyMembers, HashMap<Integer,SocialAgent> agentList, ArrayList<SocialLink> linkList)
	{
		logger.debug("initiated assignFamilies");
		int familyID = 0;   //id 0 is not assigned. can be used to check	

		ArrayList<Integer> unAssigned = new  ArrayList<Integer>();
		
		for(Integer agentID : agentList.keySet())
		{							
			unAssigned.add(agentID);	//initialise family list with agentIDs and 0 family members				
		}
		
		int unAssignedAgents = unAssigned.size();

		
	    while(unAssignedAgents > maxFamilySize)
	    {
	    	int agentID =  unAssigned.get(0);
	    	SocialAgent agent = agentList.get(agentID);
	    	
				// have agents more than or equal to  the family size 
				int agentFamilySize = agent.getFamilySize(); // current family members
		
				while(agentFamilySize < maxFamilySize)
				{
					int randomAgentIndex;
					randomAgentIndex = getRandomInteger(1,unAssigned.size());  // exclude 0 because 0 is the agent that we always get
					logger.debug(" generated randomIndex :"+randomAgentIndex);
					int randomAgentID = unAssigned.get(randomAgentIndex);

					SocialAgent randomAgent = agentList.get(randomAgentID);
					int randomAgentFamilySize = randomAgent.getFamilySize();
					
					
					if(randomAgentIndex == 1) {
						logger.debug("random index 1, size of unassigned {} randomAgentID {} family size {} ", unAssigned.size(), randomAgentID, randomAgentFamilySize);
					}
					
					// not already a family member and random agent has not reached the max family size
					if(randomAgentFamilySize < maxFamilySize && !(alreadyFamily(agentID,randomAgentID,agentList)) )
					{
						familyID++;
						if (setLink(agentID, randomAgentID,"fam",familyWeight,agentList,linkList))
						{
							logger.debug("family link created between {} {}", agentID, randomAgentID );
							agent.setFamilyID(familyID);
							randomAgent.setFamilyID(familyID);  
							
							agentFamilySize++;
							
							//removing agent if agent has maximum number of family members
							if(randomAgent.getFamilySize() == maxFamilySize)
							{
								unAssigned.remove(randomAgentIndex);
							}
						}

					}
				}
				unAssigned.remove(0); //removing agent 
				unAssignedAgents = unAssigned.size(); // re - assigning the size after removing agents with  maxFamilySize
	    }	 
		logger.debug("completed assigning families");					
	}
			
	/* 
	 * 
	 * SchoolKids : arraylist of agentIds that go to the same school		
	 */
	public void assignFriends( int maxFriends,HashMap<Integer,SocialAgent> agentlist, ArrayList<String> Schoolkids)
	{
		Random rand = Global.getRandom();
		//making a shallow copy of the kids in school arraylist
		ArrayList<String> schoolKidsCopy = new ArrayList<String> (Schoolkids);
		logger.debug("copy of the kids of school : {} ", schoolKidsCopy.toString() );
	
		if(schoolKidsCopy.size() == 2) {
			maxFriends = 1;
		}
		//making a map to store the friend counts of each agent
		HashMap<Integer,Integer> friendCountMap = new HashMap<Integer,Integer>();
		for(String id : schoolKidsCopy) {
			friendCountMap.put(Integer.parseInt(id), 0); // since an agent can only have one school
		}
				
		for(int i=0;i<schoolKidsCopy.size();i++ )
		{
			int id = Integer.parseInt( schoolKidsCopy.get(i) );
			logger.debug("considering agent {} to assign friends..", id );
			SocialAgent agent = agentlist.get(id);
			int agentFriendCount = friendCountMap.get(id);
			
			while(agentFriendCount < maxFriends) { 
				int randomIndex = rand.nextInt(schoolKidsCopy.size());
				logger.debug("generated random number is {} ", randomIndex );
				
				int randomAgentID = Integer.parseInt(schoolKidsCopy.get(randomIndex));
				SocialAgent randomAgent = agentlist.get(randomAgentID);
				int randomAgentFriendCount = friendCountMap.get(randomAgentID);
				
				if( !(alreadyFriends(id,randomAgentID,agentlist)) && id !=randomAgentID ) {
					
					if(setLink(id,randomAgentID,"fri",friendWeight,agentlist,linkList))
					{
						logger.debug("friend link successfully set for agents : "+ id + " and " + randomAgentID);
						
						agentFriendCount++;
						randomAgentFriendCount++;
						
						//checking friend count of the agent
						if(agentFriendCount == maxFriends) {
							logger.debug("agent {} has reached maximum friend limit,removing ",id);
							int index = schoolKidsCopy.indexOf(String.valueOf(id));
							schoolKidsCopy.remove(index);
						}
						else {
							friendCountMap.put(id, agentFriendCount);
						}
						
						//checking friend count of the randomAgent
						if(randomAgentFriendCount == maxFriends) {
							logger.debug("agent {} has reached maximum friend limit,removing", randomAgentID);
							int index = schoolKidsCopy.indexOf(String.valueOf(randomAgentID));
							schoolKidsCopy.remove(index);
//							schoolKidsCopy.remove(randomIndex);
				

						}
						else {
							friendCountMap.put(randomAgentID, randomAgentFriendCount);
						}
						
					}
					else
					{
						logger.error("error setting the neighbour link for agents : "+id + " and " + randomAgentID);
					}
					
					
				}				
				
			}
		}
	}

		

	
	public boolean assignNeighbours(int maxN, HashMap<Integer, SocialAgent> agentList,/* HashMap<Integer, ArrayList<SocialLink>> linkList*/ double distRange) 
	{
		
		logger.info("assigning neighbour links");
		
		int max_neighbours_limit = maxN; 
	//	int neighbourCount = 0 ;
	//	int max_selected_neighbours = 2;
		
		//contains the number of neighbours each agent has, which is initially 0
		HashMap<Integer, Integer> neighbour_count_list =  new HashMap<Integer, Integer>();
		for(Integer agentID : agentList.keySet())
		{
			neighbour_count_list.put(agentID, 0); 
		}
		
		
		for(SocialAgent agent : agentList.values())
		{
			
			int agent_neighbourCount = neighbour_count_list.get(agent.getId());
			//System.out.println(agent.getID());
			ArrayList<Integer> neighbourList = getNeighbouringAgents(agent.id,agentList, distRange);
			if(neighbourList.size() == 0)
			{
				logger.warn("WARNING: no agents found within the given distance range for agent ID : "+agent.getId());
				continue;
			}
			else if (agent_neighbourCount == max_neighbours_limit - 1) // count starts at 0
			{
				logger.debug("max number of neighbours reached for agent ID : " + agent.getId());
				continue;
			}
			else if(max_neighbours_limit > neighbourList.size())
			{
				max_neighbours_limit =  neighbourList.size();
			}
			else
			{
					// this will set the number of  neighbours a particular agent will have, depending on the number of agents in the neighbourList
	
				while(agent_neighbourCount < max_neighbours_limit )
				{
					int agentId = agent.getId();
					int randomIndex;
					if(neighbourList.size() == 1) // if there is only one element in the list
					{
						randomIndex = 0;
					}
					else
						randomIndex = getRandomInteger(0,neighbourList.size());
					
					int randomAgentId  = neighbourList.get(randomIndex);
					int randomAgent_neighbourcount = neighbour_count_list.get(randomAgentId);
					if( (randomAgent_neighbourcount <= max_neighbours_limit) && !(alreadyNeighbours(agentId,randomAgentId,agentList)) )
					{
						
						if(setLink(agent.getId(),randomAgentId,"nei",neighbourWeight,agentList,linkList))
						{
							logger.debug("neighbour link successfully set for agents : "+ agentId + " and " + randomAgentId);
							randomAgent_neighbourcount++;
							agent_neighbourCount++;
							
							neighbour_count_list.put(agentId, agent_neighbourCount);
							neighbour_count_list.put(randomAgentId, randomAgent_neighbourcount);

						}
						else
						{
							logger.error("error setting the neighbour link for agents : "+agentId + " and " + randomAgentId);
						}


				    }
							
				}
			}
					
		}		
			
		return true; 
	}
	
	/* function:get the agents inside a particular distance range
	 * input: dist in km? since euclidean function returns in km
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
    
	public int getRandomInteger(int min_value, int max_value)
	{
		//adding 1 to make the max value inclusive 
		int rand = ThreadLocalRandom.current().nextInt(min_value, max_value);
		return rand;
	}
	
	public double getRandomDouble(double max_value, double min_value)
	{
		//adding 1 to make the max value inclusive 
		double rand = ThreadLocalRandom.current().nextDouble(min_value, max_value);
		return rand;
	}

	public boolean setLink(int agent1_Id, int agent2_Id, String linkType, double weight,HashMap<Integer,SocialAgent> agentList ,ArrayList<SocialLink> linkList)
	{
		//link already exists
		if(alreadylinked(agent1_Id,agent2_Id,agentList)) // link exist
		{
			SocialAgent agent1 = agentList.get(agent1_Id);
			SocialLink link = agent1.getLink(agent2_Id);
				
			
			if(linkType.equals("nei") && link.getNeighbourWeight() == 0.0)
			{
				link.setNeighbourWeight(weight);
				
			}
			else if(linkType.equals("fam") && link.getFamilyWeight() == 0.0)
			{
				link.setFamilyWeight(weight);

			}
			else if(linkType.equals("fri") && link.getFriendshipWeight() == 0.0)
			{
				link.setFriendshipWeight(weight);

			}
			else
			{
				logger.error("modification of existing link weights or link type mismatching");
			}
			
			link.setLinkWeight(); // modify the link weight
			return true;
		}
		
		//link does not exist
		else
		{
					
			SocialLink newlink = new SocialLink(agent1_Id,agent2_Id);

			
			if(linkType.equals("nei"))
			{
				newlink.setNeighbourWeight(weight);
			}
			else if(linkType.equals("fam"))
			{
				newlink.setFamilyWeight(weight);

			}
			else if(linkType.equals("fri"))
			{
				newlink.setFriendshipWeight(weight);

			}
			else
			{
				logger.error("modification of existing link weights or link type mismatching");
			}
			// creating two reference links for the new link 
			
			newlink.setLinkWeight(); // modify the link weight
			
			SocialLink refLink1 = newlink;
			SocialLink refLink2 = newlink;
			
			
			SocialAgent agent1 = agentList.get(agent1_Id);			
			agent1.addLink(agent2_Id, refLink1);
			
			SocialAgent agent2 = agentList.get(agent2_Id);			
			agent2.addLink(agent1_Id, refLink2);
			
			linkList.add(newlink);
			return true;
		}
		
	}
	// tests wether agents have a link of any relationship type
	public boolean alreadylinked(int agent1_Id, int agent2_Id, HashMap<Integer,SocialAgent> agentList)
	{
		SocialAgent agent1 = agentList.get(agent1_Id);
		if(agent1 == null) {
			logger.error("agent {} not found in the agent map, aborting" ,agent1_Id);
			return false;
		}
		
		HashMap<Integer,SocialLink> agent1_links = agent1.getLinkSet();
		
		
		SocialAgent agent2 = agentList.get(agent2_Id);
		if(agent2 == null) {
			logger.error("agent {} not found in the agent map, aborting" ,agent2_Id);
			return false;
		}
		
		HashMap<Integer,SocialLink> agent2_links = agent2.getLinkSet();
		
		boolean link1Exists = false;
		boolean link2Exists = false;
		

		SocialLink link1=agent1.getLink(agent2_Id);
		if(link1 != null)
			link1Exists = true;

		SocialLink link2=agent2.getLink(agent1_Id);
		if(link2 != null)
			link2Exists = true;


        // if both link refernces exists
// one or both link references do not exist
        return link1Exists && link2Exists;
		
	}
	

	public boolean alreadyNeighbours(int agent1_Id, int agent2_Id, HashMap<Integer,SocialAgent> agentList )
	{
		SocialAgent agent1 = agentList.get(agent1_Id);
		HashMap<Integer,SocialLink> agent1_links = agent1.getLinkSet();
		
		
		SocialAgent agent2 = agentList.get(agent2_Id);
		HashMap<Integer,SocialLink> agent2_links = agent2.getLinkSet();
		
		boolean link1Exists = false;
		boolean link2Exists = false;
		

		SocialLink link1=agent1.getLink(agent2_Id);
		if(link1 != null && link1.getNeighbourWeight() == neighbourWeight)
			link1Exists = true;

		SocialLink link2=agent2.getLink(agent1_Id);
		if(link2 != null && link2.getNeighbourWeight() == neighbourWeight)
			link2Exists = true;

        // if both link refernces exists
// one or both link references do not exist
        return link1Exists && link2Exists;
		
	}
	
	public boolean alreadyFriends(int agent1_Id, int agent2_Id, HashMap<Integer,SocialAgent> agentList )
	{
		SocialAgent agent1 = agentList.get(agent1_Id);
		HashMap<Integer,SocialLink> agent1_links = agent1.getLinkSet();
		
		
		SocialAgent agent2 = agentList.get(agent2_Id);
		HashMap<Integer,SocialLink> agent2_links = agent2.getLinkSet();
		
		boolean link1Exists = false;
		boolean link2Exists = false;
		

		SocialLink link1=agent1.getLink(agent2_Id);
		if(link1 != null && link1.getFriendshipWeight() == friendWeight)
			link1Exists = true;

		SocialLink link2=agent2.getLink(agent1_Id);
		if(link2 != null && link2.getFriendshipWeight() == friendWeight)
			link2Exists = true;

        // if both link refernces exists
// one or both link references do not exist
        return link1Exists && link2Exists;
		
	}
	
	public boolean alreadyFamily(int agent1_Id, int agent2_Id, HashMap<Integer,SocialAgent> agentList )
	{
		SocialAgent agent1 = agentList.get(agent1_Id);
		HashMap<Integer,SocialLink> agent1_links = agent1.getLinkSet();
		
		
		SocialAgent agent2 = agentList.get(agent2_Id);
		HashMap<Integer,SocialLink> agent2_links = agent2.getLinkSet();
		
		boolean link1Exists = false;
		boolean link2Exists = false;
		

		SocialLink link1=agent1.getLink(agent2_Id);
		if(link1 != null && link1.getFamilyWeight() == familyWeight)
			link1Exists = true;

		SocialLink link2=agent2.getLink(agent1_Id);
		if(link2 != null && link2.getFamilyWeight() == familyWeight)
			link2Exists = true;

        // if both link refernces exists
// one or both link references do not exist
        return link1Exists && link2Exists;
		
	}	
	
	

    
    /*input : agent x,y are in meters?
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
    			System.out.println("agentID : "+agent.id+" X: "+agent.getX()+" Y: "+agent.getY());
    			
    		}
    		
    		System.out.println("\n"); 
    }
    

    

}
