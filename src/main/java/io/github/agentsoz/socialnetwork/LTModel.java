package io.github.agentsoz.socialnetwork;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.github.agentsoz.socialnetwork.datacollection.LTModelDataCollector;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.Global;
import io.github.agentsoz.socialnetwork.util.Utils;

/*
 *  
 * low(inactive), medium and high panic agent counts:
 * initially all agents are set as low panic . then as each agents panic level is  updated, med and high panic
 * counts are incremented while low panic counts are decremented.
 * 
 * diff turn:
 * this is  a characteristic of a difffusion model,and therefore a function should be avaialable to check a diffusion turn 
 * with the current sim time
 * isDiffTurn(time)
 * 
 * seed:
 * seed should be separately added to the seedMap plus update the agent panic levels.
 * This is needed to keep the calculation simple at lt diffuse method.
 */
public class LTModel extends DiffModel{



	private static DecimalFormat df = new DecimalFormat(".##");
	private LTModelDataCollector dc;
	
	protected HashMap<Integer,double[]> thresholdMap = new HashMap<Integer,double[]>();
	private HashMap<Integer,Double> seedMap = new HashMap<Integer,Double>();
	
	private int diffSeed;
//	private int diffStep;
	private String threholdType;
	private double meanLowThreshold;
	private double meanHighThreshold;
	private double standardDev;
	protected int diffTurnCount=1;
	
	// count variables
	//protected int lowPanicCount; // inactive agents
	//protected int medPanicCount = 0;
	//protected int highPanicCount = 0;


	public LTModel(double seed, int turn, SocialNetworkManager snMan) { 
		this.diffSeed = (int) (seed * 0.01 * snMan.getAgentMap().size()); // divide by 100 for the percentage multiply by the #agents
		this.diffStep = turn; // already converted to seconds at SNConfig
		this.snManager = snMan;

		this.dc = new LTModelDataCollector();
	}
	
	/*
	 *  Initialisation method. all the methods to initialise the diff model should run here. 
	 *  After this method the diff model should be ready for diffusion
	 *  conifgs = diffusion threshold type 
	 */
	@Override
	public void initialise() {

		logger.debug("initialising LT model...");
		//1. setupConfigs
		setupDiffConfigs();
	
		//2. assign Thresholds
		assignThresholds();
		
		//3. initially, set the  low panic agents to the agentmap size
	//	lowPanicCount = getAgentMap().size();
		
		//4. set initial panic agents based on strategy
		initSeedBasedOnStrategy();
		
		//5 write add state counters after iniatialising diffusion seed
		// The actual time here is at 4secs. but it should be 1sec
//		ScenarioThreeData.addCountData(
//				1,
//				getLowPanicCount(),
//				getMedPanicCount(),
//				getHighPanicCount());
	} 
	
	public void initTestLTModel(double low, double high,HashMap<Integer,Double> seed) {
		
		//1.  no setupconfigs
	
		//2. assign fixed Thresholds
		assignFixedThresholds(low,high);
		
		//3. initially, set the  low panic agents to the agentmap size
	//	lowPanicCount = getAgentMap().size();
		
		//4. set fixed seed from a hashmap
		initFixedSeed(seed);

	} 
	
	private void initFixedSeed(HashMap<Integer,Double> seedmap) {
		logger.debug("Test SNmodel:  seed size {}", seedmap.size());
		
		for (Entry<Integer,Double> seedRow: seedmap.entrySet()) {
			this.seedMap.put(seedRow.getKey(),seedRow.getValue());
			updatePanicValue(seedRow.getKey(),seedRow.getValue());
		}
		
	}

	// each of these methods should store selected agents panic levels to the seedmap, as well separatel update their
	// panic levels and states (which matters for the BDI behaviour)
	public void initSeedBasedOnStrategy() { 
		if(SNConfig.getStrategy_lt().equals(DataTypes.RANDOM)) {
			initRandomSeed();
		}
		
		else if(SNConfig.getStrategy_lt().equals(DataTypes.NEAR_FIRE)) {
			initNearFireSeed();
		}
		else if(SNConfig.getStrategy_lt().equals(DataTypes.PROBILITY)){
			initProbabilisiticSeed();
		}
		else
		{
			logger.error("LT model - Unknown Seeding strategy");
		}
	}
	public void assignThresholds() {
		
		for (int id: getAgentMap().keySet()) {
			
			if(threholdType.equals(DataTypes.RANDOM)) { 
				assignRandomTresholds(id);
			}
			else if(threholdType.equals(DataTypes.GAUSSIAN)) { 
				assignGaussianDistThresholds(id,meanLowThreshold,meanHighThreshold,standardDev);
			}

		}
		
		logger.info("diffusion threshold assigning complete");
	}
	
