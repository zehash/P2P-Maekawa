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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import may.State;
import may.Vote;

public class MusicalChairGame {
	final String peerDiscoveryIP = "10.9.178.52";
	
    JFrame screenGame = new JFrame();
    JLabel timerlabel = new JLabel();
    JLabel gamestatus = new JLabel();
    JLabel label = new JLabel();
    JLabel playerLabel = new JLabel();
    JButton startGame = new JButton();
    
    int numOpponent = 0;
    int movement = 5;
    int initPlayerX = 0; 
    int initPlayerY = 0;
    int[] readyStatus = new int[10];
    int[][] marked = new int[600][300];
    int counterPlayerInChair = 0;
    

    /**
     * @param args the command line arguments
     */
    
    public Arena arenaGame;
    public Player mainplayer;
    public MusicalChairGame mcg = this;
    public static ArrayList<Player> opponents = new ArrayList<Player>();
    public static ArrayList<Chair> chairs = new ArrayList<Chair>(10);
    public Node node;
    public boolean isAllowedToMove = true;
    public boolean isDecisionMaker = false;
    public boolean isMainPlayerCreated = false;
    public boolean isChairAppear = false;
    public boolean isTouchingChair = false; //To immobilize the player
    public boolean gameIsStarted = false;
    public int availablePlayer = 1;
    Vote vote;
    State state;
    
