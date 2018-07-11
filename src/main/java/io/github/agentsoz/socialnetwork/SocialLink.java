package io.github.agentsoz.socialnetwork;

/*
 * How link(id1,id2) is distinguished from link(id2,id1) ? 
 * when a link is created: the two agent ids are added as to and from. But this does not mean anything
 * specific. It doesn't mean anything. 
 * Each agent has a map of its own links. This map contains the id and the link instance.
 *  Therefore, for  id1, its id2,link and for id2, its id1,link
 *  
 *  In alreadyLinked, you try to get the link with a particular id:
 *  if null -> link doesn't exist
 *  if !null -> link exists
 * Since this is a map, there will always be a single link created between two agents
 */

public class SocialLink extends Link{

	int linkedTO;
	int linkedFROM;
	String id;
	private	double linkWeight=0.0;
	
	public SocialLink(int to, int from)
	{
	//	this.id=id;
		this.linkedTO=to;
		this.linkedFROM = from;
		
	}
	

	public String getID()
	{
		return this.id;
	}

	public void setLinkWeight(double newLinkWeight)
	{
		this.linkWeight=newLinkWeight;
	}

	
	public double getLinkWeight()
	{
		
		return this.linkWeight;
	}
	

}
