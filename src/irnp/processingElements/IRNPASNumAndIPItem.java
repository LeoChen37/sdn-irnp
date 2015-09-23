package irnp.processingElements;

import java.net.Inet4Address;

public class IRNPASNumAndIPItem {
	
	private int asNum;
	private Inet4Address asIPAddress;
	
	public IRNPASNumAndIPItem(int asNum, Inet4Address asIPAddress) {
		this.asNum = asNum;
		this.asIPAddress = asIPAddress;
	}

	public int getAsNum() {
		return asNum;
	}

	public void setAsNum(int asNum) {
		this.asNum = asNum;
	}

	public Inet4Address getAsIPAddress() {
		return asIPAddress;
	}

	public void setAsIPAddress(Inet4Address asIPAddress) {
		this.asIPAddress = asIPAddress;
	}
	
}