	public void assignFixedThresholds(double low, double high) {
		
		for (int id: getAgentMap().keySet()) {
			
			double[] tresholdArr = new double[2];
						
			tresholdArr[0] = Double.valueOf(df.format(low));
			tresholdArr[1] = Double.valueOf(df.format(high));
			
			//update the thresholdmap
			this.thresholdMap.put(id, tresholdArr);

		}
		
		logger.info("fixed threshold assigning complete");
	}
//	@Override
//	public boolean isDiffTurn(long simTime) {
//		boolean result;
//        result = simTime % this.diffTurn == 0;
//		logger.trace("isDiffTurn? {}",result);
//		return result;
//	}
	public void setupDiffConfigs() { 
		threholdType = SNConfig.getDiffusionThresholdType_lt();
		if(threholdType.equals(DataTypes.GAUSSIAN)) {
			meanLowThreshold = SNConfig.getMeanLowPanicThreshold_lt();
			meanHighThreshold = SNConfig.getMeanHighPanicThreshold();
			standardDev = SNConfig.getStandardDeviation_lt();
		}
		
		logger.debug("custom configs assigned to LTModel..");
	}
	
	@Override
	public void preDiffProcess() {}

    @Override
	public void doDiffProcess() {
    	logger.trace("diffusion process: turn {}", diffTurnCount);
    	
    	ltDiffuse();
    	
    	diffTurnCount++;
	}

    @Override
	public void postDiffProcess(long time) {
		
		//1. data collection  is done after BDI beleif udpate
		
	}


    public void assignRandomTresholds(int id) {
		double highT=0.0,actT;
		double[] tresholdArr = new double[2];
		
		actT = Global.getRandom().nextDouble();
		while(actT >= highT) { 
			highT = Global.getRandom().nextDouble();
		}
		
		tresholdArr[0] = Double.valueOf(df.format(actT));
		tresholdArr[1] = Double.valueOf(df.format(highT));
		
		//update the thresholdmap
		this.thresholdMap.put(id, tresholdArr);
	}
	
	
	/* IMP: Thigh is not used in this method eventhough considered as input parameter
	 * For each agent - draw Tlow from dist and then double that drawn value. 
	 * If values are drawn from both distributions, then it is unlikely that high = 2 * low.
	 * better not to round the doubles to two decimals as we need to have diferent 40,000 values.
	 * highTmean > lowTmean
	 * same SD for both gaussian distributions
	 */
	public void assignGaussianDistThresholds(int id, double lowTmean, double highTmean, double sd) { 
		double[] tresholdArr = new double[2];
		
		double low = Utils.getRandomGaussion(sd, lowTmean); // a value between 0 and 1.
		double high = 2 * low;
		if(high > 1) {
			high = 1;
		}
		
		if(low >= high) {
			logger.error("agent {} - Tlow  {} greater than or equal to THigh {}, terminating", id,low,high);
			System.exit(-1);
		}
		
		tresholdArr[0] = low;
		tresholdArr[1] = high;
		
		//tresholdArr[1] = Utils.getRandomGaussion(sd, highTmean);
		
		//update the thresholdmap
		this.thresholdMap.put(id, tresholdArr);
		

	}
	
