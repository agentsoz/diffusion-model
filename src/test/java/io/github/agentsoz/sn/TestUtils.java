package io.github.agentsoz.sn;

import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import org.junit.Ignore;
import org.junit.Test;

import io.github.agentsoz.socialnetwork.SocialNetworkManager;
import io.github.agentsoz.socialnetwork.util.Utils;
import io.github.agentsoz.socialnetwork.util.Global;

public class TestUtils {


	@Ignore
	@Test
	public void testRandomGaussianValues() { 
		int testCount = 25;
		for(int i=0;i<testCount; i++) {
			System.out.println(Utils.getRandomGaussion(0.05, 0.75));
		}
	}


}
