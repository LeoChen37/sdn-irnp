package irnp;


public interface IRNPElement {

	/**
	 * Generic method to encode an IRNP element
	 * @throws IRNPProtocolViolationException
	 */
	
	public void encode() throws IRNPProtocolViolationException;
	
	/**
	 * Generic method to get the byte array from an encoded IRNP element
	 * @return The byte array with the encoded IRNP element
	 */
	
	public byte[] getBytes();
	
	/**
	 * Generic method to get length an encoded IRNP element byte array
	 * @return The length of with the encoded IRNP element
	 */
	
	public int getLength();
	
}
