package io.github.agentsoz.socialnetwork.util;

import java.util.HashMap;

public class DiffusionContent {



    // data structures accessed by the Diffusion model
    HashMap<String,Object[]> contentsMapFromDiffusionModel;  // content type, parameters

    // data structures accessed by the BDI model
    private HashMap<String,Object[]> contentsMapFromBDIModel; // content type, parameters
    private HashMap<String,Object[]> broadcastContentsMapFromBDIModel; // contents that should be broadcasted to all agents in the social netwotrk.
    private HashMap<String,Object[]> snActionsMapFromBDIModel; // action type, parameters


    public DiffusionContent()
    {

        this.contentsMapFromDiffusionModel = new HashMap<>();
        this.contentsMapFromBDIModel = new HashMap<>();
        this.broadcastContentsMapFromBDIModel = new HashMap<>();
        this.snActionsMapFromBDIModel = new HashMap<>();
    }


    public HashMap<String, Object[]> getContentsMapFromDiffusionModel() {
        return contentsMapFromDiffusionModel;
    }

    public HashMap<String, Object[]> getSnActionsMapFromBDIModel() {
        return snActionsMapFromBDIModel;
    }

    public HashMap<String, Object[]> getContentsMapFromBDIModel() {
        return contentsMapFromBDIModel;
    }

    public HashMap<String, Object[]> getBroadcastContentsMapFromBDIModel() {
        return broadcastContentsMapFromBDIModel;
    }

    public boolean isEmpty(){
        return contentsMapFromDiffusionModel.isEmpty();
    }

    public String toString() {
        return contentsMapFromDiffusionModel.toString();
    }
}
