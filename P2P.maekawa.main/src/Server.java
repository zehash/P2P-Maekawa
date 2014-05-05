
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Server {

	private ObjectOutputStream output = null; // goes away from you
	private ObjectInputStream input; // goes to you
	private ServerSocket server; // accepts and sends back connections
	private MusicalChairGame mcg;
	//private Player opponent;
	private Node node;
	private NodePacket nodePacketRecv;
	private NodePacket nodePacketSend;
	//private Node Peer;
	
	/** Basic socket connection */
	private Socket connection;

	/*public static void main(String[] args) {
		Server Test = new Server();
		Test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Test.startServer();
	}*/
	
	public Server(MusicalChairGame mcg, String port, Node node) {
		System.out.println("Server is up!");
		this.mcg = mcg;
		this.node = node;
	}

	/**
	 * Set up and run the server The port number its on and how many connections
	 * can wait on it.
	 */
	public void startServer() {
		try {
			server = new ServerSocket(1234, 100);
			while (true) {
				try {
					waitforConnection(); // Wait until a connection is made
					setupStreams();
					readyListen();
					
				} catch (EOFException eofException) {
					System.out.println("\n Server Closed the Connection");
				} finally {
					//closeSockets();
				}
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

	}

	/**
	 * Wait for a connection, check the Node if the IP Address of the client exists 
	 * add to list, connect to node, and display success message if it doesn't
	 * else cut the connection and wait for another connection. 
	 */
	private void waitforConnection() throws IOException {
		System.out.println("Waiting for someone to connect... \n");
		connection = server.accept();
		node.serverOccupied();
		System.out.println("Connection Established. Now connected to "
				+ connection.getInetAddress().getHostName() + "\n");
	}
	
	private void sendAllPlayers() {

		for (int i = 0; i < mcg.opponents.size(); i++)
		{
			try {
				nodePacketSend = new NodePacket(mcg.opponents.get(i).name, mcg.opponents.get(i).positionX, mcg.opponents.get(i).positionY);
				sendPlayer(nodePacketSend);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void setupStreams() throws IOException {
		// Creating the path to another computer
		// Send Data structures
		output = new ObjectOutputStream(connection.getOutputStream());
		// Receive Data structures
		input = new ObjectInputStream(connection.getInputStream());
		// No need to flush (Other PC does that)
		System.out.println("\nStreams are now setup \n");
		nodePacketSend = new NodePacket(mcg.mainplayer.name, mcg.mainplayer.positionX, mcg.mainplayer.positionY);
		sendPlayer(nodePacketSend);
		sendAllPlayers();
	}

	/**
	 * Code running during the established connection after all the backend work
	 * Data transfer etc.
	 * 
	 * @throws IOException
	 */
	private void readyListen() throws IOException {
		//sendMessage(message);
		do {
			// Have a connection
			try {
				nodePacketRecv = (NodePacket) input.readObject(); // Read incomming stream
				//opponent = new Player(Color.RED, nodePacketRecv.getPositionX(), nodePacketRecv.getPositionY());
				//opponent.name = nodePacketRecv.getName();
				//showMessage("\n" + "Opponent name : "+opponent.name+", The opponent position : "+opponent.positionX+"," + opponent.positionY);
				mcg.updatePlayer(nodePacketRecv);
				mcg.node.sendToLeftt(nodePacketRecv);
			} catch (ClassNotFoundException cnfException) {
				System.out.println("\n User sent some corrupted data...");
			}
		} while (true);

	}
	
	/*public void sendPlayer(Player player){
		if (output != null) {
			try {
				nodePacketSend = new NodePacket(player.name, player.positionX, player.positionY);
				output.writeObject(nodePacketSend);
				output.flush();
				output.reset();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	 * Close streams and sockets after the connection is cut
	 */
	private void closeSockets() {
		System.out.println("\n Closing Connections... \n");
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
			output.writeObject("SERVER - " + message);
			output.flush();
			System.out.println("\nSERVER - " + message);		
		} catch (IOException ioexception) {
			System.out.println("\nERROR: Message was not sent...\n");
		}
	}
	
}
