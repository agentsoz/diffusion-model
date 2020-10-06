package io.github.agentsoz.socialnetwork;

import io.github.agentsoz.dataInterface.DataClient;
import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.dataInterface.DataSource;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.DiffusedContent;
import io.github.agentsoz.socialnetwork.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

//wrapper class of SNManager class, provides API functionalities through DataServer
public class SNModel implements DataSource, DataClient {

    private DataServer dataServer;
    private SocialNetworkManager snManager;
    private TreeMap<Double, DiffusedContent> allStepsInfoSpreadMap;
    private double lastUpdateTimeInMinutes = -1;
    private Time.TimestepUnit timestepUnit = Time.TimestepUnit.SECONDS;

    final Logger logger = LoggerFactory.getLogger("");

    public SNModel(String configFile) {
        this.snManager = new SocialNetworkManager(configFile);
        this.allStepsInfoSpreadMap = new TreeMap<Double, DiffusedContent>();
    }

    public SNModel(String configFile, DataServer ds) {
        this.snManager = new SocialNetworkManager(configFile);
        this.dataServer = ds;
        this.allStepsInfoSpreadMap = new TreeMap<Double, DiffusedContent>();
    }

//    public void initSocialAgentMap(List<String> idList) {
//
//        // initSNManagerBasedOnConfigs();
//
//    }

    public void initSNModel() { // init SN model with already populated social agent map


        this.getSNManager().setupSNConfigsAndLogs(); //setup configs and create log first

        this.snManager.genNetworkAndDiffModels(); // gen network and diffusion models
        this.snManager.printSNModelconfigs();

        //subscribe to BDI data updates
        this.dataServer.subscribe(this, DataTypes.BDI_STATE_UPDATES);

    }

    public void initSNModel(List<String> idList) { // init SN model with given agent id list


        this.getSNManager().setupSNConfigsAndLogs(); //first, setup configs and create log

        for (String id : idList) {
            this.snManager.createSocialAgent(id); //populate agentmap
        }

        this.snManager.genNetworkAndDiffModels(); // gen network and diffusion models
        this.snManager.printSNModelconfigs();

        //subscribe to BDI data updates
        this.dataServer.subscribe(this, DataTypes.BDI_STATE_UPDATES);

    }

    public SocialNetworkManager getSNManager() {
        return this.snManager;
    }

    public void setSNManager(SocialNetworkManager sn) {
        this.snManager = sn;
    }

    public void stepDiffusionProcess() {

      //  if (snManager.processDiffusion((long) dataServer.getTime())) {
          //  this.snManager.diffuseContent(); #FIXME
            if (SNConfig.getDiffusionModelsList().contains(DataTypes.icModel)) {
                ICModel icModel = (ICModel) getSNManager().getDiffModels()[0]; //#FIXME
                HashMap<String, ArrayList<String>> latestUpdate = icModel.getLatestDiffusionUpdates();

                icModel.recordCurrentStepSpread(this.dataServer.getTime());
                DiffusedContent dc = new DiffusedContent();
                dc.setContentSpreadMap(latestUpdate);
                this.allStepsInfoSpreadMap.put(dataServer.getTime(), dc);

                logger.debug("put timed diffusion updates for ICModel at {}", dataServer.getTime());

            }

    }

    @Override
    public Object getNewData(double timestep, Object parameters) {
        double currentTime = Time.convertTime(timestep, timestepUnit, Time.TimestepUnit.MINUTES);
        SortedMap<Double, DiffusedContent> periodicInfoSpread = allStepsInfoSpreadMap.subMap(lastUpdateTimeInMinutes, currentTime);
        lastUpdateTimeInMinutes = currentTime;
        Double nextTime = allStepsInfoSpreadMap.higherKey(currentTime);
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
                ICModel icModel = (ICModel) this.snManager.getDiffModels()[0]; // #FIXME
                icModel.updateSocialStatesFromLocalContent(data);
                return true;
            }

        }
        return false;
    }

    public DataServer getDataServer() {
        return this.dataServer;
    }

    public TreeMap<Double,DiffusedContent> getAllStepsSpreadMap() {
        return  this.allStepsInfoSpreadMap;
    }

    /**
     * Set the time step unit for this model
     *
     * @param unit the time step unit to use
     */
    void setTimestepUnit(Time.TimestepUnit unit) {
        timestepUnit = unit;
    }

    public void publishDiffusionDataUpdate() {
        this.dataServer.publish(DataTypes.DIFFUSION, "sn-data");
    }

    public void finish() {

        //output diffusion outcomes and wrap up
        for(DiffModel model: this.snManager.getDiffModels()){
            model.finish();
            if(model instanceof ICModel){
                ((ICModel) model).getDataCollector().writeSpreadDataToFile(); //uses the file path specified in the config
            }
        }

    }
}
