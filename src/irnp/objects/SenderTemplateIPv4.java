package irnp.objects;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/*
 * 

 	  SENDER_TEMPLATE Class

      SENDER_TEMPLATE class = 1.

      o    IPv4 SENDER_TEMPLATE object: Class = 1, Sub-Type = 1

           Definition same as IPv4/UDP FILTER_SPEC object.

      o    IPv6 SENDER_TEMPLATE object: Class = 1, Sub-Type = 2

           Definition same as IPv6/UDP FILTER_SPEC object.


 * 
 */

public class SenderTemplateIPv4 extends SenderTemplate {
	
	private Inet4Address srcAddress;
	
	
	private int srcPort;
	

	
	public SenderTemplateIPv4(Inet4Address srcAddress, int srcPort){
		
		classNum = 1;
		subType = 1;
		this.srcAddress = srcAddress;
		this.srcPort = srcPort;
		
		length = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE + 8;
		bytes = new byte[length];
		
	}
	
	public SenderTemplateIPv4(){
		
		classNum = 1;
		subType = 1;
		this.srcAddress = null;
		this.srcPort = 0;
		
		length = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE + 8;
		bytes = new byte[length];
		
	}
	
	/**	
    0             1              2             3
    +-------------+-------------+-------------+-------------+
    |                 		IP Address 						|
    +-------------+-------------+-------------+-------------+
    |             0             |       SrcPort             |
    +-------------+-------------+-------------+-------------+	
    
    */
	
	@Override
	public void encode() {
		encodeHeader();
		
		byte[] addr = srcAddress.getAddress();
		
		int currentIndex = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE;

		System.arraycopy(addr,0, getBytes(), currentIndex, addr.length);
		currentIndex = currentIndex + addr.length;
		
		bytes[currentIndex] = (byte) 0;
		bytes[currentIndex+1] = (byte) 0;
		bytes[currentIndex+2] = (byte)((srcPort>>8) & 0xFF);
		bytes[currentIndex+3] = (byte)(srcPort & 0xFF);
	}

	@Override
	public void decode(byte[] bytes, int offset) {
		byte[] receivedAddress = new byte[4];
		
		offset = offset + IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE;
		
		System.arraycopy(bytes,offset,receivedAddress,0,4);
		try{
			srcAddress = (Inet4Address) Inet4Address.getByAddress(receivedAddress);
		}catch(UnknownHostException e){
			System.out.println("Unknown Host received on Sender Template IPv4 Object");
		}
		offset = offset + receivedAddress.length;
		srcPort = ((bytes[offset+2]<<8)& 0xFF00) | (bytes[offset+3] & 0xFF);
		System.out.println("Sender Template LSP Tunnel IPv4 Object Decoded");
		
	}

	// Getters & Setters

		public Inet4Address getSrcAddress() {
			return srcAddress;
		}

		public void setSrcAddress(Inet4Address srcAddress) {
			this.srcAddress = srcAddress;
		}

		public int getSrcPort() {
			return srcPort;
		}

		public void setSrcPort(int srcPort) {
			this.srcPort = srcPort;
		}
	
}
