package may;
import java.util.HashMap;
import java.util.Queue;
import java.util.ArrayList;
import java.util.LinkedList;

//TODO where do we put ResultsHM

//keeps a hashmap of order in which nodes have sat on chairs
//node may sit on any chair only once
/**
*
* @author Dan
*/
public class ResultsHM {

    public static HashMap<Integer,Queue<String>> hm = new HashMap<Integer,Queue<String>>();
    public static ArrayList<String> haveEntered = new ArrayList<String>();
    
    public ResultsHM()
    {
    }
    
    public HashMap getHM() {
    	return hm; 
    }

    //enforces rule that: node may enter 1 chair only
    public void checkThenAdd(State state) {
    	String ip = state.getIP();
    	boolean dontAdd = false;
    	for (String a : haveEntered) {
    		if (ip.equals(a)) {
    			dontAdd = true;
    			break;
    			//System.out.println("sorry already added"); //DEBUG
    		}
    	}
    	if (dontAdd == false) {
    		haveEntered.add(ip);
    		addResult(state);
    	}
    }
    
    //adds result to queue for particular chair
    public synchronized void addResult(State state) {
        Integer chair = state.getChair();
        String s = state.getIP();
        try {
        	Queue<String> q = hm.get(chair);
        	q.add(s);
            hm.put(chair,q);
        } catch (NullPointerException npe) {
        	Queue<String> ll = new LinkedList<String>();
        	ll.add(s);
        	hm.put(chair, ll);
        } 
    }
    
    //winners are head of each queue for each chair
    public ArrayList<String> winners() {
        ArrayList<String> winners = new ArrayList<String>();
        for (Integer key : hm.keySet()) {
            winners.add(hm.get(key).poll());
        }
        /*
        for (String s : winners) {
        	System.out.println(s); 
        }
        */
        return winners;
    }
    
    public static void main(String args[]) {
    	new ResultsHM();
    }
    
}