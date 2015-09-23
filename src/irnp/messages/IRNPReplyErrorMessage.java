package irnp.messages;

import irnp.IRNPProtocolViolationException;
import irnp.objects.*;

public class IRNPReplyErrorMessage extends IRNPMessage {
	
	protected SenderTemplate senderTemplate;
	protected ReceiverTemplate receiverTemplate;
	protected FlowBandwidthConfirm flowBandwidthConfirm;
	protected ASPath asPath;
	protected ErrorSpec errorSpec;
	
	public IRNPReplyErrorMessage() {
		vers = 0x01;
		flags = 0x00;
		msgType = IRNPMessageTypes.MESSAGE_REPLY_ERROR;
		length = IRNPMessageTypes.IRNP_MESSAGE_HEADER_LENGTH;
	}

	@Override
	public void encode() throws IRNPProtocolViolationException {
		if(senderTemplate == null || receiverTemplate == null || flowBandwidthConfirm == null || asPath == null || errorSpec == null) {
			System.out.println("IRNP Reply Error Message misses parts, cannot be encoded");
			return;
		}
		length = length + senderTemplate.getLength() + receiverTemplate.getLength() + flowBandwidthConfirm.getLength() + asPath.getLength() + errorSpec.getLength();
		
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
		
		errorSpec.encode();
		System.arraycopy(errorSpec.getBytes(), 0, bytes, currentIndex, errorSpec.getLength());
		currentIndex = currentIndex + errorSpec.getLength();
		
		System.out.println("IRNP Reply Error Message encoding accomplished");
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
				flowBandwidthConfirm = new FlowBandwidthConfirm();
				flowBandwidthConfirm.decode(bytes, offset);
				offset = offset + flowBandwidthConfirm.getLength();
			} else if(classNum == 3) {
				asPath = new ASPath();
				asPath.decode(bytes, offset);
				offset = offset + asPath.getLength();
			} else if(classNum == 6) {
				int subType = IRNPObject.getSubType(bytes, offset);
				if(subType == 1) {
					errorSpec = new ErrorSpecIPv4();
					errorSpec.decode(bytes, offset);
					offset = offset + errorSpec.getLength();
				}
			}
			else {
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
	
	public ErrorSpec getErrorSpec() {
		return errorSpec;
	}
			
	public void setErrorSpec(ErrorSpec errorSpec) {
		this.errorSpec = errorSpec;
	}
	
}
