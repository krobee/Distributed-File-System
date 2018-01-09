package transport;

public class Protocol {
	public static final int REGISTER = 0;
	public static final int REGISTER_ACK = 1;
	
	public static final int MAJOR_HB = 2;
	public static final int MINOR_HB = 3;
	public static final int CTRL_HB = 15;
	
	public static final int SERVER3 = 4;
	public static final int SERVER3_ACK = 5;
	
	public static final int STORE = 6;
	public static final int STORE_ACK = 7;
	
	public static final int CTRL_RETRIEVE = 8;
	public static final int CTRL_RETRIEVE_ACK = 9;
	
	public static final int RETRIEVE = 10;
	public static final int RETRIEVE_ACK = 11;
	
	public static final int CTRL_FIX = 12;
	public static final int FIX = 13;
	public static final int FIX_ACK = 14;
	
	public static final int REDIS = 16;
	
	public static final int REQ_DEL = 17;
	public static final int REQ_DEL_ACK = 18;
	public static final int DEL = 19;
}
