/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jemie
 */
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;

import javax.swing.JComponent;

public class Player extends JComponent implements Serializable {
	String name;
    Color color;
    int positionX;
    int positionY;
    
    public Player(Color color, int positionX, int positionY) {
        this.color = color;
        this.positionX = positionX;
        this.positionY = positionY;
    }
    
    public Player (Player inPlayer) {
    	this.name = new String(inPlayer.name);
    	this.color = inPlayer.color;
    	this.positionX = inPlayer.positionX;
    	this.positionY = inPlayer.positionY;
    }
    
    public void setPosition(int newX, int newY) {
        positionX = newX;
        positionY = newY;
    }
    
    public void movePosition(int newX, int newY) {
        if (((positionX + newX) > 0) && ((positionX + newX) < 600 - 20))
            positionX += newX;
        if (((positionY + newY) > 0) && ((positionY + newY) < 300 - 20))
            positionY += newY;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        Ellipse2D.Double hole = new Ellipse2D.Double(positionX, positionY, 20,20);
        g2d.setPaint(color);
        g2d.fill(hole);
    }
    
    public Point getPosition() {
        Point point = new Point(positionX, positionY);
        return point;
    }
}
