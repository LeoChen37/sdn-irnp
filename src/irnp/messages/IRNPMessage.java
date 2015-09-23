package irnp.messages;

import irnp.*;


public abstract class IRNPMessage implements IRNPElement {

	protected int vers;
	protected int flags;
	protected int msgType;
	protected int length;
	protected int asNum;
	protected int flowNum;
		
	protected byte bytes[];//The bytes of the message 
	
	/*
	 *   IRNP Common Header

                0             1              2             3
         +-------------+-------------+-------------+-------------+
         | Vers | Flags|  Msg Type   |       IRNP Length       |
         +-------------+-------------+-------------+-------------+
         |  		AS num		     |        Flow num         |
         +-------------+-------------+-------------+-------------+
         
	 */
	
	public IRNPMessage(){
		vers = 0x01;
		flags = 0x00;
		msgType = IRNPMessageTypes.MESSAGE_REQUEST;
		length = 0x00;
		asNum = 0x00;
		flowNum = 0x00;
	}
	
	public IRNPMessage(byte[] bytes){
		this.bytes=bytes;
	}
	
	public void encodeHeader() {
		bytes[0]= (byte)(((vers<<4) &0xF0) | (flags & 0x0F));
		bytes[1]= (byte) msgType;
		bytes[2]= (byte)((length>>8) & 0xFF);
		bytes[3]= (byte)(length & 0xFF);
		bytes[4]= (byte)((asNum>>8) & 0xFF);
		bytes[5]= (byte)(asNum & 0xFF);;
		bytes[6]= (byte)((flowNum>>8) & 0xFF);
		bytes[7]= (byte)(flowNum & 0xFF);
	}
	
	public abstract void encode() throws IRNPProtocolViolationException;
	
	public void decodeHeader() throws IRNPProtocolViolationException {
		
		vers = (bytes[0] >> 4) & 0x0F; 
		flags = bytes[0] & 0x0F;
		msgType = bytes[1];
		length = (((int)((bytes[2]&0xFF)<<8)& 0xFF00) |  ((int)bytes[3] & 0xFF));
		asNum = (((short)((bytes[4]&0xFF)<<8)& 0xFF00) |  ((short)bytes[5] & 0xFF));
		flowNum = (((short)((bytes[6]&0xFF)<<8)& 0xFF00) |  ((short)bytes[7] & 0xFF));
		
	}
	
	public abstract void decode() throws IRNPProtocolViolationException;
	
	// GETTERS & SETTERS
	
	public int getVers() {
		return vers;
	}

	public void setVers(int vers) {
		this.vers = vers;
	}

	public int getFlags() {
		return flags;
	}


	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
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
	
	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	
}
