package io.github.agentsoz.socialnetwork;

import io.github.agentsoz.socialnetwork.datacollection.DataCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public abstract class DiffModel {

    protected  int diffStep;
    protected String contentType;

    protected double timeForNextStep = diffStep;
    protected SocialNetworkDiffusionModel snManager;

    final Logger logger = LoggerFactory.getLogger("");
	
	public void initialise() {}

    public HashMap<Integer,SocialAgent> getAgentMap() {
        return this.snManager.getAgentMap();
    }

    public int getNumAgentsForSeed(double agentPercentage) {
        return (int) (0.01 * snManager.getAgentMap().size() *agentPercentage);
    }

    public void preDiffProcess() {}

    public void step() {}

    public void postDiffProcess() {}

    public void printConfigParams() {}

    public void printthresholdMap() {}

   // public void printContentValues() {}

   // public boolean isDiffTurn(long time) {return false;}
   public boolean isDiffTurn(long simTime) {
       boolean result;
       result = simTime % this.diffStep == 0;
       logger.trace("isDiffTurn? {}",result);
       return result;
   }
    public void postDiffProcess(long time) {}

    public void finish(){

    }

    public void recordCurrentStepSpread(double timestep) {

    }

    public int getDiffStep(){
	    return this.diffStep;
    }
    public void registerContentIfNotRegistered(String newContent,String type) {};
    public double getTimeForNextStep() {
        return timeForNextStep;
    }

    public void setTimeForNextStep() {
        this.timeForNextStep = timeForNextStep + this.diffStep;
    }


    public abstract DataCollector getDataCollector();
}
