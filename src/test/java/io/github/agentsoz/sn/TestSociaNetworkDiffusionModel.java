package io.github.agentsoz.sn;

import io.github.agentsoz.dataInterface.DataServer;
import io.github.agentsoz.socialnetwork.SocialNetworkDiffusionModel;
//import io.github.agentsoz.socialnetwork.util.SNUtils;
import io.github.agentsoz.socialnetwork.util.Global;
import io.github.agentsoz.socialnetwork.util.SNUtils;
import io.github.agentsoz.util.Time;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestSociaNetworkDiffusionModel {


String testConfigFile="./case_studies/hawkesbury/test_SN_BDI_Interaction.xml";
//String testConfigFile="./case_studies/hawkesbury/test_ICModel.xml";

//private static final Logger log = LoggerFactory.getLogger(TestSociaNetworkDiffusionModel.class);
private  DataServer dataServer = null;
private int startTime = 1;


@Test
public void testSNBDIDataPassingSendData() {

    Global.setRandomSeed(4711);
    int ds_timestep = 1800;
    // dataserver initialisations
    if(dataServer == null){
         dataServer  =  DataServer.getInstance("sn_bdi1");
    }
    dataServer.setTime(startTime);
    dataServer.setTimeStep(ds_timestep);


    List<String> ids = new ArrayList<>(10);
    for(int i =0; i<100; i++){
        ids.add(String.valueOf(i));

    }

    SocialNetworkDiffusionModel sn = new SocialNetworkDiffusionModel(testConfigFile,dataServer,ids);
//    SNUtils.randomAgentMap(sn,10,1000);
    int[] startTime = {00,00};
    sn.start(startTime);
    sn.init();
    sn.setTimestepUnit(Time.TimestepUnit.SECONDS);


    //Test Model
    TestDataClientModel testModel  = new TestDataClientModel(dataServer, sn.getSocialNetworkDiffusionLogger());


    while(dataServer.getTime() <= 10800) {
        sn.getSocialNetworkDiffusionLogger().info("-----time is now {}-------",dataServer.getTime()+ds_timestep);
        dataServer.stepTime();

    }

    sn.finish();

    int[] actualCounters = testModel.getContentCountsOfDataReceivedFromTheDiffusionModel();
    int[] expectedCounters = {0,79,21}; // total should be 100
    Assert.assertArrayEquals(expectedCounters,actualCounters);

    /*
t=3600 and 7200 and 10800 IC model steps. t = 5400, 10800 LT Model steps
time	content_mult_x_ic
1.0		5
3600.0		23
7200.0		50
10800.0		71
Content= content_mult_x_ic, type: local, active= 71, exposed= 30

time	content_mult_x_lt
1.0		20
5400.0		50
10800.0		75
content= content_mult_x_lt: active 75, inactive 25
 */
}

    @Test
    public void testSNBDIDataPassingReceiveData() {

        Global.setRandomSeed(4711);
        int ds_timestep = 1800;
        // dataserver initialisations
        if(dataServer == null){
            dataServer  =  DataServer.getInstance("sn_bdi2");
        }
        dataServer.setTime(startTime);
        dataServer.setTimeStep(ds_timestep);


        List<String> ids = new ArrayList<>(10);
        for(int i =0; i<10; i++){
            ids.add(String.valueOf(i));

        }

        SocialNetworkDiffusionModel sn = new SocialNetworkDiffusionModel(testConfigFile,dataServer,ids);
//
        int[] startTime = {00,00};
        sn.start(startTime);
        sn.init();
        sn.setTimestepUnit(Time.TimestepUnit.SECONDS);


        //Test Model
        TestDataClientModel testModel  = new TestDataClientModel(dataServer, sn.getSocialNetworkDiffusionLogger());


        while(dataServer.getTime() <= 7200) {
            sn.getSocialNetworkDiffusionLogger().info("-----time is now {}-------",dataServer.getTime()+ds_timestep);
            if(dataServer.getTime() == 1801){
                testModel.sendUpdatesToDiffusionModel(); //  1 for information, 1 for influence
            }
            dataServer.stepTime();

        }

        sn.finish();

        //if dataServer.getTime() <= 5400, same values
        Assert.assertEquals(8, sn.getLTModel().getDataCollector().getFinalActiveAgents("content_mult_x_lt"));
        Assert.assertEquals(5,sn.getICModel().getDataCollector().getAdoptedAgentCountForContent(sn,"content_mult_x_ic"));


/*

time	content_mult_x_lt
1.0		2
5400.0		8

time	content_mult_x_ic
1.0		0
3600.0		5

 */


    }

    @Ignore
    @Test
    public void testInitSocialAgentMap() {

        DataServer ds1 = DataServer.getInstance("TestServer1");
        List<String> ids = Arrays.asList("1", "2", "3");


        SocialNetworkDiffusionModel snModel = new SocialNetworkDiffusionModel(testConfigFile, ds1,ids);
        snModel.init();
        System.out.println(snModel.getAgentMap().keySet().toString());
        Assert.assertEquals(ids.size(), snModel.getAgentMap().size());

    }

    @Ignore
    @Test
    public void testgenSNModel() {

        DataServer ds2 = DataServer.getInstance("TestServer2");
        List<String> ids = Arrays.asList("1", "2", "3");

        SocialNetworkDiffusionModel snModel = new SocialNetworkDiffusionModel(testConfigFile, ds2,ids);
        snModel.init();

        SNUtils.setEndSimTime(7200L);
        while (snModel.getDataServer().getTime() <= SNUtils.getEndSimTime()) {
//            snModel.stepDiffusionProcess();
            snModel.getDataServer().stepTime();
        }


    }






}


