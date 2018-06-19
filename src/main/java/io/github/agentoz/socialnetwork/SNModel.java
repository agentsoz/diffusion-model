package io.github.agentoz.socialnetwork;

import io.github.agentsoz.dataInterface.DataClient;
import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.dataInterface.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.agentoz.socialnetwork.util.DataTypes;

import java.util.HashMap;
import java.util.List;

public class SNModel implements DataSource,DataClient {

    private DataServer dataServer;
    private SocialNetworkManager  snManager;
    private String config;

    final Logger logger = LoggerFactory.getLogger("");

    public SNModel(String configFile) {
        this.config = configFile;
    }

    public void initAgentMap(List<String> idList) {

        snManager = new SocialNetworkManager(this.config);

        for (String id : idList) {  //populate agentmap
            snManager.createSocialAgent(id);
        }
    }

    public void generateSNModel(){
        
        if(snManager.initSNModel()) { // init network and diffusion models

            snManager.printSNModelconfigs();
        }
        else {
            logger.error("SNModel initialisation failed, aborting");
            System.exit(-1);
        }

        //subscribe to BDI data updates
        this.dataServer.subscribe(this,DataTypes.BDI_STATE_UPDATES);
    }

    public void stepDiffusionProcess() {

        snManager.processDiffusion((long)dataServer.getTime());
        getNewData(dataServer.getTime(),snManager.getCurrentStepDiffusionData());

        logger.debug("published diffusion latest data {}", dataServer.getTime());

    }

    @Override
    public Object getNewData(double time, Object data) { // pass the agent states/levels:
        if(dataServer != null) {
            HashMap<Double,Object> dataSet = new HashMap<Double, Object>();
            dataServer.publish(DataTypes.DIFFUSION,dataSet);
            return dataSet;
        }
        else{
            return false;
        }

    }

    @Override
    public boolean dataUpdate(double time, String dataType, Object data) { // data package from the BDI side

        switch (dataType) {

            case DataTypes.BDI_STATE_UPDATES: { // update social states based on BDI reasoning

                logger.debug("received BDI state updates");
                return true;
            }

        }
        return false;
    }

    public void finish() {
        // terminate the snModel
    }
}
