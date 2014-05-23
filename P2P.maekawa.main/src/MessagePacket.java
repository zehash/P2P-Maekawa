import java.io.Serializable;


public class MessagePacket implements Serializable {
	
	private String IP;
	private String message;
	
	public MessagePacket(String IP, String message) {
		this.IP = new String(IP);
		this.message = new String(message);
	}
	
	public String getIP() {
		return IP;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void SetIP(String IP) {
		this.IP = new String(IP);
	}
	
	public void setMessage(String message) {
		this.message = new String(message);
	}
}
