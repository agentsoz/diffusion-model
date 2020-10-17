package io.github.agentsoz.sn;

import io.github.agentsoz.bdiabm.data.PerceptContent;
import io.github.agentsoz.bdiabm.v2.AgentDataContainer;
import io.github.agentsoz.dataInterface.DataClient;
import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import io.github.agentsoz.socialnetwork.util.DiffusionContent;
import io.github.agentsoz.socialnetwork.util.DiffusionDataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TestDataClientModel implements DataClient {

    private  Logger log;
    private DataServer dataServer;
    Map<Double, DiffusionDataContainer> diffusionDataMapFromDiffusionModel; //stores data received from the DiffusionModel
    private DiffusionDataContainer diffusionDataContainerFromBDIModel = new DiffusionDataContainer(); // stores diffusion updates from BDI agents, to be sent to the DiffusionModel

    //counter variables for received agents
    private int informationContentCount = 0;
    private int influenceContentCount = 0;
    private int informationAndInfluenceContentCount = 0;

    public TestDataClientModel(DataServer ds, Logger log){
        this.dataServer = ds;
        this.log = log;
        this.dataServer.subscribe(this, DataTypes.DIFFUSION_DATA_CONTAINER_FROM_DIFFUSION_MODEL);
    }

    @Override
    public void receiveData(double time, String dataType, Object data) {
        switch (dataType) {
            case DataTypes.DIFFUSION_DATA_CONTAINER_FROM_DIFFUSION_MODEL:
                log.info("data received from SNDModel at time {}",time);
                diffusionDataMapFromDiffusionModel= (Map<Double, DiffusionDataContainer>) data;
                handleReceivedUpdatesFromDiffusionModel();
               // dataListeners.get(dataType).receiveData(time, dataType, data);
                break;
            default:
                throw new RuntimeException("Unknown data type received: " + dataType);
        }
    }

    private void handleReceivedUpdatesFromDiffusionModel() {
        resetCounterVariables(); // reset counters

        if(diffusionDataMapFromDiffusionModel == null || diffusionDataMapFromDiffusionModel.isEmpty()){
            return;
        }
        else if(diffusionDataMapFromDiffusionModel.size() == 1) { // diffusion data for one step
            DiffusionDataContainer dataContainer = diffusionDataMapFromDiffusionModel.values().iterator().next();

            HashMap<String, DiffusionContent> diffusionContentsMap = dataContainer.getDiffusionDataMap();
            for (String id : diffusionContentsMap.keySet()) {
                DiffusionContent diffusionContent  = diffusionContentsMap.get(id);
                HashMap<String,Object[]> contents = diffusionContent.getContentsMapFromDiffusionModel();
                if(contents.keySet().size() == 1 && contents.containsKey(DataTypes.INFORMATION)){
                    informationContentCount++;
                }
                else if(contents.keySet().size() == 1 && contents.containsKey(DataTypes.INFLUENCE)){
                    influenceContentCount++;
                }
                else if(contents.keySet().size() == 2){
                    informationAndInfluenceContentCount++;
                }
                Object content = contents.values().toArray()[0];
                log.info(" agent {} received content {}, {}",id,contents.keySet(),contents.values().toArray());
//                PerceptContent diffusionPercept = new PerceptContent(Constants.DIFFUSION_CONTENT,diffusionContent);
//                adc.putPercept(id,Constants.DIFFUSION_CONTENT,diffusionPercept);

            }
            log.info("Total "+ diffusionContentsMap.size()+" agents received  contents of types from DiffusionModel:");
            log.info("Agents receiving contents from DiffusionModel are: {}",
                    Arrays.toString(diffusionContentsMap.keySet().toArray()));

            diffusionDataMapFromDiffusionModel.clear(); // now clear diffusion data
        }
        else{
            log.warn("Received diffusion data for multiple steps. This is not encouraged as BDI agents need to decide what to do with contents for each diffusion step. Otherwise the diffusion model cannot execute the next step! {}", diffusionDataMapFromDiffusionModel.size());

        }


    }

    public void resetCounterVariables(){
        influenceContentCount =0;
        informationContentCount =0;
        informationAndInfluenceContentCount=0;
    }

    public int[] getContentCountsOfDataReceivedFromTheDiffusionModel(){
        int[] counter = {informationContentCount,influenceContentCount,informationAndInfluenceContentCount};
        return counter;
    }

    public  void sendUpdatesToDiffusionModel(){

//        if(diffusionDataContainerFromBDIModel.getDiffusionDataMap().isEmpty()){
//            return;
//        }
        diffusionDataContainerFromBDIModel.getDiffusionDataMap().clear();

        DiffusionContent content1 = new DiffusionContent(); // information
        String[] params = {"content_mult_x_ic"};
        content1.getContentsMapFromBDIModel().put(DataTypes.INFORMATION,params);

        DiffusionContent content2 = new DiffusionContent(); // influence
        Object[] params2 = {"content_mult_x_lt",0.8}; // if its a new content, need to pass threhold values as well.
        content2.getContentsMapFromBDIModel().put(DataTypes.INFLUENCE,params2);


        diffusionDataContainerFromBDIModel.getDiffusionDataMap().put("1",content1);
        diffusionDataContainerFromBDIModel.getDiffusionDataMap().put("0",content2);


        dataServer.publish(DataTypes.DIFFUSION_DATA_CONTAINDER_FROM_BDI, diffusionDataContainerFromBDIModel);
    }



}
