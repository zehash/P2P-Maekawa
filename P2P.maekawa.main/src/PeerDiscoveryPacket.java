import java.io.Serializable;

import javax.swing.JComponent;

/**
 * This Packet 
 * 
 * @author Hash
 */
public class PeerDiscoveryPacket implements Serializable {

	
	private String ipAddress;
	private boolean clientStatus = false;
	private boolean serverStatus = false;
	private boolean isRemoved = false;
	private int peerNumber;
		
	public PeerDiscoveryPacket(String IPAddress, int peerNumber, boolean Client, boolean Server, boolean isRemoved) {
		this.ipAddress = IPAddress;
		this.clientStatus = Client;
		this.serverStatus = Server;
		this.peerNumber = peerNumber;
		this.isRemoved = isRemoved;
	}
	
	public String getIP() { 
		return ipAddress;
	}
	
	public int getPeerNumber() {
		return peerNumber;
	}
	
	public boolean getClientStatus() {
		return clientStatus;
	}
	
	public boolean getServerStatus() {
		return serverStatus;
	}
	
	public boolean getIsRemoved() {
        return isRemoved;
    }
	
	public void setClientStatus(boolean newClientStatus) {
		clientStatus = newClientStatus;
	}
	
	public void setServerStatus(boolean newServerStatus) {
		serverStatus = newServerStatus;
	}
	
	public void setIsRemoved(boolean isRemoved) {
        this.isRemoved = isRemoved;
    }
	
	public void setPeerNumber(int peerNumber) {
		this.peerNumber = peerNumber;
	}
	
}
