package irnp.objects;

import irnp.IRNPProtocolViolationException;

public class FlowBandwidthSpec extends IRNPObject {
	
	private int bandwidth;
	
	public FlowBandwidthSpec(int bandwidth) {
		classNum = 4;
		subType = 1;
		this.bandwidth = bandwidth;
		
		length = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE + 4;
		bytes = new byte[length];
	}
	
	public FlowBandwidthSpec() {
		classNum = 4;
		subType = 1;
		this.bandwidth = 0;
		
		length = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE + 4;
		bytes = new byte[length];
	}

	@Override
	public void encode() throws IRNPProtocolViolationException {
		encodeHeader();
		
		int currentIndex = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE;
		bytes[currentIndex+3] = (byte) (this.bandwidth & 0xFF);  
		bytes[currentIndex+2] = (byte) (this.bandwidth >> 8 & 0xFF);  
		bytes[currentIndex+1] = (byte) (this.bandwidth >> 16 & 0xFF);  
		bytes[currentIndex] = (byte) (this.bandwidth >> 24 & 0xFF);

	}

	@Override
	public void decode(byte[] bytes, int offset) throws IRNPProtocolViolationException {
		offset = offset + IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE;
		
		int b0 = bytes[offset] & 0xFF;  
        int b1 = bytes[offset + 1] & 0xFF;  
        int b2 = bytes[offset + 2] & 0xFF;  
        int b3 = bytes[offset + 3] & 0xFF;  
        this.bandwidth = ((b0 << 24) | (b1 << 16) | (b2 << 8) | b3);

	}
	
	// Getters & Setters
	public int getBandwidth() {
		return bandwidth;
	}
	
	public void setBandwidth(int bandwidth) {
		this.bandwidth = bandwidth;
	}

}
