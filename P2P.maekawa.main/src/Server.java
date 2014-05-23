
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/*Server class is responsible for being a server and implements server activity,
 * starts from opening a connection and waiting the connection to wait.
 * In this game, we should allow only one node that can connect to the game
 */

public class Server {

	private ObjectOutputStream output = null; // goes away from you
	private ObjectInputStream input; // goes to you
	private ServerSocket server; // accepts and sends back connections
	private MusicalChairGame mcg;
	private Node node;
	private NodePacket nodePacketRecv;
	private NodePacket nodePacketSend;
	private MessagePacket messagePacketRecv;
	private MessagePacket messagePacketSend;
	
	/** Basic socket connection */
	private Socket connection;

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
	
	/*Send all players method is to send all of the opponent information to the connected
	 * node. 
	 */
	private void sendAllOpponentsInformation() {

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

	/*Setting up the streams*/
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
		sendAllOpponentsInformation();
	}

	/**
	 * Code running during the established connection after all the backend work
	 * Data transfer etc.
	 * 
	 * @throws IOException
	 */
	private void readyListen() throws IOException {
		Object messageRecv;
		do {
			// Have a connection
			try {
				messageRecv = input.readObject();
				if (messageRecv instanceof NodePacket)
				{
					nodePacketRecv = (NodePacket) messageRecv; // Read incomming stream
					mcg.updatePlayer(nodePacketRecv);
					mcg.node.sendToLeft(nodePacketRecv);
				}
				else
				{
					if (messageRecv instanceof MessagePacket) {
						messagePacketRecv = (MessagePacket) messageRecv;
						if (messagePacketRecv.getMessage().equals("READY")) {
							if (mcg.isGameDecisionMaker())
								mcg.receiveReadyStatus(messagePacketRecv.getIP());
							else
								node.sendMessageLeft(messagePacketRecv);
							System.out.println("Receive Message");
						}
					}
				}
			} catch (ClassNotFoundException cnfException) {
				System.out.println("\n User sent some corrupted data...");
			}
		} while (true);

	}
	
	/*Sending a player information to the connected node. The server can only send a messge if
	 * there is a client connection*/
	public void sendPlayer(NodePacket playerPacket){
		if (output != null) {
			try {
				output.writeObject(playerPacket);
				output.flush();
				output.reset();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*Sending a Message information to the connected node. The server can only send a message if
	 * there is a client connection*/
	public void sendMessage(MessagePacket messagePacket){
		if (output != null) {
			try {
				output.writeObject(messagePacket);
				output.flush();
				output.reset();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	
}
