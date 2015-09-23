package irnp.objects;

import java.util.ArrayList;

import irnp.IRNPProtocolViolationException;

public class ASPath extends IRNPObject {
	
	private int asPathLength;
	
	private int asPathType;
	
	private ArrayList<Integer> asList;
	
	public ASPath(ArrayList<Integer> asList) {
		classNum = 3;
		subType = 1;
		this.asPathLength = asList.size();
		this.asPathType = 1;
		this.asList = asList;
		
		if(asPathLength%2 == 0) {
			length = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE + 2 * (asPathLength + 2);
		} else {
			length = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE + 2 * (asPathLength + 1);
		}
		
		bytes = new byte[length];
	}
	
	public ASPath() {
		classNum = 3;
		subType = 1;
		this.asPathLength = 0;
		this.asPathType = 1;
		this.asList = null;
		
		if(asPathLength%2 == 0) {
			length = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE + 2 * (asPathLength + 2);
		} else {
			length = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE + 2 * (asPathLength + 1);
		}
		
		bytes = new byte[length];
	}
	
	/**	
    0             1              2             3
    +-------------+-------------+-------------+-------------+
    |ASPath Length|    Type	    |			(Value)			|
    +-------------+-------------+-------------+-------------+
    |       (Value)             |       (Value)             |
    +-------------+-------------+-------------+-------------+	
    
    */
	
	@Override
	public void encode() throws IRNPProtocolViolationException {
		encodeHeader();
		
		int currentIndex = IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE;
		
		bytes[currentIndex++] = (byte)asPathLength;
		bytes[currentIndex++] = (byte)asPathType;
		
		for(int i=0;i<asPathLength;i++) {
			int value = asList.get(i);
			bytes[currentIndex++] = (byte)((value>>8) & 0xFF);
			bytes[currentIndex++] = (byte)((value) & 0xFF);
		}
		if(asPathLength%2 == 0) {
			bytes[currentIndex++] = (byte)0;
			bytes[currentIndex++] = (byte)0;
		}
	}

	@Override
	public void decode(byte[] bytes, int offset) throws IRNPProtocolViolationException {
		
		offset = offset + IRNPObjectParameters.IRNP_OBJECT_COMMON_HEADER_SIZE;
		ArrayList<Integer> list = new ArrayList<Integer>();
		asPathLength = (int)bytes[offset++];
		asPathType = (int)bytes[offset++];
		for(int i=0;i<asPathLength;i++) {
			int value = ((int)((bytes[offset] << 8) & 0xFF00)) | ((int)(bytes[offset+1] & 0x00FF));
			list.add(value);
			offset+=2;
		}
	}
	
	// Getters & Setters
	public int getAsPathLength() {
		return asPathLength;
	}
	
	public int getAsPathType() {
		return asPathType;
	}
	
	public void setAsPathType(int asPathType) {
		this.asPathType = asPathType;
	}
	
	public ArrayList<Integer> getAsList() {
		return asList;
	}
	
	public void setAsList(ArrayList<Integer> list) {
		this.asList = list;
		this.asPathLength = list.size();
	}

}
