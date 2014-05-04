
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Server extends JFrame {

	private JTextField userText;
	private JTextArea chatWindow;
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
		super("Hashim's instant Messenger");
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
					whileChatting();
					
				} catch (EOFException eofException) {
					showMessage("\n Server Closed the Connection");
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
		showMessage("Waiting for someone to connect... \n");
		connection = server.accept();
		node.serverOccupied();
		showMessage("Connection Established. Now connected to "
				+ connection.getInetAddress().getHostName() + "\n");
	}

	private void setupStreams() throws IOException {
		// Creating the path to another computer
		// Send Data structures
		output = new ObjectOutputStream(connection.getOutputStream());
		// Receive Data structures
		input = new ObjectInputStream(connection.getInputStream());
		// No need to flush (Other PC does that)
		showMessage("\nStreams are now setup \n");

	}

	/**
	 * Code running during the established connection after all the backend work
	 * Data transfer etc.
	 * 
	 * @throws IOException
	 */
	private void whileChatting() throws IOException {
		String message = "You are now connected! ";
		//sendMessage(message);
		ableToType(true); // allows the user to type stuff in the textbox
		do {
			// Have a connection
			try {
				opponent = (Player) input.readObject(); // Read incomming stream
				//showMessage("\n" + "Opponent name : "+opponent.name+", The opponent position : "+opponent.positionX+"," + opponent.positionY);
				//mcg.updatePlayer(opponent);
			} catch (ClassNotFoundException cnfException) {
				showMessage("\n User sent some corrupted data...");
			}
		} while (true);

	}

	/**
	 * Close streams and sockets after the connection is cut
	 */
	private void closeSockets() {
		showMessage("\n Closing Connections... \n");
		ableToType(false);
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
			showMessage("\nSERVER - " + message);		
		} catch (IOException ioexception) {
			chatWindow.append("\nERROR: Message was not sent...\n");
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
			chatWindow.append("\nERROR: Message was not sent...\n");
		}
	}
	
	/**
	 * Display messages in the GUI for the user to see.
	 * Using a separate thread to handle GUI Invocations 
	 */
	private void showMessage(final String text) {
		SwingUtilities.invokeLater(
				new Runnable(){ // Create a thread to update the GUI
					public void run(){
						chatWindow.append(text);
					}
				}
		);
	}
	
	private void ableToType(final boolean editable) {
		SwingUtilities.invokeLater(
				new Runnable(){ // Create a thread to update the GUI
					public void run(){
						userText.setEditable(editable);
					}
				}
		);
	}

	public InetAddress getIPAddress() {
		return server.getInetAddress();
	}

}
