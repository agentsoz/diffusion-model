package io.github.agentoz.socialnetwork;

public class Link {

	int id;
	double weight;
	boolean hasLink;
	
	public int getId()
	{
		
		return this.id;
	}
	
	public boolean setId(int newID)
	
	{
		this.id=newID;
		return true;
	}
	
	public double getWeight()
	{
		
		return this.weight;
	}

}
