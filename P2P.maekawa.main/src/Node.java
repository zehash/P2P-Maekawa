import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import may.ResultsHM;
import may.State;
import may.Vote;

/*Node Class is responsible to give the program a property as a node in P2P application
 * The node will handle a connection from a Peer Discovery, Server, and Client
 * Server and Client will run on different Threads
 */

public class Node extends JFrame {

	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream outputPD; // Sends data Packets to PD
	private ObjectInputStream inputPD; // Gets Data Packets from PD
	
	private String message = "";
	private String serverIP;
	/** Basic socket connection */
	private Socket connectionPeerD;
	private Server rightConnector = null; //Server variable
	private Client leftConnector = null; //Client variable
	private PeerDiscoveryPacket packet; // Packets sent to PD
	private PeerDiscoveryPacket messageRecvPD; // Packets received from PD
	private MusicalChairGame mcg;
	private Node node = this;
	private boolean isConnectedAsClient = false;
	private boolean neverBeClient = false;

	///////////////////////////
	
    public State myState; //which chair the node wants
    public ArrayList<State> receivedState = new ArrayList<State>(); //ideally an uptodate list of all node states
    public ArrayList<Vote> votes = new ArrayList<Vote>(); //circulating votes
    public HashMap<Integer,Queue<String>> myVotes = new HashMap<Integer,Queue<String>>();
    public ArrayList<String> alreadyReleased = new ArrayList<String>(); //nodes ive already received release state confirmation from
    public ArrayList<String> alreadyVoted = new ArrayList<String>(); //nodes ive already voted for
    public int k; //number of votes required to win chair
    public int myK = 0; //my votes received
    public String myIP; //my ip address
    public ArrayList<String> whoVotedForMe = new ArrayList<String>(); //nodes who sent vote to me
    public ResultsHM results = new ResultsHM();
    
