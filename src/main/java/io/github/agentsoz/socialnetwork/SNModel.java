package io.github.agentsoz.socialnetwork;

import io.github.agentsoz.dataInterface.DataClient;
import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.dataInterface.DataSource;
import io.github.agentsoz.socialnetwork.util.DiffusedInformation;
import io.github.agentsoz.socialnetwork.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.agentsoz.socialnetwork.util.DataTypes;

import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

//wrapper class of SNManager class, provides API functionalities through DataServer
public class SNModel implements DataSource,DataClient {

    private DataServer dataServer;
    private SocialNetworkManager  snManager;
    private TreeMap<Double,DiffusedInformation> overallInfoSpreadMap;
    private double lastUpdateTimeInMinutes = -1 ;
    private Time.TimestepUnit timestepUnit = Time.TimestepUnit.SECONDS;

    final Logger logger = LoggerFactory.getLogger("");

    public SNModel(String configFile, DataServer ds) {
        this.snManager = new SocialNetworkManager(configFile);
        this.dataServer =ds;
        this.overallInfoSpreadMap =  new TreeMap<Double,DiffusedInformation>();
    }

    public void initSocialAgentMap(List<String> idList) {

       // initSNManagerBasedOnConfigs();
        for (String id : idList) {
            this.snManager.createSocialAgent(id); //populate agentmap
        }
    }

    public void initSNModel(){ // set SNManager based on main configs unless already set

        //setTimestepUnit();
        this.snManager.genNetworkAndDiffModels(); // setup configs, gen network and diffusion models
        this.snManager.printSNModelconfigs();

        //subscribe to BDI data updates
        this.dataServer.subscribe(this,DataTypes.BDI_STATE_UPDATES);

    }

    public SocialNetworkManager getSNManager() {
        return this.snManager;
    }

    public void setSNManager(SocialNetworkManager sn) {
        this.snManager = sn;
    }

    public void stepDiffusionProcess() {

        if(snManager.processDiffusion((long)dataServer.getTime())) {
            getNewData(dataServer.getTime(),snManager.getCurrentStepDiffusionData());
            logger.debug("published latest diffusion  data {}", dataServer.getTime());

        }

    }
    @Override
    public Object getNewData(double timestep, Object parameters) {
        double currentTime = Time.convertTime(timestep, timestepUnit, Time.TimestepUnit.MINUTES);
        SortedMap<Double, DiffusedInformation> periodicInfoSpread = overallInfoSpreadMap.subMap(lastUpdateTimeInMinutes, currentTime);
        lastUpdateTimeInMinutes = currentTime;
        Double nextTime = overallInfoSpreadMap.higherKey(currentTime);
        if (nextTime != null) {
            dataServer.registerTimedUpdate(DataTypes.DIFFUSION, this, Time.convertTime(nextTime, Time.TimestepUnit.MINUTES, timestepUnit));
        }
        return periodicInfoSpread;
    }


    @Override
    public boolean dataUpdate(double time, String dataType, Object data) { // data package from the BDI side

        switch (dataType) {

            case DataTypes.BDI_STATE_UPDATES: { // update social states based on BDI reasoning

                logger.debug("SNModel: received BDI state updates");
                ICModel icModel = (ICModel) this.snManager.getDiffModel();
                icModel.updateSocialStatesFromBDIPercepts(data);
                return true;
            }

        }
        return false;
    }

    public DataServer getDataServer() {
        return this.dataServer;
    }

    /**
     * Set the time step unit for this model
     * @param unit the time step unit to use
     */
    void setTimestepUnit(Time.TimestepUnit unit) {
        timestepUnit = unit;
    }

    //public void publishDiffusionDataUpdate() {
//        this.dataServer.publish(DataTypes.DIFFUSION, "sn-data");
//    }

    public void finish() {
        // cleaning
    }
}
