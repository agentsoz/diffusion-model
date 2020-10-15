package io.github.agentsoz.socialnetwork.util;


import java.util.HashMap;

public class DiffusionDataContainer {

    HashMap<String,DiffusionContent> diffusionDataMap; // agent id, Diffusion content

    public DiffusionDataContainer(){
        this.diffusionDataMap = new HashMap<>();
    }

    public HashMap<String, DiffusionContent> getDiffusionDataMap() {
        return diffusionDataMap;
    }


    public void putContentToContentsMapFromDiffusionModel(String agentId, String contentType, Object[] params ){

        DiffusionContent dc = getOrCreateDiffusionContent(agentId);
        dc.getContentsMapFromDiffusionModel().put(contentType,params);
    }

//    public void putContentToContentsMapFromBDIModel(String agentId, String contentType, Object[] params ){
//
//        DiffusionContent dc = getOrCreateDiffusionContent(agentId);
//        dc.getContentsMapFromBDIModel().put(contentType,params);
//    }
//
//    public void putContentToBroadcastContentsMapFromBDIModel(String agentId, String contentType, Object[] params ){
//
//        DiffusionContent dc = getOrCreateDiffusionContent(agentId);
//        dc.getBroadcastContentsMapFromBDIModel().put(contentType,params);
//    }
//
//    public void putActionToActionMapFromBDIModel(String agentId, String contentType, Object[] params ){
//
//        DiffusionContent dc = getOrCreateDiffusionContent(agentId);
//        dc.getSnActionsMapFromBDIModel().put(contentType,params);
//    }


    private DiffusionContent getOrCreateDiffusionContent(String agentId) {
        DiffusionContent content = this.getDiffusionDataMap().get(agentId);
        if (content == null) {
            content = new DiffusionContent();
            this.getDiffusionDataMap().put(agentId, content);
        }

        return content;
    }

//    public void clearDiffusionModelData(){
//
//        for (DiffusionContent dc : diffusionDataMap.values()){
//            dc.getContentsMapFromDiffusionModel().clear();
//        }
//    }
//
//    public void clearBDIModelData(){
//
//        for (DiffusionContent dc : diffusionDataMap.values()){
//            dc.getSnActionsMapFromBDIModel().clear();
//            dc.getContentsMapFromBDIModel().clear();
//            dc.getBroadcastContentsMapFromBDIModel().clear();
//        }
//    }


}
