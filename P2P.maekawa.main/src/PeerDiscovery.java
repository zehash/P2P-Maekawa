import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main PeerDiscovery class is responsible for creating a running
 * PeerDiscoveryNetwork at a designated Socket, making a thread pool, and
 * maintaining it. Nodes will connect to this network to locate each other via a
 * IPLookup table built into this class. Nodes will maintain their connection to
 * this network in order to use communicate with each other via the lookup
 * table...
 * 
 * @author Hash
 * 
 */
public class PeerDiscovery {
	static int activeNodes = 0; // active Nodes online
	private static int reqcount = 0; // number of requests handled by server
	public static final int PORT = 13360;
	static ArrayList<Task> Threads = new ArrayList<>(10); // ArrayList to manage
															// Node
															// multi-threading
	public static ArrayList<PeerDiscoveryPacket> nodeTable = new ArrayList<>();
	public ExecutorService threadexecutor = Executors.newFixedThreadPool(10);

	public static void main(String[] args) throws IOException {
		System.out.println("Your IP Address : "+InetAddress.getLocalHost().getHostAddress());
		new PeerDiscovery().runPeerDiscovery();
	}

	/**
	 * This method starts up the thread pool and Server operations including
	 * creation of Sockets and issuance of Tasks(connections with Nodes) to
	 * threads
	 * 
	 * @throws IOException
	 *             in case of input/output stream errors.
	 */
	public void runPeerDiscovery() throws IOException {
		int count = 0;
		ServerSocket peerDiscovery = new ServerSocket(PORT, 100); // start
																	// server at
																	// a
																	// designated
																	// port #
		System.out
				.println("PEERDISCOVERY>>> Server up and ready for connections...\n");
		do {
			try { // accepting the connection - we could try breaking this up
					// we will create up to 10 threads
				Socket connection = peerDiscovery.accept();
				System.out
						.println("PEERDISCOVERY>>> Connection recieved from IP: "
								+ connection.getInetAddress().getHostName());

				// thread executor sends the connection socket to an existing
				// thread
				threadexecutor.execute(new Task(connection, count));
				count++;
			} catch (IOException ioException) {
				System.out
						.println("PEERDISCOVERY>>> Failed to connect to Node");
			}

		} while (true);
	}

	/**
	 * Task is an inner class, within the Server, which is responsible for
	 * managing thread operations.
	 * 
	 * Each thread will cater to a separate Node connection and store their
	 * information inside a static ArrayList. Furthermore, the Task class will
	 * broadcast IPaddresses of all connected nodes on the network to all other
	 * connected Nodes periodically.
	 * 
	 * @author Hash
	 * 
	 */
	class Task implements Runnable {
		private Socket connection; // socket connection
		private int id; // thread id
		private ObjectOutputStream output; // To Node from server
		private ObjectInputStream input; // From Node to server
		private boolean FOUND = false;

		public Task(Socket connection, int id) {
			this.connection = connection;
			this.id = id;
		}

		/**
		 * This method runs after a Task object has been created. It adds all
		 * tasks to a static ArrayList created earlier and proceeds to respond
		 * to Node requests
		 */
		public void run() {
			Threads.add(this); // add Tasks to an ArrayList of tasks 
			try { // try to read the Nodes message

				getStreams(); // get input/output data streams
				activeNodes++;
				System.out.println("PEERDISCOVERY>>> PeerDiscovery now has "
						+ activeNodes + " active Node(s) online\n");

			} catch (IOException e) {
				System.out
						.println("PEERDISCOVERY>>> PeerDiscovery unable to pick up messages from Node using thread A0"
								+ id);
			}

			try { // read message and display it
				do {
					processConnection();
				} while (true);
			} catch (EOFException eofException) {
				System.out
						.println("PEERDISCOVERY>>> Server terminated connection");
			} catch (ClassNotFoundException classNotFoundException) {
				System.out
						.println("PEERDISCOVERY>>> Unknown object type recieved");
			} // end catch
			catch (IOException e) {
				System.out.println("PEERDISCOVERY>>> Connection with Node IP "
						+ connection.getInetAddress().getHostName()
						+ " data streams lost or corrupt...");
			} finally {
				
				closeConnection();
				removeData(this.connection.getInetAddress().toString());
				Threads.remove(this);
				reqcount++;
				activeNodes--;
				System.out
						.println("PEERDISCOVERY>>> "
								+ reqcount
								+ " number of requests handled since it started.\nPEERDISCOVERY>>> Network now has "
								+ activeNodes
								+ " active Node(s)/Peer(s) online.");
			}
		}

		
		/**
		 * Get the data streams from the Node
		 * 
		 * @throws IOException
		 *             in case of stream read and write error
		 */
		private void getStreams() throws IOException {

			// set up output stream for objects
			output = new ObjectOutputStream(connection.getOutputStream());
			output.flush();

			// set up input streams for objects
			input = new ObjectInputStream(connection.getInputStream());

		} // end method getStreams

