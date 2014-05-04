
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Client extends JFrame {

	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output; // goes away from you
	private ObjectInputStream input; // goes to you
	private String message = "";
	private String serverIP;
	//private Node   Peer; 	
	/** Basic socket connection */
	private Socket connection;
	private PeerDiscoveryPacket packet;
	private MusicalChairGame mcg;
	
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
			Thread t1 = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						listenClient(); // Allows messaging back and forth
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			});
			Thread t2 = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						while (true)
						{
							sendMessage(); // Allows messaging back and forth
							Thread.sleep(1000);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}}
				
			});
			t1.start();
			t2.start();
		} catch (EOFException eofException) {
			showMessage("\n Client Closed the Connection");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
		//	closeSockets();
		}
	}
	
	public void sendMessage() {
		try {
			// Sends the message through the output stream
			output.writeObject(packet);
			output.flush();
			output.reset();
			//showMessage("\n Sending a packet : "+packet.getIP());
		} catch (IOException ioexception) {
			chatWindow.append("\nERROR: Message was not sent...\n");
		}
	}

	/**
	 * Connect to a server if such a server is available.
	 * Else make the connection wait for a server and activate the server using the node. 
	 */
	private void connectToServer() throws IOException {
		showMessage("Connecting to a server... \n");
		connection = new Socket(InetAddress.getByName(serverIP), 1234);
	}	
	
	private void setupStreams() throws IOException {
		// Creating the path to another computer
		// Send Data structures
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
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
	private void listenClient() throws IOException {
		ableToType(true); // allows the user to type stuff in the textbox
		PeerDiscoveryPacket message;
		do {
			// Have a connection
			try {
				message = (PeerDiscoveryPacket) input.readObject(); // Read incomming stream
				showMessage("\n" + "IPAddress : "+message.getIP()+"Server : "+message.getServerStatus()+"Client : "+message.getClientStatus());

			} catch (ClassNotFoundException cnfException) {
				showMessage("\nUser sent some corrupted data...");
			}
		} while (true);

	}

	/**
	 * Close streams and sockets after the connection is cut
	 */
	private void closeSockets() {
		showMessage("\nClosing Connections... \n");
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
			output.writeObject("CLIENT - " + message);
			output.flush();
			showMessage("\nCLIENT - " + message);		
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

	
}
