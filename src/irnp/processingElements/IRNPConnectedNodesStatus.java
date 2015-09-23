package irnp.processingElements;

public class IRNPConnectedNodesStatus {
	
	private int asNum;
	
	private long upstreamBandwidth;
	
	private long downstreamBandwidth;
	
	public IRNPConnectedNodesStatus() {
		asNum = 0;
		upstreamBandwidth = 0;
		downstreamBandwidth = 0;
	}
	
	public IRNPConnectedNodesStatus(int asNum, long upstreamBandwidth, long downstreamBandwidth) {
		this.asNum = asNum;
		this.upstreamBandwidth = upstreamBandwidth;
		this.downstreamBandwidth = downstreamBandwidth;
	}

	public int getAsNum() {
		return asNum;
	}

	public void setAsNum(int asNum) {
		this.asNum = asNum;
	}

	public long getUpstreamBandwidth() {
		return upstreamBandwidth;
	}

	public void setUpstreamBandwidth(long upstreamBandwidth) {
		this.upstreamBandwidth = upstreamBandwidth;
	}

	public long getDownstreamBandwidth() {
		return downstreamBandwidth;
	}

	public void setDownstreamBandwidth(long downstreamBandwidth) {
		this.downstreamBandwidth = downstreamBandwidth;
	}
	
	
}
