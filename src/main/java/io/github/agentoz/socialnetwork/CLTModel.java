package io.github.agentoz.socialnetwork;

import io.github.agentoz.socialnetwork.util.DataTypes;
import io.github.agentoz.socialnetwork.util.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


public class CLTModel extends LTModel{


    private int seedW = 0; // #agents
    private int seedP = 0;

    // seed panic values are used in the diffusion method to udpate the agent panic value
    private HashMap<Integer,Double> waitSeedMap = new HashMap<Integer,Double>();
    private HashMap<Integer,Double> panicSeedMap = new HashMap<Integer,Double>();


    private double meanWaitThreshold;
    private double meanPanicThreshold;
    private double standardDev;

    //threshold maps - LTModel variable used. index 0 = wait, index 1= panic

    //count variables
  //  protected int optCount=getAgentMap().size();
  //  protected int waitCount=0;
  //  protected int panicCount=0;

    public CLTModel(double sw,double sp, int turn, SocialNetworkManager snm){
        super(0,turn,snm); // init turn and snmanger
        this.seedW =  (int)( sw * 0.01 * snm.getAgentMap().size());
        this.seedP =  (int) (sp * 0.01 * snm.getAgentMap().size());
        logger.trace("seedW {} seedP {} ", sw,this.seedP);
    }

    @Override
    public void setupDiffConfigs() {
       this.meanWaitThreshold = SNConfig.getWaitThreshold();
       this.meanPanicThreshold = SNConfig.getPanicThreshold();
       this.standardDev = SNConfig.getStandardDeviation();

        logger.trace("diffusion configs assigned to CLTModel..");
    }


    @Override
    public void initialise() {

        setupDiffConfigs(); // 1.set the CLT diffusion configs

        assignThresholds(); // assign the waitT panicT thresholds

        //3. initially, set the  med panic agents to the agentmap size  - Already initialised at the variable
//        medPanicCount = getAgentMap().size();

        //4. set initial panic agents based on strategy
        // seeding methods should be done sequenctial for each process.
        initSeedBasedOnStrategy();

//        //5. add count data
//        ScenarioThreeData.addCountData(
//                1,
//                getWaitCount(),
//                getOptCount(),
//                getPanicCount());

    }

    @Override
    public void assignThresholds() {
        if(SNConfig.getDiffusionThresholdType().equals(DataTypes.GAUSSIAN)) {
            for(int id: getAgentMap().keySet()) {
                assignGaussianDistThresholds(id,this.meanWaitThreshold,this.meanPanicThreshold,this.standardDev);

            }
            logger.trace("map size {}", this.thresholdMap.size());
        }


    }

    @Override
    public void doDiffProcess(long time){

        logger.debug("competitive diffusion: {}  simtime: {}", diffTurnCount, time);
        runCompetitiveDiffusion();

        diffTurnCount++;
    }

    @Override
    public void postDiffProcess(long time) {

//        //1. data collection
//        ScenarioThreeData.addCountData(
//                time,
//                getWaitCount(),
//                getOptCount(),
//                getPanicCount());


    }
    @Override
    public void assignGaussianDistThresholds(int id, double waitT, double panicT, double sd) {
        double[] thresholds = new double[2];

        thresholds[0] = Utils.getRandomGaussionWithinThreeSD(standardDev,waitT); // index 0
        thresholds[1] = Utils.getRandomGaussionWithinThreeSD(standardDev,panicT); //index 1
        logger.trace("threshold values : {} {}", thresholds[0], thresholds[1]);

        this.thresholdMap.put(id,thresholds);
    }

    @Override
    public void initRandomSeed() {
        logger.error("CLT - random seed method not implemented..");
    }

    @Override
    public void initNearFireSeed(){
        logger.error("CLT  - nearfire seed method is not implemented");
    }

    @Override
    public void initProbabilisiticSeed(){
        logger.info("CLT - probablistic seeding ");

        selectContentBasedSeed(DataTypes.WAIT,seedW);
     //   selectContentBasedSeed(DataTypes.PANIC,seedP);

    }

