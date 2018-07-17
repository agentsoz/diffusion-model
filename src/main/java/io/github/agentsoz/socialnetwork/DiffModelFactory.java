package io.github.agentsoz.socialnetwork;

import io.github.agentsoz.socialnetwork.util.DataTypes;

import javax.xml.crypto.Data;

public class DiffModelFactory {

	
	   public DiffModel getDiffusionModel(String diffType, SocialNetworkManager snMan){
		      if(diffType == null){
		         return null;
		      }		
		      else if(diffType.equals(DataTypes.ltModel)){
		          return new LTModel(SNConfig.getSeed(), SNConfig.getDiffturn(),snMan);
		         
		      }
		      else if(diffType.equals(DataTypes.CLTModel)){
		      	return new CLTModel(SNConfig.getWaitSeed(),SNConfig.getPanicSeed(),SNConfig.getDiffturn(),snMan);
			  }
			  else if(diffType.equals(DataTypes.icModel)) {
		      	return new ICModel(snMan,SNConfig.getDiffturn(),SNConfig.getDiffProbability());
			  }
		      
		      return null;
		   }
	   
}
