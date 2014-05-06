/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jemie
 */
import javax.swing.*;
import java.awt.*;
import static javax.swing.JFrame.EXIT_ON_CLOSE;

/*
 * Arena class is responsible for setting up the board/area of the game
 */
class BgPanel extends JPanel {
    
    Image bg;
    int x;
    int y;

    public BgPanel(int x, int y) {
        this.x = x;
        this.y = y;
        this.setSize(x, y);
        
        try{
            bg = javax.imageio.ImageIO.read(new java.net.URL(getClass().getResource("woodenfield.jpg"), "woodenfield.jpg"));
        } catch(Exception e) {
            System.out.println("Could not load picture!");
            e.printStackTrace();
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bg != null) {
            g.drawImage(bg, 0, 0, x, y,this);
        }
    }
}
public class Arena {
    
    private JFrame arenaFrame;
    private BgPanel arenaPanel;
    private int x;
    private int y;
    
    public Arena(JFrame screen, int x, int y) {
        this.x = x;
        this.y = y;
        
        arenaFrame = screen;
        
        arenaPanel = new BgPanel(x,y);
        
        arenaPanel.setLayout(new OverlayLayout(arenaPanel));
        arenaFrame.add(arenaPanel);
    }
    
    public void setPlayerinArena(Player player)
    {
        player.setOpaque(false);
        arenaPanel.add(player);
    }
    
    public void addChair(Chair chair)
    {
        chair.setOpaque(false);
        arenaPanel.add(chair);
    }
}
