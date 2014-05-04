
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Server {

	private ObjectOutputStream output; // goes away from you
	private ObjectInputStream input; // goes to you
	private ServerSocket server; // accepts and sends back connections
	private MusicalChairGame mcg;
	private Player opponent;
	private Node node;

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

	private void setupStreams() throws IOException {
		// Creating the path to another computer
		// Send Data structures
		output = new ObjectOutputStream(connection.getOutputStream());
		// Receive Data structures
		input = new ObjectInputStream(connection.getInputStream());
		// No need to flush (Other PC does that)
		System.out.println("\nStreams are now setup \n");

	}

	/**
	 * Code running during the established connection after all the backend work
	 * Data transfer etc.
	 * 
	 * @throws IOException
	 */
	private void readyListen() throws IOException {
		String message = "You are now connected! ";
		//sendMessage(message);
		do {
			// Have a connection
			try {
				opponent = (Player) input.readObject(); // Read incomming stream
				//showMessage("\n" + "Opponent name : "+opponent.name+", The opponent position : "+opponent.positionX+"," + opponent.positionY);
				//mcg.updatePlayer(opponent);
			} catch (ClassNotFoundException cnfException) {
				System.out.println("\n User sent some corrupted data...");
			}
		} while (true);

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
	
	/**
	 * This method sends the message to our peers or clients
	 */
	public void send(Player message) {
		try {
			// Sends the message through the output stream
			System.out.println("User location : "+message.positionX+","+message.positionY);
			output.writeObject(message);
			output.flush();
			output.reset();		
		} catch (IOException ioexception) {
			System.out.println("\nERROR: Message was not sent...\n");
		}
	}
	
}
