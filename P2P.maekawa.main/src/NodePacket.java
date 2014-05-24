import java.awt.Color;
import java.io.Serializable;


public class NodePacket implements Serializable {
	private String name;
	private int positionX;
	private int positionY;
	private Color color;
	
	public NodePacket(String name, int x, int y) {
		this.name = name;
		positionX = x;
		positionY = y;
	}
	
	public Color getColor() {
		return color;
	}
	
	public String getName() {
		return name;
	}
	
	public int getPositionX() {
		return positionX;
	}
	
	public int getPositionY() {
		return positionY;
	}
	
	public void setColor(Color inColor) {
		color = inColor;
	}
}