	/*
	 * randomly select seed amount of agents
	 * gen random panic level (above the activation threshold)	and set panic level and the state
	 * Note - selected number of agents from this method differ to the actual active agents(medium+high)
	 * as the same agent can be selected twice.
	 * The ouput from this method is stored  in the data collection arraylists. 
	 * 
	 * Note - if the same agent is selected twice, then the diffusion counters go minus. 
	 * So ensuring that unique set of agents are selected as only a fraction of agents are selected always. 
	 * therefore the expected number of agents should be equal to the actual active agents(medium+high) here. 
	 */
	public void initRandomSeed() {

		int selected = 0 ;
		List<Integer> selectedAgentIds = new ArrayList<Integer>();
		
		while(selected < this.diffSeed) {
			
			List<Integer> keysAsArray = new ArrayList<Integer>(getAgentMap().keySet());
			int randomId= keysAsArray.get(Global.getRandom().nextInt(keysAsArray.size() - 1)); // first randomly select an id
			
			while(selectedAgentIds.contains(randomId)) {
				randomId= keysAsArray.get(Global.getRandom().nextInt(keysAsArray.size() - 1)); // another random id
			}
			
			selectedAgentIds.add(randomId); // finally add the unique random id to the selected list
			
			logger.trace("extDiffuse - random Id: {}",randomId);
			//activation threshold of the random agent
			double min = getActivationThreshold(randomId);
			
			logger.trace("extDiffuse - actT: {}",min);
			double max = 1.0;
			double randomPanicVal =  min + ((max - min) * Global.getRandom().nextDouble());
						
			//2steps to update panic levels.
			updatePanicValue(randomId,randomPanicVal);
			seedMap.put(randomId, randomPanicVal);
			getAgentMap().get(randomId).setIsSeedTrue(); // agent is part of the seed
			selected++;
		}

		// counting social states
		dc.countLowMedHighAgents(this.snManager);

		logger.info("initialise random seed complete-expected active agents: {}", selected);
		logger.info("INACTIVE agents: {}  | ACTIVE agents: {}",dc.getLowCt(), dc.getMedCt());
	}
	
	/* function: selects the agents near fire 
	 * 
	 * 
	 */
	public void initNearFireSeed() {

		// total agents in the high risk area = 12511 ~ 32% of the population
		int nearDistanceEastingRange = 290000;

		List<Integer> agentsNearFire = new ArrayList<Integer>();
		
		//1. select all agents near fire
		for (SocialAgent agent: getAgentMap().values()) {
			
			double easting = agent.getX();
			
			if( easting <= nearDistanceEastingRange) {
				agentsNearFire.add(agent.getID());
			}
			
			
			
		}
		logger.info("total agents near fire: {}",agentsNearFire.size());
		if(agentsNearFire.size() == 0) {
			logger.error("no agents near fire found - aborting");
			return;
		}
		
		//2. randomly select a unique subset near fire
		// same functionality as random seed generation, except list of agents is agents near fire
		// and not the  whole agent map.
		int selected = 0 ;
		List<Integer> selectedAgentIds = new ArrayList<Integer>();
		
		while(selected < this.diffSeed) { // percentage
			
			//List<Integer> keysAsArray = new ArrayList<Integer>(getAgentMap().keySet());
			int randomId= agentsNearFire.get(Global.getRandom().nextInt(agentsNearFire.size() - 1)); // first randomly select an id
			
			while(selectedAgentIds.contains(randomId)) {
				randomId= agentsNearFire.get(Global.getRandom().nextInt(agentsNearFire.size() - 1)); // another random id
			}
			
			selectedAgentIds.add(randomId); // finally add the unique random id to the selected list
			
			logger.trace("agentsNearFire  strategy - random Id: {}",randomId);
			//activation threshold of the random agent
			double min = getActivationThreshold(randomId);
			
			logger.trace("agentsNearFire  strategy - actT: {}",min);
			double max = 1.0;
			double randomPanicVal =  min + ((max - min) * Global.getRandom().nextDouble());
						
			updatePanicValue(randomId,randomPanicVal);
			seedMap.put(randomId, randomPanicVal);
			getAgentMap().get(randomId).setIsSeedTrue(); // agent is part of the seed
			selected++;
		}
		
		logger.info("initialise near fire seed complete- selected agents ({}): {}", this.diffSeed, selected);

		dc.countLowMedHighAgents(this.snManager);
		logger.info("INACTIVE agents: {}  | ACTIVE agents: {}",dc.getLowCt(), dc.getMedCt() );


	}
	/*
	 * This function selects the seed based on a probability, where agents near the seed has a higher probability to get
	 * selected and this probability decreases linearly as the distance from firefront increases.
	 * By looking at the homeocations.txt, the agents are stored from 28km -> 30km.
	 * seed -right number of agents are selected - tested 
	 */
	public void initProbabilisiticSeed() {
		logger.info("LT - initialising probabiistic seeding..");
		// total agents in the high risk area = 12511 ~ 32% of the population  for distance range 290000
		//int nearDistanceEastingRange = 10;  
		int firefront = 280000; // distance is measured from this 
		int selected = 0; 
		
		List<Integer> replicateIdList = new ArrayList<Integer>();
		
		//logger.trace("before shuffle: {}",getAgentMap().keySet().toString());
		for (int id: getAgentMap().keySet()) { // add all ids
			replicateIdList.add(id);
		}
		
		//shuffle the id list -  TESTED with small size
		Collections.shuffle(replicateIdList,Global.getRandom());
		//logger.trace("after shuffle: {}",replicateIdList.toString());
		
				for(int id: replicateIdList) { 
						
				if(selected >= this.diffSeed) { 
					logger.info("probabilistic seed - activated agents: {}",selected);
					return;
				}
		
				SocialAgent agent = getAgentMap().get(id);
				double easting = agent.getX(); 
				double dist = (easting - firefront)/1000;  // in km
				if (dist <= 0) {dist = 0;} // max activation probability will be 0.75
				double activationProb = getProbabilityForDistanceFromFormula(dist);
				
				if( Global.getRandom().nextDouble() <= activationProb) {
					
					
					double min = getActivationThreshold(agent.getID());
					
					logger.trace("probabilistic seed strategy - actT: {} id: {}",min,id);
					double max = 1.0;
					double randomPanicVal =  min + ((max - min) * Global.getRandom().nextDouble()); // random panic value
		//			logger.trace("seed selected agent: {} plevel: {}",agent.getID(),randomPanicVal );
					//2steps
					seedMap.put(agent.getID(), randomPanicVal);
					agent.setIsSeedTrue(); // agent is part of the seed
					updatePanicValue(agent.getID(),randomPanicVal);
					selected++;
					
						}
				
				
			
			
		}


//		System.exit(0);
	}
	
