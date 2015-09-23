package irnp.messages;

import irnp.IRNPProtocolViolationException;
import irnp.objects.*;

public class IRNPReplyMessage extends IRNPMessage {
	
	protected SenderTemplate senderTemplate;
	protected ReceiverTemplate receiverTemplate;
	protected FlowBandwidthConfirm flowBandwidthConfirm;
	protected ASPath asPath;
	
	public IRNPReplyMessage() {
		vers = 0x01;
		flags = 0x00;
		msgType = IRNPMessageTypes.MESSAGE_REPLY;
		length = IRNPMessageTypes.IRNP_MESSAGE_HEADER_LENGTH;
	}

	@Override
	public void encode() throws IRNPProtocolViolationException {
		if(senderTemplate == null || receiverTemplate == null || flowBandwidthConfirm == null || asPath == null) {
			System.out.println("IRNP Reply Message misses parts, cannot be encoded");
			return;
		}
		length = length + senderTemplate.getLength() + receiverTemplate.getLength() + flowBandwidthConfirm.getLength() + asPath.getLength();
		
		bytes = new byte[length];
		encodeHeader();
		int currentIndex = IRNPMessageTypes.IRNP_MESSAGE_HEADER_LENGTH;
		
		senderTemplate.encode();
		System.arraycopy(senderTemplate.getBytes(), 0, bytes, currentIndex, senderTemplate.getLength());
		currentIndex = currentIndex + senderTemplate.getLength();
		
		receiverTemplate.encode();
		System.arraycopy(receiverTemplate.getBytes(), 0, bytes, currentIndex, receiverTemplate.getLength());
		currentIndex = currentIndex + receiverTemplate.getLength();
		
		flowBandwidthConfirm.encode();
		System.arraycopy(flowBandwidthConfirm.getBytes(), 0, bytes, currentIndex, flowBandwidthConfirm.getLength());
		currentIndex = currentIndex + flowBandwidthConfirm.getLength();
		
		asPath.encode();
		System.arraycopy(asPath.getBytes(), 0, bytes, currentIndex, asPath.getLength());
		currentIndex = currentIndex + asPath.getLength();
		
		System.out.println("IRNP Reply Message encoding accomplished");
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
			} else if(classNum == 5) {
				flowBandwidthConfirm = new FlowBandwidthConfirm();
				flowBandwidthConfirm.decode(bytes, offset);
				offset = offset + flowBandwidthConfirm.getLength();
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
	
	public FlowBandwidthConfirm getFlowBandwidthConfirm() {
		return flowBandwidthConfirm;
	}
	
	public void setFlowBandwidthConfirm(FlowBandwidthConfirm fc) {
		this.flowBandwidthConfirm = fc;
	}
	
	public ASPath getAsPath() {
		return asPath;
	}
	
	public void setAsPath(ASPath asPath) {
		this.asPath = asPath;
	}
}
