package io.github.agentsoz.sn;

import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.socialnetwork.SNConfig;
import io.github.agentsoz.socialnetwork.SNModel;
import io.github.agentsoz.socialnetwork.util.Log;
import io.github.agentsoz.socialnetwork.util.SNUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

public class TestSNModel {

    String logFile = "./testSNModel.log";
    final Logger logger = Log.createLogger("", logFile);

    @Ignore
    @Test
    public void testInitAgentMap() {

    List<String> ids = Arrays.asList("1", "2", "3");
    SNModel snModel = new SNModel(SNConfig.getDefaultConfigFile());
    snModel.initAgentMap(ids);
    System.out.println(snModel.getSNManager().getAgentMap().keySet().toString());

}
    @Ignore
    @Test
    public void testgenSNModel() {
        List<String> ids = Arrays.asList("1", "2", "3");
        SNModel snModel = new SNModel(SNConfig.getDefaultConfigFile());
        snModel.initAgentMap(ids);

        DataServer ds = new DataServer("Bushfire");
        snModel.registerDataServer(ds);
        snModel.genSNModel();

        SNUtils.setEndSimTime(7200L);
        while(ds.getTime() <= SNUtils.getEndSimTime()) {
            snModel.stepDiffusionProcess();
            ds.stepTime();
        }




    }


}
