package io.github.agentoz.socialnetwork;

/* In the links map, always the to will the particular agent that have the linkmap, and from will

 * the other agents 
 * 
 * Why should the A and B have same weights of influences? They may be different. Therefore it is better to have the link weight as an attribute of an
 * agent rather than have a link object with the associated agents.
 * 
 */
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.agentoz.socialnetwork.util.DataTypes;

public class SocialAgent extends Node{
	
//private int ID;
private double Xcord;
private double Ycord;
private int schoolID;	
private String diffState;
private boolean isSeed=false; // identfy weather the agent is part of a seed or not. If so the agent panic level remains static
	private boolean evacStatus = false;

private String contentType;
private double panicLevel=0.0;	
private ArrayList<Integer> familyIDs ;
private HashMap<Integer,Double> linkMap;
private HashMap<String,Double> contentValuesMap =  new HashMap<String, Double>();

public HashMap<Integer,SocialLink> links ;
final Logger logger = LoggerFactory.getLogger("");
private static DecimalFormat df = new DecimalFormat(".##");

	public SocialAgent(int id, double x_cord, double y_cord)
	{
		super.id=id;	
		this.diffState=DataTypes.LOW;
		this.Xcord=x_cord;
		this.Ycord=y_cord;
		this.familyIDs = new ArrayList<Integer>();
		this.links= new HashMap<Integer,SocialLink>();
	}

	public SocialAgent(int id) // For LT model
	{
		super.id=id;
		this.diffState=DataTypes.LOW;
		this.familyIDs = new ArrayList<Integer>();

		this.linkMap= new HashMap<Integer,Double>();
		
		
	}

	public SocialAgent(int id, String initState) // For CLT model and a generic
	{
		super.id=id;
		this.diffState=initState;
		this.linkMap= new HashMap<Integer,Double>();

		this.contentValuesMap.put(DataTypes.WAIT, 0.0);
		this.contentValuesMap.put(DataTypes.PANIC, 0.0);

	}

	public void setContentlevel(String type, double level){
		if(type.equals(DataTypes.WAIT)) {
			this.contentValuesMap.put(DataTypes.WAIT,level);
		}
		else if(type.equals(DataTypes.PANIC)){
			this.contentValuesMap.put(DataTypes.PANIC,level);
		}

	}



	public double getContentlevel(String type) {
		return this.contentValuesMap.get(type);
	}

	public HashMap<String, Double> getContentValuesMap() {
		return contentValuesMap;
	}

	public double getX()
	{
		return this.Xcord;
		
	}
	public double getY()
	{
		return this.Ycord;
		
	}
	
	public void setX(double xcord) {
		Xcord = xcord;
	}

	public void setY(double ycord) {
		Ycord = ycord;
	}
	
	public void setState(String newState)
	{
		this.diffState=newState;
	}	
	
	
	public String getState()
	{
		return this.diffState;
	}	
	
	public boolean isActive(){
		boolean result=false;
		if(this.diffState.equals(DataTypes.MEDIUM) || this.diffState.equals(DataTypes.HIGH) ) { 
			result = true;
		}
		
		return result;
	}
	public int getID()
	{
		return this.id;
	}	
	
	public double getSumWeights() { 
		double  sum = 0.0;
		for(double weight: this.linkMap.values()) {
			sum = sum + weight;
		}
		
		return sum;
	}
	public double getPanicLevel()
	{
		return this.panicLevel;
		
	}

	public String getActivatedContentType() {
		if(this.diffState.equals(DataTypes.LOW)) {
			return DataTypes.WAIT;
		}
		else if(this.diffState.equals(DataTypes.HIGH)){
			return DataTypes.PANIC;
		}
		else{
			return DataTypes.INACTIVE; //inactive
		}
	}
	public void setPanicLevel(double newPanicLevel)
	{
		//newly added rounding utility
//		double roundedPanic = Double.valueOf(df.format(newPanicLevel));
//		this.panicLevel=roundedPanic;
		
		this.panicLevel=newPanicLevel;
		logger.trace(" agent {} updated panic value: {}", this.getId(), newPanicLevel);
	}
	
