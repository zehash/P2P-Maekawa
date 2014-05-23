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
import java.awt.geom.Rectangle2D;

public class Chair extends JComponent {
    
	String index;
    int positionX;
    int positionY;
    
    public Chair(int positionX, int positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        Rectangle2D.Double rect = new Rectangle2D.Double(positionX, positionY,20,20);
        g2d.setPaint(Color.LIGHT_GRAY);
        g2d.fill(rect);
    }
    
    public Point getPosition() {
        Point point = new Point(positionX, positionY);
        return point;
    }
}