	/*generates a probability based on the distance
	 * this function can be used for both firefront distance and farEnd distance values. this is actually independant from that.
	 * Based on the distance, a probability is generated
	 * if dist = 0, p = 0.75
	 * if dist = 30, p= 0.25
	 */
	public double  getProbabilityForDistanceFromFormula(double dist){
		double prob =  (-1*0.0167 * dist) + 0.75;
		if (prob < 0) {prob = 0;}

		return prob;
	}
	
	
	public void ltDiffuse() {
		logger.trace("linear threshold diffusion process..");
		HashMap<Integer,Double> panicValsToBeUpadated =  new HashMap<Integer,Double>();
		for (SocialAgent agent: getAgentMap().values()) {

			//these conditions are checked to maintain static panic levels/state of agents.
			//Conceptually, this should happen at the network level changing the linksmap
			if(agent.isSeed()) {
				continue;
			}
			if(agent.getEvacStatus()) {
				continue;
			}

			double pValue = 0.0 ; 
			HashMap<Integer,Double> neiMap = agent.getLinkMap();
			for(int neiId:neiMap.keySet()) { 
				if(isActive(neiId)) {
					pValue = pValue + agent.getLinkWeight(neiId);
				}
			}
			
			panicValsToBeUpadated.put(agent.getId(), pValue);
			
		}
	//	logger.debug("panic values: {}", panicValsToBeUpadated.toString());
		for (Map.Entry entry: panicValsToBeUpadated.entrySet()) {
			
			int id = (int) entry.getKey();
			double pNei = (double) entry.getValue();
			
			double pSeed = getSeedPanicLevel(id);
			double pTot = pSeed + pNei;
			
			if(pTot > 1.0) { pTot = 1.0;} // tot may exceed 1.0 coz of the seed, eventhough 
			
			//finally to all agents update panic value if the calculated panic value is greater than the current. 
			// if externally activated, then there  might be no active agents.
			// if there's a change in pvalue, modify it. 
			// if agent is externally activated with random panic value, and internal panic calcualted should not replace that
			//value until it exceeds the randomly generated value
//			if(pTot > getPanicValue(id)){ // NOT VALID IN NON_PROGRESSIVE LT DIFFUSSION
//				updatePanicValue(id,pTot) ;
//			}
			updatePanicValue(id,pTot); // update panic value without any condition
		}
		//printPanicValues();
	}
	
	@Override
	public void printthresholdMap() {
	
		logger.debug(" agent threshold values: ");
		for (Map.Entry entry: this.thresholdMap.entrySet()) {
			int id = (int) entry.getKey();
			double[] arr = (double[]) entry.getValue();
			
			if (SNConfig.getDiffusionModelsList().contains(DataTypes.ltModel)) {
				logger.debug("id  {}: actT {} highT {}", id, arr[0], arr[1]);
			}
			else if(SNConfig.getDiffusionModelsList().contains(DataTypes.CLTModel)) {
				logger.debug("id  {}: waitT {} panicT {}", id, arr[0], arr[1]);
			}
		}
	}
	// the condition that is checked to change the state
	public boolean checkActivationCondition(double value,  int id) { 
		boolean result=false;
		if( value >= getActivationThreshold(id)) {
			result = true;
		}
		
		return result;
	}
	