    public MusicalChairGame() {
	    try {
	    	for (int i = 0; i < 600; i++)
	    		for (int j = 0; j < 300; j++)
	    			marked[i][j] = 0;
	        screenGame.setLayout(null);
	        arenaGame = new Arena(screenGame, 600,300);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
    
    /* This method will create mainplayer with its own color, then launch the game*/
    public void createMainPlayer(Color inColor) {
    	try {
    		setInitPlayerPosition();
    		mainplayer = new Player(inColor, initPlayerX,initPlayerY);
    		mainplayer.name = InetAddress.getLocalHost().getHostAddress();
            setListenerPlayer();
    		launchGame();
    		
    		JLabel label = new JLabel();
    		label.setText("You are : ");
    		label.setBounds(10, 340, 200, 20);
    		playerLabel.setBackground(inColor);
    		playerLabel.setOpaque(true);
    		playerLabel.setBounds(80, 340, 100, 20);
    		
    		screenGame.add(label); 
    		screenGame.add(playerLabel);
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	isMainPlayerCreated = true;
    }
    
    public Color setPlayerColor(int index) {
    	Color playerColor = Color.RED;
    	
    	switch(index) {
    	case 0 : playerColor = Color.RED;
    			 break;
    	case 1 : playerColor = Color.BLACK;
		   		 break;
    	case 2 : playerColor = Color.BLUE;
  		 		 break;
    	case 3 : playerColor = Color.YELLOW;
  		 		 break;
    	case 4 : playerColor = Color.CYAN;
  		 		 break;
    	case 5 : playerColor = Color.DARK_GRAY;
  		 		 break;
    	case 6 : playerColor = Color.LIGHT_GRAY;
  		 		 break;
    	case 7 : playerColor = Color.MAGENTA;
	 		     break;
    	case 8 : playerColor = Color.ORANGE;
	 		     break;
    	case 9 : playerColor = Color.PINK;
	 		 	 break;
    	}
    	
    	return playerColor;
    }
    
    /*Make this node as a decision maker*/
    
    public void setScreen() {
        
        label = new JLabel();
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
    	for (int i = 0; i < 10; i++)
    		chairs.add(new Chair(i+"", 1000,1000));
        setScreen();
        setListenerButton();
    }
    
    /*Make the chair appear*/
    public void setChairAppear() {
    	Chair chair;
    	boolean okey = false;
    	for (int i = 0; i < 10; i++)
    	{
    		okey = false;
    		while (!okey) {
		        Random randomGenerator = new Random();
		        int getX = 1;
		        while (getX % 5 != 0)
		            getX = randomGenerator.nextInt(600-40);
		        int getY = 1;
		        while (getY % 5 != 0)
		            getY = randomGenerator.nextInt(300-40);
		        if (marked[getX][getY] == 0) {
		        	chair = new Chair(""+i,getX, getY);
		        	chairs.set(i,chair);
		        	marked[getX][getY] = 1;
		        	okey = true;
		        }
    		}
    	}
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
    
    /*Removing opponent on the game*/
    public void removePlayer(String opponentIP) {
        int found = getPlayerIndex(opponentIP);
        
        Player updateOpponent = opponents.get(found);
        updateOpponent.positionX = 1000;
        updateOpponent.positionY = 1000;
        updateOpponent.repaint();
        availablePlayer--;
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
            	if (isTouchingChair == false) {
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
	                NodePacket mainPlayerPacket = new NodePacket(mainplayer.name, mainplayer.positionX, mainplayer.positionY);
                    mainPlayerPacket.setColor(mainplayer.color);
                    node.sendToLeft(mainPlayerPacket);
                    node.sendToRight(mainPlayerPacket);
	                /*Checking whether the main player is touching the chair*/
	                if (isChairAppear) {
		                boolean touching = false;
		                if (gameIsStarted) {
    		                for (int i= 0; i < numOpponent+1;i++)
    		                {
    		                	if (isOverlap(mainplayer,chairs.get(i),false)) {
    		                		counterPlayerInChair++;
    		                		touching = true;
    		                	}
    		                }
    		                if (counterPlayerInChair == availablePlayer) {
    		                    startTimerResult();
    		                }
		                }
	                }
		            if (isAllowedToMove) {
		                startDelay();
		            }
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
							System.out.println("Game started!");
							startGame.setEnabled(false);
							startGame.setFocusable(false);
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
						setListenerPlayer();
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
    	setChairAppear();
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
    	return (count == availablePlayer);
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
    	Player updateOpponent;
    	
    	if (found == -1) {
    		numOpponent++;
    		availablePlayer++;
    		updateOpponent = opponents.get(numOpponent-1);
    		updateOpponent.name = new String(playerPacket.getName());
    		updateOpponent.color = playerPacket.getColor();
    		updateOpponent.positionX = playerPacket.getPositionX();
    		updateOpponent.positionY = playerPacket.getPositionY();
    		updateOpponent.repaint();
    		opponents.set(numOpponent-1, updateOpponent);
    	} else {
    		updateOpponent = opponents.get(found);
    		updateOpponent.color = playerPacket.getColor();
    		updateOpponent.positionX = playerPacket.getPositionX();
    		updateOpponent.positionY = playerPacket.getPositionY();
    		updateOpponent.repaint();
    		opponents.set(found, updateOpponent);
    	}
    	
    	if (gameIsStarted) {
        	for (int i = 0; i < numOpponent+1; i++)
        	{
        	    if (isOverlap(updateOpponent,chairs.get(i),true))
        	        counterPlayerInChair++;
        	}
        	if (counterPlayerInChair == availablePlayer) {
                startTimerResult();
            }
    	}
    }
    
    /* Update the chair information
     */
    public void updateChair(NodePacket chairPacket) {
    	int chairIndex = Integer.parseInt(chairPacket.getName().split(" ")[1]);
    	chairs.set(chairIndex, new Chair(chairIndex+"",chairPacket.getPositionX(),chairPacket.getPositionY()));
    }
    
    /*Checking whether the object is overlap to each other*/
    public boolean isOverlap(Player obj1, Chair obj2, boolean checking) {
    	Rectangle newBoundPlayer = new Rectangle(obj1.positionX, obj1.positionY, 20, 20);
    	Rectangle newBoundChair = new Rectangle(obj2.positionX, obj2.positionY, 20, 20);
    	boolean overlap = false;
        overlap = newBoundPlayer.intersects(newBoundChair);
        if (overlap && !checking ) {
            isTouchingChair = true;
            node.iWantChair(Integer.parseInt(obj2.index));
        }
        return overlap;
    }
    
    /*Timer of the game. when the seconds are 5 seconds left, the chair will appear
     * 
     */
    public void startTimer() {
        node.setK();
    	gameIsStarted= true;
        Thread t;
        t = new Thread(new Runnable() {

            @Override
            public void run() {
                int seconds = 0;
                for (seconds = 5; seconds >= 0; seconds--) {
                    timerlabel.setText(""+seconds);
                    if (seconds == 1) {
                        isChairAppear = true;
                        for (int i = 0; i < numOpponent+1; i++)
                            arenaGame.addChair(chairs.get(i));
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        },"timer"
        );
        t.start();
    }
    
    /*Timer of the game. when the seconds are 5 seconds left, the chair will appear
     * 
     */
    public void startTimerResult() {
        label.setText("Time to calculate result : ");
        Thread t;
        t = new Thread(new Runnable() {

            @Override
            public void run() {
                int count = -1;
                int seconds = 0;
                for (seconds = 5; seconds >= 0; seconds--) {
                    timerlabel.setText(""+seconds);
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                count = node.results.countEveryoneInResults();
                System.out.println("Last game result : "+count);
                node.debugAlreadyReleased();
                node.debugAlreadyVoted();
                node.debugMyVotes();
                node.results.printAllResults();
                if (count != numOpponent+1) {
                    gamestatus.setText("draw");
                }
                else {
                    
                    ArrayList<String> winners = node.results.winners();
                    for (String s: winners) {
                        System.out.println("winner : "+s);
                    }
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
					node = new Node(peerDiscoveryIP, mcg);
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
        mcg.startNode();
    }
    
}
