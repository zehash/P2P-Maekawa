/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/** The game will start by initializing the GUI, pre-process the opponent (giving slots)
 *
 * @author Jemie
 */
import static javax.swing.JFrame.EXIT_ON_CLOSE;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class MusicalChairGame {
    JFrame screenGame = new JFrame();
    JLabel timerlabel = new JLabel();
    JLabel gamestatus = new JLabel();
    JButton startGame = new JButton();
    
    Chair chair;
    int numOpponent = 0;
    int movement = 5;
    int initPlayerX = 0; 
    int initPlayerY = 0;
    int[] readyStatus = new int[10];

    /**
     * @param args the command line arguments
     */
    
    public Arena arenaGame;
    public Player mainplayer;
    public MusicalChairGame mcg = this;
    public static ArrayList<Player> opponents = new ArrayList<Player>();
    public Node node;
    public boolean isAllowedToMove = true;
    public boolean isDecisionMaker = false;
    
    public MusicalChairGame() {
	    try {
	        screenGame.setLayout(null);
	        arenaGame = new Arena(screenGame, 600,300);
	        setInitPlayerPosition();
	        mainplayer = new Player(Color.RED, initPlayerX,initPlayerY);
	        mainplayer.name = InetAddress.getLocalHost().getHostAddress();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
    
    /*Make this node as a decision maker*/
    
    public void setScreen() {
        
        JLabel label = new JLabel();
        label.setText("TIMER :");
        label.setBounds(10, 300, 50, 20);
 
        timerlabel.setText("0");
        timerlabel.setBounds(10, 320, 100, 20);
        gamestatus.setText("Game started..");
        gamestatus.setBounds(300,310,100,20);
        startGame.setText("Ready");
        startGame.setBounds(300, 340, 100, 20);
        
        screenGame.getContentPane().add(label);
        screenGame.getContentPane().add(timerlabel);
        screenGame.getContentPane().add(gamestatus);
        screenGame.getContentPane().add(startGame);
        
        
        screenGame.setSize(600,400);
        screenGame.setResizable(false);
        screenGame.setTitle("Musical Chair Game");
        screenGame.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    /*Initialize the game, by setting the arena, set the keyboard listener to player*/
    public void initGame() {
    	for (int i = 0; i < 10; i++)
    		readyStatus[i] = 0;
        setScreen();
        setListenerPlayer();
        setListenerButton();
        setChairAppear();
    }
    
    /*Make the chair appear*/
    public void setChairAppear() {
        Random randomGenerator = new Random();
        int getX = 1;
        while (getX % 5 != 0)
            getX = randomGenerator.nextInt(600-40);
        int getY = 1;
        while (getY % 5 != 0)
            getY = randomGenerator.nextInt(300-40);
        chair = new Chair(getX, getY);
    }
    
    /*Initializing 10 empty opponents (Based on numberOpponentSlotCreated)*/
    public void setInitOpponents() {
    	int numberOpponentSlotCreated = 10;
    	for (int i = 0; i < numberOpponentSlotCreated; i++)
    	{
    		Player opponent = new Player(Color.RED, 1000,1000);
    		opponent.name = "";
        	arenaGame.setPlayerinArena(opponent);
        	opponents.add(opponent);
    	}
    }
    
    /*initialize the main player position at the beginning of the game*/
    public void setInitPlayerPosition() {
        Random randomGenerator = new Random();
        initPlayerX = 1;
        while (initPlayerX % 5 != 0)
        	initPlayerX = randomGenerator.nextInt(600-40);
        initPlayerY = 1;
        while (initPlayerY % 5 != 0)
        	initPlayerY = randomGenerator.nextInt(300-40);
    }
    
    public void setListenerPlayer() {
        mainplayer.setFocusable(true);
        mainplayer.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
               //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                switch (e.getKeyChar()) {
                    case 'w' : mainplayer.movePosition(0, -movement, isAllowedToMove);
                               break;
                    case 'a' : mainplayer.movePosition(-movement, 0, isAllowedToMove);
                               break;
                    case 'd' : mainplayer.movePosition(+movement, 0, isAllowedToMove);
                               break;
                    case 's' : mainplayer.movePosition(0, +movement, isAllowedToMove);
                               break;
                }
                mainplayer.repaint();
	            if (isAllowedToMove) {
	                NodePacket mainPlayerPacket = new NodePacket(mainplayer.name, mainplayer.positionX, mainplayer.positionY);
	                node.sendToLeft(mainPlayerPacket);
	                node.sendToRight(mainPlayerPacket);
	                startDelay();
	            }
	            
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
            //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }
    
    /*Button Listener*/
    public void setListenerButton() {
        startGame.addActionListener(new ActionListener()  {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if (isDecisionMaker)
				{
					if (readyToPlay())
					{
						try {
							MessagePacket message = new MessagePacket(InetAddress.getLocalHost().getHostAddress(), "START");
							node.sendMessageRight(message);
							startTimer();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				else
				{
					try {
						MessagePacket message = new MessagePacket(InetAddress.getLocalHost().getHostAddress(), "READY");
						node.sendMessageLeft(message);
						startGame.setText("Waiting for others to be ready");
						startGame.setEnabled(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
            
        });
    }
    
    public boolean isGameDecisionMaker() {
    	return isDecisionMaker;
    }
    
    /*Decide this node as a game starter*/
    public void asGameDecisionMaker() {
    	isDecisionMaker = true;
    	startGame.setText("Start Game");
    	startGame.setEnabled(false);
    }
    
    public void receiveReadyStatus(String IP)
    {
    	int found = getPlayerIndex(IP);
    	System.out.println("Index : "+found);
    	readyStatus[found] = 1;
    	if (readyToPlay())
    	{
    		startGame.setEnabled(true);
    	}
    }
    
    /*Check if all other players are ready*/
    public boolean readyToPlay()
    {
    	int count = 0;
    	for (int i = 0; i < readyStatus.length; i++)
    	{
    		if (readyStatus[i] == 1)
    			count++;
    	}
    	return (count == opponents.size());
    }
    
    public void startDelay() {
    	Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				isAllowedToMove = false;
				try {
					Thread.sleep(150);
				} catch (Exception e) {
					e.printStackTrace();
				}
				isAllowedToMove = true;
			}
    		
    	});
    	t.start();
    }
    
    public int getPlayerIndex(String IP) {
    	int found = -1;
    	for (int i = 0; i < opponents.size(); i++) {
    		if (opponents.get(i).name.equals(IP))
    			found = i;
    	}
    	return found;
    }
    
    /*Update the opponent player : if the opponent name is not exists, set an empty
     * opponent as it is, else it will update the existing one
     */
    public void updatePlayer(NodePacket playerPacket) {
    	int found = getPlayerIndex(playerPacket.getName());
    	
    	if (found == -1) {
    		numOpponent++;
    		Player updateOpponent = opponents.get(numOpponent-1);
    		updateOpponent.name = new String(playerPacket.getName());
    		updateOpponent.positionX = playerPacket.getPositionX();
    		updateOpponent.positionY = playerPacket.getPositionY();
    		updateOpponent.repaint();
    		opponents.set(numOpponent-1, updateOpponent);
    	} else {
    		Player updateOpponent = opponents.get(found);
    		updateOpponent.positionX = playerPacket.getPositionX();
    		updateOpponent.positionY = playerPacket.getPositionY();
    		updateOpponent.repaint();
    		opponents.set(found, updateOpponent);
    	}
    }
    
    /*Checking whether the object is overlap to each other*/
    public boolean isOverlap(Object obj1, Object obj2) {
        boolean overlap = false;
        Point p1,p2;
        
        if (obj1 instanceof Player)
            p1 = ((Player) obj1).getPosition();
        else
            p1 = ((Chair) obj1).getPosition();
        if (obj2 instanceof Player)
            p2 = ((Player) obj2).getPosition();
        else
            p2 = ((Chair) obj2).getPosition();
        
        if (p1.equals(p2)) {
            overlap = true;
        }
        
        return overlap;
    }
    
    /*Timer of the game. when the seconds are 5 seconds left, the chair will appear
     * 
     */
    public void startTimer() {
        Thread t;
        t = new Thread(new Runnable() {

            @Override
            public void run() {
                int seconds = 0;
                for (seconds = 15; seconds >= 0; seconds--) {
                    timerlabel.setText(""+seconds);
                    if (seconds == 5) {
                        arenaGame.addChair(chair);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                if (isOverlap(mainplayer,chair)) {
                    gamestatus.setText("Player Wins");
                } else {
                    gamestatus.setText("Player Loses");
                }
            }
        },"timer"
        );
        t.start();
    }
    
    /*Simply a method to launch the game, by initializing an empty opponents as slots 
     * putting the mainplayer in the arena
     */
    public void launchGame() {
    	setInitOpponents();
        arenaGame.setPlayerinArena(mainplayer);
       // startTimer();
        screenGame.setVisible(true);
    }
    
    /*The node is started in a different Thread
     * 
     */
    public void startNode() {
        Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					node = new Node("10.1.1.23", mcg);
					node.startPeerDiscoveryConnection();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
        	
        });
        
        thread.start();
    }
    
    /*Main method of the game, it will create the GUI, launching the GUI, and start
     * its responsibility as a node
     */
    public static void main(String[] args) {
        // TODO code application logic here
        MusicalChairGame mcg = new MusicalChairGame();
        mcg.initGame();
        mcg.launchGame();
        mcg.startNode();
    }
    
}
