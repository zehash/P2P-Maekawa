import java.awt.BorderLayout;
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

public class Node extends JFrame {

	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream outputPD; // Sends data Packets to PD
	private ObjectInputStream inputPD; // Gets Data Packets from PD
	
	private String message = "";
	private String serverIP;
	/** Basic socket connection */
	private Socket connectionPeerD;
	private Server rightConnector;
	private Client leftConnector;
	private PeerDiscoveryPacket packet; // Packets sent to PD
	private PeerDiscoveryPacket messageRecvPD; // Packets received from PD
	private MusicalChairGame mcg;
	private Node node = this;
	private boolean isConnectedAsClient = false;
	private boolean neverBeClient = false;

	
	// Constructor
	public Node(String host, MusicalChairGame mcg) throws Exception {
		super("Client - ");
		serverIP = host;
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				sendMessage(event.getActionCommand());
				userText.setText("");
			}
		});
		add(userText, BorderLayout.NORTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		setSize(400, 150);
		setVisible(true);
		this.mcg = mcg;
		packet = new PeerDiscoveryPacket(InetAddress.getLocalHost().getHostAddress(), 0, true, true);
	}

	/**
	 * Set up and run the Client Side The port number its on and how many connections
	 * can wait on it.
	 */
	public void startPeerDiscoveryConnection() {
		try {
			connectToPeerDiscovery(); // Connect to a Server
			setupStreamsPD(); // Creates data streams
			//listenPD();
			Thread t1 = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						listenPD(); // Allows messaging back and forth
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			});
			/*Thread t2 = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						while (true)
						{
							sendMessagePD(); // Allows messaging back and forth
							Thread.sleep(1000);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}}
				
			});*/
			t1.start();
		} catch (EOFException eofException) {
			showMessage("\n Client Closed the Connection");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}	
	
	public void sendMessagePD() {
		try {
			// Sends the message through the output stream
			outputPD.writeObject(packet);
			outputPD.flush();
			outputPD.reset();
			//showMessage("\n Sending a packet : "+packet.getIP());
		} catch (IOException ioexception) {
			chatWindow.append("\nERROR: Message was not sent...\n");
		}
	}

	/**
	 * Connect to a Peer Discovery server if such a server is available. 
	 */
	private void connectToPeerDiscovery() throws IOException {
		showMessage("Connecting to the Peer Network... \n");
		connectionPeerD = new Socket(InetAddress.getByName(serverIP), 13360);
	}	
	
	/**
	 * Sending to Peer discovery that server-socket of the node is occupied 
	 */
	public void serverOccupied() {
		packet.setServerStatus(false);
		sendMessagePD();
	}
	
	/**
	 * Sets up streams to receive data packets from the PeerDiscovery Network.
	 * Next it turns on its ServerSocket to await incomming connections.
	 * @throws IOException
	 */
	private void setupStreamsPD() throws IOException {
		// Creating the path to another computer
		// Send Data structures
		outputPD = new ObjectOutputStream(connectionPeerD.getOutputStream());
		outputPD.flush();
		// Receive Data structures
		inputPD = new ObjectInputStream(connectionPeerD.getInputStream());
		// No need to flush (Other PC does that)
		showMessage("\nStreams Peer Discovery is now setup \n");
		try {
			Thread serverThread = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						rightConnector = new Server(mcg, "1234", node);
						rightConnector.startServer();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			});
			serverThread.start();
			System.out.println("Sending a packet : "+packet.getIP()+", Server Status : "+packet.getServerStatus());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Code running during the established connection after all the backend work
	 * Data transfer etc.
	 * 
	 * @throws IOException
	 */
	private void listenPD() throws IOException {
		ableToType(true); // allows the user to type stuff in the textbox
		do {
			// Have a connection
			try {
				messageRecvPD = (PeerDiscoveryPacket) inputPD.readObject(); // Read incomming stream
				showMessage("\n" + "IPAddress : "+messageRecvPD.getIP()+"Server : "+messageRecvPD.getServerStatus()+"Client : "+messageRecvPD.getClientStatus());
				if (messageRecvPD.getPeerNumber() == 1) 
					neverBeClient = true;
				if ((isConnectedAsClient == false) && (neverBeClient == false) && (messageRecvPD.getServerStatus() == true) && !(messageRecvPD.getIP().equals(Inet4Address.getLocalHost().getHostAddress())))
					{
					System.out.println("isConnectedAsClient : "+isConnectedAsClient+", Neverbeclient : "+neverBeClient+"Message IP : "+messageRecvPD.getIP()+", Packet PeerNumber : "+messageRecvPD.getPeerNumber());
					
						try {
							Thread clientThread = new Thread(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									try {
										leftConnector = new Client(messageRecvPD.getIP(), mcg);
										System.out.println("Try connecting to "+messageRecvPD.getIP()+", This IP : "+Inet4Address.getLocalHost().getHostAddress());
										leftConnector.startClient();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								
							});
							clientThread.start();
							packet.setClientStatus(false);
							isConnectedAsClient = true;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			catch (ClassNotFoundException cnfException) {
				showMessage("\nUser sent some corrupted data...");
			}
		} while (true);

	}

	/**
	 * Close streams and sockets after the connection is cut
	 */
	private void closeSocketsPD() {
		showMessage("\nClosing Connections... \n");
		ableToType(false);
		try {
			outputPD.close();
			inputPD.close();
			connectionPeerD.close();
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
			outputPD.writeObject("CLIENT - " + message);
			outputPD.flush();
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