    // independant method to select a seed based on size and content type
    //MAX wait seed from the whole population = ~18000
    // genereate seedmap and update agent content levels
    public void selectContentBasedSeed(String type, int seedSize) {
        String otherType=null;
        if(type.equals(DataTypes.WAIT)) { // set content types
            otherType = DataTypes.PANIC;
        }
        else if(type.equals(DataTypes.PANIC)){
            otherType = DataTypes.WAIT;
        }
        int selected = 0;

        try{
            List<Integer> repIdLsit = getShuffledIDList(); //get shuffled agent id list each time during a seeding strategy
            logger.trace("replicated id list  {}", repIdLsit.toString());
            for(int id: repIdLsit) {
                if(selected == seedSize) {
                    logger.trace(" selected the expected amount of seed");
                    break; //stop selection, log the seed size
                }
                if(alreadyInSeed(id,otherType)){
                    continue; //skip
                }

                SocialAgent agent = getAgentMap().get(id);
                double actProb  = getContentBasedProbability(id,agent.getX(),type); //get activation probability based on distance and contenttype
                logger.trace("generated activation probability: {}",actProb);
                if(rand.nextDouble() <= actProb) {
                    //selected
                    logger.trace("agent {} selected",id);
                    double min = getAgentContentTreshold(id,type);
                    double max = 1.0;
                    double randomPanicVal =  min + ((max - min) * rand.nextDouble()); // random panic value

                    //update seedmap
                    getSeedMap(type).put(id,randomPanicVal);
                    updateAgentStateValueAndCounters(id,type,randomPanicVal);
                    selected++;
                }
            }

            logger.info(" {} seed secltion ended - selected {} : expected size {}", type, selected,seedSize);
        }
        catch(NullPointerException e){
            logger.error(" Null pointer execption caught: {}", e.getMessage());
        }

    }
    // test function available in TestCLTModel
    public double getContentBasedProbability(int id, double easting, String contentType) {
        int firefront = 280000; // panic type distance benchmark
        int farEnd = 310000; // wait type distance benchmark
        Double dist= null;

        try{
            //calcualtes the distance based on the firefront/farend, and then get the proability for the calculated distance
            if(contentType.equals(DataTypes.PANIC)) { // nearfire
                dist = (easting - firefront)/1000;  // in km

            }
            else if(contentType.equals(DataTypes.WAIT)){ // faraway from fire
                dist = (farEnd - easting)/1000; //in km
            }
            if (dist < 0) {dist = 0.0;}

        }
        catch (NullPointerException e) {
            logger.debug("Null Pointer Exception caught: {}", e.getMessage());
        }
        double prob = getProbabilityForDistanceFromFormula(dist);
     //   logger.trace("geenrated probability: {}",prob);
        return prob;

    }

    public List<Integer> getShuffledIDList() { // tested with small agent sample -5
        List<Integer> replicateIdList = new ArrayList<Integer>();

        for (int id: getAgentMap().keySet()) { // add all ids
            replicateIdList.add(id);
        }

        //shuffle the id list
        Collections.shuffle(replicateIdList);

        return replicateIdList;
    }

