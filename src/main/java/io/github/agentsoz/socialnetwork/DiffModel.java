package io.github.agentsoz.socialnetwork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DiffModel {

    final Logger logger = LoggerFactory.getLogger("");
	
	public void initialise() {}


    public void preDiffProcess() {}

    public void doDiffProcess(long time) {}

    public void postDiffProcess() {}

    public void printConfigParams() {}

    public void printthresholdMap() {}

    public void printPanicValues() {}

    public boolean isDiffTurn(long time) {return false;}

    public void postDiffProcess(long time) {}


	
}
