/**
 * @author bling
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
import java.util.Vector;

public class Msg implements java.io.Serializable {
	//private Vector _v;
	private int _type;	
	int _checksum;
	private long _exp;
	private Object _data;
	private long _key;
	private int _id;

	// receivers
	private transient int[] _ll;
	private transient static int _reqCounter = 1;
	private transient static Object _reqLock = new Object();

	private Integer _reqID;
	private Integer _responseID;
	private String _hostname;
	private int _port;

	private transient long _time;
	private long _timeout;

	public static transient int READ_REQ = 0;
	public static transient int WRITE_REQ = 1;
	public static transient int READ_REPLY = 2;
	public static transient int WRITE_REPLY = 3;
	public static transient int HEART = 4;
	public static transient int STUB = 5;

	public Msg() {
		//_v = null;	
		_data = null;
		_ll = null;
		synchronized(_reqLock) {
			_reqID = new Integer(_reqCounter++);
		}
		_responseID = null;
		_time = 0;
	}

	public void MakeReadReq(long key, long exp) {
		_type = READ_REQ;
		_exp = exp;
		_key = key;
	}

	public void MakeWriteReq(long key, Object data, int check, long exp) {
		_type = WRITE_REQ;
		_data = data;
		_checksum = check;
		_exp = exp;
		_key = key;	
	}

	public void MakeWriteReply(long key) {
		_type = WRITE_REPLY;
		_key = key;

	}

	public void MakeReadReply(long key, Object data, int check, long exp) {
		_key = key;
		_type = READ_REPLY;
		_data = data;
		_checksum = check;
		_exp = exp;
	}

	public void MakeHeartBeat() {
		_type = HEART;
	}

	public void setSender(int id) {
		_id = id;
	}

	public void setReceiver(int[] ll) {
		_ll = ll;
	}

	public long getKey() { return _key; }

	public int getType() { return _type; }

	public Object getData() { return _data; }

	public int getChecksum() { return _checksum; }

	public long getExpiry() { return _exp; }

	public int getSender() { return _id; }

	public int[] getReceiver() { return _ll; }

	public void setInResponseTo(Integer i) { _responseID = i; }

	public Integer getRequestID() { return _reqID; }

	public Integer getInResponseTo() { return _responseID; }
	
	public int getPort() { return _port; }
	
	public void setPort(int i) { _port = i; }

	public String getHostName() { return _hostname; }

	public void setHostName(String s) { _hostname = s; }
	
	public void setTime() { _time = System.currentTimeMillis(); }
	
	public long getTime() { return _time; }

	public void setTimeOut(long l) { _timeout  = l;}
	
	public long getTimeOut() { return _timeout; }
	
}