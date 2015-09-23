package irnp.processingElements;

import irnp.*;
import irnp.messages.*;
import irnp.objects.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class IRNPProcessor {
	
	private static int currentFlownum = 0;
	
	public static final int servicePort = 6666;
	public ServerSocket welcomeSocketWithDataPort;
	private int processorAsNum;
	private Inet4Address processorAddress;
	
	public long totalUpstreamBandwidth;
	public long totalDownstreamBandwidth;
	
	private long currentTotalUpstreamBandwidth;
	private long currentTotalDownstreamBandwidth;
	
	public ArrayList<IRNPASNumAndIPItem> AsToIPDic;
	public ArrayList<IRNPConnectedNodesStatus> connectStatus;
	
	private ArrayList<IRNPConnectedNodesStatus> currentConnectStatus;
	private ArrayList<IRNPConnectedNodesStatus> currentReservedConnectStatus;
	
	private ArrayList<IRNPInterDomainFlowNum> flowList;
	private ArrayList<IRNPFlowNumBandwidth> flowBandwidthList;
	
	public IRNPProcessor(int asNum) throws UnknownHostException {
		this.processorAsNum = asNum;
		this.processorAddress = (Inet4Address) Inet4Address.getLocalHost();
	}
	
	//Message processing
	public void service() throws IOException, IRNPProtocolViolationException {
		Socket connectionSocketWithDataPort = welcomeSocketWithDataPort.accept();
		DataInputStream dis = new DataInputStream(connectionSocketWithDataPort
				.getInputStream());
		
		byte[] buf = new byte[4*1024];
		while(true) {
			int length = dis.read(buf);
			if(length < 8) {
				System.out.println("Invalid Message Recieved");
			} else {
				byte[] messageBytes = new byte[length];
				System.arraycopy(buf, 0, messageBytes, 0, length);
				IRNPRequestMessage message = new IRNPRequestMessage();
				message.setBytes(messageBytes);
				message.encodeHeader();
				switch(message.getMsgType()) {
				case IRNPMessageTypes.MESSAGE_REQUEST: {
					handleRequestMessage(messageBytes);
				}
				case IRNPMessageTypes.MESSAGE_REPLY: {
					handleReplyMessage(messageBytes);
				}
				case  IRNPMessageTypes.MESSAGE_REQUEST_ERROR: {
					handleRequestErrorMessage(messageBytes);
				}
				case  IRNPMessageTypes.MESSAGE_REPLY_ERROR: {
					handleReplyErrorMessage(messageBytes);
				}
				case IRNPMessageTypes.MESSAGE_REQUEST_WITHDRAW: {
					handleRequestWithdrawMessage(messageBytes);
				}
				case IRNPMessageTypes.MESSAGE_REPLY_WITHDRAW: {
					handleReplyWithdrawMessage(messageBytes);
				}
				case IRNPMessageTypes.MESSAGE_REQUEST_WITHDRAW_ERROR: {
					handleRequestWithdrawErrorMessage(messageBytes);
				}
				case IRNPMessageTypes.MESSAGE_REPLY_WITHDRAW_ERROR: {
					handleReplyWithdrawErrorMessage(messageBytes);
				}
				}
			}
		}
	}
	
	private void handleRequestMessage(byte[] messageBytes) throws IRNPProtocolViolationException {
		IRNPRequestMessage message = new IRNPRequestMessage();
		message.setBytes(messageBytes);
		try {
			message.decode();
		} catch (IRNPProtocolViolationException e) {
			System.out.println("Cannot handle the RequestMessage");
			e.printStackTrace();
			return;
		}
		
		SenderTemplateIPv4 st = (SenderTemplateIPv4) message.getSenderTemplate();
		Inet4Address senderIP = st.getSrcAddress();
		if(isSenderTemplateAddressAuthorized(senderIP)) {
			//checkPathWithPreviousIPAddress here will always be true this time, needs to be changed(wrong parameter)
			if(checkPathWithPreviousIPAddress(senderIP)) {
				ReceiverTemplateIPv4 rt = (ReceiverTemplateIPv4) message.getReceiverTemplate();
				Inet4Address receiverIP = rt.getDstAddress();
				int index = asInPathAtIndex(this.processorAsNum, message.getAsPath().getAsList());
				if(index <= 0) {
					System.out.println("Processor: Request wrong about path");
					return;
				}
				
				if(receiverIP.equals(this.processorAddress)) {
					//Confirm flow
					FlowBandwidthSpec fbs = message.getFlowBandwidthSpec();
					int confirmedFlowBandwidth = confirmFlowBandwidth(message.getAsNum(), message.getFlowNum(), fbs.getBandwidth());
					
					int receiverAsNum = (message.getAsPath().getAsList().get(index-1));
					if(!isAsConnected(receiverAsNum)) {
						System.out.println("Processor: Request wrong about AS connection");
						return;
					}
					
					//make reservation
					int targetAsNum = getAsNumFromIPAddress(receiverIP);
					makeReservation(message.getAsNum(), message.getFlowNum(), targetAsNum, confirmedFlowBandwidth, message.getAsPath().getAsList());
					
					//Generate and send IRNPReplyMessage
					IRNPReplyMessage rMessage = new IRNPReplyMessage();
					rMessage.setAsNum(message.getAsNum());
					rMessage.setFlowNum(message.getFlowNum());
					rMessage.setSenderTemplate(message.getSenderTemplate());
					rMessage.setReceiverTemplate(message.getReceiverTemplate());
					rMessage.setAsPath(message.getAsPath());
					FlowBandwidthConfirm flowBandwidthConfirm = new FlowBandwidthConfirm(confirmedFlowBandwidth);
					rMessage.setFlowBandwidthConfirm(flowBandwidthConfirm);
					rMessage.encode();
					
					Inet4Address receiverIPAddress = getIPAddressFormAsNum(receiverAsNum);
					
					Socket clientSocket;
					try {
						clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
						DataOutputStream outToServer = new DataOutputStream(
								clientSocket.getOutputStream());
						outToServer.write(rMessage.getBytes());
						
						outToServer.close();
						clientSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println("Processor: Reply Withdraw Sending error");
						e.printStackTrace();
					}
				} else {
					// Pass the Request to next AS
					if(index <= 0) {
						System.out.println("Processor: Request wrong about path");
						return;
					}
					int receiverAsNum = (message.getAsPath().getAsList().get(index+1));
					if(!isAsConnected(receiverAsNum)) {
						System.out.println("Processor: Request wrong about AS connection");
						return;
					}
					message.encode();
					
					Inet4Address receiverIPAddress = getIPAddressFormAsNum(receiverAsNum);
					
					Socket clientSocket;
					try {
						clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
						DataOutputStream outToServer = new DataOutputStream(
								clientSocket.getOutputStream());
						outToServer.write(message.getBytes());
						
						outToServer.close();
						clientSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println("Processor: Reply Error Sending error");
						e.printStackTrace();
					}
				}
			} else {
				// Path Invalid
				IRNPRequestErrorMessage rMessage = new IRNPRequestErrorMessage();
				rMessage.setAsNum(message.getAsNum());
				rMessage.setFlowNum(message.getFlowNum());
				rMessage.setSenderTemplate(message.getSenderTemplate());
				rMessage.setReceiverTemplate(message.getReceiverTemplate());
				rMessage.setAsPath(message.getAsPath());
				rMessage.setFlowBandwidthSpec(message.getFlowBandwidthSpec());
				ErrorSpecIPv4 errorSpec = new ErrorSpecIPv4(this.processorAddress, IRNPObjectParameters.IRNP_OBJECT_ERROR_CODE_WRONG_PATH, this.processorAsNum);
				rMessage.setErrorSpec(errorSpec);
				
				rMessage.encode();
				
				ReceiverTemplateIPv4 rt = (ReceiverTemplateIPv4) message.getReceiverTemplate();
				Inet4Address receiverIPAddress = rt.getDstAddress();
				
				Socket clientSocket;
				try {
					clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
					DataOutputStream outToServer = new DataOutputStream(
							clientSocket.getOutputStream());
					outToServer.write(rMessage.getBytes());
					
					outToServer.close();
					clientSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Processor: Request Error Sending error");
					e.printStackTrace();
				}
			}
			
		} else {
			// Not authorized(policy control)
			IRNPRequestErrorMessage rMessage = new IRNPRequestErrorMessage();
			rMessage.setAsNum(message.getAsNum());
			rMessage.setFlowNum(message.getFlowNum());
			rMessage.setSenderTemplate(message.getSenderTemplate());
			rMessage.setReceiverTemplate(message.getReceiverTemplate());
			rMessage.setAsPath(message.getAsPath());
			rMessage.setFlowBandwidthSpec(message.getFlowBandwidthSpec());
			ErrorSpecIPv4 errorSpec = new ErrorSpecIPv4(this.processorAddress, IRNPObjectParameters.IRNP_OBJECT_ERROR_CODE_POLICY_CONTROL_FAILURE, 0);
			rMessage.setErrorSpec(errorSpec);
			
			rMessage.encode();
			
			ReceiverTemplateIPv4 rt = (ReceiverTemplateIPv4) message.getReceiverTemplate();
			Inet4Address receiverIPAddress = rt.getDstAddress();
			
			Socket clientSocket;
			try {
				clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
				DataOutputStream outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
				outToServer.write(rMessage.getBytes());
				
				outToServer.close();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Processor: Request Error Sending error");
				e.printStackTrace();
			}
		}
	}
	
	private int confirmFlowBandwidth(int asNum, int flowNum, int bandwidth) {
		// Needs to be implemented
		return bandwidth;
	}
	
	private boolean checkPathWithPreviousIPAddress(Inet4Address address) {
		// Path check, needs to be implemented
		return address == null ? false : true;
	}
	
	private boolean isSenderTemplateAddressAuthorized(Inet4Address senderAddress) {
		// Policy control, needs to be implemented
		return senderAddress == null ? false : true;
	}
	
	private void handleReplyMessage(byte[] messageBytes) throws IRNPProtocolViolationException {
		IRNPReplyMessage message = new IRNPReplyMessage();
		message.setBytes(messageBytes);
		try {
			message.decode();
		} catch (IRNPProtocolViolationException e) {
			System.out.println("Cannot handle the ReplyMessage");
			e.printStackTrace();
			return;
		}
		
		SenderTemplateIPv4 st = (SenderTemplateIPv4) message.getSenderTemplate();
		Inet4Address senderIP = st.getSrcAddress();
		ReceiverTemplateIPv4 rt = (ReceiverTemplateIPv4) message.getReceiverTemplate();
		Inet4Address receiverIP = rt.getDstAddress();
		if(senderIP.equals(this.processorAddress)) {
			// Sender received message, and make record for its own flow
			makeReservation(message.getAsNum(), message.getFlowNum(), getAsNumFromIPAddress(receiverIP), message.getFlowBandwidthConfirm().getBandwidth(), message.getAsPath().getAsList());
		} else {
			if(isAsNumInFlowlist(message.getAsNum())){
				if(isFlowInFlowlist(message.getAsNum(), message.getFlowNum())) {
					if(maxBandwidthThisASAllow(message.getAsNum(), message.getFlowNum(), message.getFlowBandwidthConfirm().getBandwidth()) > 0) {
						// Reservation success
						makeReservation(message.getAsNum(), message.getFlowNum(), getAsNumFromIPAddress(receiverIP), message.getFlowBandwidthConfirm().getBandwidth(), message.getAsPath().getAsList());
						int index = asInPathAtIndex(this.processorAsNum, message.getAsPath().getAsList());
						if(index <= 0) {
							System.out.println("Processor: Reply wrong about path");
							return;
						}
						int receiverAsNum = (message.getAsPath().getAsList().get(index-1));
						if(!isAsConnected(receiverAsNum)) {
							System.out.println("Processor: Reply wrong about AS connection");
							return;
						}
						message.encode();
						
						Inet4Address receiverIPAddress = getIPAddressFormAsNum(receiverAsNum);
						
						Socket clientSocket;
						try {
							clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
							DataOutputStream outToServer = new DataOutputStream(
									clientSocket.getOutputStream());
							outToServer.write(message.getBytes());
							
							outToServer.close();
							clientSocket.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							System.out.println("Processor: Reply Error Sending error");
							e.printStackTrace();
						}
						
					} else {
						// Reservation failed
						IRNPReplyErrorMessage rMessage = new IRNPReplyErrorMessage();
						rMessage.setAsNum(message.getAsNum());
						rMessage.setFlowNum(message.getFlowNum());
						rMessage.setSenderTemplate(message.getSenderTemplate());
						rMessage.setReceiverTemplate(message.getReceiverTemplate());
						rMessage.setAsPath(message.getAsPath());
						rMessage.setFlowBandwidthConfirm(message.getFlowBandwidthConfirm());
						ErrorSpecIPv4 errorSpec = new ErrorSpecIPv4(this.processorAddress, IRNPObjectParameters.IRNP_OBJECT_ERROR_CODE_RESERVATION_FAILTURE, 0);
						rMessage.setErrorSpec(errorSpec);
						
						rMessage.encode();
						
						Inet4Address receiverIPAddress = rt.getDstAddress();
						
						Socket clientSocket;
						try {
							clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
							DataOutputStream outToServer = new DataOutputStream(
									clientSocket.getOutputStream());
							outToServer.write(rMessage.getBytes());
							
							outToServer.close();
							clientSocket.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							System.out.println("Processor: Reply Error Sending error");
							e.printStackTrace();
						}
					}
					
				} else {
					// No request
					IRNPReplyErrorMessage rMessage = new IRNPReplyErrorMessage();
					rMessage.setAsNum(message.getAsNum());
					rMessage.setFlowNum(message.getFlowNum());
					rMessage.setSenderTemplate(message.getSenderTemplate());
					rMessage.setReceiverTemplate(message.getReceiverTemplate());
					rMessage.setAsPath(message.getAsPath());
					rMessage.setFlowBandwidthConfirm(message.getFlowBandwidthConfirm());
					ErrorSpecIPv4 errorSpec = new ErrorSpecIPv4(this.processorAddress, IRNPObjectParameters.IRNP_OBJECT_ERROR_CODE_NO_REQUEST_FOR_REPLY_MESSAGE, 0);
					rMessage.setErrorSpec(errorSpec);
					
					rMessage.encode();
					
					Inet4Address receiverIPAddress = rt.getDstAddress();
					
					Socket clientSocket;
					try {
						clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
						DataOutputStream outToServer = new DataOutputStream(
								clientSocket.getOutputStream());
						outToServer.write(rMessage.getBytes());
						
						outToServer.close();
						clientSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println("Processor: Reply Error Sending error");
						e.printStackTrace();
					}
				}
			} else {
				// No sender
				IRNPReplyErrorMessage rMessage = new IRNPReplyErrorMessage();
				rMessage.setAsNum(message.getAsNum());
				rMessage.setFlowNum(message.getFlowNum());
				rMessage.setSenderTemplate(message.getSenderTemplate());
				rMessage.setReceiverTemplate(message.getReceiverTemplate());
				rMessage.setAsPath(message.getAsPath());
				rMessage.setFlowBandwidthConfirm(message.getFlowBandwidthConfirm());
				ErrorSpecIPv4 errorSpec = new ErrorSpecIPv4(this.processorAddress, IRNPObjectParameters.IRNP_OBJECT_ERROR_CODE_NO_SENDER_FOR_REPLY_MESSAGE, 0);
				rMessage.setErrorSpec(errorSpec);
				
				rMessage.encode();
				
				Inet4Address receiverIPAddress = rt.getDstAddress();
				
				Socket clientSocket;
				try {
					clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
					DataOutputStream outToServer = new DataOutputStream(
							clientSocket.getOutputStream());
					outToServer.write(rMessage.getBytes());
					
					outToServer.close();
					clientSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Processor: Reply Error Sending error");
					e.printStackTrace();
				}
				
			}
			
		}
	}
	
	private int maxBandwidthThisASAllow(int asNum, int flowNum, int bandwidth) {
		// Needs to be implemented
		return bandwidth;
	}
	
	private void makeReservation(int asNum, int flowNum, int targetAsNum, int bandwidth, ArrayList<Integer> path) {
		// Needs to be implemented
		IRNPFlowNumBandwidth fnb = new IRNPFlowNumBandwidth(asNum, flowNum, bandwidth, targetAsNum, path);
		this.flowBandwidthList.add(fnb);
	}
	
	private void handleRequestErrorMessage(byte[] messageBytes) throws IRNPProtocolViolationException {
		IRNPRequestErrorMessage message = new IRNPRequestErrorMessage();
		message.setBytes(messageBytes);
		try {
			message.decode();
		} catch (IRNPProtocolViolationException e) {
			System.out.println("Cannot handle the RequestErrorMessage");
			e.printStackTrace();
			return;
		}
		
		SenderTemplateIPv4 st = (SenderTemplateIPv4) message.getSenderTemplate();
		Inet4Address senderIP = st.getSrcAddress();
		int index = asInPathAtIndex(this.processorAsNum, message.getAsPath().getAsList());
		
		//Delete flow information
		deleteFlowInfo(message.getAsNum(),message.getFlowNum());
		deleteFlowBandwidthInfo(message.getAsNum(),message.getFlowNum());
		if(senderIP.equals(this.processorAddress)) {
			//Generate and send IRNPReplyWithdrawMessage
			if(index <= 0) {
				System.out.println("Processor: Reply Error Withdraw wrong about path");
				return;
			}
			int receiverAsNum = (message.getAsPath().getAsList().get(index+1));
			if(!isAsConnected(receiverAsNum)) {
				System.out.println("Processor: Reply Error Withdraw wrong about AS connection");
				return;
			}
			
			IRNPRequestErrorWithdrawMessage rMessage = new IRNPRequestErrorWithdrawMessage();
			rMessage.setAsNum(message.getAsNum());
			rMessage.setFlowNum(message.getFlowNum());
			rMessage.setSenderTemplate(message.getSenderTemplate());
			rMessage.setReceiverTemplate(message.getReceiverTemplate());
			rMessage.setAsPath(message.getAsPath());
			rMessage.setErrorSpec(message.getErrorSpec());
			
			rMessage.encode();
			
			Inet4Address receiverIPAddress = getIPAddressFormAsNum(receiverAsNum);
			
			Socket clientSocket;
			try {
				clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
				DataOutputStream outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
				outToServer.write(rMessage.getBytes());
				
				outToServer.close();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Processor: Reply Withdraw Sending error");
				e.printStackTrace();
			}
		}
	}
	
	private void handleReplyErrorMessage(byte[] messageBytes) throws IRNPProtocolViolationException {
		IRNPReplyErrorMessage message = new IRNPReplyErrorMessage();
		message.setBytes(messageBytes);
		try {
			message.decode();
		} catch (IRNPProtocolViolationException e) {
			System.out.println("Cannot handle the RequestErrorMessage");
			e.printStackTrace();
			return;
		}
		
		ReceiverTemplateIPv4 st = (ReceiverTemplateIPv4) message.getReceiverTemplate();
		Inet4Address receiverIP = st.getDstAddress();
		int index = asInPathAtIndex(this.processorAsNum, message.getAsPath().getAsList());
		
		//Delete flow information
		deleteFlowInfo(message.getAsNum(),message.getFlowNum());
		deleteFlowBandwidthInfo(message.getAsNum(),message.getFlowNum());
		if(receiverIP.equals(this.processorAddress)) {
			//Generate and send IRNPReplyWithdrawMessage
			if(index <= 0) {
				System.out.println("Processor: Reply Error Withdraw wrong about path");
				return;
			}
			int receiverAsNum = (message.getAsPath().getAsList().get(index-1));
			if(!isAsConnected(receiverAsNum)) {
				System.out.println("Processor: Reply Error Withdraw wrong about AS connection");
				return;
			}
			
			IRNPReplyErrorWithdrawMessage rMessage = new IRNPReplyErrorWithdrawMessage();
			rMessage.setAsNum(message.getAsNum());
			rMessage.setFlowNum(message.getFlowNum());
			rMessage.setSenderTemplate(message.getSenderTemplate());
			rMessage.setReceiverTemplate(message.getReceiverTemplate());
			rMessage.setAsPath(message.getAsPath());
			rMessage.setFlowBandwidthConfirm(message.getFlowBandwidthConfirm());
			rMessage.setErrorSpec(message.getErrorSpec());
			
			rMessage.encode();
			
			Inet4Address receiverIPAddress = getIPAddressFormAsNum(receiverAsNum);
			
			Socket clientSocket;
			try {
				clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
				DataOutputStream outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
				outToServer.write(rMessage.getBytes());
				
				outToServer.close();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Processor: Reply Withdraw Sending error");
				e.printStackTrace();
			}
		}
	}
	
	private void handleRequestWithdrawMessage(byte[] messageBytes) throws IRNPProtocolViolationException {
		IRNPRequestWithdrawMessage message = new IRNPRequestWithdrawMessage();
		message.setBytes(messageBytes);
		try {
			message.decode();
		} catch (IRNPProtocolViolationException e) {
			System.out.println("Cannot handle the RequestWithdrawMessage");
			e.printStackTrace();
			return;
		}
		
		ReceiverTemplateIPv4 rt = (ReceiverTemplateIPv4) message.getReceiverTemplate();
		Inet4Address receiverIP = rt.getDstAddress();
		int index = asInPathAtIndex(this.processorAsNum, message.getAsPath().getAsList());
		if(receiverIP.equals(this.processorAddress)) {
			//Delete flow information
			deleteFlowInfo(message.getAsNum(),message.getFlowNum());
			deleteFlowBandwidthInfo(message.getAsNum(),message.getFlowNum());
			
			//Generate and send IRNPReplyWithdrawMessage
			if(index <= 0) {
				System.out.println("Processor: Reply Withdraw wrong about path");
				return;
			}
			
			IRNPReplyWithdrawMessage replyWithdrawMessage = new IRNPReplyWithdrawMessage();
			replyWithdrawMessage.setAsNum(message.getAsNum());
			replyWithdrawMessage.setFlowNum(message.getFlowNum());
			replyWithdrawMessage.setSenderTemplate(message.getSenderTemplate());
			replyWithdrawMessage.setReceiverTemplate(message.getReceiverTemplate());
			replyWithdrawMessage.setAsPath(message.getAsPath());
			FlowBandwidthConfirm flowBandwidthConfirm = new FlowBandwidthConfirm(getConfirmedBandWidth(message.getAsNum(),message.getFlowNum()));
			replyWithdrawMessage.setFlowBandwidthConfirm(flowBandwidthConfirm);
			replyWithdrawMessage.encode();
			
			int receiverAsNum = (message.getAsPath().getAsList().get(index-1));
			if(!isAsConnected(receiverAsNum)) {
				System.out.println("Processor: Reply Withdraw wrong about AS connection");
				return;
			}
			Inet4Address receiverIPAddress = getIPAddressFormAsNum(receiverAsNum);
			
			Socket clientSocket;
			try {
				clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
				DataOutputStream outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
				outToServer.write(replyWithdrawMessage.getBytes());
				
				outToServer.close();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Processor: Reply Withdraw Sending error");
				e.printStackTrace();
			}
		} else {
			int receiverAsNum = (message.getAsPath().getAsList().get(index+1));
			if(!isAsConnected(receiverAsNum)) {
				System.out.println("Processor: Reply Withdraw wrong about AS connection");
				return;
			}
			message.encode();
			Inet4Address receiverIPAddress = getIPAddressFormAsNum(receiverAsNum);
			
			Socket clientSocket;
			try {
				clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
				DataOutputStream outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
				outToServer.write(message.getBytes());
				
				outToServer.close();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Processor: Reply Withdraw Sending error");
				e.printStackTrace();
			}
		}
	}
	
	private void handleReplyWithdrawMessage(byte[] messageBytes) throws IRNPProtocolViolationException {
		IRNPReplyWithdrawMessage message = new IRNPReplyWithdrawMessage();
		message.setBytes(messageBytes);
		try {
			message.decode();
		} catch (IRNPProtocolViolationException e) {
			System.out.println("Cannot handle the ReplyWithdrawMessage");
			e.printStackTrace();
			return;
		}
		
		SenderTemplateIPv4 st = (SenderTemplateIPv4) message.getSenderTemplate();
		Inet4Address senderIP = st.getSrcAddress();
		int index = asInPathAtIndex(this.processorAsNum, message.getAsPath().getAsList());
		
		//Delete flow information
		deleteFlowInfo(message.getAsNum(),message.getFlowNum());
		deleteFlowBandwidthInfo(message.getAsNum(),message.getFlowNum());
		if(!senderIP.equals(this.processorAddress)) {
			//Generate and send IRNPReplyWithdrawMessage
			if(index <= 0) {
				System.out.println("Processor: Reply Withdraw wrong about path");
				return;
			}
			int receiverAsNum = (message.getAsPath().getAsList().get(index-1));
			if(!isAsConnected(receiverAsNum)) {
				System.out.println("Processor: Reply Withdraw wrong about AS connection");
				return;
			}
			message.encode();
			
			Inet4Address receiverIPAddress = getIPAddressFormAsNum(receiverAsNum);
			
			Socket clientSocket;
			try {
				clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
				DataOutputStream outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
				outToServer.write(message.getBytes());
				
				outToServer.close();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Processor: Reply Withdraw Sending error");
				e.printStackTrace();
			}
		}
	}
	
	private void handleRequestWithdrawErrorMessage(byte[] messageBytes) throws IRNPProtocolViolationException {
		IRNPRequestErrorWithdrawMessage message = new IRNPRequestErrorWithdrawMessage();
		message.setBytes(messageBytes);
		try {
			message.decode();
		} catch (IRNPProtocolViolationException e) {
			System.out.println("Cannot handle the ReplyErrorWithdrawMessage");
			e.printStackTrace();
			return;
		}
		
		ReceiverTemplateIPv4 rt = (ReceiverTemplateIPv4) message.getReceiverTemplate();
		Inet4Address receiverIP = rt.getDstAddress();
		int index = asInPathAtIndex(this.processorAsNum, message.getAsPath().getAsList());
		
		//Delete flow information
		deleteFlowInfo(message.getAsNum(),message.getFlowNum());
		deleteFlowBandwidthInfo(message.getAsNum(),message.getFlowNum());
		if(!receiverIP.equals(this.processorAddress)) {
			//Generate and send IRNPReplyErrorWithdrawMessage
			if(index <= 0) {
				System.out.println("Processor: Reply Error Withdraw wrong about path");
				return;
			}
			int receiverAsNum = (message.getAsPath().getAsList().get(index+1));
			if(!isAsConnected(receiverAsNum)) {
				System.out.println("Processor: Reply Error Withdraw wrong about AS connection");
				return;
			}
			message.encode();
			
			Inet4Address receiverIPAddress = getIPAddressFormAsNum(receiverAsNum);
			
			Socket clientSocket;
			try {
				clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
				DataOutputStream outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
				outToServer.write(message.getBytes());
				
				outToServer.close();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Processor: Reply Error Withdraw Sending error");
				e.printStackTrace();
			}
		}
	}
	
	private void handleReplyWithdrawErrorMessage(byte[] messageBytes) throws IRNPProtocolViolationException {
		IRNPReplyErrorWithdrawMessage message = new IRNPReplyErrorWithdrawMessage();
		message.setBytes(messageBytes);
		try {
			message.decode();
		} catch (IRNPProtocolViolationException e) {
			System.out.println("Cannot handle the ReplyErrorWithdrawMessage");
			e.printStackTrace();
			return;
		}
		
		SenderTemplateIPv4 rt = (SenderTemplateIPv4) message.getSenderTemplate();
		Inet4Address senderIP = rt.getSrcAddress();
		int index = asInPathAtIndex(this.processorAsNum, message.getAsPath().getAsList());
		
		//Delete flow information
		deleteFlowInfo(message.getAsNum(),message.getFlowNum());
		deleteFlowBandwidthInfo(message.getAsNum(),message.getFlowNum());
		if(!senderIP.equals(this.processorAddress)) {
			//Generate and send IRNPReplyErrorWithdrawMessage
			if(index <= 0) {
				System.out.println("Processor: Reply Error Withdraw wrong about path");
				return;
			}
			int receiverAsNum = (message.getAsPath().getAsList().get(index-1));
			if(!isAsConnected(receiverAsNum)) {
				System.out.println("Processor: Reply Error Withdraw wrong about AS connection");
				return;
			}
			message.encode();
			
			Inet4Address receiverIPAddress = getIPAddressFormAsNum(receiverAsNum);
			
			Socket clientSocket;
			try {
				clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
				DataOutputStream outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
				outToServer.write(message.getBytes());
				
				outToServer.close();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Processor: Reply Error Withdraw Sending error");
				e.printStackTrace();
			}
		}
	}
	
	private int getConfirmedBandWidth(int asNum, int flowNum) {
		for(IRNPFlowNumBandwidth fb:this.flowBandwidthList) {
			if(asNum==fb.getAsNum()&&flowNum==fb.getFlowNum()){
				return fb.getBandwidth();
			}
		}
		return 0;
	}
	
	private boolean isAsNumInFlowlist(int asNum) {
		for(int i=0;i<this.flowList.size();i++) {
			IRNPInterDomainFlowNum idf = this.flowList.get(i);
			if(asNum==idf.getAsNum()){
				return true;
			}
		}
		return false;
	}
	
	private boolean isFlowInFlowlist(int asNum, int flowNum) {
		for(int i=0;i<this.flowList.size();i++) {
			IRNPInterDomainFlowNum idf = this.flowList.get(i);
			if(asNum==idf.getAsNum()&&flowNum==idf.getFlowNum()){
				return true;
			}
		}
		return false;
	}
	
	private boolean isFlowBandwidthInFlowBandwidthlist(int asNum, int flowNum) {
		for(int i=0;i<this.flowBandwidthList.size();i++) {
			IRNPFlowNumBandwidth idf = this.flowBandwidthList.get(i);
			if(asNum==idf.getAsNum()&&flowNum==idf.getFlowNum()){
				return true;
			}
		}
		return false;
	}
	
	private void deleteFlowInfo(int asNum, int flowNum) {
		for(int i=0;i<this.flowList.size();i++) {
			IRNPInterDomainFlowNum idf = this.flowList.get(i);
			if(asNum==idf.getAsNum()&&flowNum==idf.getFlowNum()){
				this.flowList.remove(i);
			}
		}
	}
	
	private void deleteFlowBandwidthInfo(int asNum, int flowNum) {
		for(int i=0;i<this.flowBandwidthList.size();i++) {
			IRNPFlowNumBandwidth idf = this.flowBandwidthList.get(i);
			if(asNum==idf.getAsNum()&&flowNum==idf.getFlowNum()){
				this.flowBandwidthList.remove(i);
			}
		}
	}
	
	//Application calls
	public void requestPath(ArrayList<Integer> path, int flowBandwidth) throws IRNPProtocolViolationException {
		
		if(path.size() < 2) {
			System.out.println("Processor: Invalid Request ASPath");
			return;
		}
		
		int senderAsNum = path.get(0);
		if(senderAsNum != processorAsNum) {
			System.out.println("Processor: Wrong Request ASPath");
			return;
		}
		
		int receiverAsNum = path.get(1);
		Inet4Address receiverIPAddress = getIPAddressFormAsNum(receiverAsNum);
		int dstAsNumber = path.get(path.size()-1);
		Inet4Address dstIPAddress = getIPAddressFormAsNum(dstAsNumber);
		
		IRNPRequestMessage requestMessage = new IRNPRequestMessage();
		SenderTemplate senderTemplate = new SenderTemplateIPv4(processorAddress, IRNPProcessor.servicePort);
		requestMessage.setSenderTemplate(senderTemplate);
		ReceiverTemplate receiverTemplate = new ReceiverTemplateIPv4(dstIPAddress, IRNPProcessor.servicePort);
		requestMessage.setReceiverTemplate(receiverTemplate);
		FlowBandwidthSpec flowBandwidthSpec = new FlowBandwidthSpec(flowBandwidth);
		requestMessage.setFlowBandwidthSpec(flowBandwidthSpec);
		ASPath asPath = new ASPath(path);
		requestMessage.setAsPath(asPath);
		requestMessage.setAsNum(this.processorAsNum);
		requestMessage.setFlowNum(getANewFlowNumber());
		requestMessage.encode();
		
		Socket clientSocket;
		try {
			clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
			DataOutputStream outToServer = new DataOutputStream(
					clientSocket.getOutputStream());
			outToServer.write(requestMessage.getBytes());
			
			outToServer.close();
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Processor: Request Sending error");
			e.printStackTrace();
		}
	}
	
public void requestWithdrawPath(int flowNum, int targetAsNum, int flowBandwidth) throws IRNPProtocolViolationException {
		
		int asNum = this.processorAsNum;
		if(!isFlowInFlowlist(asNum, flowNum)) {
			System.out.println("Processor: Wrong Request Withdraw flow");
			return;
		}
		
		if(!isFlowBandwidthInFlowBandwidthlist(asNum, flowNum)) {
			System.out.println("Processor: Wrong Request Withdraw ASPath");
			return;
		}
		
		ArrayList<Integer> path = getPathFromFlow(asNum, flowNum);
		
		if(path.size() < 2) {
			System.out.println("Processor: Invalid Request Withdraw ASPath");
			return;
		}
		
		int receiverAsNum = path.get(1);
		Inet4Address receiverIPAddress = getIPAddressFormAsNum(receiverAsNum);
		int dstAsNumber = targetAsNum;
		Inet4Address dstIPAddress = getIPAddressFormAsNum(dstAsNumber);
		
		IRNPRequestWithdrawMessage rMessage = new IRNPRequestWithdrawMessage();
		SenderTemplate senderTemplate = new SenderTemplateIPv4(processorAddress, IRNPProcessor.servicePort);
		rMessage.setSenderTemplate(senderTemplate);
		ReceiverTemplate receiverTemplate = new ReceiverTemplateIPv4(dstIPAddress, IRNPProcessor.servicePort);
		rMessage.setReceiverTemplate(receiverTemplate);
		//FlowBandwidthConfirm flowBandwidthConfirm = new FlowBandwidthConfirm(flowBandwidth);
		ASPath asPath = new ASPath(path);
		rMessage.setAsPath(asPath);
		rMessage.setAsNum(this.processorAsNum);
		rMessage.setFlowNum(getANewFlowNumber());
		rMessage.encode();
		
		Socket clientSocket;
		try {
			clientSocket = new Socket(receiverIPAddress,IRNPProcessor.servicePort);
			DataOutputStream outToServer = new DataOutputStream(
					clientSocket.getOutputStream());
			outToServer.write(rMessage.getBytes());
			
			outToServer.close();
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Processor: Request Sending error");
			e.printStackTrace();
		}
	}
	
	private ArrayList<Integer> getPathFromFlow(int asNum, int flowNum) {
		for(IRNPFlowNumBandwidth item : this.flowBandwidthList) {
			if(item.asNum==asNum&&item.flowNum==flowNum) {
				return item.getPath();
			}
		}
		return null;
	}

	//Others
	private int asInPathAtIndex(int asNum, ArrayList<Integer> path) {
		for(int i=0;i<path.size();i++) {
			if(path.get(i)==asNum){
				return i;
			}
		}
		return -1;
	}
	
	private int getANewFlowNumber() {
		// Need a little fix
		return currentFlownum++;
	}
	
	private Inet4Address getIPAddressFormAsNum(int asNum) {
		for(IRNPASNumAndIPItem item : this.AsToIPDic) {
			if(item.getAsNum() == asNum) {
				return item.getAsIPAddress();
			}
		}
		return null;
	}
	
	private int getAsNumFromIPAddress(Inet4Address addr) {
		for(IRNPASNumAndIPItem item : this.AsToIPDic) {
			if(addr.equals(item.getAsIPAddress())) {
				return item.getAsNum();
			}
		}
		return -1;
	}
	
	public boolean isAsConnected(int asNum) {
		for(int i=0;i<this.connectStatus.size();i++) {
			IRNPConnectedNodesStatus node = this.connectStatus.get(i);
			if(node.getAsNum() == asNum) {
				return true;
			}
		}
		return false;
	}
	
	//Getter and Setters
	public long getCurrentTotalUpstreamBandwidth() {
		return currentTotalUpstreamBandwidth;
	}
	public void setCurrentTotalUpstreamBandwidth(long currentTotalUpstreamBandwidth) {
		this.currentTotalUpstreamBandwidth = currentTotalUpstreamBandwidth;
	}
	public long getCurrentTotalDownstreamBandwidth() {
		return currentTotalDownstreamBandwidth;
	}
	public void setCurrentTotalDownstreamBandwidth(long currentTotalDownstreamBandwidth) {
		this.currentTotalDownstreamBandwidth = currentTotalDownstreamBandwidth;
	}
	public ArrayList<IRNPConnectedNodesStatus> getCurrentConnectStatus() {
		return currentConnectStatus;
	}
	public void setCurrentConnectStatus(ArrayList<IRNPConnectedNodesStatus> currentConnectStatus) {
		this.currentConnectStatus = currentConnectStatus;
	}
	public ArrayList<IRNPConnectedNodesStatus> getCurrentReservedConnectStatus() {
		return currentReservedConnectStatus;
	}
	public void setCurrentReservedConnectStatus(ArrayList<IRNPConnectedNodesStatus> currentReservedConnectStatus) {
		this.currentReservedConnectStatus = currentReservedConnectStatus;
	}
	public ArrayList<IRNPInterDomainFlowNum> getFlowList() {
		return flowList;
	}
	public void setFlowList(ArrayList<IRNPInterDomainFlowNum> flowList) {
		this.flowList = flowList;
	}

	public int getProcessorAsNum() {
		return processorAsNum;
	}

	public void setProcessorAsNum(int processorAsNum) {
		this.processorAsNum = processorAsNum;
	}

	public Inet4Address getProcessorAddress() {
		return processorAddress;
	}

	public void setProcessorAddress(Inet4Address processorAddress) {
		this.processorAddress = processorAddress;
	}

	public ArrayList<IRNPFlowNumBandwidth> getFlowBandwidthList() {
		return flowBandwidthList;
	}
	
}
