import java.io.Serializable;

/**
 * This Packet 
 * 
 * @author Hash
 */
public class PeerDiscoveryPacket implements Serializable {

	
	private String ipAddress;
	private boolean clientStatus = false;
	private boolean serverStatus = false;
		
	public PeerDiscoveryPacket(String IPAddress, boolean Client, boolean Server) {
		this.ipAddress = IPAddress;
		this.clientStatus = Client;
		this.serverStatus = Server;
	}
	
	public String getIP() { 
		return ipAddress;
	}
	
	public boolean getClientStatus() {
		return clientStatus;
	}
	
	public boolean getServerStatus() {
		return serverStatus;
	}
	
	public void setClientStatus(boolean newClientStatus) {
		clientStatus = newClientStatus;
	}
	
	public void setServerStatus(boolean newServerStatus) {
		serverStatus = newServerStatus;
	}
	
}
