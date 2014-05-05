
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Client {

	private ObjectOutputStream output; // goes away from you
	private ObjectInputStream input; // goes to you
	private String message = "";
	private String serverIP;
	//private Node   Peer; 	
	/** Basic socket connection */
	private Socket connection;
	private PeerDiscoveryPacket packet;
	private MusicalChairGame mcg;
	//private Player opponent;
	private NodePacket nodePacketRecv;
	private NodePacket nodePacketSend;
	
	/*public static void main(String[] args) {
		Client Fox = new Client("10.1.1.9");
		Fox.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Fox.startClient();
	}*/
	
	// Constructor
	public Client(String host, MusicalChairGame mcg) throws Exception {
		serverIP = host;
		this.mcg = mcg;
	}

	/**
	 * Set up and run the Client Side The port number its on and how many connections
	 * can wait on it.
	 */
	public void startClient() {
		try {
			connectToServer(); // Connect to a Server
			setupStreams(); // Creates data streams
			readyListen();
		} catch (EOFException eofException) {
			System.out.println("\n Client Closed the Connection");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
		//	closeSockets();
		}
	}
	
	/*public void sendPlayer(Player player){
		try {
			nodePacketSend = new NodePacket(player.name, player.positionX, player.positionY);
			output.writeObject(nodePacketSend);
			output.flush();
			output.reset();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}*/
	public void sendPlayer(NodePacket playerPacket){
		try {
			output.writeObject(playerPacket);
			output.flush();
			output.reset();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * Connect to a server if such a server is available.
	 * Else make the connection wait for a server and activate the server using the node. 
	 */
	private void connectToServer() throws IOException {
		System.out.println("Connecting to a server... \n");
		connection = new Socket(Inet4Address.getByName(serverIP), 1234);
	}	
	
	private void setupStreams() throws IOException {
		// Creating the path to another computer
		// Send Data structures
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		// Receive Data structures
		input = new ObjectInputStream(connection.getInputStream());
		// No need to flush (Other PC does that)
		System.out.println("\nStreams are now setup \n");
		nodePacketSend = new NodePacket(mcg.mainplayer.name, mcg.mainplayer.positionX, mcg.mainplayer.positionY);
		sendPlayer(nodePacketSend);
	}
	
	/**
	 * Code running during the established connection after all the backend work
	 * Data transfer etc.
	 * 
	 * @throws IOException
	 */
	private void readyListen() throws IOException {
		do {
			// Have a connection
			try {
				nodePacketRecv = (NodePacket) input.readObject(); // Read incomming stream
				//opponent = new Player(Color.RED, nodePacketRecv.getPositionX(), nodePacketRecv.getPositionY());
				//opponent.name = nodePacketRecv.getName();
				mcg.updatePlayer(nodePacketRecv);
				mcg.node.sendToRight(nodePacketRecv);
			} catch (ClassNotFoundException cnfException) {
				System.out.println("\nUser sent some corrupted data...");
			}
		} while (true);

	}

	/**
	 * Close streams and sockets after the connection is cut
	 */
	private void closeSockets() {
		System.out.println("\nClosing Connections... \n");
		try {
			output.close();
			input.close();
			connection.close();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
	
	/**
	 * This method sends the message to our peers or clients
	 */
	private void sendMessage(String message) {
		try {
			// Sends the message through the output stream
			output.writeObject("CLIENT - " + message);
			output.flush();
			System.out.println("\nCLIENT - " + message);		
		} catch (IOException ioexception) {
			System.out.println("\nERROR: Message was not sent...\n");
		}
	}
	

	
}