    // limit panic value 1.0 if > 1.0
    // if index 0 is not added, then there might be a arrayIndexOutOfBound issue in nei weights arraylists
    public void runCompetitiveDiffusion() {

        HashMap<Integer,Double> neiWaitLevels = new HashMap<Integer,Double>();
        HashMap<Integer,Double> neiPanicLevels = new HashMap<Integer,Double>();

        logger.debug("competitive diffusion process startted..");

        for (SocialAgent agent : getAgentMap().values()) {
            double totNeiW = 0.0;
            double totNeiP = 0.0;

            HashMap<Integer, Double> neiMap = agent.getLinkMap();


            for (int neiId : neiMap.keySet()) {
                SocialAgent nei = getAgentMap().get(neiId);

                if (nei.getActivatedContentType().equals(DataTypes.PANIC)) { // panic activated neighbour
                    totNeiP = totNeiP + agent.getLinkWeight(neiId);
                } else if (nei.getActivatedContentType().equals(DataTypes.WAIT)) { // wait activated neighbour
                    totNeiW = totNeiW + agent.getLinkWeight(neiId);
                }
            }

            logger.trace(" agent {}  totweight -WAIT {}", agent.getID(), totNeiW );

            neiWaitLevels.put(agent.getID(), totNeiW);
            neiPanicLevels.put(agent.getID(), totNeiP);


        }

            for(int id: getAgentMap().keySet()){ //calculate the total content values from seed and neighbour weights.
                double  totWaitLevel = 0.0;
                double  totPanicLevel = 0.0;

                if(getSeedMap(DataTypes.WAIT).containsKey(id)) { // included in WAIT seed
                    totWaitLevel = neiWaitLevels.get(id) + getSeedMap(DataTypes.WAIT).get(id); // nei + seed
                    totPanicLevel = neiPanicLevels.get(id); // only nei
//                    logger.trace("agent {} totweight-WAIT: {}", id, totWaitLevel);
                }
                else if(getSeedMap(DataTypes.PANIC).containsKey(id)) { // included in PANIC seed
                    totPanicLevel = neiPanicLevels.get(id) + getSeedMap(DataTypes.PANIC).get(id); // nei+ seed
                    totWaitLevel =  neiWaitLevels.get(id); // only nei
//                    logger.trace("agent {} totweight-PANIC: {}", id, totPanicLevel);
                }
                else{ // only nei
                    totWaitLevel =  neiWaitLevels.get(id);
                    totPanicLevel = neiPanicLevels.get(id);
                }

                if(totWaitLevel > 1.0) {totWaitLevel = 1.0;}
                if(totPanicLevel > 1.0) {totPanicLevel = 1.0;}

                logger.trace(" agent {} totWaitLevel: {} totPanicLevel: {}", id,totWaitLevel,totPanicLevel);

                //1. equal content levels, randomyly select a content type and update
                // activation conditions are checked in updateAgent function, therefore if values are less than act thresholds
                // only the values get updated, sequentially.
                if(totPanicLevel == totWaitLevel) {
                    List<String> types = new ArrayList<String>(getAgentMap().get(id).getContentValuesMap().keySet());
                 //   String randomType = types.get(rand.nextInt(types.size())) ; //0 or 1
                    String randomType = types.get(1) ; // SELECT PANIC - PRIORITY FOR  PERCEPTS

                    if(randomType.equals(DataTypes.PANIC)) { //panic activation
                        updateAgentStateValueAndCounters(id,randomType,totPanicLevel);
                        updateAgentStateValueAndCounters(id,DataTypes.WAIT,totWaitLevel);
                    }
                    else if(randomType.equals(DataTypes.WAIT)){ // wait activation
                        updateAgentStateValueAndCounters(id,randomType,totWaitLevel);
                        updateAgentStateValueAndCounters(id,DataTypes.PANIC,totPanicLevel);
                    }

                }

                // 2. panic acitvation should happen if activation condition matches
                else if(totPanicLevel > totWaitLevel) {
                    updateAgentStateValueAndCounters(id,DataTypes.PANIC,totPanicLevel);
                    updateAgentStateValueAndCounters(id,DataTypes.WAIT,totWaitLevel);
                }

                // 3. wait activation should happen if activation condition matches
                else if(totWaitLevel > totPanicLevel) {
                    updateAgentStateValueAndCounters(id,DataTypes.WAIT,totWaitLevel);
                    updateAgentStateValueAndCounters(id,DataTypes.PANIC,totPanicLevel);

                }
            }


    }

