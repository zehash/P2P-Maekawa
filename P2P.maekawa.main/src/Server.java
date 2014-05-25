
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import may.State;
import may.Vote;

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
		    if (server == null)
		        server = new ServerSocket(1234, 100);
		    output = null;
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
		    System.out.println("\n Connection lost to other");
		    node.serverFree = true;
		    try {
		        node.setPacket(InetAddress.getLocalHost().getHostAddress(), 0,node.isConnectedAsClient, node.serverFree);
		    } catch (Exception e) {
		        System.out.println("Address Error");
		    }
            node.sendMessagePD();
		    node.startNodeServer();
		    //ioException.printStackTrace();
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
				nodePacketSend.setColor(mcg.opponents.get(i).color);
				sendPlayer(nodePacketSend);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*Send all chairs method is to send all of the chair information to the connected
	 * node. 
	 */
	private void sendAllChairsInformation() {

		for (int i = 0; i < mcg.chairs.size(); i++)
		{
			try {
				nodePacketSend = new NodePacket("chair "+i, mcg.chairs.get(i).positionX, mcg.chairs.get(i).positionY);
				sendChairInfo(nodePacketSend);
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
		nodePacketSend.setColor(mcg.mainplayer.color);
		sendPlayer(nodePacketSend);
		sendAllOpponentsInformation();
		sendAllChairsInformation();
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
					node.sendToLeft(nodePacketRecv);
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
					else {
                        if (messageRecv instanceof State) {
                            State incoming_state = (State) messageRecv;
                            //System.out.println("Incoming state from : "+incoming_state.getIP());
                            node.receiveNeighbourState(incoming_state);
                            node.sendStateLeft(incoming_state);
                        }
                        else {
                            if (messageRecv instanceof Vote) {
                                Vote incoming_vote = (Vote) messageRecv;
                                //System.out.println("Incoming state from : "+incoming_state.getIP());
                                node.receiveVote(incoming_vote);
                                node.sendVoteLeft(incoming_vote);
                            }
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
	
	/*Sending a chair information to the connected node. The server can only send a messge if
	 * there is a client connection*/
	public void sendChairInfo(NodePacket chairPacket){
		if (output != null) {
			try {
				output.writeObject(chairPacket);
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
	
	/*Sending a State information to the connected node. The server can only send a message if
     * there is a client connection*/
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
    
    /* Sending a Vote information to the connected node. The server can only send a message if
     * there is a client connection*/
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
