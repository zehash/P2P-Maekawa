
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.Socket;

import may.State;
import may.Vote;

/* Client class is responsible for being a client and implements client activity,
 * starts from opening a connection to connect to a waiting server.
 * In this game, we should allow only one connection to a server
 */

public class Client {

	private ObjectOutputStream output; // goes away from you
	private ObjectInputStream input; // goes to you
	private String message = "";
	private String serverIP;
	private Node node;
	
	/** Basic socket connection */
	private Socket connection;
	private PeerDiscoveryPacket packet;
	private MusicalChairGame mcg;
	
	private NodePacket nodePacketRecv;
	private NodePacket nodePacketSend;
	private MessagePacket messagePacketRecv;
	
	
	// Constructor
	public Client(String host, MusicalChairGame mcg, Node node) throws Exception {
		serverIP = host;
		this.mcg = mcg;
		this.node = node;
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
	
	/*Send a player information to the server*/
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
	
	/*Sending a Message information to the server side*/
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
	
	/*Sending a State information to the server side*/
    public void sendState(State state){
        if (output != null) {
            try {
                output.writeObject(state);
                output.flush();
                output.reset();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    /*Sending a Vote information to the server side*/
    public void sendVote(Vote vote){
        if (output != null) {
            try {
                output.writeObject(vote);
                output.flush();
                output.reset();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
	
	/*Setting up input and output stream*/
	private void setupStreams() throws IOException {
		// Creating the path to another computer
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		// Receive Data structures
		input = new ObjectInputStream(connection.getInputStream());
		// No need to flush (Other PC does that)
		System.out.println("\nStreams are now setup \n");
		nodePacketSend = new NodePacket(mcg.mainplayer.name, mcg.mainplayer.positionX, mcg.mainplayer.positionY);
		nodePacketSend.setColor(mcg.mainplayer.color);
		sendPlayer(nodePacketSend);
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
					//Check if the packet is for chair information or player
					if (nodePacketRecv.getName().indexOf("chair") > -1) {
						mcg.updateChair(nodePacketRecv);
					}
					else {
						mcg.updatePlayer(nodePacketRecv);
						mcg.node.sendToRight(nodePacketRecv);
					}
				}
				else
				{
					if (messageRecv instanceof MessagePacket) {
						messagePacketRecv = (MessagePacket) messageRecv;
						if (messagePacketRecv.getMessage().equals("START"))
						{
							mcg.startTimer();
							node.sendMessageRight(messagePacketRecv);
						}
					}
					else {
					    if (messageRecv instanceof State) {
					        State incoming_state = (State) messageRecv;
					        //System.out.println("Incoming state from : "+incoming_state.getIP());
					        node.receiveNeighbourState(incoming_state);
					    }
					    else {
					        if (messageRecv instanceof Vote) {
                                Vote incoming_vote = (Vote) messageRecv;
                                //System.out.println("Incoming state from : "+incoming_state.getIP());
                                node.receiveVote(incoming_vote);
					        }
					    }
					}
				}
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
	
}
