package may;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.LinkedList;

//TODO get k value
//TODO remove k = 2
//TODO does receiving state/votes need multithreading
//

/**
 *
 * @author Dan
 */
public class Node
{
    public State myState; //which chair the node wants
    public ArrayList<State> receivedState = new ArrayList<State>(); //ideally an uptodate list of all node states
    public ArrayList<Vote> votes = new ArrayList<Vote>(); //circulating votes
    public HashMap<Integer,Queue<String>> myVotes = new HashMap<Integer,Queue<String>>();
    public ArrayList<String> alreadyReleased = new ArrayList<String>(); //nodes ive already received release state confirmation from
    public ArrayList<String> alreadyVoted = new ArrayList<String>(); //nodes ive already voted for
    public int k = 2; //number of votes required to win chair
    public int myK = 0; //my votes received
    public String myIP; //my ip address
    public ArrayList<String> whoVotedForMe = new ArrayList<String>(); //nodes who sent vote to me
    public ResultsHM results = new ResultsHM();
    
    public Node()
    {

    }
    
    //set a fake ip
    public void debugSetMyIP(String myIP) {
    	this.myIP = myIP;
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
    
    //TODO connect receiveState to CORD
    //check if a neighbour [wants || has released] chair
    public void receiveNeighbourState(State state) {
    	int status = state.getStatus();
    	String ip = state.getIP();
    	int chair = state.getChair();
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
    		//TODO vote send to everyone through CORD
    	}
    }
    
    //TODO connect receive vote to CORD
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
    	//TODO send state through CORD
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
}
