package irnp.objects;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class ErrorSpecIPv4 extends ErrorSpec {

	private Inet4Address errorNodeAddress;
	
	private int flags;
	
	private int errorCode;
	
	private int errorValue;
	
	public ErrorSpecIPv4(Inet4Address address, int errorCode, int errorValue) {
		classNum = 6;
		subType = 1;
		this.errorNodeAddress = address;
		this.flags = 0;
		this.errorCode = errorCode;
		this.errorValue = errorValue;
		
		length = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE + 8;
		bytes = new byte[length];
	}
	
	public ErrorSpecIPv4() {
		classNum = 6;
		subType = 1;
		this.errorNodeAddress = null;
		this.flags = 0;
		this.errorCode = 0;
		this.errorValue = 0;
		
		length = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE + 8;
		bytes = new byte[length];
	}
	
	/**	
    0             1              2             3
    +-------------+-------------+-------------+-------------+
    |                 		IP Address 						|
    +-------------+-------------+-------------+-------------+
    |    flags    | Error Code  |      Error value          |
    +-------------+-------------+-------------+-------------+	
    
    */
	
	@Override
	public void encode() {
		encodeHeader();
		
		byte[] addr = errorNodeAddress.getAddress();
		
		int currentIndex = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE;

		System.arraycopy(addr,0, getBytes(), currentIndex, addr.length);
		currentIndex = currentIndex + addr.length;
		
		bytes[currentIndex] = (byte) flags;
		bytes[currentIndex+1] = (byte) errorCode;
		bytes[currentIndex+2] = (byte)((errorValue>>8) & 0xFF);
		bytes[currentIndex+3] = (byte)(errorValue & 0xFF);

	}

	@Override
	public void decode(byte[] bytes, int offset) {
		byte[] receivedAddress = new byte[4];
		
		offset = offset + IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE;
		
		System.arraycopy(bytes,offset,receivedAddress,0,4);
		try{
			errorNodeAddress = (Inet4Address) Inet4Address.getByAddress(receivedAddress);
		}catch(UnknownHostException e){
			System.out.println("Unknown Host sent on Error Spec IPv4 Object");
		}
		offset = offset + receivedAddress.length;
		flags = (int)bytes[offset];
		errorCode = (int)bytes[offset+1];
		errorValue = (int)(bytes[offset+2] | bytes[offset+3]);
		System.out.println("Error Spec IPv4 Object Decoded");

	}

	// Getters & Setters
	public Inet4Address getErrorNodeAddress() {
		return errorNodeAddress;
	}
	
	public void setErrorNodeAddress(Inet4Address address) {
		this.errorNodeAddress = address;
	}
	
	public int getFlags() {
		return flags;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	
	public int getErrorValue() {
		return errorValue;
	}
	
	public void setErrorValue(int errorValue) {
		this.errorValue = errorValue;
	}
}
