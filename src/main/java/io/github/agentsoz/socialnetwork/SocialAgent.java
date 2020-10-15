package io.github.agentsoz.socialnetwork;

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
import io.github.agentsoz.socialnetwork.util.DataTypes;

public class SocialAgent extends Node{
	
//private int ID;
private double Xcord;
private double Ycord;
private HashMap<String,String> diffStates =  new HashMap<String,String>();
//// identfy weather the agent is part of a seed or not. If so the agent panic level remains static. Now uses isSeedMap
//private boolean isSeed=false;
private boolean evacStatus = false;

//private double panicLevel=0.0;
private HashMap<Integer,Double> linkMap;
private HashMap<String,Double> contentValuesMap =  new HashMap<String, Double>(); // used for the CLT Model, so created a new one
	private HashMap<String,Double> contentLevelsMap =  new HashMap<String, Double>(); //generic one for LT model, multiple contents
private HashMap<String,Boolean> isSeedMap = new HashMap<String, Boolean>();

//LT/probabilistic models
private ArrayList<String> adoptedContentList;

public HashMap<Integer,SocialLink> links ;
final Logger logger = LoggerFactory.getLogger("");
private static DecimalFormat df = new DecimalFormat(".##");

	public SocialAgent(int id, double x_cord, double y_cord)
	{
		super.id=id;	
//		this.diffStates =DataTypes.LOW;
		this.Xcord=x_cord;
		this.Ycord=y_cord;
		this.links= new HashMap<Integer,SocialLink>();
	}

	public SocialAgent(int id) // For LT model
	{
		super.id=id;
//		this.diffStates =DataTypes.LOW; // inactive
		this.linkMap= new HashMap<Integer,Double>();
		
		
	}

	public SocialAgent(int id, String content, String initState) // For CLT model and a generic
	{
		super.id=id;
		this.diffStates.put(content,initState);
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

	public ArrayList<String> getAdoptedContentList() {
		return adoptedContentList;
	}

	public void initAdoptedContentList() {
		if(this.adoptedContentList == null) {
			this.adoptedContentList = new ArrayList<String>();
		}
	}

	public void adoptContent(String newContent) {
		if(this.adoptedContentList == null) {
			this.adoptedContentList = new ArrayList<String>();
		}
		this.adoptedContentList.add(newContent);
	}

	public boolean alreadyAdoptedContent(String content) {
		if(this.adoptedContentList.contains(content)) {
			return true;
		}
		else{
			return false;
		}

	}
//	public double getContentlevel(String type) {
//		return this.contentValuesMap.get(type);
//	}

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
	
	public void setState(String content, String newState)
	{
		this.diffStates.put(content,newState) ;
	}	
	
	
	public String getState(String content)
	{
		return this.diffStates.get(content);
	}	
	
	public boolean isActive(String content){
		boolean result=false;
		String state = this.diffStates.get(content);
		if(state.equals(DataTypes.MEDIUM) || state.equals(DataTypes.HIGH) ) {
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
	public double getContentLevel(String content)
	{
		return this.contentLevelsMap.get(content);
		
	}

	public boolean checkIfPartOfTheSeed(String content) {
		return isSeedMap.get(content);
	}

	public void setAsPartOfTheSeed(String content, boolean status) {
		this.isSeedMap.put(content,status);
	}

	public String getActivatedContentType() { // only used in CLT Model
		if(this.diffStates.get(DataTypes.WAIT).equals(DataTypes.LOW)) {
			return DataTypes.WAIT;
		}
		else if(this.diffStates.get(DataTypes.PANIC).equals(DataTypes.HIGH)){
			return DataTypes.PANIC;
		}
		else{
			return DataTypes.INACTIVE; //inactive
		}
	}
	public void setContentLevel(String content, double newPanicLevel)
	{
		//newly added rounding utility
//		double roundedPanic = Double.valueOf(df.format(newPanicLevel));
//		this.panicLevel=roundedPanic;
		
		this.contentLevelsMap.put(content,newPanicLevel);
		logger.trace(" agent {} updated panic value: {} for content {}", this.getId(), newPanicLevel,content);
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
	
//	public boolean  isSeed()
//	{
//		return this.isSeed;
//	}
//
//	public void  setIsSeedTrue(String content)
//	{
//		 this.isSeed =  true;
//	}

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
	

	public SocialLink getLink(int agentId)
	{
		SocialLink link = this.links.get(agentId);
		return link;
	}


//	public void printLinks()
//	{
//		socialNetworkDiffusionLogger.debug("agentID : "+this.id);
//		int i=1;
//		for (Map.Entry entry :this.links.entrySet())
//		{
//			SocialLink  link = (SocialLink) entry.getValue();
//			socialNetworkDiffusionLogger.debug("   linkNo:"+i+" connectedTo:"+entry.getKey()+" linkweight:"+link.getLinkWeight()+" neighbour:"+link.getNeighbourWeight()+" family:"+link.getFamilyWeight()+" friend:"+link.getFriendshipWeight());
//			i++;
//		}
//	}
	
}