		/**
		 * Read data streams. *
		 * 
		 * 'Input' reads in node connections periodically and stores any
		 * new/updated information into its NodeTable as a PeerDiscoveryPacket.
		 * 'Output' reads PeerDiscoveryPackets within the stored NodeTable and
		 * periodically MultiCasts them to all connected nodes.
		 * 
		 * @throws IOException
		 *             in case of stream read and write errors
		 * @throws ClassNotFoundException
		 *             in case an unknown data type is received
		 */
		private void processConnection() throws IOException,
				ClassNotFoundException {
			try {
				PeerDiscoveryPacket packetData = (PeerDiscoveryPacket) input
						.readObject();
				
				FOUND = false;
				for (int h = 0; h < nodeTable.size(); h++) {
					System.out.println("PEERDISCOVERY>>> Im here");
					if (packetData.getIP().compareTo(nodeTable.get(h).getIP()) == 0) {
						nodeTable.set(h, packetData);
						FOUND = true;
						System.out
								.println("PEERDISCOVERY>>> Packet Data Updated - IP address of Packet is "
										+ packetData.getIP());
					}
				}
				if (FOUND != true) {
					nodeTable.add(packetData); // add new packet
				}
				
				prepareToSendDataToPeers();
				
			} // end try
			catch (ClassNotFoundException classNotFoundException) {
				System.out
						.println("\nPEERDISCOVERY>>> Unknown object type recieved");
			}

		}// end processConnection

		/**
		 * Scan for all available active Node(s)/Peer(s) and get ready to send 
		 * them new/updated broadcasted data 
		 */
		public void prepareToSendDataToPeers() {
			for (int i = 0; i < Threads.size(); i++) {
				for (int j = 0; j < nodeTable.size(); j++) {
					Threads.get(i).broadcastDataToPeers(nodeTable.get(j));
				}
			}
		}
		
		
		/**
		 * This method allows for all connected Nodes to receive discovery data
		 * about each other in real time
		 * 
		 * @param peerDiscoveryPacket
		 *            The Packet we want to show all Nodes.
		 */
		private void broadcastDataToPeers(PeerDiscoveryPacket peerDiscoveryPacket) {
			try { 
				
				System.out
						.println("PEERDISCOVERY>>> Transmitting DATA PACKETS to PEERS "
								+ peerDiscoveryPacket.getIP() + " Nodetable.size = " + nodeTable.size() + " ServerStat " +
								peerDiscoveryPacket.getServerStatus() + " AND ClientStat " + peerDiscoveryPacket.getClientStatus() );
				
				peerDiscoveryPacket.setPeerNumber(nodeTable.size());
				output.writeObject(peerDiscoveryPacket);
				output.flush(); // flush output to Node
			} 
			catch (IOException ioException) {
				System.out.println("PEERDISCOVERY>>> Error writing object to clients ...");
			} 
		}

		/**
		 * Removes Node/Peer from the nodeTables storage whenever the Node/Peer
		 * disconnects from the discovery service. Next it sends updated
		 * information on who's connected to the network to the remaining peers
		 * 
		 */
		private void removeData(String closingConnectionIP) {
			for(int i = 0; i < nodeTable.size(); i++) {
				if(closingConnectionIP.compareTo(nodeTable.get(i).getIP()) == 0){
					nodeTable.remove(i);
				}
			}
			
			prepareToSendDataToPeers();
		}
		
		/**
		 * Terminate the connection with a Node and close input and output
		 * streams before closing the socket and returning the thread back to
		 * its pool
		 */
		private void closeConnection() {
			System.out.println("PEERDISCOVERY>>> Terminating connection\n");
			try {
				output.close(); // close output stream
				input.close(); // close input stream
				connection.close(); // close socket
			} // end try
			catch (IOException ioException) {
				System.out
						.println("PEERDISCOVERY>>> Errors found flushing streams - Forcing close now...");
			} // end catch
		} // end method closeConnection
	} // End Task class
}// end PeerDiscovery class