import java.io.Serializable;


public class NodePacket implements Serializable {
	private String name;
	private int positionX;
	private int positionY;
	
	public NodePacket(String name, int x, int y) {
		this.name = name;
		positionX = x;
		positionY = y;
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
}
