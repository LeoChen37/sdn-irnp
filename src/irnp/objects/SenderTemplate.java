package irnp.objects;

public abstract class SenderTemplate extends IRNPObject{

	public abstract void encode();

	public abstract void decode(byte[] bytes, int offset);

}