	public boolean alreadyLinked(int neiID) { 
		return this.linkMap.containsKey(neiID);
	}
	
	/*
	 * Two Functions:
	 * 1. to initially add a neighbour to  the neighbour map 
	 * 2. to modify the weight of an existing neighbour
	 */
	public void addNeighbourOrModifyWeight(int neiID, double weight) { 

		//double roundedWeight = Double.valueOf(df.format(weight));
//		this.linkMap.put(neiID,roundedWeight);
		this.linkMap.put(neiID,weight);
	}
	
	public void setSchoolID(int id)
	{
		this.schoolID=id;
		
	}
	public int getSchoolID()
	{
		return this.schoolID;
		
	}
	
	public void addLink(int partnerId, SocialLink ref_link)
	{
		this.links.put(partnerId, ref_link);
	}
	
	public HashMap<Integer,SocialLink>  getLinkSet()
	{
		return this.links;
	}
	
	public void normaliseWeights() {
		double sumW = this.getSumWeights(); 
		for( Map.Entry entry : this.linkMap.entrySet()) {
			 int neiId =  (int) entry.getKey();
			 double neiWeight = (double)entry.getValue();
			 double normWeight =  neiWeight/ sumW;
			// df.setRoundingMode(RoundingMode.DOWN);
			// this.linkMap.put(neiId, Double.valueOf(df.format(normWeight)));
			 this.linkMap.put(neiId, normWeight);
		}
	}
	
	
	public void printWeights()
	{
		logger.debug("agent: "+this.id);
		for (Map.Entry entry :this.linkMap.entrySet()) {
				int neiId = (int) entry.getKey();
				double weight = (double) entry.getValue();
				logger.debug("nei: {}  weight: {}",neiId,weight);
		}
	}
	
	public int  getLinkMapSize()
	{
		return this.linkMap.size();
	}
	
	public HashMap<Integer,Double>  getLinkMap()
	{
		return this.linkMap;
	}
	
	
	public String  getLinkedNeighbourIDs()
	{
		return this.linkMap.keySet().toString();
	}
	
	public boolean  isSeed()
	{
		return this.isSeed;
	}

	public void  setIsSeedTrue()
	{
		 this.isSeed =  true;
	}

	public boolean  getEvacStatus()
	{
		return this.evacStatus;
	}

	public void  setEvacStatus(boolean status)
	{
		this.evacStatus =  status;
	}

	public double  getLinkWeight(int id)
	{
		return this.linkMap.get(id);
	}
	
	public HashMap<Integer,SocialLink>  getLink()
	{
		return this.links;
	}
	
	public SocialLink getLink(int agentId)
	{
		SocialLink link = this.links.get(agentId);
		return link;
	}
	public void setFamilyID(int id)
	{
		this.familyIDs.add(id);
		
	}
	
	public ArrayList getFamilyIDs()
	{
		return this.familyIDs;
		
	}

	public int getFamilySize()
	{
		return this.familyIDs.size();
		
	}
		
	public void printFamilyIDs()
	{
		logger.debug(" agent {} family IDs : {}", id, familyIDs.toString());

//		for(Integer familyID : familyIDs)
//		{
//		}
	}
	
	public void printLinks()
	{
		logger.debug("agentID : "+this.id);
		int i=1;
		for (Map.Entry entry :this.links.entrySet())
		{
			SocialLink  link = (SocialLink) entry.getValue();
			logger.debug("   linkNo:"+i+" connectedTo:"+entry.getKey()+" linkweight:"+link.getLinkWeight()+" neighbour:"+link.getNeighbourWeight()+" family:"+link.getFamilyWeight()+" friend:"+link.getFriendshipWeight());
			i++;
		}
	}
	
}
