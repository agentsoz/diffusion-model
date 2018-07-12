package io.github.agentsoz.socialnetwork;

public abstract class Node {

	int id;
	double linkWeight;
	
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

		return this.linkWeight;
	}
}
