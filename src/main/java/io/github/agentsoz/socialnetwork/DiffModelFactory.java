package io.github.agentsoz.socialnetwork;

import io.github.agentsoz.socialnetwork.util.DataTypes;

public class DiffModelFactory {

	
	   public DiffModel getDiffusionModel(String diffType, SocialNetworkManager snMan){
		      if(diffType == null){
		         return null;
		      }		
		      else if(diffType.equals(DataTypes.ltModel)){
		          return new LTModel(SNConfig.getSeed_lt(), SNConfig.getDiffTurn_lt(),snMan);
		         
		      }
		      else if(diffType.equals(DataTypes.CLTModel)){
		      	return new CLTModel(SNConfig.getWaitSeed(),SNConfig.getPanicSeed(),SNConfig.getDiffTurn_lt(),snMan);
			  }
			  else if(diffType.equals(DataTypes.icModel)) {
		      	return new ICModel(snMan,SNConfig.getDiffTurn_ic(),SNConfig.getDiffusionProbability_ic());
			  }
		      
		      return null;
		   }
	   
}
