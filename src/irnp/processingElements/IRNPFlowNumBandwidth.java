package irnp.processingElements;

import java.util.ArrayList;

public class IRNPFlowNumBandwidth extends IRNPInterDomainFlowNum {
	public int bandwidth;
	public ArrayList<Integer> path;
	
	public IRNPFlowNumBandwidth(int asNum, int flowNum, int targetAsNum, int bandwidth, ArrayList<Integer> path) {
		super(asNum, flowNum, targetAsNum);
		this.bandwidth = bandwidth;
		this.path = path;
	}

	public int getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(int bandwidth) {
		this.bandwidth = bandwidth;
	}

	public ArrayList<Integer> getPath() {
		return path;
	}

	public void setPath(ArrayList<Integer> path) {
		this.path = path;
	}
}