    // make sure to pass a value between 0 and 1 - doesn't  restrict other findings.
    public void updateAgentStateValueAndCounters(int id, String type, Double newVal) {

        SocialAgent agent = getAgentMap().get(id);


        String otherType=null;
        if(type.equals(DataTypes.WAIT)) { // set other content type
            otherType = DataTypes.PANIC;
        }
        else if(type.equals(DataTypes.PANIC)){
            otherType = DataTypes.WAIT;
        }

        //update content level if greater than previous step - regarldess of activation state
        // If a wait agent becomes panic, then the wait influence can be less than the previuos wait value.
//        if(newVal > agent.getContentlevel(type))  { // can the same
//            logger.trace(" agent {} value to update: {}",id, newVal);
            agent.setContentlevel(type,newVal);
//        }
       /* else*/ if(newVal < agent.getContentlevel(type)) {
            logger.error("agent {} , content level {} is less than previous {}",id,newVal,agent.getContentlevel(type) );
        }

        if(agent.getState().equals(DataTypes.MEDIUM)) { //1. activation process

            if(newVal > agent.getContentlevel(otherType) && newVal >= getAgentContentTreshold(id,type)) {

                if(type.equals(DataTypes.WAIT)){ // wait activation
//                    logger.debug("wait activation: agent: {} newVal: {}", id, newVal);
                    agent.setState(DataTypes.LOW);
//                    optCount--;
//                    waitCount++;
                }
                else if(type.equals(DataTypes.PANIC)){ //panic  activation
                    agent.setState(DataTypes.HIGH);
//                    optCount--;
//                    panicCount++;
                }
            }
        }

        if(agent.getState().equals(DataTypes.LOW) && type.equals(DataTypes.PANIC) && newVal >= getAgentContentTreshold(id,type)) { //2.  re-activation (panicked)

            // panic value doesn't matter, even if it is less than the current wait value update state
            // if just modellnig competitive diffusion, then a panic value below threshold can come, then the state should not be changed to H
            // in the fire progression experiment, you should only receive panic values higher than panic threshold.

                agent.setState(DataTypes.HIGH);

        }

    }

    public void manualSeedSet(String type, int id, double val){

        getSeedMap(type).put(id,val);
        updateAgentStateValueAndCounters(id,type,val);

    }



    public boolean alreadyInSeed(int id, String checkType) {

        HashMap<Integer,Double> seedMap = null;
        if(checkType.equals(DataTypes.WAIT)) {
            seedMap =  this.waitSeedMap;
        }
        else if(checkType.equals(DataTypes.PANIC)) {
            seedMap = this.panicSeedMap;
        }

        return seedMap.containsKey(id);
    }


    public double getAgentContentTreshold(int id, String contentType) {

    double[] agentThresholds = this.thresholdMap.get(id);
    Double t = null;
    try {
        if (contentType.equals(DataTypes.WAIT)) {
            t = agentThresholds[0];
        } else if (contentType.equals(DataTypes.PANIC)) {
            t = agentThresholds[1];
        }


    }
    catch(NullPointerException e) {
        logger.error("null pointer expection: {}", e.getMessage());
    }

            return t;
    }


    public HashMap<Integer,Double> getSeedMap(String type) {
        if(type.equals(DataTypes.PANIC)) {
            return panicSeedMap;
        }
        else if(type.equals(DataTypes.WAIT)){
            return waitSeedMap;
        }
        else{ // NULL values returned on type mismatch
            return null;
        }
    }

    public void printAgentContentValues() {

        for (SocialAgent agent : getAgentMap().values())
        {
            logger.debug("id: {} adopted content:{} waitVal: {} panicVal {}",
                    agent.getID(), agent.getActivatedContentType(),agent.getContentlevel(DataTypes.WAIT),agent.getContentlevel(DataTypes.PANIC));
        }
    }

    public void printSeedMaps() {

        logger.debug(" wait seed: ");
        if(this.waitSeedMap.isEmpty()) {
            logger.debug("wait seed map empty");
        }
        for (Map.Entry<Integer,Double> entry: this.waitSeedMap.entrySet()) {

            int id =  entry.getKey();
            double val =  entry.getValue();
            logger.debug("id  {}: value: {}", id, val);

        }

        logger.debug(" panic seed: ");
        if(this.panicSeedMap.isEmpty()) {
            logger.debug("wait seed map empty");
        }
        for (Map.Entry<Integer,Double> entry: this.panicSeedMap.entrySet()) {

            int id =  entry.getKey();
            double val = entry.getValue();
            logger.debug("id  {}: value: {}", id, val);

        }
    }


}
