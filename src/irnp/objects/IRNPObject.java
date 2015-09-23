package irnp.objects;

import irnp.*;

/* 
 * Object Formats

         Every object consists of one or more 32-bit words with a one-
         word header, with the following format:

                0             1              2             3
         +-------------+-------------+-------------+-------------+
         |       Length (bytes)      |  Class-Num  |   Sub-Type    |
         +-------------+-------------+-------------+-------------+
         |                                                       |
         //                  (Object contents)                   //
         |                                                       |
         +-------------+-------------+-------------+-------------+
 * 
 * 
 */


public abstract class IRNPObject implements IRNPElement {

	protected int length;	// The object length
	protected int classNum;//Identifies the object class
	protected int subType;//Object sub-type, unique within Class-Num
	protected byte[] bytes; // Byte Object representation
	
	public IRNPObject(byte[] bytes, int offset){
		this.length = ((bytes[offset]<<8)& 0xFF00) |  (bytes[offset+1] & 0xFF);
		this.bytes = new byte[this.length];
		System.arraycopy(bytes, offset, this.bytes, 0, this.length);
		classNum = (int) bytes[offset+2];
		subType = (int) bytes[offset+3];
	}
	
	public IRNPObject(){
		
	}
	
	public void encodeHeader(){
		
		bytes[0] = (byte)((length>>8) & 0xFF);
		bytes[1] = (byte)(length & 0xFF);
		bytes[2] = (byte) classNum;
		bytes[3] = (byte) subType;
		
	}
	
public void decodeHeader(byte[] bytes, int offset){
		
		length = ((int)((bytes[offset] << 8) & 0xFF00)) | ((int)(bytes[offset+1] & 0x00FF));
		classNum = (int) bytes[offset+2];
		subType = (int) bytes[offset+3];
		
	}
	
	public abstract void encode() throws IRNPProtocolViolationException;
	
	public abstract void decode(byte[] bytes, int offset) throws IRNPProtocolViolationException;
	
	// Getters & Setters
	
		public int getLength() {
			return length;
		}

		public void setLength(int length) {
			this.length = length;
		}

		public int getClassNum() {
			return classNum;
		}

		public void setClassNum(int classNum) {
			this.classNum = classNum;
		}

		public int getSubType() {
			return subType;
		}

		public void setSubType(int subType) {
			this.subType = subType;
		}

		public byte[] getBytes() {
			return bytes;
		}

		public void setBytes(byte[] bytes) {
			this.bytes = bytes;
		}	

		public static int getClassNum(byte[] bytes, int offset) {
			return (int) bytes[offset+2];
		}
		
		public static int getSubType(byte[] bytes, int offset) {
			return (int) bytes[offset+3];		
		}
		
		public static int getLength(byte[] bytes, int offset) {
			
			return ((int)((bytes[offset] << 8) & 0xFF00)) | ((int)(bytes[offset+1] & 0x00FF));
			
		}

	
}
