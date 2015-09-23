package irnp.messages;

import irnp.IRNPProtocolViolationException;
import irnp.objects.*;

public class IRNPRequestMessage extends IRNPMessage {
	
	protected SenderTemplate senderTemplate;
	protected ReceiverTemplate receiverTemplate;
	protected FlowBandwidthSpec flowBandwidthSpec;
	protected ASPath asPath;
	
	public IRNPRequestMessage() {
		vers = 0x01;
		flags = 0x00;
		msgType = IRNPMessageTypes.MESSAGE_REQUEST;
		length = IRNPMessageTypes.IRNP_MESSAGE_HEADER_LENGTH;
	}

	@Override
	public void encode() throws IRNPProtocolViolationException {
		if(senderTemplate == null || receiverTemplate == null || flowBandwidthSpec == null || asPath == null) {
			System.out.println("IRNP Request Message misses parts, cannot be encoded");
			return;
		}
		length = length + senderTemplate.getLength() + receiverTemplate.getLength() + flowBandwidthSpec.getLength() + asPath.getLength();
		
		bytes = new byte[length];
		encodeHeader();
		int currentIndex = IRNPMessageTypes.IRNP_MESSAGE_HEADER_LENGTH;
		
		senderTemplate.encode();
		System.arraycopy(senderTemplate.getBytes(), 0, bytes, currentIndex, senderTemplate.getLength());
		currentIndex = currentIndex + senderTemplate.getLength();
		
		receiverTemplate.encode();
		System.arraycopy(receiverTemplate.getBytes(), 0, bytes, currentIndex, receiverTemplate.getLength());
		currentIndex = currentIndex + receiverTemplate.getLength();
		
		flowBandwidthSpec.encode();
		System.arraycopy(flowBandwidthSpec.getBytes(), 0, bytes, currentIndex, flowBandwidthSpec.getLength());
		currentIndex = currentIndex + flowBandwidthSpec.getLength();
		
		asPath.encode();
		System.arraycopy(asPath.getBytes(), 0, bytes, currentIndex, asPath.getLength());
		currentIndex = currentIndex + asPath.getLength();
		
		System.out.println("IRNP Request Message encoding accomplished");
	}
	
	@Override
	public void decode() throws IRNPProtocolViolationException {
		decodeHeader();
		
		int offset = IRNPMessageTypes.IRNP_MESSAGE_HEADER_LENGTH;
		while(offset < length) {
			int classNum = IRNPObject.getClassNum(bytes,offset);
			if(classNum == 1) {
				int subType = IRNPObject.getSubType(bytes, offset);
				if(subType == 1) {
					senderTemplate = new SenderTemplateIPv4();
					senderTemplate.decode(bytes, offset);
					offset = offset + senderTemplate.getLength();
				}
			} else if(classNum == 2) {
				int subType = IRNPObject.getSubType(bytes, offset);
				if(subType == 1) {
					receiverTemplate = new ReceiverTemplateIPv4();
					receiverTemplate.decode(bytes, offset);
					offset = offset + receiverTemplate.getLength();
				}
			} else if(classNum == 4) {
				flowBandwidthSpec = new FlowBandwidthSpec();
				flowBandwidthSpec.decode(bytes, offset);
				offset = offset + flowBandwidthSpec.getLength();
			} else if(classNum == 3) {
				asPath = new ASPath();
				asPath.decode(bytes, offset);
				offset = offset + asPath.getLength();
			} else {
				throw new IRNPProtocolViolationException();
			}
		}
	}
	
	//Getter & Setter
	public SenderTemplate getSenderTemplate() {
		return senderTemplate;
	}
			
	public void setSenderTemplate(SenderTemplate senderTemplate) {
		this.senderTemplate = senderTemplate;
	}
			
	public ReceiverTemplate getReceiverTemplate() {
		return receiverTemplate;
	}
			
	public void setReceiverTemplate(ReceiverTemplate receiverTemplate) {
		this.receiverTemplate = receiverTemplate;
	}
			
	public FlowBandwidthSpec getFlowBandwidthSpec() {
		return flowBandwidthSpec;
	}
			
	public void setFlowBandwidthSpec(FlowBandwidthSpec fs) {
		this.flowBandwidthSpec = fs;
	}
			
	public ASPath getAsPath() {
		return asPath;
	}
			
	public void setAsPath(ASPath asPath) {
		this.asPath = asPath;
	}

}
