package irnp.objects;

public class IRNPObjectParameters {
	
	public static final int IRNP_OBJECT_COMMON_HEADER_SIZE = 4;
	
	
	public static final int IRNP_OBJECT_CLASS_SENDER_TEMPLATE = 1;
	public static final int IRNP_OBJECT_CLASS_RECEIVER_TEMPLATE = 2;
	public static final int IRNP_OBJECT_CLASS_AS_PATH = 3;
	public static final int IRNP_OBJECT_CLASS_FLOW_BANDWIDTH_SPEC = 4;
	public static final int IRNP_OBJECT_CLASS_FLOW_BANDWIDTH_CONFIRM = 5;
	public static final int IRNP_OBJECT_CLASS_ERROR_SPEC = 6;
	
	public static final int IRNP_OBJECT_ERROR_CODE_RESERVED = 0;
	public static final int IRNP_OBJECT_ERROR_CODE_POLICY_CONTROL_FAILURE = 1;
	public static final int IRNP_OBJECT_ERROR_CODE_NO_REQUEST_FOR_REPLY_MESSAGE = 2;
	public static final int IRNP_OBJECT_ERROR_CODE_NO_SENDER_FOR_REPLY_MESSAGE = 3;
	public static final int IRNP_OBJECT_ERROR_CODE_RESERVATION_FAILTURE = 4;
	public static final int IRNP_OBJECT_ERROR_CODE_WRONG_PATH = 5;
	public static final int IRNP_OBJECT_ERROR_CODE_OTHERS = 6;
}
