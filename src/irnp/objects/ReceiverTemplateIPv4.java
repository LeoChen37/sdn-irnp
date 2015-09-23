package irnp.objects;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/*
 * 

 	  RECEIVER_TEMPLATE Class

      RECEIVER_TEMPLATE class = 2.

      o    IPv4 RECEIVER_TEMPLATE object: Class = 2, Sub-Type = 1

           Definition same as IPv4/UDP FILTER_SPEC object.

      o    IPv6 RECEIVER_TEMPLATE object: Class = 2, Sub-Type = 2

           Definition same as IPv6/UDP FILTER_SPEC object.


 * 
 */

public class ReceiverTemplateIPv4 extends ReceiverTemplate {
	
	private Inet4Address dstAddress;
	
	
	private int dstPort;
	
	public ReceiverTemplateIPv4(Inet4Address dstAddress, int dstPort){
		
		classNum = 2;
		subType = 1;
		this.dstAddress = dstAddress;
		this.dstPort = dstPort;
		
		length = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE + 8;
		bytes = new byte[length];
		
	}
	
	public ReceiverTemplateIPv4(){
		
		classNum = 2;
		subType = 1;
		this.dstAddress = null;
		this.dstPort = 0;
		
		length = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE + 8;
		bytes = new byte[length];
		
	}
	
	/**	
    0             1              2             3
    +-------------+-------------+-------------+-------------+
    |                 		IP Address 
    +-------------+-------------+-------------+-------------+
    |             0             |       dstPort             |
    +-------------+-------------+-------------+-------------+	
    
    */
	
	@Override
	public void encode() {
		encodeHeader();
		
		byte[] addr = dstAddress.getAddress();
		
		int currentIndex = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE;

		System.arraycopy(addr,0, getBytes(), currentIndex, addr.length);
		currentIndex = currentIndex + addr.length;
		
		bytes[currentIndex] = (byte) 0;
		bytes[currentIndex+1] = (byte) 0;
		bytes[currentIndex+2] = (byte)((dstPort>>8) & 0xFF);
		bytes[currentIndex+3] = (byte)(dstPort & 0xFF);
	}

	@Override
	public void decode(byte[] bytes, int offset) {
		byte[] receivedAddress = new byte[4];
		
		offset = offset + IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE;
		
		System.arraycopy(bytes,offset,receivedAddress,0,4);
		try{
			dstAddress = (Inet4Address) Inet4Address.getByAddress(receivedAddress);
		}catch(UnknownHostException e){
			System.out.println("Unknown Host received on RECEIVER Template IPv4 Object");
		}
		offset = offset + receivedAddress.length;
		dstPort = ((bytes[offset+2]<<8)& 0xFF00) | (bytes[offset+3] & 0xFF);
		System.out.println("RECEIVER Template LSP Tunnel IPv4 Object Decoded");
		
	}

	// Getters & Setters

		public Inet4Address getDstAddress() {
			return dstAddress;
		}

		public void setDstAddress(Inet4Address dstAddress) {
			this.dstAddress = dstAddress;
		}

		public int getdstPort() {
			return dstPort;
		}

		public void setdstPort(int dstPort) {
			this.dstPort = dstPort;
		}
	
}

