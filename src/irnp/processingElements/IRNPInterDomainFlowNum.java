package irnp.processingElements;

public class IRNPInterDomainFlowNum {
	
	public int asNum;
	public int flowNum;
	public int targetAsNum;
	
	public IRNPInterDomainFlowNum(int asNum, int flowNum, int targetAsNum) {
		this.asNum = asNum;
		this.flowNum = flowNum;
		this.targetAsNum = targetAsNum;
	}

	public int getAsNum() {
		return asNum;
	}

	public void setAsNum(int asNum) {
		this.asNum = asNum;
	}

	public int getFlowNum() {
		return flowNum;
	}

	public void setFlowNum(int flowNum) {
		this.flowNum = flowNum;
	}

	public int getTargetAsNum() {
		return targetAsNum;
	}

	public void setTargetAsNum(int targetAsNum) {
		this.targetAsNum = targetAsNum;
	}

}
