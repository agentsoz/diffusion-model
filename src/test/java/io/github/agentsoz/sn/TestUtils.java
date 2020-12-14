package io.github.agentsoz.sn;

import org.junit.Ignore;
import org.junit.Test;

import io.github.agentsoz.socialnetwork.util.Utils;

public class TestUtils {




	@Ignore
	@Test
	public void testRandomGaussianValues() { 
		int testCount = 25;
		for(int i=0;i<testCount; i++) {
			System.out.println(Utils.getRandomGaussion(0.0, 0.0));
		}
	}

	@Ignore
	@Test
	public void testRandomGaussianWithin3SDValues() {
		int testCount = 25;
		for(int i=0;i<testCount; i++) {
			System.out.println(Utils.getRandomGaussionWithinThreeSD(0.0, 0.6));
		}
	}


}