	// this method updates the panic level and also sets the state and the count variables
	// Reason  why state is updated inside this method: 
	//pvalue can change without a change in the state. But the state cannot change w/o a change in the pvalue
	//lt diffusion is progressive.
	//two instances:
	//1. initially if the state is not set, state is set but there is an increase in the panic level 
	// when counts are increased/decraesed, state has to be checked. 
	// states:  low , med high 
	// this is the only method that sets the state
	// 
/*all possible cases:
 * low->high medium -> high high -> high
 * low->medium  medium->medium 
 * low->low 
 * 
 * For each row of panic data, the total low,med and high should equal to 38,343
 * 
 * lt diffusion is progressive : low -> medium -> high
 * Note -  if the same agent is selected twice in setInitPanicAgents method, then flow can change, 
 * e.g. an agent can be first selected as high panic and then second time it can have a medium panic
 * 
 * Panic values are not rounded. also they are not checked if less than 1
 */
	public void updatePanicValue(int id, double pVal) {

		SocialAgent agent = getAgentMap().get(id);
		//1. firstly, check if agent evacuated already don't change the panic value
		// this conditions should be chcked earlier in seeding and diffusion methods.
			if(agent.getEvacStatus()) {
				return ;
			}


		// update the panic level regardless of state
		agent.setPanicLevel(pVal);

		//2. update state
//		if ((pVal >= getHighPanicThreshold(id)) && !agent.getState().equals(DataTypes.HIGH)) {
//			agent.setState(DataTypes.HIGH);
//		}
		if ((pVal >= getActivationThreshold(id) /*&& pVal < getHighPanicThreshold(id)*/ )  && !agent.getState().equals(DataTypes.MEDIUM)) {
			agent.setState(DataTypes.MEDIUM);

		}
		else if (pVal < getActivationThreshold(id) && !agent.getState().equals(DataTypes.LOW)) {
			agent.setState(DataTypes.LOW);
		}

		logger.trace(" updated state: agent {} panic value {} state: {}", id, pVal,getAgentMap().get(id).getState());

	}
	
	
	public double getPanicValue(int id) {
		SocialAgent agent = getAgentMap().get(id);
		return agent.getPanicLevel();
	}
	
	public double getActivationThreshold(int id) {
		double[] thresholdArr = this.thresholdMap.get(id);
		return thresholdArr[0];
	}
	
	
	public double getHighPanicThreshold(int id) {
		double[] thresholdArr = this.thresholdMap.get(id);
		return thresholdArr[1];
	}
	
	public double getSeedPanicLevel(int id) {
		double panic=0.0;
		if(seedMap.containsKey(id)) { 
			panic = this.seedMap.get(id);
		}

		return panic;
	}
	
	public int getDiffTurnCount() {
		return this.diffTurnCount;
	}

	public LTModelDataCollector getDataCollector() {
		return dc;
	}

	public boolean isActive(int id) {
		boolean result=false;
		SocialAgent agent= getAgentMap().get(id);
		if(agent.getState().equals(DataTypes.MEDIUM) || agent.getState().equals(DataTypes.HIGH) ) { 
			result = true;
		}
		
		return result;
	}
	
//	@Override
//	public int getLowPanicCount() {
//		return this.lowPanicCount;
//	}
//
//	@Override
//	public int getMedPanicCount() {
//		return this.medPanicCount;
//	}
//	@Override
//	public int getHighPanicCount() {
//		return this.highPanicCount;
//	}
	
	// prints the panic  levels of the agents
	@Override
	public void printPanicValues()
	{
		HashMap<Integer,SocialAgent> agents = getAgentMap();
		logger.debug("panic values:");
		for (SocialAgent agent : getAgentMap().values())
		{
			 double panicVal = agent.getPanicLevel();
			 logger.debug("agent " + agent.getId() + " : "+panicVal + " : "+agent.getState());
			
		}
	}
	
	@Override
	public void printConfigParams() {
		logger.debug("assigned config parameters:");
		logger.debug("thresholdType {}", this.threholdType);
		logger.debug("seed {}", this.diffSeed);
		logger.debug("turn {}", this.diffStep);
		logger.debug("meanLowT {}", this.meanLowThreshold);
		logger.debug("meanHighT {}", this.meanHighThreshold);
		logger.debug("standard deviation {}", this.standardDev);
	}
}
