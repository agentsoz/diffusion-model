package io.github.agentoz.socialnetwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DiffModel {


	
	public void initialise() {}


    public void preDiffProcess() {}

    public void doDiffProcess(long time) {}

    public void postDiffProcess() {}

    public void printConfigParams() {}

    public void printthresholdMap() {}

    public void printPanicValues() {}

    public int getLowPanicCount() {return -1;}

    public boolean isDiffTurn(long time) {return false;}

    public void postDiffProcess(long time) {}

    public int getMedPanicCount() {return -1;}

    public int getHighPanicCount() {return -1;}

    final Logger logger = LoggerFactory.getLogger("");
	
}
