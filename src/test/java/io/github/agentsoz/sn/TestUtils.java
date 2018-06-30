package sn;

import io.github.agentsoz.socialnetwork.SocialAgent;
import io.github.agentsoz.socialnetwork.util.DataTypes;
import org.junit.Test;

import io.github.agentsoz.socialnetwork.SocialNetworkManager;
import io.github.agentsoz.socialnetwork.util.Utils;
import io.github.agentsoz.socialnetwork.util.Global;

public class TestUtils {



	
	
//	@Ignore
	@Test
	public void testRandomGaussianValues() { 
		int testCount = 25;
		for(int i=0;i<testCount; i++) {
			System.out.println(Utils.getRandomGaussion(0.05, 0.75));
		}
	}

	public static void randomAgentMap(int nodes, int cordRange, SocialNetworkManager sn_manager) {

		for(int id=0; id < nodes; id++) {
			int x = Global.getRandom().nextInt(cordRange);
			int y = Global.getRandom().nextInt(cordRange);
			sn_manager.createSocialAgent(Integer.toString(id));sn_manager.setCords(Integer.toString(id),x,y);
		}
	}


}
