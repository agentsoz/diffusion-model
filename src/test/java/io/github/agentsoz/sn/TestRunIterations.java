package io.github.agentsoz.sn;

import org.junit.Test;

import java.io.File;

public class TestRunIterations {

    @Test
    public void testCombinedInfo() {

        String dirPath = "../sn_model_data/seedAB_spreadABC/";
        File theDir = new File(dirPath);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }

        int iterations = 5;
        for (int itr = 1; itr <= iterations; itr++) {
            TestCombiningInfluenceInteraction test = new TestCombiningInfluenceInteraction();
            String filePath = dirPath.concat("iteration" + String.valueOf(itr) + ".out");
            test.seedAB_SpreadABC(filePath,1000,false,true);
        }

    }
}
