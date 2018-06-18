package agentoz.socialnetwork;

import java.util.HashMap;
import agentoz.socialnetwork.util.DataTypes;

public class NetworkFactory {

		   
		   public Network getNetwork(String netType, HashMap<Integer, SocialAgent> agents){
		      if(netType == null){
		         return null;
		      }		
		      if(netType.equals(DataTypes.RANDOM)){
		         return  new RandomNetwork(agents.size(),SNConfig.getRandomNetAvgLinks());
//		         randNet.setNormaliseParam(SNConfig.normaliseNetwork());
//		         return randNet;
		         
		      } else if(netType.equals(DataTypes.SMALL_WORLD)){
		         return new SWNetwork(SNConfig.getSWNetNeiDistance(),SNConfig.getSWNetAvgLinks(),SNConfig.getSWNetRewireProb());
		         
		      } 
		      
		      else if(netType.equals(DataTypes.RANDOM_REGULAR)){
			         return new RandomRegularNetwork(agents.size(),SNConfig.getRandRegNetAvgLinks());
			         
			      }
		      
		      return null;
		   }
	
}
