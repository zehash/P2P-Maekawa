/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jemie
 */
import static javax.swing.JFrame.EXIT_ON_CLOSE;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyListener;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class MusicalChairGame {
    JFrame screenGame = new JFrame();
    JLabel timerlabel = new JLabel();
    JLabel gamestatus = new JLabel();
    Node node;
    Chair chair;
    int numOpponent = 0;
    int movement = 5;

    /**
     * @param args the command line arguments
     */
    
    public Arena arenaGame;
    public Player mainplayer;
    public MusicalChairGame mcg = this;
    public ArrayList<Player> opponents = new ArrayList<Player>();
    
    public MusicalChairGame() {
	    try {
	        screenGame.setLayout(null);
	        arenaGame = new Arena(screenGame, 600,300);
	        mainplayer = new Player(Color.RED, 200,200);
	        mainplayer.name = InetAddress.getLocalHost().getHostAddress();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
    
    public void setScreen() {
        
        JLabel label = new JLabel();
        label.setText("TIMER :");
        label.setBounds(10, 300, 50, 20);
 
        timerlabel.setText("0");
        timerlabel.setBounds(10, 320, 100, 20);
        gamestatus.setText("Game started..");
        gamestatus.setBounds(300,320,100,20);
        
        screenGame.getContentPane().add(label);
        screenGame.getContentPane().add(timerlabel);
        screenGame.getContentPane().add(gamestatus);
        
        
        screenGame.setSize(600,400);
        screenGame.setResizable(false);
        screenGame.setTitle("Musical Chair Game");
        screenGame.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    public void initGame() {
        setScreen();
        setListenerPlayer();
        setChairAppear();
    }
    
    public void setChairAppear() {
        Random randomGenerator = new Random();
        int getX = 1;
        while (getX % 5 != 0)
            getX = randomGenerator.nextInt(600-40);
        int getY = 1;
        while (getY % 5 != 0)
            getY = randomGenerator.nextInt(300-40);
        System.out.println(getX+" "+getY);
        chair = new Chair(getX, getY);
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
                    case 'w' : mainplayer.movePosition(0, -movement);
                               break;
                    case 'a' : mainplayer.movePosition(-movement, 0);
                               break;
                    case 'd' : mainplayer.movePosition(+movement, 0);
                               break;
                    case 's' : mainplayer.movePosition(0, +movement);
                               break;
                }
                mainplayer.repaint();
                System.out.println("mainplayer position : "+mainplayer.positionX + ","+ mainplayer.positionY);
          //      server.send(mainplayer);
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
            //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }
    
    public void updatePlayer(Player opponent) {
    	int found = -1;
    	for (int i = 0; i < opponents.size(); i++) {
    		if (opponents.get(i).name.equals(opponent.name))
    			found = i;
    	}
    	
    	System.out.println("Found : "+found);
    	
    	if (found == -1) {
    		numOpponent++;
    		opponents.add(opponent);
    		System.out.println("Painting : "+opponents.get(numOpponent-1).name);
    		arenaGame.setPlayerinArena(opponents.get(numOpponent-1));
    	} else {
    		Player updateOpponent = opponents.get(found);
    		updateOpponent.positionX = opponent.positionX;
    		updateOpponent.positionY = opponent.positionY;
    		updateOpponent.repaint();
    		opponents.set(found, updateOpponent);
    	}
    }
    
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
    
    public void launchGame() {
        arenaGame.setPlayerinArena(mainplayer);
       // startTimer();
        screenGame.setVisible(true);
    }
    
    
    public void startNode() {
        Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					node = new Node("10.9.136.177", mcg);
					node.startPeerDiscoveryConnection();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
        	
        });
        
        thread.start();
    }
    
    
    public static void main(String[] args) {
        // TODO code application logic here
        MusicalChairGame mcg = new MusicalChairGame();
        mcg.initGame();
        mcg.launchGame();
        mcg.startNode();
    }
    
}