    /*
    Getting a number of players : mcg.numOpponent+1
    
    */
	
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
		try {
            myIP = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }

	}

	/**
	 * Set up and run the Peer Discovery connection. The listen state is run in another Thread
	 * So the peer discovery will not be blocked while listening to the connection
	 */
	public void startPeerDiscoveryConnection() {
		try {
			connectToPeerDiscovery(); // Connect to a Server
			setupStreamsPD(); // Creates data streams
			Thread t1 = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						listenPD(); // Allows messaging back and forth
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			});
			t1.start();
		} catch (EOFException eofException) {
			showMessage("\n Client Closed the Connection");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}	
	
	/*
	 * This method is to send a Peer Discovery Packet to the Peer Discovery Server
	 * of which reporting the status of the node
	 */
	public void sendMessagePD() {
		try {
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
			sendMessagePD();
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
				{
					neverBeClient = true;
					mcg.asGameDecisionMaker();
				}
				if (mcg.isMainPlayerCreated == false) {
					mcg.createMainPlayer(mcg.setPlayerColor(messageRecvPD.getPeerNumber()-1));
					mcg.launchGame();
				}
				if ((isConnectedAsClient == false) && (neverBeClient == false) && (messageRecvPD.getServerStatus() == true) && !(messageRecvPD.getIP().equals(Inet4Address.getLocalHost().getHostAddress())))
					{
					System.out.println("isConnectedAsClient : "+isConnectedAsClient+", Neverbeclient : "+neverBeClient+"Message IP : "+messageRecvPD.getIP()+", Packet PeerNumber : "+messageRecvPD.getPeerNumber());
					leftConnector = new Client(messageRecvPD.getIP(), mcg, this);
					
						try {
							Thread clientThread = new Thread(new Runnable() {
								@Override
								public void run() {
									try {
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
			} catch (Exception e1) {
				e1.printStackTrace();
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
	
	/*Send NodePacket from the server*/
	public void sendToRight(NodePacket playerPacket) {
		if (rightConnector != null) {
			rightConnector.sendPlayer(playerPacket);
		}
	}
	
	/*Send MessagePacket from the server*/
	public void sendMessageRight(MessagePacket messagePacket) {
		if (rightConnector != null) {
			rightConnector.sendMessage(messagePacket);
		}
	}
	
	/*Send State Packet from the server*/
    public void sendStateRight(State state) {
        if (rightConnector != null) {
            rightConnector.sendState(state);
        }
    }
    
    /*Send Vote Packet from the server*/
    public void sendVoteRight(Vote vote) {
        if (rightConnector != null) {
            rightConnector.sendVote(vote);
        }
    }

	/*Send NodePacket from the client*/
	public void sendToLeft(NodePacket playerPacket) {
		if (leftConnector != null) {
			leftConnector.sendPlayer(playerPacket);
		}
	}
	
	/*Send MessagePacket from the client*/
	public void sendMessageLeft(MessagePacket messagePacket) {
		if (leftConnector != null) {
			leftConnector.sendMessage(messagePacket);
		}
	}
	
	/*Send State Packet from the client*/
    public void sendStateLeft(State state) {
        if (leftConnector != null) {
            leftConnector.sendState(state);
        }
    }
    
    /*Send Vote Packet from the client*/
    public void sendVoteLeft(Vote vote) {
        if (leftConnector != null) {
            leftConnector.sendVote(vote);
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
	
	public void setK() {
	    k = ((mcg.numOpponent+1)/2)+1;
	}
	
	/////////////////////////////
    
    //check if a neighbour [wants || has released] chair
    public void receiveNeighbourState(State state) {
    	int status = state.getStatus();
    	String ip = state.getIP();
    	int chair = state.getChair();
    	System.out.println("received state: " + ip + " " + status + " " + chair); //DEBUG
    	//if RELEASED
    	if (status == 1) {
	    	boolean ignore = false;
	    	//if i already ack the release, do nothing
	    	for (String s : alreadyReleased) {
	    		if (ip.equals(s)) {
	    			ignore = true;
	    			break;
	    		}
	    	}
	    	//must release vote, and vote for next in queue
	    	if (ignore == false) {
	    		results.checkThenAdd(state);
	    		try {
	    			//if i had voted for this node...
		    		Queue<String> q = myVotes.get(chair);
		    		//if queue is empty, i never voted
		    		if (q.size() == 0) {
		    			//the k value was reached and i hadnt voted for node
		    			alreadyReleased.add(ip); //pretend i received ack for my 'vote'
		    			alreadyVoted.add(ip);
		    		}
		    		else {
		    			String firstE = q.element();
			    		//...acknowledge release
			    		if (firstE.equals(ip)) {
				        	q.poll(); //remove first element
				        	alreadyReleased.add(ip);
				        	if (q.size() == 0) {
				        		myVotes.put(chair,q); //update
				        	}
				        	else {
					        	//if there are queued requests
					        	String secondE = q.element();
					        	sendVote(secondE, chair); //send vote for next in queue
					        	alreadyVoted.add(secondE);
					        	myVotes.put(chair,q); //update
				        	}
			    		}
			    		//the k value was reached and i hadnt voted for node
			    		else {
			    			alreadyReleased.add(ip); //pretend i received ack for my 'vote'
			    			alreadyVoted.add(ip);
			    		}
		    		}
		    		
	    		} catch (NullPointerException npe) {
	    			//the k value was reached and i hadnt voted for node
	    			alreadyReleased.add(ip); //pretend i received ack for my 'vote'
	    			alreadyVoted.add(ip);
	    		}
	    	}
    	}
    	//if WANTED
    	//check if ive already voted
    	else {
    	//System.out.println("wanted"); //DEBUG
    		boolean ignore = false;
    		//have i already voted?
    		for(String s : alreadyVoted) {
    			if (ip.equals(s)) {
    				ignore = true;
    				break;
    			}
    		}
    		if (ignore == false) {
    			//for that chair, am i waiting for a release first?
    			try {
    				//System.out.println("wanted - no exception"); //DEBUG
    				Queue<String> q = myVotes.get(chair);
    				//if queue is empty, send vote immediately, wait for release
    				if (q.size() == 0) {
    					sendVote(ip, chair);
        				alreadyVoted.add(ip);
        				q.add(ip);
        				myVotes.put(chair, q);
    				}
    				else {
    					//add request to queue (already waiting for ack for this chair)
    					q.add(ip);
        				myVotes.put(chair,q);
    				}
    			} catch (NullPointerException npe) {
    				//System.out.println("wanted - there was an exception"); //DEBUG
    				//the queue for this chair is empty, give vote, wait for release
    				sendVote(ip, chair);
    				alreadyVoted.add(ip);
    				Queue<String> q = new LinkedList<String>();
    				q.add(ip);
    				myVotes.put(chair, q);
    			}
    		}
    	}
    }
    
    //send vote to neighbours
    public void sendVote(String ip, Integer chair) {
    	//receiving a vote from self
    	if (myIP.equals(ip)) {
    		receiveVote(new Vote(myIP, myIP, chair));
    	}
    	else {
    		Vote vote = new Vote(myIP, ip, chair);
    		if (leftConnector != null) {
    		    leftConnector.sendVote(vote);
    		}
    		if (rightConnector != null) {
                rightConnector.sendVote(vote);
            }
    	}
    }
    
    //receive vote from neighbour
    public void receiveVote(Vote vote) {
    	String whoVoted = vote.getI();
    	String votedFor = vote.getVoteFor();
    	int chair = vote.getChair();
    	//if the vote is for me...
    	if (votedFor.equals(myIP)) {
    		boolean alreadyCounted = false;
    		for (String s : whoVotedForMe) {
    			//if the vote has already been counted... do nothing
    			if (whoVoted.equals(s)) {
    				alreadyCounted = true;
    				break;
    			}
    		}
    		//if the vote has not been counted...
    		if (alreadyCounted == false) {
    			myK++;
    			whoVotedForMe.add(whoVoted);
    			if (myK == k) {
    				updateMyState(1); //occupy then release chair
    			}
    			
    		}
    	} //do nothing if vote is not relevant to me	
    }
    
    //change state from Wanted 0 to Released 1
    public void updateMyState(int status) {
    	myState.setStatus(status);
    	receiveNeighbourState(myState); //vote for self if necessary
    	
    }
    
    //send state to neighbours
    public void sendState(State state) {
        if (leftConnector != null)
            leftConnector.sendState(state);
        if (rightConnector != null)
            rightConnector.sendState(state);
    }
    
    //when voting round begins, call this method when i touch chair
    public void iWantChair(int chair) {
    	myState = new State(myIP, 0, chair);
    	updateMyState(0);
    	sendState(myState);
    }
    
    //TODO tidy
    public void endRound () {
    	myState = null;
    	receivedState = new ArrayList<State>();
    	//votes
    	myVotes = new HashMap<Integer,Queue<String>>();
    	alreadyReleased = new ArrayList<String>();
    	alreadyVoted = new ArrayList<String>();
    	//k
    	//myK
    	whoVotedForMe = new ArrayList<String>();
    	//results
    	
    }
    
    //print small values
    public void debugSmallValues() {
        System.out.println("k : " + k);
        System.out.println("myK : " + myK);
        System.out.println("myIP : " + myIP);
        
    }
    
    //print myState
    public void debugMyState() {
        System.out.println("myState - IP: " + myState.getIP() + ", status: " + myState.getStatus() + ", chair: " + myState.getChair());
    }
    
    //print myVotes
    public void debugMyVotes() {
        System.out.println("myVotes");
        Iterator iter = myVotes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry mEntry = (Map.Entry) iter.next();
            System.out.println(mEntry.getKey() + " : " + mEntry.getValue());
        }
    }
    
    //print myVotes
    public void debugMyVotes2() {
        System.out.println("myVotes");
        for (int key : myVotes.keySet() ) {
            System.out.println(key);
            Queue<String> q = myVotes.get(key);
            for (String s : q) {
                System.out.println(" " + s + " ");
            }
        }
    }
    
    //print alreadyReleased values
    public void debugAlreadyReleased() {
        System.out.println("already released");
        for (String s : alreadyReleased) {
            System.out.println(" " + s + " ");
        }
    }
    
    //print alreadyVoted values
    public void debugAlreadyVoted() {
        System.out.println("already voted");
        for (String s : alreadyVoted) {
            System.out.println(" " + s + " ");
        }
    }
    
    //print whoVotedForMe values
    public void debugWhoVotedForMe() {
        System.out.println("who voted for me");
        for (String s : whoVotedForMe) {
            System.out.println(" " + s + " ");
        }
    }
    
    //print results
    public void debugResults() {
        System.out.println("results");
        HashMap<Integer,Queue<String>> r = results.getHM();
        for (int key : r.keySet() ) {
            System.out.println(key);
            Queue<String> q = r.get(key);
            for (String s : q) {
                System.out.println(" " + s + " ");
            }
        }
    }
}
